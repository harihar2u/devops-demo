#!/usr/bin/env bash

export $(cat .env | xargs)

# Pull the Latest Images
docker pull $DOCKER_REGISTRY/tkm/db_migration:$TKM_VERSION
docker pull $DOCKER_REGISTRY/a-sptsm/db_migration:$ASPTSM_VERSION
docker pull $DOCKER_REGISTRY/coles/db-migration-demo-set1-dvm:$COLES_PROJECT_VERSION

# Wait for DBs to be ready
declare -A migrate_command
dblist=( "db-tkm" "db-a-sptsm" "db-upp" )
migrate_command["db-tkm"]='docker run --rm --network="$COMPOSE_PROJECT_NAME"_network $DOCKER_REGISTRY/tkm/db_migration:$TKM_VERSION -url=jdbc:postgresql://db-tkm:5432/postgres -configFile=conf/flyway-tkm.conf migrate'
migrate_command["db-a-sptsm"]='docker run --rm --network="$COMPOSE_PROJECT_NAME"_network $DOCKER_REGISTRY/a-sptsm/db_migration:$ASPTSM_VERSION -url=jdbc:postgresql://db-a-sptsm:5432/postgres -configFile=conf/flyway-a-sptsm.conf migrate'
migrate_command["db-upp"]='docker run --rm --network="$COMPOSE_PROJECT_NAME"_network $DOCKER_REGISTRY/coles/db-migration-demo-set1-dvm:$COLES_PROJECT_VERSION  -url=jdbc:postgresql://db-upp:5432/postgres -configFile=conf/flyway-dvm.conf migrate'
for dbname in "${dblist[@]}"
do
  retry_count=0
  until
    docker run --network="$COMPOSE_PROJECT_NAME"_network -i postgres psql -h $dbname -U postgres -c "select 1" > /dev/null 2>&1;
  do
    retry_count+=1;
    if [ $retry_count -gt 15 ]
      then
        echo "One of the DB was not up, Migrations not successfully applied to all DBs"
        exit 1;
        break 2;
    fi
    sleep 2;
  done
  eval ${migrate_command[$dbname]}
done