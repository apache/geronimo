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

package org.apache.geronimo.kernel.management;


/**
 * Static constants class which contains all of the J2EE notification types from the
 * J2EE management specification.
 *
 * @version $Rev$ $Date$
 */
public final class NotificationType {
    private NotificationType() {
    }

    /**
     * A new managed object was created.
     */
    public static final String OBJECT_CREATED = "j2ee.object.created";

    /**
     * A managed object was deleted
     */
    public static final String OBJECT_DELETED = "j2ee.object.deleted";

    /**
     * A state manageable object entered the starting state
     */
    public static final String STATE_STARTING = "j2ee.state.starting";

    /**
     * A state manageable object entered the running state
     */
    public static final String STATE_RUNNING = "j2ee.state.running";

    /**
     * A state manageable object entered the stopping state
     */
    public static final String STATE_STOPPING = "j2ee.state.stopping";

    /**
     * A state manageable object entered the stopped state.
     */
    public static final String STATE_STOPPED = "j2ee.state.stopped";

    /**
     * A state manageable object entered the failed state
     */
    public static final String STATE_FAILED = "j2ee.state.failed";

    /**
     * An attribute has change value
     */
    public static final String ATTRIBUTE_CHANGED = "j2ee.attribute.changed";

    /**
     * An array containg all of the know J2EE notification types
     */
    public static final String[] TYPES = new String[]{
        OBJECT_CREATED, OBJECT_DELETED,
        STATE_STARTING, STATE_RUNNING, STATE_STOPPING, STATE_STOPPED, STATE_FAILED,
        ATTRIBUTE_CHANGED
    };
}
