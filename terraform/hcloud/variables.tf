variable "name" {
  description = "The name of the Hetzner server (e.g., dev, prod)"
  type        = string
}

variable "server_type" {
  type        = string
  default     = "cpx11"
  description = "Hetzner cloud server type"
}

variable "server_location" {
  type        = string
  default     = "nbg1"
  description = "Hetzner cloud server location"
}

variable "nixos_vars_file" {
  type        = string
  description = "File to write NixOS configuration variables to"
}

variable "sops_file" {
  type        = string
  description = "File to SOPS secrets file"
}

variable "nixos_flake_attr" {
  type        = string
  description = "NixOS configuration flake attribute"
}

variable "network_id" {
  type = string
}

variable "public_ip" {
  type    = bool
  default = false
}

variable "domain" {
  type        = string
  description = "Domain name"
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to add to the server"
}
