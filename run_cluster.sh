#!/usr/bin/env bash
set -x
set -e

./gradlew jar

./bin/zookeeper-server-start.sh  config/zookeeper.properties &

sleep 10

env LOG_DIR=./logs0 ./bin/kafka-server-start.sh config/server0.properties &

env LOG_DIR=./logs1 ./bin/kafka-server-start.sh config/server1.properties &


