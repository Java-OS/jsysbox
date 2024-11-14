#!/bin/bash 

docker run -it --rm --name=build-jsysbox \
  -v $PWD:/opt/jsysbox \
  -v $HOME/.m2:/root/.m2 \
  --privileged \
  mah454/josb:latest /opt/jsysbox/build.sh

  # --cap-add=NET_ADMIN \
  # --cap-add=NET_RAW \
