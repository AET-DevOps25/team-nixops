variable "name" {
  description = "The name of the AWS EC2 instance (e.g., dev, prod)"
  type        = string
}

variable "instance_type" {
  description = "AWS EC2 instance type (e.g., t3.micro, t3.small)"
  type        = string
  default     = "t3.small"
}

variable "aws_region" {
  description = "AWS region (e.g., us-east-1)"
  type        = string
  default     = "eu-north-1"
}

variable "profile" {
  description = "Profile of signed in aws CLI"
  type        = string
  default     = "terraform"
}

variable "subnet_id" {
  description = "The ID of the subnet in which to launch the instance"
  type        = string
  default     = null
}

variable "security_group_ids" {
  description = "List of security group IDs to assign to the instance"
  type        = list(string)
  default     = []
}

variable "ami_id" {
  description = "The AMI ID to use for the EC2 instance (e.g., a minimal Debian or NixOS base AMI)"
  type        = string
  default     = "ami-042b4708b1d05f512" #Ubuntu
}

variable "public_ip" {
  description = "Whether to associate a public IP address"
  type        = bool
  default     = false
}

variable "domain" {
  description = "Domain name"
  type        = string
}

variable "tags" {
  type        = map(string)
  default     = {}
  description = "Tags to add to the EC2 instance"
}

variable "nixos_vars_file" {
  type        = string
  description = "File to write NixOS configuration variables to"
}

variable "sops_file" {
  type        = string
  description = "Path to SOPS secrets file"
}

variable "nixos_flake_attr" {
  type        = string
  description = "NixOS configuration flake attribute"
}

variable "nixos_special_args" {
  type        = any
  description = "Extra arguments to pass to NixOS configuration"
  default     = {}
}

