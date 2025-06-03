# GenAI MicroService

## Getting Started
Start the api:
```sh
fastapi dev src
```

Re-generate api types in case of changes to openapi.yml with the following:
```sh
rm -rf api # clean up
openapi-generator-cli generate -g python-fastapi -i ../openapi.yml
```

Try out streaming!
```sh
fastapi dev src
curl  "http://localhost:8000/stream?prompt=heythere" --no-buffer
```
