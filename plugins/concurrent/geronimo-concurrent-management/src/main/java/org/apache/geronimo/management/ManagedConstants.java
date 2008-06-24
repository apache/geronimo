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
package org.apache.geronimo.management;

public interface ManagedConstants {
    
    public static final String MANAGED_THREAD = "ManagedThread";
    public static final String MANAGED_THREAD_FACTORY = "ManagedThreadFactory";
    public static final String MANAGED_EXECUTOR_SERVICE = "ManagedExecutorService";
        
    public static final String NEW_THREAD_EVENT = "threadfactory.newthread";
    
    public static final String TASK_HUNG_STATE = "task.state.hung";
    public static final String TASK_CANCELLED_STATE = "task.state.cancelled";
    public static final String TASK_RELEASED_STATE = "task.state.released";
        
}
