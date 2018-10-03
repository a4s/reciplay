#!/bin/sh

APPLICATION_HOST=$1
TOKEN=$3

while IFS='' read -r line || [[ -n "$line" ]]; do
	echo "Text read from file: $line"
	curl -i -H "Content-Type: application/json" -H "X-Auth-Token: ${TOKEN}"  -X POST -d "${line}" "http://${APPLICATION_HOST}/recipes"
done < "$2"



