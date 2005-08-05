/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.databasemanager.connectionmanager;

public class ConnectionManagerInfo {

    private final int partitionCount;

    private final int connectionCount;

    private final int idleConnectionCount;

    private final int partitionMaxSize;

    private final int partitionMinSize;

    private final int blockingTimeoutMilliseconds;

    private final int idleTimeoutMinutes;

    public ConnectionManagerInfo(int partitionCount, int connectionCount,
            int idleConnectionCount, int partitionMaxSize,
            int partitionMinSize, int blockingTimeoutMilliseconds,
            int idleTimeoutMinutes) {
        this.partitionCount = partitionCount;
        this.connectionCount = connectionCount;
        this.idleConnectionCount = idleConnectionCount;
        this.partitionMaxSize = partitionMaxSize;
        this.partitionMinSize = partitionMinSize;
        this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
        this.idleTimeoutMinutes = idleTimeoutMinutes;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public int getIdleConnectionCount() {
        return idleConnectionCount;
    }

    public int getPartitionMaxSize() {
        return partitionMaxSize;
    }

    public int getPartitionMinSize() {
        return partitionMinSize;
    }

    public int getBlockingTimeoutMilliseconds() {
        return blockingTimeoutMilliseconds;
    }

    public int getIdleTimeoutMinutes() {
        return idleTimeoutMinutes;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer("ConnectionManagerInfo:\n");
        buff.append("partitionCount: ").append(partitionCount).append("\n");
        buff.append("connectionCount: ").append(connectionCount).append("\n");
        buff.append("idleConnectionCount: ").append(idleConnectionCount)
                .append("\n");
        buff.append("partitionMaxSize: ").append(partitionMaxSize).append("\n");
        buff.append("partitionMinSize: ").append(partitionMinSize).append("\n");
        buff.append("blockingTimeoutMilliseconds: ").append(
                blockingTimeoutMilliseconds).append("\n");
        buff.append("idleTimeoutMinutes: ").append(idleTimeoutMinutes).append(
                "\n");

        return buff.toString();
    }
}
