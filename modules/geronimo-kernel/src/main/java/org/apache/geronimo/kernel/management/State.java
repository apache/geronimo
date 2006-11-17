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

import java.io.Serializable;


/**
 * This class contains a type safe enumeration of the states from the J2EE Management specification.
 *
 * @version $Rev$ $Date$
 */
public final class State implements Serializable {
    public static final int STARTING_INDEX = 0;
    public static final int RUNNING_INDEX = 1;
    public static final int STOPPING_INDEX = 2;
    public static final int STOPPED_INDEX = 3;
    public static final int FAILED_INDEX = 4;

    public static final State STARTING = new State("starting", STARTING_INDEX, NotificationType.STATE_STARTING);
    public static final State RUNNING = new State("running", RUNNING_INDEX, NotificationType.STATE_RUNNING);
    public static final State STOPPING = new State("stopping", STOPPING_INDEX, NotificationType.STATE_STOPPING);
    public static final State STOPPED = new State("stopped", STOPPED_INDEX, NotificationType.STATE_STOPPED);
    public static final State FAILED = new State("failed", FAILED_INDEX, NotificationType.STATE_FAILED);

    private static final State[] fromInt = {STARTING, RUNNING, STOPPING, STOPPED, FAILED};

    /**
     * Get a State from an int index
     *
     * @param index int index of the state
     * @return The State instance or null if no such State.
     */
    public static State fromInt(int index) {
        if (index < 0 || index >= fromInt.length) {
            return null;
        }
        return fromInt[index];
    }

    /**
     * Get a State from an Integer index
     *
     * @param index Integer index of the state
     * @return The State instance or null if no such State.
     */
    public static State fromInteger(Integer index) {
        return fromInt(index.intValue());
    }

    public static String toString(int state) {
        if (state < 0 || state >= fromInt.length) {
            throw new IllegalArgumentException("State must be between 0 and " + fromInt.length);
        }
        return fromInt[state].name;
    }

    /**
     * The user readable name of this state from the J2EE Management specification
     */
    private final String name;

    /**
     * The state index from the J2EE Management specification
     */
    private final int index;

    /**
     * Type value to be broadcasted on entering this state.
     */
    private final String eventTypeValue;

    private State(String name, int index, String anEventTypeValue) {
        this.name = name;
        this.index = index;
        eventTypeValue = anEventTypeValue;
    }

    /**
     * Gets the integer value of this state as specified in the J2EE Management specification
     */
    public int toInt() {
        return index;
    }

    /**
     * Gets the event type that should be send after changeing to this state.
     *
     * @return the event type that should be sent after a transistion to this state
     */
    public String getEventTypeValue() {
        return eventTypeValue;
    }

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        return this == State.RUNNING;
    }

    public boolean isStopped() {
        return this == State.STOPPED;
    }

    public boolean isFailed() {
        return this == State.FAILED;
    }

    public String toString() {
        return name;
    }

    private Object readResolve() {
        return fromInt[index];
    }
}
