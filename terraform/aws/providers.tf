terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "6.3.0"
    }
    local = { source = "hashicorp/local" }
  }
}
