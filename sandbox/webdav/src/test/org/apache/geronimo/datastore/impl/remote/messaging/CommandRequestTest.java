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

package org.apache.geronimo.datastore.impl.remote.messaging;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/11 15:36:14 $
 */
public class CommandRequestTest extends TestCase {
    
    private DummyConnector connector;
    private static final String name = "test"; 
    
    protected void setUp() throws Exception {
        connector = new DummyConnector(name, new NodeInfo[0]);
    }
    
    public void testExecute0() throws Exception {
        CommandRequest request = new CommandRequest("getName", null);
        request.setTarget(connector);
        CommandResult result = request.execute();
        assertTrue(result.isSuccess());
        assertEquals(name, result.getResult());
    }
    
    public void testExecute1() throws Exception {
        CommandRequest request = new CommandRequest("doesNotExist", null);
        request.setTarget(connector);
        CommandResult result = request.execute();
        assertFalse(result.isSuccess());
        assertTrue(result.getException() instanceof NoSuchMethodException);
    }
    
    public void testExecute2() throws Exception {
        CommandRequest request = new CommandRequest("raiseISException", null);
        request.setTarget(connector);
        CommandResult result = request.execute();
        assertFalse(result.isSuccess());
        assertTrue(result.getException() instanceof IllegalStateException);
    }

}
