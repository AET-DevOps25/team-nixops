import os
import subprocess
import yaml
from jinja2 import Environment, FileSystemLoader


def get_git_root():
    try:
        # Run git command to get the top-level directory
        root = (
            subprocess.check_output(
                ["git", "rev-parse", "--show-toplevel"], stderr=subprocess.DEVNULL
            )
            .decode()
            .strip()
        )
        return root
    except subprocess.CalledProcessError:
        raise EnvironmentError("Not a git repository or git is not installed")


# Get git root directory
git_root = get_git_root()

# Construct paths relative to git root
template_path = os.path.join(git_root, "nix", "devShell")
keys_yaml_path = os.path.join(template_path, "keys.yaml")
output_path = os.path.join(git_root, ".sops.yaml")

# Load keys.yaml
with open(keys_yaml_path) as f:
    keys = yaml.safe_load(f)["keys"]

# Setup Jinja2 environment and load template
env = Environment(loader=FileSystemLoader(template_path))
template = env.get_template("sops.yaml.j2")

# Render
output = template.render(keys=keys)

# Write output file
with open(output_path, "w") as f:
    f.write(output)

print(f"Generated {output_path}")
