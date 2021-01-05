#!/usr/bin/env bash

export $(cat .env | xargs)
docker-compose --no-ansi -f docker-compose.yml -f docker-stack.config.yml -f pull
docker-compose --no-ansi -f docker-compose.yml -f docker-stack.config.yml config > stack.yml
docker stack deploy --prune --with-registry-auth -c stack.yml $COMPOSE_PROJECT_NAME
