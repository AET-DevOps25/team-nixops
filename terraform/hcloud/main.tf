# Record the SSH public key into Hetzner Cloud
data "hcloud_ssh_keys" "this" {
  with_selector = "nixops=true"
}

resource "hcloud_server" "this" {
  image       = "debian-11"
  keep_disk   = true
  name        = var.name
  server_type = var.server_type
  ssh_keys    = data.hcloud_ssh_keys.this.ssh_keys.*.name
  backups     = false
  labels      = var.tags

  location = var.server_location

  lifecycle {
    # Don't destroy server instance if ssh keys changes.
    ignore_changes  = [ssh_keys]
    prevent_destroy = true
  }
}

module "deploy" {
  depends_on             = [local_file.nixos_vars]
  source                 = "github.com/numtide/nixos-anywhere//terraform/all-in-one"
  nixos_system_attr      = ".#nixosConfigurations.${var.nixos_flake_attr}.config.system.build.toplevel"
  nixos_partitioner_attr = ".#nixosConfigurations.${var.nixos_flake_attr}.config.system.build.diskoScriptNoDeps"
  target_host            = hcloud_server.this.ipv4_address
  instance_id            = hcloud_server.this.id
  extra_files_script     = "${path.module}/decrypt-age-keys.sh"
  extra_environment = {
    SOPS_FILE = var.sops_file
  }
  debug_logging = true
}

locals {
  nixos_vars = {
    ipv6_address = hcloud_server.this.ipv6_address
    ipv4_address = hcloud_server.this.ipv4_address
    ssh_keys     = data.hcloud_ssh_keys.this.ssh_keys.*.public_key
  }
}

output "ipv4_address" {
  value = hcloud_server.this.ipv4_address
}

output "ipv6_address" {
  value = hcloud_server.this.ipv6_address
}
