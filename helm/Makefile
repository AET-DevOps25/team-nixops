.PHONY: deploy

all: deploy

deploy:
	helm upgrade --install --namespace nixops-default nixops .

uninstall:
	helm uninstall --namespace nixops-default nixops
