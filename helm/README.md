# Helm

## Getting Started
Deploy using:
```bash
helm upgrade --install --namespace nixops-default nixops .
```


## Setup of a new namespace
```bash
kubectl --namespace=nixops-ge47vaq create secret generic minio --from-literal=root_user=minioadmin --from-literal=root_password=minioadmin
kubectl --namespace=nixops-ge47vaq  create secret generic genai --from-literal=llm_api_key=sk-...
helm install --namespace nixops-ge47vaq nixops .
```
default for milvus is "minioadmin", if other secrets are chosen: set secrets also in milvus
