/**
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.system.threads;

/**
 * Management interface for thread pools
 *
 * @version $Rev$ $Date$
 */
public interface ThreadPool {
    /**
     * Gets the maximum number of threads allowed for this thread pool
     */
    int getPoolSize();

    /**
     * Executes work on behalf of a named client.  This helps the thread pool
     * track who's using its threads.
     * @param consumerName  A name identifying the caller, to be used in
     *                      the management statistics for this pool, etc.
     * @param runnable      The work to be done by a thread in the pool
     */
    void execute(String consumerName, Runnable runnable) throws InterruptedException;
}
