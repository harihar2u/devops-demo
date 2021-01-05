###### Apply Configuration 
kubectl apply -f postgres-diployment.yml
kubectl apply -f estore-ws-diployment.yml

psql -h localhost -U postgres --password -p 32080 postgres
http://localhost:32082/estore-ws/swagger-ui.html

###### Deployment information
kubectl get nodes --output=wide
kubectl get pods --output=wide
kubectl describe deployments <name>

kubectl logs <IPOD name>
kubectl exec <IPOD name> -- ls
kubectl get services
kubectl describe services estore-service

###### Cleaning up
kubectl delete all --all

###### Install Rancher
https://rancher.com/docs/rancher/v2.x/en/installation/other-installation-methods/single-node-docker/

docker run -d --restart=unless-stopped -p 80:80 -p 443:443 --privileged rancher/rancher
add custom cluster option
http://localhost:80/

###### Cleaning up 
docker rm -f $(docker ps -aq)
docker stop $(docker ps -aq)

