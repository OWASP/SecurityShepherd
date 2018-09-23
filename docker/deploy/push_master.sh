#!/usr/bin/env bash
export $(cat ../../.env | xargs)
export TAG=latest

docker tag $CONTAINER_TOMCAT $IMAGE_TOMCAT:$VERSION
docker tag $IMAGE_TOMCAT:$VERSION $IMAGE_TOMCAT:$TAG
docker push $IMAGE_TOMCAT:$TAG
docker push $IMAGE_TOMCAT:$VERSION