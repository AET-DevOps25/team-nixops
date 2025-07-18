# Hetzner SSH keys (only created if provider is hcloud)
resource "hcloud_ssh_key" "hcloud" {
  for_each   = var.use_hcloud ? var.ssh_keys : {}
  name       = each.key
  public_key = each.value

  labels = {
    "nixops" = "true"
  }
}

# AWS SSH keys (only created if provider is aws)
resource "aws_key_pair" "aws" {
  for_each   = var.use_aws ? var.ssh_keys : {}
  key_name   = each.key
  public_key = each.value

  tags = {
    "nixops" = "true"
  }
}

provider "aws" {
  region  = "eu-north-1" # ← required to target correct data-center
  profile = "terraform"  # ← required to use correct credentials
}
