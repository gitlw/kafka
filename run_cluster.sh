#!/usr/bin/env bash
set -x
set -e

./gradlew jar

# clearing services that may be already running
ps -ef|grep -E "zookeeper|kafka"|grep -v grep|awk '{print $2}'|xargs kill -9

(rm -rf /tmp/zookeeper /tmp/kafka-logs* || echo "no data to clear")

./bin/zookeeper-server-start.sh  config/zookeeper.properties &

sleep 10

env LOG_DIR=./logs0 ./bin/kafka-server-start.sh config/server0.properties &

env LOG_DIR=./logs1 ./bin/kafka-server-start.sh config/server1.properties &


