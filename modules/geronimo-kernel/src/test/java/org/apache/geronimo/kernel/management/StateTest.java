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

import junit.framework.TestCase;

/**
 * Unit test for org.apache.geronimo.common.State class
 *
 * @version $Rev$ $Date$
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
