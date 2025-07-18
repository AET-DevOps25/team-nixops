resource "local_file" "nixos_vars" {
  content         = jsonencode(local.nixos_vars)
  filename        = var.nixos_vars_file
  file_permission = "600"

  provisioner "local-exec" {
    interpreter = ["bash", "-c"]
    command     = <<EOT
git_root=$(git rev-parse --show-toplevel)
lock_file="$git_root/.git/terraform-git-add.lock"

(
  flock 200

  echo "Acquired lock, running git add..."

  git add -f "${var.nixos_vars_file}"
  git add "$(dirname "${var.nixos_vars_file}")"/{hosts,flake-module.nix}

) 200>"$lock_file"
EOT
    on_failure  = continue
  }
}
