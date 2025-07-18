variable "use_hcloud" {
  type        = bool
  description = "Whether to create SSH keys in Hetzner Cloud"
  default     = false
}

variable "use_aws" {
  type        = bool
  description = "Whether to create SSH keys in AWS"
  default     = false
}

variable "ssh_keys" {
  type        = map(string)
  description = "SSH public keys for admin user (name -> key)"
}
