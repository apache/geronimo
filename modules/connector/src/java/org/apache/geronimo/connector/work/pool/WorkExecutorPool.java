/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

/**
 * Defines the operations that a pool in charge of the execution of Work
 * instances must expose.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:33 $
 */
public interface WorkExecutorPool {

    /**
     * Executes the specified work. The execution policy (synchronous vs.
     * asynchronous) is implementation specific.
     *
     * @param aWork Work to be executed.
     *
     * @throws WorkException Indicates that the Work instance can not be
     * executed or that its execution has thrown an exception.
     */
    public void executeWork(WorkerContext aWork) throws WorkException;

    /**
     * Gets the current number of active threads in the pool.
     *
     * @return Number of active threads in the pool.
     */
    public int getPoolSize();

    /**
     * Gets the minimum number of threads to simultaneously execute.
     *
     * @return Minimum size.
     */
    public int getMinimumPoolSize();

    /**
     * Sets the minimum number of threads to simultaneously execute.
     *
     * @param aSize Minimum size.
     */
    public void setMinimumPoolSize(int aSize);

    /**
     * Gets the maximum number of threads to simultaneously execute.
     *
     * @return Maximim size.
     */
    public int getMaximumPoolSize();

    /**
     * Sets the maximum number of threads to simultaneously execute.
     *
     * @param Maximum size.
     */
    public void setMaximumPoolSize(int aSize);

}
