#!/bin/bash
set -e

cd "$(dirname "$0")"

mvn clean package -DskipTests
docker build -t ress-srv:latest .

echo "Done. Image: ress-srv:latest"
