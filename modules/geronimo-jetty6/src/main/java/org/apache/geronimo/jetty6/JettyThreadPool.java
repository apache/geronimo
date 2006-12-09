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
package org.apache.geronimo.jetty6;

import org.mortbay.thread.ThreadPool;

/**
 * JettyThreadPool
 * 
 * Class to implement Jetty org.mortbay.jetty.thread.ThreadPool interface
 * and delegate to a Geronimo thread pool impl.
 *
 */
public class JettyThreadPool implements ThreadPool {

    private org.apache.geronimo.pool.ThreadPool geronimoThreadPool;
    
    public JettyThreadPool(org.apache.geronimo.pool.ThreadPool geronimoThreadPool) {
        this.geronimoThreadPool = geronimoThreadPool;
    }

    
    public boolean dispatch(Runnable work) {
        this.geronimoThreadPool.execute(work);
        return true;
        //what has changed?
//        try {
//            this.geronimoThreadPool.execute(work);
//            return true;
//        }
//        catch (Exception e) {
//            log.warn(e);
//            return false;
//        }
    }

    /**
     * Jetty method. Caller wants to wait until the
     * thread pool has stopped.
     * 
     * @see org.mortbay.thread.ThreadPool#join()
     */
    public void join() throws InterruptedException {
        throw new UnsupportedOperationException("join not supported");
    }

    public int getThreads() {
        return this.geronimoThreadPool.getPoolSize();
    }

    public int getIdleThreads() {
        //TODO: not supported in geronimo thread pool
        return 0;
    }

    public boolean isLowOnThreads() {
        // TODO: not supported in geronimo thread pool
        return false;
    }
    
}
