#!/usr/bin/env bash

export $(cat .env | xargs)
docker-compose --no-ansi -f docker-compose.yml -f docker-stack.config.yml -f docker-compose.qa.yml -f docker-stack.qa_ports.yml -f docker-stack.proxy-services.yml -f docker-compose.op.yml -f docker-stack.op.config.yml -f docker-compose.op.proxy-services.yml pull
docker-compose --no-ansi -f docker-compose.yml -f docker-stack.config.yml -f docker-compose.qa.yml -f docker-stack.qa_ports.yml -f docker-stack.proxy-services.yml -f docker-compose.op.yml -f docker-stack.op.config.yml -f docker-compose.op.proxy-services.yml config > stack.yml
docker stack deploy --prune --with-registry-auth -c stack.yml $COMPOSE_PROJECT_NAME
