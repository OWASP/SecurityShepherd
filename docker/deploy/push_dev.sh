#!/usr/bin/env bash
if [ -f .env ]
then
  export $(cat .env | xargs)
else
    export $(cat ../../.env | xargs)
fi

export TAG=dev

docker login -u=$DOCKER_USER -p=$DOCKER_PASS
docker tag ${IMAGE_TOMCAT}:latest ${IMAGE_TOMCAT}:${TAG}
docker push ${IMAGE_TOMCAT}:${TAG}