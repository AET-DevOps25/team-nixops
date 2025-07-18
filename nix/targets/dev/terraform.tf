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

#TODO: uncomment when having admint rights to repo
# provider "github" {
#   owner = "AET-DevOps25"
# }
#
# resource "github_repository_environment" "env" {
#   repository  = "team-nixops"
#   environment = "dev"
# }
#
# resource "github_actions_environment_variable" "ec2_public_ip" {
#   repository    = github_repository_environment.env.repository
#   environment   = github_repository_environment.env.environment
#   variable_name = "EC2_PUBLIC_IP"
#   value         = module.dev.ipv4_address
# }


module "dev" {
  source           = "../../../terraform/aws"
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

output "public_ip" {
  value = module.dev.public_ip
}

output "public_dns" {
  value = module.dev.public_dns
}
