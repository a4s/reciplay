#!/bin/sh

APPLICATION_HOST=$1
TOKEN=$2

curl -i -H "X-Auth-Token: ${TOKEN}"  -X GET "http://${APPLICATION_HOST}/recipes"



