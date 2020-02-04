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
package org.apache.kafka.common.requests;

import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.kafka.common.protocol.types.Struct;

// Abstract class for all control requests including UpdateMetadataRequest, LeaderAndIsrRequest and StopReplicaRequest
public abstract class AbstractControlRequest extends AbstractRequest {
    public static final long UNKNOWN_BROKER_EPOCH = -1L;

    protected static final Field.Int32 CONTROLLER_ID = new Field.Int32("controller_id", "The controller id");
    protected static final Field.Int32 CONTROLLER_EPOCH = new Field.Int32("controller_epoch", "The controller epoch");
    protected static final Field.Int64 MAX_BROKER_EPOCH = new Field.Int64("max_broker_epoch", "The max value of all broker epochs");

    protected final int controllerId;
    protected final int controllerEpoch;
    protected final long maxBrokerEpoch;

    public static abstract class Builder<T extends AbstractRequest> extends AbstractRequest.Builder<T> {
        protected final int controllerId;
        protected final int controllerEpoch;
        protected final long maxBrokerEpoch;

        protected Builder(ApiKeys api, short version, int controllerId, int controllerEpoch, long maxBrokerEpoch) {
            super(api, version);
            this.controllerId = controllerId;
            this.controllerEpoch = controllerEpoch;
            this.maxBrokerEpoch = maxBrokerEpoch;
        }

    }

    public int controllerId() {
        return controllerId;
    }

    public int controllerEpoch() {
        return controllerEpoch;
    }

    public long maxBrokerEpoch() {
        return maxBrokerEpoch;
    }

    protected AbstractControlRequest(ApiKeys api, short version, int controllerId, int controllerEpoch, long maxBrokerEpoch) {
        super(api, version);
        this.controllerId = controllerId;
        this.controllerEpoch = controllerEpoch;
        this.maxBrokerEpoch = maxBrokerEpoch;
    }

    protected AbstractControlRequest(ApiKeys api, Struct struct, short version) {
        super(api, version);
        this.controllerId = struct.get(CONTROLLER_ID);
        this.controllerEpoch = struct.get(CONTROLLER_EPOCH);
        this.maxBrokerEpoch = struct.getOrElse(MAX_BROKER_EPOCH, UNKNOWN_BROKER_EPOCH);
    }

    // Used for test
    long size() {
        return toStruct().sizeOf();
    }

}
