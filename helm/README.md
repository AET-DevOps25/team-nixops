# Helm

## Getting Started
Deploy using:
```bash
helm upgrade --install --namespace nixops-default nixops .
```


## Setup of a new namespace

Required secrets:
- minio: username, password
- genai: llm_api_key
- genai-postgres: username, password
- scraper-postgres: username, password

Setup using:
```bash
kubectl --namespace=nixops-default create secret generic minio --from-literal=username=... --from-literal=password=...
kubectl --namespace=nixops-default create secret generic genai --from-literal=llm_api_key=...
kubectl --namespace=nixops-default create secret generic genai-postgres --from-literal=username=... --from-literal=password=...
kubectl --namespace=nixops-default create secret generic scraper-postgres --from-literal=username=... --from-literal=password=...

helm install --namespace nixops-default nixops .
```
