/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.connector.outbound.connectionmanagerconfig;

import org.apache.geronimo.connector.outbound.ConnectionInterceptor;
import org.apache.geronimo.connector.outbound.MultiPoolConnectionInterceptor;
import org.apache.geronimo.connector.outbound.PoolingAttributes;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class PartitionedPool implements PoolingSupport {

    private boolean partitionByConnectionRequestInfo;
    private boolean partitionBySubject;

    private final SinglePool singlePool;

    private PoolingAttributes poolingAttributes;

    public PartitionedPool(int maxSize, int minSize, int blockingTimeoutMilliseconds, int idleTimeoutMinutes, boolean matchOne, boolean matchAll, boolean selectOneAssumeMatch, boolean partitionByConnectionRequestInfo, boolean partitionBySubject) {
        singlePool = new SinglePool(maxSize, minSize, blockingTimeoutMilliseconds, idleTimeoutMinutes, matchOne, matchAll, selectOneAssumeMatch);
        this.partitionByConnectionRequestInfo = partitionByConnectionRequestInfo;
        this.partitionBySubject = partitionBySubject;
    }

    public boolean isPartitionByConnectionRequestInfo() {
        return partitionByConnectionRequestInfo;
    }

    public void setPartitionByConnectionRequestInfo(boolean partitionByConnectionRequestInfo) {
        this.partitionByConnectionRequestInfo = partitionByConnectionRequestInfo;
    }

    public boolean isPartitionBySubject() {
        return partitionBySubject;
    }

    public void setPartitionBySubject(boolean partitionBySubject) {
        this.partitionBySubject = partitionBySubject;
    }

    public int getMaxSize() {
        return singlePool.getMaxSize();
    }

    public void setMaxSize(int maxSize) {
        singlePool.setMaxSize(maxSize);
    }

    public int getBlockingTimeoutMilliseconds() {
        return poolingAttributes.getBlockingTimeoutMilliseconds();
    }

    public void setBlockingTimeoutMilliseconds(int blockingTimeoutMilliseconds) {
        poolingAttributes.setBlockingTimeoutMilliseconds(blockingTimeoutMilliseconds);
    }

    public int getIdleTimeoutMinutes() {
        return poolingAttributes.getIdleTimeoutMinutes();
    }

    public void setIdleTimeoutMinutes(int idleTimeoutMinutes) {
        poolingAttributes.setIdleTimeoutMinutes(idleTimeoutMinutes);
    }

    public boolean isMatchOne() {
        return singlePool.isMatchOne();
    }

    public void setMatchOne(boolean matchOne) {
        singlePool.setMatchOne(matchOne);
    }

    public boolean isMatchAll() {
        return singlePool.isMatchAll();
    }

    public void setMatchAll(boolean matchAll) {
        singlePool.setMatchAll(matchAll);
    }

    public boolean isSelectOneAssumeMatch() {
        return singlePool.isSelectOneAssumeMatch();
    }

    public void setSelectOneAssumeMatch(boolean selectOneAssumeMatch) {
        singlePool.setSelectOneAssumeMatch(selectOneAssumeMatch);
    }

        public ConnectionInterceptor addPoolingInterceptors(ConnectionInterceptor tail) {
            MultiPoolConnectionInterceptor pool = new MultiPoolConnectionInterceptor(
                            tail,
                            singlePool,
                            isPartitionBySubject(),
                            isPartitionByConnectionRequestInfo());
            this.poolingAttributes = pool;
            return pool;
        }

    public int getPartitionCount() {
        return poolingAttributes.getPartitionCount();
    }

    public int getPartitionMaxSize() {
        return poolingAttributes.getPartitionMaxSize();
    }

    public void setPartitionMaxSize(int maxSize) throws InterruptedException {
        poolingAttributes.setPartitionMaxSize(maxSize);
    }

    public int getPartitionMinSize() {
        return poolingAttributes.getPartitionMinSize();
    }

    public void setPartitionMinSize(int minSize) {
        poolingAttributes.setPartitionMinSize(minSize);
    }

    public int getIdleConnectionCount() {
        return poolingAttributes.getIdleConnectionCount();
    }

    public int getConnectionCount() {
        return poolingAttributes.getConnectionCount();
    }
}
