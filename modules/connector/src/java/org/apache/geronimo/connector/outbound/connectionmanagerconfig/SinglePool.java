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
import org.apache.geronimo.connector.outbound.SinglePoolConnectionInterceptor;
import org.apache.geronimo.connector.outbound.SinglePoolMatchAllConnectionInterceptor;
import org.apache.geronimo.connector.outbound.PoolingAttributes;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class SinglePool implements PoolingSupport {
    private int maxSize;
    private int blockingTimeoutMilliseconds;
    private boolean matchOne;
    private boolean matchAll;
    private boolean selectOneAssumeMatch;

    private PoolingAttributes pool;

    public SinglePool(int maxSize, int blockingTimeoutMilliseconds, boolean matchOne, boolean matchAll, boolean selectOneAssumeMatch) {
        this.maxSize = maxSize;
        this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
        this.matchOne = matchOne;
        this.matchAll = matchAll;
        this.selectOneAssumeMatch = selectOneAssumeMatch;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getBlockingTimeoutMilliseconds() {
        return blockingTimeoutMilliseconds;
    }

    public void setBlockingTimeoutMilliseconds(int blockingTimeoutMilliseconds) {
        this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
    }

    public boolean isMatchOne() {
        return matchOne;
    }

    public void setMatchOne(boolean matchOne) {
        this.matchOne = matchOne;
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }

    public boolean isSelectOneAssumeMatch() {
        return selectOneAssumeMatch;
    }

    public void setSelectOneAssumeMatch(boolean selectOneAssumeMatch) {
        this.selectOneAssumeMatch = selectOneAssumeMatch;
    }

    public ConnectionInterceptor addPoolingInterceptors(ConnectionInterceptor tail) {
        if (isMatchAll()) {
            SinglePoolMatchAllConnectionInterceptor pool = new SinglePoolMatchAllConnectionInterceptor(
                    tail,
                    getMaxSize(),
                    getBlockingTimeoutMilliseconds());
            this.pool = pool;
            return pool;

        } else {
            SinglePoolConnectionInterceptor pool = new SinglePoolConnectionInterceptor(
                    tail,
                    getMaxSize(),
                    getBlockingTimeoutMilliseconds(),
                    isSelectOneAssumeMatch());
            this.pool = pool;
            return pool;
        }
    }

    public int getPartitionCount() {
        return 1;
    }

    public int getPartitionMaxSize() {
        return pool == null?  maxSize: pool.getPartitionMaxSize();
    }

    public int getIdleConnectionCount() {
        return pool.getIdleConnectionCount();
    }

    public int getConnectionCount() {
        return pool.getConnectionCount();
    }
}
