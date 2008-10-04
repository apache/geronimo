/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.jmsmanager;

/**
 * 
 *
 * @version $Rev$ $Date$
 */
public class DestinationStatistics {
    private long enqueueCount;
    private long dequeueCount;
    private long consumerCount;
    private long queueSize;
    
    public long getQueueSize() {
        return queueSize;
    }
    public void setQueueSize(long queueSize) {
        this.queueSize = queueSize;
    }
    public long getConsumerCount() {
        return consumerCount;
    }
    public void setConsumerCount(long consumerCount) {
        this.consumerCount = consumerCount;
    }
    public long getDequeueCount() {
        return dequeueCount;
    }
    public void setDequeueCount(long dequeueCount) {
        this.dequeueCount = dequeueCount;
    }
    public long getEnqueueCount() {
        return enqueueCount;
    }
    public void setEnqueueCount(long enqueueCount) {
        this.enqueueCount = enqueueCount;
    }
    
    
}
