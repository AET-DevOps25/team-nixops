locals {
  cluster_instance_map = merge([
    for role, cfg in var.cluster_nodes : {
      for i in range(cfg.count) :
      "${role}-${i + 1}" => merge(
        {
          role  = role
          index = i + 1
        },
        cfg.defaults,
        lookup(cfg.overrides, "${role}-${i + 1}", {})
      )
    }
  ]...)
}

resource "hcloud_network" "cluster_net" {
  name     = "cluster-net"
  ip_range = "10.0.0.0/16"
}

resource "hcloud_network_subnet" "cluster_subnet" {
  network_id   = hcloud_network.cluster_net.id
  type         = "cloud"
  network_zone = "eu-central" # or your preferred zone
  ip_range     = "10.0.0.0/24"
}

module "k8s" {
  source   = var.node_source
  for_each = local.cluster_instance_map

  name             = each.key
  domain           = "${each.key}.${var.domain_suffix}"
  nixos_flake_attr = each.value.role
  nixos_vars_file  = "${path.module}/nixos-vars-${each.key}.json"
  sops_file        = abspath("${path.module}/secrets/secrets-${each.key}.yaml")
  tags             = merge(var.common_tags, { Role = each.value.role })
  network_id       = hcloud_network.cluster_net.id
}



