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

package org.apache.geronimo.datastore.impl.remote.messaging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

/**
 * Processor pool.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
class Processors {

    private static final Log log = LogFactory.getLog(Processors.class);
    
    /**
     * Actual Thread pool.
     */
    private PooledExecutor executor;
    
    /**
     * Name.
     */
    private final String name;
    
    private int idSeq;
    
    /**
     * Create a Processor pool.
     * 
     * @param aName Processor name.
     * @param aMinSize Minimum size of the pool.
     * @param aMaxSize Maximum size of the pool.
     */
    public Processors(String aName, int aMinSize, int aMaxSize) {
        if ( null == aName ) {
            throw new IllegalArgumentException("Name is required.");
        }
        executor = new PooledExecutor();
        idSeq = 0;
        name = aName;
        executor.setThreadFactory(
            new ThreadFactory() {
                public Thread newThread(Runnable arg0) {
                    Thread thread = new Thread(arg0, name + "-" + idSeq);
                    thread.setDaemon(true);
                    return thread;
                }
            }
        );
        executor.setMinimumPoolSize(aMinSize);
        executor.setMaximumPoolSize(aMaxSize);
        executor.abortWhenBlocked();
    }
    
    /**
     * Execute a Processor in a separate Thread.
     *  
     * @param aProcessor Processor to be executed.
     */
    public void execute(Processor aProcessor) {
        try {
            executor.execute(aProcessor);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }
    
}
