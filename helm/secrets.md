```bash
kubectl create secret generic minio --namespace=genai --from-literal=root_user=minioadmin --from-literal=root_password=minioadmin
```
default for milvus is "minioadmin", if other secrets are chosen: set secrets also in milvus
