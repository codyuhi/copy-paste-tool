#!/bin/zsh

# Build the docker image with the custom Harbor registry tag
docker build -t harbor.minipc.local/library/copy-paste-tool:latest .

# Push the built image to the Harbor registry
docker push harbor.minipc.local/library/copy-paste-tool:latest