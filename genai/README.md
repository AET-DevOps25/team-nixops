# GenAI MicroService

## Getting Started
Start the api:
```sh
fastapi dev src/app.py
```

Re-generate api types in case of changes to openapi.yml with the following:
```sh
openapi-generator-cli generate -g python-fastapi -i ../openapi.yml^C
```
