/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.clients.consumer;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.record.TimestampType;

/**
 * In case of passthrough, value contains the message batch while the key is null
 * @param <K> null
 * @param <V> batch of messages
 */
public class PassThroughConsumerRecord<K, V> extends ConsumerRecord<K, V> {
    private final byte magic;

    public PassThroughConsumerRecord(String topic, int partition, long offset, K key, V value, byte magic) {
        super(topic, partition, offset, key, value);
        this.magic = magic;
    }

    public PassThroughConsumerRecord(String topic, int partition, long offset, long timestamp,
        TimestampType timestampType, long checksum, int serializedKeySize, int serializedValueSize, K key, V value,
        byte magic) {
        super(topic, partition, offset, timestamp, timestampType, checksum, serializedKeySize, serializedValueSize, key,
            value);
        this.magic = magic;
    }

    public PassThroughConsumerRecord(String topic, int partition, long offset, long timestamp,
        TimestampType timestampType, Long checksum, int serializedKeySize, int serializedValueSize, K key, V value,
        Headers headers, byte magic) {
        super(topic, partition, offset, timestamp, timestampType, checksum, serializedKeySize, serializedValueSize, key,
            value, headers);
        this.magic = magic;
    }

    public byte magic() {
        return magic;
    }
}