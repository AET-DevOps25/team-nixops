variable "passphrase" {}

terraform {
  encryption {
    key_provider "pbkdf2" "mykey" {
      passphrase = var.passphrase
    }

    method "aes_gcm" "encrypted" {
      keys = key_provider.pbkdf2.mykey
    }

    state {
      method   = method.aes_gcm.encrypted
      enforced = true
    }
  }
}

module "dev" {
  source  = "../../../terraform/admins"
  use_aws = true
  ssh_keys = {
    floriannadam = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIEbn4CJG3JtDrziLAEQ21bZxL5w4+MkDwD17LoQeEuJc florian@nixoslaptop"
    ghoscht      = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIBKg7pWqDj8X+4YFbrL99PgwuIfV8W4J1tsClG2e1A8w openpgp:0x7505C713"
    mikilio      = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDYPjdpzOZCLJfSGaeMo7yYBmTTVSnjf04aB3K74qZN9zOBotyBmLqD+SzVBrvbmGH3byRZF1V5bWdckc9ttdeJnLGJBtBNpBoCEb7V9AufzUC6njka2pvw8yIrOJZmK7JHK/IY21Wjsagu10f4OdUWF+CRevLmECOK0CJQmzbpjksFqVB9vaCI6fTBm0ZD+/AezXideg4FDBnfzjvT/0WEJbYj9yV6UO7rNIx7mYIErCnTg3PUUMuNz1By3pUGBjXnhDogW9KgrDqGDYbkqalxiNOW35D0QyxiIBhqy96B1Irt+dIQPG2qj6uAsMqfAyycGyZ34QukxKbudE/j+F/JlmGAfB3wbS1zaIyASd3vV0nO8zp2fQcyyP2wkjYe/qB9QFnNDh6/OUANKtMdXwFL94ZYJd4ZVwxsVZPdFlCS34Jf10o4P0rXAEcsQplsHFo0bjxn5yySwjEl26HZKBKd7PYQ7hb/zMCVroqcmBLoqGLD5vDaeZ3EMvTIHw6Gumbg6TggLopCzdwNiUqYdqelXwVC/mdpdyOYP/aBzMuN7FzOkehC4p99Pn3tiS+saqmU6em5y4l+U722J9fuKppYIB1VvZY8sDLQlxXepUykNzj0wwJse9fcgZ8X2P48F4gg8OvjZJ/Efyygvi6xsFoSmsP5itC0PIUG95aLhE78vQ== cardno:23_674_753a"
  }
}
