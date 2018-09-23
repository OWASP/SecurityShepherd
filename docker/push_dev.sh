#!/usr/bin/env bash
if [ -f .env ]
then
  export $(cat .env | sed 's/#.*//g' | xargs)
fi

export TAG=dev

docker tag '${CONTAINER_TOMCAT}' $IMAGE_TOMCAT:$TAG
docker push $IMAGE_TOMCAT:$TAG