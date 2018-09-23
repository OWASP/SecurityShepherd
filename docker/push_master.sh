#!/usr/bin/env bash
if [ -f .env ]
then
  export $(cat .env | sed 's/#.*//g' | xargs)
fi

export TAG=latest

docker tag $CONTAINER_TOMCAT $IMAGE_TOMCAT:$VERSION
docker tag $IMAGE_TOMCAT:$VERSION $IMAGE_TOMCAT:$TAG
docker push $IMAGE_TOMCAT:$TAG
docker push $IMAGE_TOMCAT:$VERSION