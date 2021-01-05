#!/usr/bin/env bash

export $(cat .env | xargs)
docker-compose --no-ansi -f docker-compose.yml -f docker-stack.config.yml  pull
docker-compose --no-ansi -f docker-compose.yml -f docker-stack.config.yml  > stack.yml
docker stack deploy --prune --with-registry-auth -c stack.yml $COMPOSE_PROJECT_NAME
