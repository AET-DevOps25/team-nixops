variable "passphrase" {}

terraform {
  encryption {
    key_provider "pbkdf2" "sops" {
      passphrase = var.passphrase
    }

    method "aes_gcm" "sops" {
      keys = key_provider.pbkdf2.sops
    }

    state {
      method   = method.aes_gcm.sops
      enforced = true
    }
  }
}

module "dev" {
  source           = "../../../terraform/hcloud"
  name             = "dev"
  domain           = "nixops.aet.cit.tum.de"
  nixos_flake_attr = "dev"
  nixos_vars_file  = "${path.module}/nixos-vars.json"
  sops_file        = abspath("${path.module}/secrets/secrets.yaml")
  tags = {
    Terraform = "true"
    Target    = "nixops.aet.cit.tum.de"
  }
}

output "ipv4_address" {
  value = module.dev.ipv4_address
}

output "ipv6_address" {
  value = module.dev.ipv6_address
}
