# Terraform Modules

This folder contains **reusable Terraform modules** that are consumed by the `nix/targets` environments.

### Modules:

* `aws/` – Resources for deploying NixOS on AWS
* `hcloud/` – Hetzner Cloud VM provisioning
* `k8s/` – Kubernetes infrastructure components
* `admins/` – Provision admin keys across different resources

Each module includes:

* `main.tf`, `providers.tf`, `variables.tf`
* Some modules include `nixos_vars.tf` to pass variables to the NixOS config
* Optional: `decrypt-age-keys.sh` for provisioning a key that provides access the necessary sops secrets

## 🔁 Usage

These are not standalone deployments — they’re used from `nix/targets/*/terraform.tf` via `module` blocks.
