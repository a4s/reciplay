#!/bin/sh

APPLICATION_HOST=$1

while IFS='' read -r line || [[ -n "$line" ]]; do
	echo "Text read from file: $line"
	curl -v -H "Content-Type: application/json" -X POST -d "${line}" "http://${APPLICATION_HOST}/recipes"
done < "$2"



