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

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class PartitionedPool extends PoolingSupport {

    private boolean partitionByConnectionRequestInfo;
    private boolean partitionBySubject;

    private final SinglePool singlePool;

    public PartitionedPool(boolean partitionByConnectionRequestInfo, boolean partitionBySubject, int maxSize, int blockingTimeoutMilliseconds, boolean matchOne, boolean matchAll, boolean selectOneAssumeMatch) {
        singlePool = new SinglePool(maxSize, blockingTimeoutMilliseconds, matchOne, matchAll, selectOneAssumeMatch);
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
        return singlePool.getBlockingTimeoutMilliseconds();
    }

    public void setBlockingTimeoutMilliseconds(int blockingTimeoutMilliseconds) {
        singlePool.setBlockingTimeoutMilliseconds(blockingTimeoutMilliseconds);
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
            return new MultiPoolConnectionInterceptor(
                            tail,
                            singlePool,
                            isPartitionBySubject(),
                            isPartitionByConnectionRequestInfo());
        }
}
