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

package org.apache.geronimo.connector.work.pool;

import javax.resource.spi.work.WorkException;

import org.apache.geronimo.connector.work.WorkerContext;

import EDU.oswego.cs.dl.util.concurrent.Latch;

/**
 * WorkExecutorPool handling the submitted Work instances synchronously.
 * More accurately, its execute method blocks until the work completion.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:12 $
 */
public class SyncWorkExecutorPool
        extends AbstractWorkExecutorPool {

    /**
     * Creates a pool with the specified minimum and maximum sizes.
     *
     * @param minSize Minimum size of the work executor pool.
     * @param maxSize Maximum size of the work executor pool.
     */
    public SyncWorkExecutorPool(int minSize, int maxSize) {
        super(minSize, maxSize);
    }

    /**
     * Performs the actual work execution. This execution is synchronous.
     *
     * @param work Work to be executed.
     */
    public void doExecute(WorkerContext work)
            throws WorkException, InterruptedException {
        Latch latch = work.provideEndLatch();
        execute(work);
        latch.acquire();
    }

}
