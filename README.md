# TUM Scheduler – AI-Powered Semester Planner
[![CI](https://img.shields.io/badge/Deploy-passing-brightgreen)](https://github.com/AET-DevOps25/team-nixops/actions/workflows/ci.yml)
[![Staging](https://img.shields.io/badge/Build_Infrastructure-passing-brightgreen)](https://github.com/AET-DevOps25/team-nixops/actions/workflows/stage.yml)
\
![client](https://img.shields.io/github/v/tag/AET-DevOps25/team-nixops?sort=semver&filter=client%40v*&label=%20)
![embedding-bridge](https://img.shields.io/github/v/tag/AET-DevOps25/team-nixops?sort=semver&filter=embedding-bridge%40v*&label=%20)
![genai](https://img.shields.io/github/v/tag/AET-DevOps25/team-nixops?sort=semver&filter=genai%40v*&label=%20)
![schedule-manager](https://img.shields.io/github/v/tag/AET-DevOps25/team-nixops?sort=semver&filter=schedule_manager%40v*&label=%20)
![scraper](https://img.shields.io/github/v/tag/AET-DevOps25/team-nixops?sort=semver&filter=scraper%40v*&label=%20)
![schedule-optimizer](https://img.shields.io/github/v/tag/AET-DevOps25/team-nixops?sort=semver&filter=schedule-optimizer%40v*&label=%20)

TUM Scheduler is an intelligent course scheduling tool designed to help students at the [Technical University of Munich (TUM)](https://www.tum.de/) create,
optimize, and refine their semester plans with ease.

Leveraging GenAI, the tool offers personalized course recommendations,
conflict-free scheduling, and an interactive planning experience tailored to user preferences, academic goals, and interests.

![demo](https://github.com/user-attachments/assets/8604fa6f-e9a6-491f-a353-3256e47fd960)

- Live Demo: https://nixops-devops25.student.k8s.aet.cit.tum.de/
- Statistics: https://stats.nixops-devops25.student.k8s.aet.cit.tum.de/
- Alerts: https://push.nixops-devops25.student.k8s.aet.cit.tum.de/alerts
- Dev Server: http://ec2-16-171-59-110.eu-north-1.compute.amazonaws.com
- Documentation: [Wiki](https://github.com/AET-DevOps25/team-nixops/wiki)


##  Microservice Architecture

![architecture](https://github.com/user-attachments/assets/ca07f243-3b4e-4e6b-ad8f-27fd67a8abae)

### client
- **User facing interface for semester planning**
- Chat interface using the [genai service](#genai)
- Calendar interface synced with the [schedule-manager](#schedule-manager)
- Tech Stack: React (or Next.js), Shadcn/ui, FullCalendar

### genai
- **The core AI assistant service**
- Queries local vector database with course embeddings
- Suggests courses in interactive dialogue
- Creates and manages schedules using the [schedule-manager](#schedule-manager)
- Manages longterm AI memory
- Tech Stack: Python, LangGraph, Milvus, Redis, OpenAI + Ollama
- SwaggerUI: http://localhost:8000/docs

### scraper
- **The data ingestion service**
- Fetches course data by combining different TUM APIs
- Establishes connection between study programs and courses
- Abstracts away most of the complexity of the TUM database system
- Offers a simple REST API
- Tech Stack: SpringBoot, Kotlin

### embedding-bridge
- **The bridge between the [scraper](#scraper) and [genai](#genai) services**
- Pulls data from the scraper and genai
- Determines which courses need to be embedded
- Sends data to genai and triggers a new embedding job
- Monitors the running embedding
- Tech Stack: SpringBoot, Kotlin

### schedule-manager
- **The service responsible for managing users schedules**
- Used by the [genai service](#genai) with tool-calls
- Supports creating and editing schedules
- Pulls course data from the [scraper](#scraper)
- Creates schedules and associated appointments for the calendar in the [client](#client)
- Tech Stack: SpringBoot, Kotlin

## Running the Application

For ease of use we provide a [docker compose](https://docs.docker.com/compose/) file to run the application with prebuilt images.
First clone the repository:

```bash
git clone https://github.com/AET-DevOps25/team-nixops
cd team-nixops
```

Then create a `.env` files based on the provided `.env.template`
And then simply run it with docker compose:

```bash
docker-compose up -d
```

The client UI will be available at [http://localhost:3030](http://localhost:3030).\

Alternatively follow the [setup instructions](#setup-instructions) for a development environment

## Setup Instructions

### Prerequisites:
- Nix
- Docker & Docker Compose

### Cloning the Repository

First clone the repository:

```bash
git clone https://github.com/AET-DevOps25/team-nixops
cd team-nixops
```

### Building the services

All services can be build with nix:
```bash
nix build .#client
nix build .#genai
nix build .#scraper
nix build .#embedding-bridge
nix build .#schedule-manager
```

### Building docker images

All docker images can be built with nix:
```bash
nix build .#client.dockerImage           -o client
nix build .#genai.dockerImage            -o genai
nix build .#scraper.dockerImage          -o scraper
nix build .#embedding-bridge.dockerImage -o embedding-bridge
nix build .#schedule-manager.dockerImage -o schedule-manager
```

These can then be loaded into docker using:
```bash
cat client           | docker load
cat genai            | docker load
cat scraper          | docker load
cat embedding-bridge | docker load
cat schedule-manager | docker load
```

### Running the services

- **Running the [client service](#client):**\
  The client service can be run with:
  ```bash
  nix run .#client
  ```
  or for development with:
  ```bash
  cd client
  npm run dev
  ```
  The client UI will be available at [http://localhost:3030](http://localhost:3030).

- **Running the [genai service](#genai):**\
  The genai service needs some databases to be available.
  These can be started with
  ```bash
  docker compose up -d -f genai/compose.yml
  ```
  The genai service can then be run with:
  ```bash
  nix run .#genai
  ```
  or for development with:
  ```bash
  cd genai
  uvicorn genai.app:app --host 0.0.0.0 --port 8000 --workers 4
  ```

- **Running the [scraper service](#scraper):**\
  The genai service needs a postgres database to be available.
  This database can be started with
  ```bash
  docker compose up -d -f scraper/docker-compose.yml
  ```
  The scraper service can then be run with:
  ```bash
  nix run .#scraper
  ```
  or for development with:
  ```bash
  cd scraper
  ./gradlew bootRun
  ```

- **Running the [schedule-manager service](#schedule-manager):**\
  The schedule-manager service can be run with:
  ```bash
  nix run .#schedule-manager
  ```
  or for development with:
  ```bash
  cd schedule-manager
  ./gradlew bootRun
  ```

- **Running the [embedding-bridge service](#embedding-bridge):**\
  The embedding-bridge service can be run with:
  ```bash
  nix run .#embedding-bridge
  ```
  or for development with:
  ```bash
  cd embedding-bridge
  ./gradlew bootRun
  ```
 
## Testing

Unit tests are automatically run during the nix build process.\
Therefor all unit tests can be run using:
```bash
nix build .#client
nix build .#genai
nix build .#scraper
nix build .#embedding-bridge
nix build .#schedule-manager
```
Integration tests can be run using the following command:
```bash
nix flake check -L --option sandbox false --no-pure-eval
```
This will test all dev environments, packages, infrastructure configurations, and also run scripted tests in a VM.

## CI/CD Pipeline

### GitHub Actions

The project includes GitHub Actions workflows for:
- **Testing**: All changes to services are tested automatically. Long integration tests are only run on `main`.
- **Linting**: All OpenAPI and helm files are linted for correctness
- **Building Docker Images**: Automatically builds and pushes Docker images to GitHub Container Registry. Specifically, `latest` tracks the `main` branch and versioned releases correspond to tagged commits.
- **Automatic Versioning**: Commits that pass the CI are automatically tagged if any version is updated. Tags will have the format: `<name>@v<version>`.
- **Deploy Docker Images**: Deploys the application to a production Kubernetes environment using helm and to a provisioned server on AWS using `podman-compose`.

### Reproducible Environments

All environments are provisioned with automated tools such as helm, nix and terraform to ensure that the system is reproducible with minimal MTTR from any state.
Because these operations are critical and can have financial implications, these need to be performed by an admin.

**Secrets**: In reproducible environments it is not trivial to manage secrets as these a potentially revealed in public configuration files. To solve this we use sops &mdash; a way to store secrets that only get decrypted by authorized targets at runtime.\
**No decrypted secret gets written to persistent storage, ever!**\
The configuration that determines recipients for a secret it generated to improve maintainability.

Details can be found at the respective subfolders.

### Git-Hooks

The project includes Git pre-commit hooks for:
- **Formatting**: All files are formatted with `nix fmt .`
- **Linting**: All OpenAPI and helm files are linted for correctness

## Project Structure

```
├── client
│   ├── src/                # source code
│   └── package.json        # client dependencies
│
├── genai/
│   ├── compose.yml         # docker compose for genai development
│   ├── src/                # source code
│   ├── pyproject.toml      # genai dependencies
│   └── openapi.yml         # genai openapi contract
│
├── scraper/
│   ├── docker-compose.yml  # docker compose for scraper development
│   ├── src/                # source code
│   ├── build.gradle.kts    # scraper dependencies
│   └── openapi.yaml        # scraper openapi contract
│
├── embedding-bridge/
│   ├── src/                # source code
│   └── build.gradle.kts    # embedding-bridge dependencies
│
├── schedule-manager/
│   ├── src/                # source code
│   ├── build.gradle.kts    # schedule-manager dependencies
│   └── openapi.yaml        # schedule-manager openapi contract
│
├── schedulingEngine/
│   ├── src/                # source code
│   └── build.gradle.kts    # scheduling-engine dependencies
│
├── terraform/              # terraform code for custom kubernetes deployment
│   ├── admins
│   ├── hcloud
│   └── k8s
│
├── nix/
│   ├── checks/
│   ├── devShell            # nix development environments
│   ├── modules/
│   ├── targets/
│   └── treefmt.nix         # nix formatting setup
│
├── helm                    # helm deployment
│   ├── Chart.yaml
│   ├── templates/
│   │   ├── client/
│   │   ├── embedding-bridge/
│   │   ├── genai/
│   │   ├── ingress/
│   │   ├── monitoring/
│   │   ├── schedule-manager/
│   │   └── scraper/
│   ├── values.ci.yaml
│   └── values.yaml
│
├── docker-compose.yml
├── flake.lock
├── flake.nix
└── .github/workflows/       # CI/CD workflows
```

## License

This project is licensed under the MIT License.
