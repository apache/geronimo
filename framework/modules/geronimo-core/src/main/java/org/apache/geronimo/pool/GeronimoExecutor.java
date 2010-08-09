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
package org.apache.geronimo.pool;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A Geronimo-specific extension that contributes a little extra manageability
 * to the standard Executor interface.
 *
 * @version $Rev$ $Date$
 */
public interface GeronimoExecutor extends Executor, org.apache.geronimo.system.threads.ThreadPool {
    /**
     * Gets a human-readable name identifying this object.
     */
    String getName();

    /**
     * Gets the unique name of this object.  The object name must comply with
     * the ObjectName specification in the JMX specification.
     *
     * @return the unique name of this object within the server
     */
    String getObjectName();
    
    /**
     * Executes the given command at some time in the future, if it can not be started within the given timeout,
     * RejectedExecutionException will be thrown. The command may execute in a new thread, in a pooled thread, or in the
     * calling thread, at the discretion of the <tt>Executor</tt> implementation.
     * 
     * @param command the runnable task
     * @param timeout duration of timeout
     * @param unit TimeUnit of timeout
     * @throws RejectedExecutionException if this task cannot be accepted for execution.
     * @throws NullPointerException if command is null
     */
    public void execute(Runnable runnable, long timeout, TimeUnit unit);
}
