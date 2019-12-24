#!/usr/bin/env bash

# Pull in the .env file
if [ -f .env ]
then
  export $(cat .env | xargs)
else
    export $(cat ../../.env | xargs)
fi

# Check if its master or dev branch and deploy accordingly
if [ $1 == 'master' ]
then
    export TAG=latest

    docker login -u=$DOCKER_USER -p=$DOCKER_PASS
    docker tag $IMAGE_TOMCAT:$TAG $IMAGE_TOMCAT:$VERSION
    docker push $IMAGE_TOMCAT:$TAG
    docker push $IMAGE_TOMCAT:$VERSION
elif [ $1 == 'dev' ]
then
    export TAG=dev

    docker login -u=$DOCKER_USER -p=$DOCKER_PASS
    docker tag ${IMAGE_TOMCAT}:latest ${IMAGE_TOMCAT}:${TAG}
    docker push ${IMAGE_TOMCAT}:${TAG}
fi