import argparse
import ipaddress
import subprocess
from pathlib import Path
from cryptography import x509
from cryptography.x509.oid import NameOID
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives.serialization import (
    Encoding,
    PrivateFormat,
    NoEncryption,
    PublicFormat,
)
from cryptography.hazmat.backends import default_backend
from datetime import datetime, timedelta, UTC
from ruamel.yaml import YAML

yaml = YAML()


def gen_rsa_key():
    return rsa.generate_private_key(
        public_exponent=65537, key_size=2048, backend=default_backend()
    )


def write_pem(path, obj, is_key=False):
    with open(path, "wb") as f:
        if is_key:
            f.write(
                obj.private_bytes(
                    Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
                )
            )
        else:
            f.write(obj.public_bytes(Encoding.PEM))
    subprocess.run(["sops", "--encrypt", "--in-place", path], check=True)


def gen_ca(name):
    key = gen_rsa_key()
    subject = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, name)])
    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(subject)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(datetime.now(UTC))
        .not_valid_after(datetime.now(UTC) + timedelta(days=3650))
        .add_extension(x509.BasicConstraints(ca=True, path_length=None), True)
        .sign(key, hashes.SHA256(), default_backend())
    )
    return cert, key


def gen_cert(cn, issuer_cert, issuer_key, key, sans=None, org=None):
    name = [x509.NameAttribute(NameOID.COMMON_NAME, cn)]
    if org:
        name.append(x509.NameAttribute(NameOID.ORGANIZATION_NAME, org))
    builder = (
        x509.CertificateBuilder()
        .subject_name(x509.Name(name))
        .issuer_name(issuer_cert.subject)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(datetime.now(UTC))
        .not_valid_after(datetime.now(UTC) + timedelta(days=3650))
        .add_extension(x509.BasicConstraints(ca=False, path_length=None), True)
    )
    if sans:
        san_list = []
        for san in sans:
            try:
                san_list.append(x509.IPAddress(ipaddress.ip_address(san)))
            except ValueError:
                san_list.append(x509.DNSName(san))
        builder = builder.add_extension(x509.SubjectAlternativeName(san_list), False)
    return builder.sign(issuer_key, hashes.SHA256(), default_backend())


