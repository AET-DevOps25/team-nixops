# Project Grading Checklist

## 1. Functional Application
- [x] End-to-end functionality between all components (client, server, database)
- [x] Smooth and usable user interface
- [x] REST API is clearly defined and matches functional needs
- [x] Server Side has at least 3 microservices
- [x] Application topic is appropriately chosen and fits project objectives

## 2. GenAI Integration
- [x] GenAI module is well-embedded and fulfills a real user-facing purpose
- [x] Connects to cloud/local LLM - !! todo: double check ollama !!
- [x] Modularity of the GenAI logic as a microservice

## 3. Containerization & Local Setup
- [x] Each component is containerized and runnable in isolation
- [x] docker-compose.yml enables local development and testing with minimal effort and provides sane defaults
  - [x] Individual dev envs
  - [x] Complete docker-compose.yml

## 4. CI/CD & Deployment
- [x] CI pipeline with build, test, and Docker image generation via GitHub Action
- [x] CD pipeline set up to automatically deploy to Kubernetes on main merge
- [x] Deployment works on our infrastructure (Rancher)
- [ ] Deployment works on Cloud (AWS)

## 5. Monitoring & Observability
- [x] Prometheus integrated and collecting meaningful metrics - !! double check !!
- [ ] Grafana dashboards for system behavior visualization
- [ ] At least one alert rule set up

## 6. Testing & Structured Engineering Process
- [ ] Test cases
  - [ ] Server
    - [x] Scraper
    - [ ] Schedule-manager
    - [ ] Embedding-bridge
    - [ ] Client
    - [ ] GenAI
  - [ ] GenAI
- [ ] Evidence of software engineering process
  - [ ] Requirements
  - [ ] Architecture models
    - [ ] Top-level architecture
    - [ ] Use-case diagramm
    - [ ] Analysis object model
- [ ] Tests run automatically in CI and cover key functionality

## 7. Documentation
- [ ] README.md
  - [ ] Setup instructions
  - [ ] Architecture overview
  - [ ] Usage guide
  - [ ] Clear mapping of responsibilities
  - [ ] Documentation of CI/CD setup, and GenAI usage included
  - [ ] API-driven deployment
  - [ ] Monitoring instructions

## Bonus
- [ ] Advances Kubernetes
  - [x] Self-healing
  - [ ] Custom-operators
  - [ ] Auto-scaling
- [x] Full RAG pipeline - milvus as part of core feature set
- [ ] Observability
  - [x] Log aggregation - !! double check completeness !!
- [x] Beatiful, original UI or impactful project topic
  - [x] Beatiful UI
  - [x] Original UI
  - [x] Impactful project topic
- [ ] Advanced monitoring setup
  - [x] Custom prometheus exporters
  - [ ] Grafana
    - [x] Imports
    - [ ] Dashboard

