#!/bin/bash

if [ -z "$DYNAMO_HOME" ]; then
    docker run -p 9000:9000 extender;
else
    docker run -p 9000:9000 -v ${DYNAMO_HOME}:/dynamo_home -e DYNAMO_HOME=/dynamo_home extender;
fi

