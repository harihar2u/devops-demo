## Apply Configuration 
kubectl apply -f postgres-diployment.yml
kubectl apply -f estore-ws-diployment.yml

curl http://localhost:32080/estore-ws

## Deployment information
kubectl get pods --output=wide

kubectl describe deployments <name>

## ReplicaSet ###
kubectl get replicasets

kubectl describe replicasets

kubectl get services

kubectl describe services estore-service

kubectl get pods --output=wide

kubectl logs <IPOD name>

kubectl exec <IPOD name> -- ls

## Cleaning up ##
kubectl delete all --all

#### Rancher

https://github.com/rancher/rancher.git
docker run -d --restart=unless-stopped -p 80:80 -p 443:443 rancher/rancher

kubectl create configmap estore-config --from-file=D:/Archive/BackUp/estore/src/main/resources/application.yml

