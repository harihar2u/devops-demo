#!/usr/bin/env bash

export $(cat .env | xargs)

# re-deploy traefik, we do this early, and hope traefik is up by
# the time we try to connect the network
#docker stack deploy --prune --with-registry-auth -c docker-stack.proxy.yml traefik

COMPOSE_FILES="-f docker-compose.yml  -f docker-stack.config.yml"
for var in "$@"
do
    case $var in
    proxy)
      COMPOSE_FILES="$COMPOSE_FILES -f docker-stack.proxy-services.yml"
      ;;
    qa)
      COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.qa.yml -f docker-stack.qa_ports.yml"
      ;;
    dev)
      COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.dev.yml"
      ;;
    ecom)
      COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.qa.ecom.yml -f docker-stack.config-ecom.yml"
      ;;
    *)
      echo "Unknown deployment $var, valid deployment options are proxy, qa, dev and ecom."
      exit
      ;;
    esac
done

echo "$COMPOSE_FILES"
docker-compose $COMPOSE_FILES pull
docker-compose --no-ansi $COMPOSE_FILES config > stack.yml
docker stack deploy --prune --with-registry-auth -c stack.yml $COMPOSE_PROJECT_NAME

# connect stack network to traefik container
# this only works when traefik container is running
#TRAEFIK_CONTAINER_ID=$(docker ps --filter=name='traefik_proxy' --format='{{.ID}}')
#SWARM_NETWORK_NAME=${COMPOSE_PROJECT_NAME}_network
#docker network connect $SWARM_NETWORK_NAME $TRAEFIK_CONTAINER_ID
