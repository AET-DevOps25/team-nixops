# GenAI MicroService

## Getting Started
Start the api:
```sh
fastapi dev src/genai/app.py
or
LLM_API_KEY="sk-..." nix run .#genai
```

Note that nix run needs all env variables to be passed explicitly.

Re-generate api types in case of changes to openapi.yml with the following:
```sh
rm -rf api # clean up
openapi-generator-cli generate -g python-fastapi -i ../openapi.yml
```

## Milvus
web dashboard at http://localhost:9091/webui/
