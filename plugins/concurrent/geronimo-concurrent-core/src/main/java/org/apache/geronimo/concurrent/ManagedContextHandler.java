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
package org.apache.geronimo.concurrent;

import java.util.Map;

/**
 * The ManagedContextHandler is used for capturing context information in one thread and 
 * restoring it on a different thread. 
 * ManagedContextHandlers are stateless. All state information must be stored in the
 * passed context object.  
 * 
 * {@link #saveContext(Map)} will usually be called on a different thread then 
 * {@link #setContext(Map)} or {@link #unsetContext(Map)} but 
 * {@link #setContext(Map)} and {@link #unsetContext(Map)} will always be called
 * on the same thread.
 */
public interface ManagedContextHandler {

    /**
     * Captures and saves the context information of the current environment.
     * That context information will later be set on the thread in the
     * {@link #setContext(Map)} function.
     * 
     * @param context
     */
    void saveContext(Map<String, Object> context);
    
    /**
     * Sets the context information captured by {@link #saveContext(Map)} on the
     * current thread.
     * This function must also save current thread context information which will
     * be restored by {@link #unsetContext(Map)}.
     * 
     * @param threadContext 
     */
    void setContext(Map<String, Object> threadContext);
    
    /**
     * Restores thread context information that was in place when 
     * {@link #setContext(Map)} was called.
     * 
     * @param threadContext
     */
    void unsetContext(Map<String, Object> threadContext);
    
}
