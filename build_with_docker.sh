#!/bin/bash 

docker run -it --rm --name=build-jsysbox \
  -v $PWD:/opt/jsysbox \
  -v $HOME/.m2:/root/.m2 \
  --privileged \
  -e EXEC_UNIT_TEST=${EXEC_UNIT_TEST} \
  mah454/josb:latest /opt/jsysbox/build.sh

  # --cap-add=NET_ADMIN \
  # --cap-add=NET_RAW \
