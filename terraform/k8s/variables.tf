variable "node_source" {
  description = "Source of the node module"
  type        = string
}

variable "cluster_name" {
  description = "Name of the cluster"
  type        = string
}

variable "cluster_nodes" {
  description = <<EOT
Cluster node specification.
Each role maps to:
- count (required): number of nodes
- defaults (optional): default settings for nodes of this role
- overrides (optional): node-specific settings by node name

Example:
{
  worker = {
    count = 3
    defaults = {
      server_type = "cx21"
      location    = "nbg1"
    }
    overrides = {
      "worker-1" = {
        server_type = "cx31"
      }
    }
  }

  control = {
    count = 1
  }
}
EOT

  type = map(object({
    count     = number
    defaults  = optional(map(any), {})
    overrides = optional(map(map(any)), {})
  }))
}


variable "common_tags" {
  type    = map(string)
  default = {}
}

variable "domain_suffix" {
  type = string
}

