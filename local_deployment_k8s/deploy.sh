#!/bin/zsh
docker build -t catalog-service-0.0.1 .

kubectl create deployment catalog-service \
    --image=catalog-service-0.0.1

kubectl patch deployment catalog-service \
  --type='json' \
  -p='[{"op": "replace", "path": "/spec/template/spec/containers/0/imagePullPolicy", "value":"Never"}]'

sleep 2s

 kubectl expose deployment catalog-service \
    --name=catalog-service \
    --port=8080

sleep 2s

kubectl port-forward service/catalog-service 9292:8080