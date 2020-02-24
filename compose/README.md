## DOCKER Image
docker build -t estore-app .
docker run -p 9010:8020 -e server.port=8020 estore-app
http://localhost:9010/estore-ws/index

docker container ls
docker container kill containerId

docker system prune
docker container prune
docker image prune
docker rmi $(docker images -a -q)

## DOCKER-COMPOSE
docker-compose --compatibility --no-ansi -f docker-compose.yml up -d  

http://localhost:9010/index

docker-compose ps
docker-compose logs --tail 1000 estore

docker-compose down -v
docker-compose stop estore-app
docker-compose rm estore-app

## start portainer
 docker volume create portainer_data
 docker run -d -p 9000:9000 -p 8000:8000 --name portainer --restart always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer