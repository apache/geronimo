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

package org.apache.management.j2ee;

import junit.framework.TestCase;

/**
 * Unit test for org.apache.geronimo.common.State class
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/18 13:29:30 $
 */

public class StateTest extends TestCase {

    public void testValues() {

        assertEquals(0, State.STARTING_INDEX);
        assertEquals(1, State.RUNNING_INDEX);
        assertEquals(2, State.STOPPING_INDEX);
        assertEquals(3, State.STOPPED_INDEX);
        assertEquals(4, State.FAILED_INDEX);

        assertEquals(0, State.STARTING.toInt());
        assertEquals(1, State.RUNNING.toInt());
        assertEquals(2, State.STOPPING.toInt());
        assertEquals(3, State.STOPPED.toInt());
        assertEquals(4, State.FAILED.toInt());
    }

    public void testLessThanMin() {
        assertNull(State.fromInt(-1));
    }

    public void testGreaterThanMax() {
        assertNull(State.fromInt(5));
    }

    public void testName() {
        assertEquals("starting", State.STARTING.toString());
        assertEquals("running", State.RUNNING.toString());
        assertEquals("stopping", State.STOPPING.toString());
        assertEquals("stopped", State.STOPPED.toString());
        assertEquals("failed", State.FAILED.toString());
    }

    public void testEventTypeValue() {
        assertEquals("j2ee.state.starting", State.STARTING.getEventTypeValue());
        assertEquals("j2ee.state.running", State.RUNNING.getEventTypeValue());
        assertEquals("j2ee.state.stopping", State.STOPPING.getEventTypeValue());
        assertEquals("j2ee.state.stopped", State.STOPPED.getEventTypeValue());
        assertEquals("j2ee.state.failed", State.FAILED.getEventTypeValue());
    }
}
