/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package org.apache.geronimo.management;

/**
 * This class contains a type safe enumeration of the states from the J2EE Management specification.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/23 06:26:04 $
 */
public final class State {
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
     * @return
     */
    public int toInt() {
        return index;
    }

    /**
     * Gets the event type that should be send after changeing to this state.
     * @return the event type that should be sent after a transistion to this state
     */
    public String getEventTypeValue() {
        return eventTypeValue;
    }

    public String toString() {
        return name;
    }
}
