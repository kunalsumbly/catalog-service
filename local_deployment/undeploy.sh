#!/bin/zsh
kubectl delete deployments.apps catalog-service

kubectl delete service catalog-service

yes | docker container prune