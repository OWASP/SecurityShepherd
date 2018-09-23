#!/usr/bin/env bash
export $(cat ../../.env | xargs)
export TAG=dev

docker tag ${CONTAINER_TOMCAT} ${IMAGE_TOMCAT}:${TAG}
docker push ${IMAGE_TOMCAT}:${TAG}