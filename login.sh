#!/bin/sh

APPLICATION_HOST=localhost:9000
NAME=$1
ID=$2

curl -v -H "Content-Type: application/json" -X POST -d "{\"name\": \"${NAME}\", \"id\": \"${ID}\"}" "http://${APPLICATION_HOST}/login"



