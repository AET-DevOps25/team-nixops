# Canteen App Helm Chart

This Helm chart deploys the Canteen App application consisting of a server and client component.

## Prerequisites

- Kubernetes 1.16+
- Helm 3.0+
- Ingress Controller (like nginx-ingress)

## Installing the Chart

To install the chart with the release name `canteen`:

```bash
helm install canteen ./helm/canteen-app
```

## Uninstalling the Chart

To uninstall/delete the `canteen` release:

```bash
helm uninstall canteen
```

## Parameters

### Global Parameters

| Name                   | Description                                | Value           |
|------------------------|--------------------------------------------|-----------------|
| `tumid`                | TUM ID used in hostname generation         | `ga12abc`       |
| `namespace.create`     | Create the namespace                       | `true`          |
| `namespace.name`       | Namespace to use for the application       | `canteen-app`   |

### Server Parameters

| Name                       | Description                                | Value                                          |
|----------------------------|--------------------------------------------|------------------------------------------------|
| `server.image.repository`  | Server image repository                    | `ghcr.io/aet-devops25/w05-template/server`     |
| `server.image.tag`         | Server image tag                           | `latest`                                       |
| `server.image.pullPolicy`  | Server image pull policy                   | `Always`                                       |
| `server.service.type`      | Server service type                        | `NodePort`                                     |
| `server.service.port`      | Server service port                        | `8080`                                         |
| `server.service.targetPort`| Server container port                      | `8080`                                         |
| `server.service.nodePort`  | Server node port                           | `30001`                                        |
| `server.replicaCount`      | Number of server replicas                  | `1`                                            |
| `server.env`               | Server environment variables                | See `values.yaml` for defaults                 |

### Client Parameters

| Name                       | Description                                | Value                                          |
|----------------------------|--------------------------------------------|------------------------------------------------|
| `client.image.repository`  | Client image repository                    | `ghcr.io/aet-devops25/w05-template/client`     |
| `client.image.tag`         | Client image tag                           | `latest`                                       |
| `client.image.pullPolicy`  | Client image pull policy                   | `Always`                                       |
| `client.service.type`      | Client service type                        | `NodePort`                                     |
| `client.service.port`      | Client service port                        | `3000`                                         |
| `client.service.targetPort`| Client container port                      | `3000`                                         |
| `client.service.nodePort`  | Client node port                           | `30000`                                        |
| `client.replicaCount`      | Number of client replicas                  | `1`                                            |
| `client.apiUrl`            | API URL for the client to connect to       | `http://localhost:30001`                       |
