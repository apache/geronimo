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
package org.apache.geronimo.common;

/**
 *
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/14 07:14:34 $
 */
public final class State {
	
    public static final int STARTING_INDEX=0;
    public static final int RUNNING_INDEX=1;
    public static final int STOPPING_INDEX=2;
    public static final int STOPPED_INDEX=3;
    public static final int FAILED_INDEX=4;
    
    public static final State STARTING =
        new State("starting",STARTING_INDEX,"j2ee.state.starting");
    public static final State RUNNING =
        new State("running",RUNNING_INDEX,"j2ee.state.running");
    public static final State STOPPING =
        new State("stopping",STOPPING_INDEX,"j2ee.state.stopping");
    public static final State STOPPED =
        new State("stopped",STOPPED_INDEX,"j2ee.state.stopped");
    public static final State FAILED =
        new State("failed",FAILED_INDEX,"j2ee.state.failed");

    private static final State[] fromInt=
        {STARTING,RUNNING,STOPPING,STOPPED,FAILED};
        
    /**
     * Get a State from an int index
     * @param index int index of the state
     * @return The State instance or null if no such State.
     */
    public static State fromInt(int index)
    {
        if (index<0 || index>=fromInt.length)
            return null;
        return fromInt[index];
    }
        
    private final String name;
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

    public int toInt() {	
        return index;
    }
    
    public String getEventTypeValue() {
        return eventTypeValue;
    }
	
    public String toString() {
        return name;
    }
}
