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
}