def write_node_yaml(node_type, index, secrets):
    fn = f"{node_type}-{index}.yaml"
    data = {k: v.decode() for k, v in secrets.items()}
    with open(fn, "w") as f:
        yaml.dump(data, f)
    subprocess.run(["sops", "--encrypt", "--in-place", fn], check=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--etcd", type=int, required=True)
    parser.add_argument("--control", type=int, required=True)
    parser.add_argument("--worker", type=int, required=True)
    parser.add_argument("--old-account-keys", type=Path, required=False)
    args = parser.parse_args()

    ca_cert, ca_key = gen_ca("kubernetes")
    etcd_ca_cert, etcd_ca_key = gen_ca("etcd")
    write_pem("ca.pem", ca_cert)
    write_pem("ca-key.pem", ca_key, is_key=True)
    write_pem("etcd-ca.pem", etcd_ca_cert)
    write_pem("etcd-ca-key.pem", etcd_ca_key, is_key=True)

    sa_key = gen_rsa_key()
    sa_pub = sa_key.public_key().public_bytes(
        Encoding.PEM, PublicFormat.SubjectPublicKeyInfo
    )
    sa_pub_combined = sa_pub

    if args.old_account_keys:
        with args.old_account_keys.open("rb") as f:
            sa_pub_combined = f.read() + sa_pub
        with args.old_account_keys.open("wb") as f:
            f.write(sa_pub_combined)
        subprocess.run(
            ["sops", "--encrypt", "--in-place", str(args.old_account_keys)], check=True
        )

    for i in range(args.control):
        secrets = {}
        # apiserver
        k_apiserver = gen_rsa_key()
        c_apiserver = gen_cert(
            "kube-apiserver",
            ca_cert,
            ca_key,
            k_apiserver,
            [
                "10.96.0.1",
                "kubernetes",
                "kubernetes.default",
                "kubernetes.default.svc",
                "kubernetes.default.svc.cluster.local",
            ],
        )
        secrets["apiserver"] = c_apiserver.public_bytes(Encoding.PEM)
        secrets["apiserver-key"] = k_apiserver.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        # etcd client
        k_etcd = gen_rsa_key()
        c_etcd = gen_cert("etcd-client", etcd_ca_cert, etcd_ca_key, k_etcd)
        secrets["etcd-client"] = c_etcd.public_bytes(Encoding.PEM)
        secrets["etcd-client-key"] = k_etcd.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        # kubelet client
        k_kubelet = gen_rsa_key()
        c_kubelet = gen_cert("kubelet-client", ca_cert, ca_key, k_kubelet)
        secrets["kubelet-client"] = c_kubelet.public_bytes(Encoding.PEM)
        secrets["kubelet-client-key"] = k_kubelet.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        # controller manager
        k_cm = gen_rsa_key()
        c_cm = gen_cert(
            "system:kube-controller-manager",
            ca_cert,
            ca_key,
            k_cm,
            org="system:kube-controller-manager",
        )
        secrets["controller-manager"] = c_cm.public_bytes(Encoding.PEM)
        secrets["controller-manager-key"] = k_cm.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        # scheduler
        k_sched = gen_rsa_key()
        c_sched = gen_cert(
            "system:kube-scheduler",
            ca_cert,
            ca_key,
            k_sched,
            org="system:kube-scheduler",
        )
        secrets["scheduler"] = c_sched.public_bytes(Encoding.PEM)
        secrets["scheduler-key"] = k_sched.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        # service signing
        secrets["service-account-verify"] = sa_pub_combined
        secrets["service-account-key"] = sa_key.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        # ca
        secrets["ca"] = ca_cert.public_bytes(Encoding.PEM)
        secrets["etcd-ca"] = etcd_ca_cert.public_bytes(Encoding.PEM)
        write_node_yaml("control", i, secrets)

    for i in range(args.etcd):
        secrets = {}
        k_server = gen_rsa_key()
        c_server = gen_cert("etcd-server", etcd_ca_cert, etcd_ca_key, k_server)
        secrets["server"] = c_server.public_bytes(Encoding.PEM)
        secrets["server-key"] = k_server.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        k_peer = gen_rsa_key()
        c_peer = gen_cert("etcd-peer", etcd_ca_cert, etcd_ca_key, k_peer)
        secrets["peer"] = c_peer.public_bytes(Encoding.PEM)
        secrets["peer-key"] = k_peer.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        secrets["ca"] = ca_cert.public_bytes(Encoding.PEM)
        secrets["etcd-ca"] = etcd_ca_cert.public_bytes(Encoding.PEM)
        write_node_yaml("etcd", i, secrets)

    for i in range(args.worker):
        secrets = {}
        k_kubelet = gen_rsa_key()
        c_kubelet = gen_cert(
            f"system:node:worker-{i}", ca_cert, ca_key, k_kubelet, org="system:nodes"
        )
        secrets["kubelet"] = c_kubelet.public_bytes(Encoding.PEM)
        secrets["kubelet-key"] = k_kubelet.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        k_proxy = gen_rsa_key()
        c_proxy = gen_cert(
            "system:kube-proxy", ca_cert, ca_key, k_proxy, org="system:node-proxier"
        )
        secrets["proxy"] = c_proxy.public_bytes(Encoding.PEM)
        secrets["proxy-key"] = k_proxy.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        secrets["ca"] = ca_cert.public_bytes(Encoding.PEM)
        write_node_yaml("worker", i, secrets)

    print("✅ All certs and SOPS-encrypted YAMLs created.")


if __name__ == "__main__":
    main()
