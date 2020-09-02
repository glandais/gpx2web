#!/usr/bin/env bash

docker build -t graalvmvn -f Dockerfile-mvn .

docker rm gpxtools-build-helper
docker volume rm gpxtools-build
docker run -v gpxtools-build:/data --name gpxtools-build-helper busybox true
docker cp ../. gpxtools-build-helper:/data
docker rm gpxtools-build-helper

docker run -w /home/mvn/app -v gpxtools-build:/home/mvn/app -v m2-repository:/home/mvn/.m2/repository -it --rm graalvmvn \
    mvn -Duser.home=/home/mvn clean package -Pgraal

docker run -v gpxtools-build:/data --name gpxtools-build-helper busybox true
docker cp gpxtools-build-helper:/data/gpxtools-cli/target/gpxtools ../gpxtools
docker cp gpxtools-build-helper:/data/gpxtools-cli/target/gpxtools.jar ../gpxtools.jar
docker rm gpxtools-build-helper
