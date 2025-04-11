#!/bin/zsh

docker build -t copy-paste-tool .
docker run -p 80:80 copy-paste-tool