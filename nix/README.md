# NixOS Infrastructure Configuration

This folder contains the **Nix-based infrastructure setup** involving Kubernetes clusters, Podman containers, and Terraform-based deployments. It uses Nix Flakes, modular NixOS configuration, and secret provisioning to manage reproducible infrastructure.

## 📁 Content Overview

### `checks/`

Contains test configurations and environment checks:

* `k8s/`: Nix-based Kubernetes test setup with JSON-based variables and secrets.
* `podman/`: Podman container testing environment with environment variable definitions.

### `devShell/`

Development shell environment powered by Nix. Includes:

* `default.nix`: Shell definition.
* `scripts/`: Python scripts for provisioning and generating encrypted secrets (`sops`).
* `keys.yaml` / `sops.yaml.j2`: Templates and keys for managing secrets with [SOPS](https://github.com/mozilla/sops).

### `modules/`

Reusable NixOS modules to configure:

* Kubernetes roles (`control`, `worker`, `etcd`, `loadbalancer`).
* Podman support.
* Disk setup profiles (MBR / UEFI).
* Preferred nix settings.

### `targets/`

Concrete system configurations grouped by environment:

* `admins/`: Terraform-managed list of admin acress all deployments.
* `cluster/`: Main Kubernetes cluster config.
* `dev/`: Developer VM or test environment.
  Each environment contains:
* `configuration.nix`: Entry point for system configuration.
* `nixos-vars/`: Role-based variable files (`control-0.json`, etc.).
* `secrets/`: Encrypted secrets and TLS keys (not for public sharing).
* Terraform files for infrastructure provisioning.

## 🛠️ Technologies Used

* **Nix / NixOS**
* **Terraform**
* **SOPS (Secrets OPerationS)**
* **Kubernetes**
* **Podman**
* **Python** (for secret and cert provisioning scripts)

## 🚀 Getting Started

1. **Install dependencies**:

If you followed the instructions to install Nix just enter the command

```bash
   direnv allow
```

After that you will enter a development shell with all dependencies present.

3. **Run checks or build configuration**:

   ```bash
   nix build .#checks.k8s
   ```

4. **(Optional) Provision secrets** (e.g., generate certs):

To certificates for a self-hosted kubernetes cluster run the following commands

   ```bash
   cd targets/cluster/secrets
   provision-certificates --worker 2 --control 3 --etcd 3
   ```

5. **Deploy with Terraform**:

   ```bash
   cd targets/dev
   # or cd targets/cluster
   ./tf.sh apply
   ```

## 🔒 Secrets Handling

Secrets are managed using [SOPS](https://github.com/mozilla/sops) and templated with Jinja. Do **not** commit decrypted secrets. All sensitive files are stored in:

* `secrets/` folders across targets
* Encrypted with `keys.yaml` and managed via `sops.yaml.j2`
* The generation happens automatically upon entering the developement shell.

## 🧪 Testing

Run test configurations in `checks/`:

```bash
nix flake check -L --option sandbox false --no-pure-eval
```

Or directly test Kubernetes and Podman environments using the provided Nix files.
