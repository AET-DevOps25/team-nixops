provider "aws" {
  region  = var.aws_region
  profile = var.profile

}

# Load the SSH key pair (must be created beforehand in AWS)
data "aws_key_pair" "deployer" {
  include_public_key = true
  #TODO:remove when https://github.com/hashicorp/terraform-provider-aws/issues/43425 solved
  key_name = "mikilio"
  filter {
    name   = "tag:nixops"
    values = ["true"]
  }
}

resource "aws_security_group" "instance" {
  name_prefix            = "dev"
  description            = "Dev instance security group"
  revoke_rules_on_delete = true
}

resource "aws_security_group_rule" "ssh_in" {
  type              = "ingress"
  from_port         = 22
  to_port           = 22
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.instance.id
  description       = "Dev ssh access"
}


resource "aws_security_group_rule" "http_in" {
  type              = "ingress"
  from_port         = 80
  to_port           = 80
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.instance.id
  description       = "Dev http ingress"
}

resource "aws_security_group_rule" "https_in" {
  type              = "ingress"
  from_port         = 443
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.instance.id
  description       = "Dev https ingress"
}

resource "aws_security_group_rule" "dns_in" {
  type              = "ingress"
  from_port         = 53
  to_port           = 53
  protocol          = "all"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.instance.id
  description       = "Dev dns traffic"
}

resource "aws_security_group_rule" "egress" {
  type              = "egress"
  from_port         = 0
  to_port           = 65535
  protocol          = -1
  security_group_id = aws_security_group.instance.id
  cidr_blocks       = ["0.0.0.0/0"]
  description       = "Dev egress"
}

# Create EC2 instance
resource "aws_instance" "this" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = data.aws_key_pair.deployer.key_name
  vpc_security_group_ids      = [aws_security_group.instance.id]
  associate_public_ip_address = var.public_ip
  tags = merge(
    {
      Terraform = "true"
      Name      = var.name
    },
    var.tags
  )
  root_block_device {
    volume_type           = "gp3"
    volume_size           = 16
    delete_on_termination = true
  }
  lifecycle {
    # Avoid destroying the instance when SSH key or security changes
    ignore_changes = [key_name, vpc_security_group_ids]
  }
}

# Deploy NixOS to EC2 via nixos-anywhere
module "deploy" {
  depends_on             = [local_file.nixos_vars]
  source                 = "github.com/numtide/nixos-anywhere//terraform/all-in-one"
  nixos_system_attr      = ".#nixosConfigurations.${var.nixos_flake_attr}.config.system.build.toplevel"
  nixos_partitioner_attr = ".#nixosConfigurations.${var.nixos_flake_attr}.config.system.build.diskoScriptNoDeps"
  target_host            = aws_instance.this.public_ip
  install_user           = "ubuntu"
  instance_id            = aws_instance.this.id
  extra_files_script     = "${path.module}/decrypt-age-keys.sh"
  extra_environment = {
    SOPS_FILE = var.sops_file
  }
  debug_logging = true

  special_args = var.nixos_special_args
}

# Local values used by nixos-anywhere
locals {
  nixos_vars = {
    public_ip  = aws_instance.this.public_ip
    public_dns = aws_instance.this.public_dns
    private_ip = aws_instance.this.private_ip
    #TODO:add multiple keys when https://github.com/hashicorp/terraform-provider-aws/issues/43425 solved
    ssh_keys = [data.aws_key_pair.deployer.public_key]
  }
}

# Outputs
output "public_ip" {
  value = aws_instance.this.public_ip
}

output "public_dns" {
  value = aws_instance.this.public_dns
}
