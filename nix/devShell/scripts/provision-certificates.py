import argparse
import ipaddress
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


# === Helpers ===
def gen_rsa_key():
    return rsa.generate_private_key(
        public_exponent=65537, key_size=2048, backend=default_backend()
    )


def write_pem(path, obj, pem_type="CERTIFICATE"):
    with open(path, "wb") as f:
        if pem_type == "CERTIFICATE":
            f.write(obj.public_bytes(Encoding.PEM))
        else:
            f.write(
                obj.private_bytes(
                    Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
                )
            )


def gen_ca(name):
    key = gen_rsa_key()
    subject = x509.Name([x509.NameAttribute(NameOID.COMMON_NAME, name)])
    now = datetime.now(UTC)
    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(subject)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + timedelta(days=3650))
        .add_extension(x509.BasicConstraints(ca=True, path_length=None), True)
        .sign(key, hashes.SHA256(), default_backend())
    )
    return cert, key


def gen_cert(subject_cn, issuer_cert, issuer_key, key, san_list=None, org=None):
    subject = [x509.NameAttribute(NameOID.COMMON_NAME, subject_cn)]
    if org:
        subject.append(x509.NameAttribute(NameOID.ORGANIZATION_NAME, org))
    subject = x509.Name(subject)
    now = datetime.now(UTC)
    builder = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer_cert.subject)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(now)
        .not_valid_after(now + timedelta(days=3650))
        .add_extension(x509.BasicConstraints(ca=False, path_length=None), True)
    )
    if san_list:
        sans = []
        for san in san_list:
            try:
                sans.append(x509.IPAddress(ipaddress.ip_address(san)))
            except ValueError:
                sans.append(x509.DNSName(san))
        builder = builder.add_extension(x509.SubjectAlternativeName(sans), False)
    return builder.sign(issuer_key, hashes.SHA256(), default_backend())


def write_node_yaml(node_type, idx, secrets):
    fn = f"{node_type}-{idx}.yaml"
    data = {k: {"data": v.decode()} for k, v in secrets.items()}
    with open(fn, "w") as f:
        yaml.dump({"sops": {"secrets": data}}, f)


# === Main ===
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
    write_pem("ca-key.pem", ca_key, "KEY")
    write_pem("etcd-ca.pem", etcd_ca_cert)
    write_pem("etcd-ca-key.pem", etcd_ca_key, "KEY")

    sa_key = gen_rsa_key()
    sa_pub_pem = sa_key.public_key().public_bytes(
        Encoding.PEM, PublicFormat.SubjectPublicKeyInfo
    )
    write_pem("service-account-key.pem", sa_key, "KEY")
    with open("service-account-key.pub.pem", "wb") as f:
        f.write(sa_pub_pem)

    if args.old_account_keys:
        with args.old_account_keys.open("ab") as f:
            f.write(sa_pub_pem)

    for i in range(args.control):
        secrets = {}
        key = gen_rsa_key()
        cert = gen_cert(
            "kube-apiserver",
            ca_cert,
            ca_key,
            key,
            san_list=[
                "10.96.0.1",
                "kubernetes",
                "kubernetes.default",
                "kubernetes.default.svc",
                "kubernetes.default.svc.cluster.local",
            ],
        )
        secrets["apiserver"] = cert.public_bytes(Encoding.PEM)
        secrets["apiserver-key"] = key.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        secrets["ca"] = ca_cert.public_bytes(Encoding.PEM)
        secrets["etcd-ca"] = etcd_ca_cert.public_bytes(Encoding.PEM)
        secrets["service-account-verify"] = sa_pub_pem
        secrets["service-account-key"] = sa_key.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        write_node_yaml("control", i, secrets)

    for i in range(args.etcd):
        secrets = {}
        key = gen_rsa_key()
        cert = gen_cert("etcd-server", etcd_ca_cert, etcd_ca_key, key)
        secrets["server"] = cert.public_bytes(Encoding.PEM)
        secrets["server-key"] = key.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        secrets["ca"] = ca_cert.public_bytes(Encoding.PEM)
        secrets["etcd-ca"] = etcd_ca_cert.public_bytes(Encoding.PEM)
        write_node_yaml("etcd", i, secrets)

    for i in range(args.worker):
        secrets = {}
        key = gen_rsa_key()
        cert = gen_cert(
            f"system:node:worker-{i}", ca_cert, ca_key, key, org="system:nodes"
        )
        secrets["kubelet"] = cert.public_bytes(Encoding.PEM)
        secrets["kubelet-key"] = key.private_bytes(
            Encoding.PEM, PrivateFormat.TraditionalOpenSSL, NoEncryption()
        )
        secrets["ca"] = ca_cert.public_bytes(Encoding.PEM)
        write_node_yaml("worker", i, secrets)

    print("✅ All certificates and node YAMLs generated.")


if __name__ == "__main__":
    main()
