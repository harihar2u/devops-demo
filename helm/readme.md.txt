 ## Create the tiller ServiceAccount
 
 kubectl create serviceaccount --namespace kube-system tiller
 kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=kube-system:tiller
 kubectl patch deploy --namespace kube-system tiller-deploy -p '{"spec":{"template":{"spec":{"serviceAccount":"tiller"}}}}' 
 helm init --service-account tiller
 
 helm install .
 
 helm install stable/kubernetes-dashboard --name dashboard-demo
 
 #####Helm Lint:  helm lint ./estore
 
 #####Helm Template: helm template ./estore
 
 #####Helm Install: helm install --name estore ./estore
 
 #####Helm Get: helm ls --all
 
 ######Helm Upgrade: helm upgrade --name estore ./estore
 
 #####Helm Rollback:  helm install estore 1
  
 ######Helm Delete:  helm delete --purge estore
  
 ######Helm Package:  helm package ./estore
 
 
###### Reference
https://kubernetes.io/

https://helm.sh/docs/intro/

https://s3.us.cloud-object-storage.appdomain.cloud/developer/videos/helm-101/static/helm101.pdf