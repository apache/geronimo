/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.shared;

import junit.framework.TestCase;

public class CommandTypeTest extends TestCase {
    public void testValues() {
        assertEquals(0, CommandType.DISTRIBUTE.getValue());
        assertEquals(1, CommandType.START.getValue());
        assertEquals(2, CommandType.STOP.getValue());
        assertEquals(3, CommandType.UNDEPLOY.getValue());
        assertEquals(4, CommandType.REDEPLOY.getValue());
    }

    public void testToString() {
        assertEquals("distribute", CommandType.DISTRIBUTE.toString());
        assertEquals("start", CommandType.START.toString());
        assertEquals("stop", CommandType.STOP.toString());
        assertEquals("undeploy", CommandType.UNDEPLOY.toString());
        assertEquals("redeploy", CommandType.REDEPLOY.toString());
        // only possible due to package local access
        assertEquals("10", new ActionType(10).toString());
        assertEquals("-1", new ActionType(-1).toString());
    }

    public void testValueToSmall() {
        try {
            CommandType.getCommandType(-1);
            fail("Expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }

    public void testValueToLarge() {
        try {
            CommandType.getCommandType(10);
            fail("Expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        }
    }
}
