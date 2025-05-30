resource "hcloud_ssh_key" "hcloud" {
  for_each   = var.ssh_keys
  name       = each.key
  public_key = each.value
  labels = {
    "nixops" = "true"
  }
}

/*
  TODO: add other providers
*/
