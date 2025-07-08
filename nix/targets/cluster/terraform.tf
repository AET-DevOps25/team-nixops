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

module "cluster" {
  source = "../../../terraform/k8s"

  node_source   = "../hcloud"
  domain_suffix = "nixops.aet.cit.tum.de"
  cluster_name  = "cluster"
  cluster_nodes = {
    worker  = { count = 2 }
    control = { count = 3 }
    etcd    = { count = 3 }
    loadbalancer = {
      count     = 1
      public_ip = true
    }
  }
  common_tags = {
    Terraform = "true"
    Target    = "nixops.aet.cit.tum.de"
  }
}


output "ipv4_addresses" {
  description = "IPv4 addresses of all nodes"
  value = {
    for k, m in module.cluster : k => m.ipv4_address
  }
}

output "ipv6_addresses" {
  description = "IPv6 addresses of all nodes"
  value = {
    for k, m in module.cluster : k => m.ipv6_address
  }
}
