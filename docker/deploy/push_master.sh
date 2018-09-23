#!/usr/bin/env bash
if [ -f .env ]
then
  export $(cat .env | xargs)
else
    export $(cat ../../.env | xargs)
fi

export TAG=latest

docker login -u=$DOCKER_USER -p=$DOCKER_PASS
docker tag $IMAGE_TOMCAT:$TAG $IMAGE_TOMCAT:$VERSION
docker push $IMAGE_TOMCAT:$TAG
docker push $IMAGE_TOMCAT:$VERSION