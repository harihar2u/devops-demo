## Apply Configuration 
kubectl apply -f ./diployment.yml

## Deployment information
kubectl get pods --output=wide

kubectl describe deployments <name>

## ReplicaSet ###
kubectl get replicasets

kubectl describe replicasets

## Service object that exposes the deployment ##
kubectl expose deployment estore-ws --type=NodePort --name=estore-service

kubectl get services

kubectl describe services <service name>

kubectl get pods --output=wide

kubectl logs <IPOD name>

curl http://external-ip:port

http://localhost:<NodePort>/estore

## Cleaning up ##
kubectl delete services estore-service

kubectl delete deployment,service --all