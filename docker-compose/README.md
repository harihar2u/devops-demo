#### DOCKER Image
docker build -t harihar2u/project-demo/estore/estore-rest:0.0.1-SNAPSHOT .
docker run -p 8082:8082 -e server.port=8082 harihar2u/project-demo/estore/estore-rest:0.0.1-SNAPSHOT
http://localhost:8082/estore-ws/swagger-ui.html

docker container ls
docker container kill containerId
docker system prune
docker container prune
docker image prune
docker volume prune
docker rmi $(docker images -a -q)
docker rm -f $(docker ps -aq)

#### DOCKER-COMPOSE
docker-compose --compatibility --no-ansi -f docker-compose.yml up -d 
docker-compose --compatibility --no-ansi -f docker-compose.yml stop
http://localhost:8082/estore-ws/swagger-ui.html

docker-compose ps
docker-compose logs --tail 1000 estore
docker-compose down -v
docker-compose stop estore-app
docker-compose rm estore-app

#### Portainer
 docker volume create portainer_data
 docker run -d -p 9000:9000 -p 8000:8000 --name portainer --restart always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer
 http://localhost:9000/