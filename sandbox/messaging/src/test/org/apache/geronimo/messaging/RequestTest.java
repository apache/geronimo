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

package org.apache.geronimo.messaging;

import java.io.IOException;

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public class RequestTest extends TestCase {
    
    private DummyTarget target;
    private static final String name = "test"; 
    
    protected void setUp() throws Exception {
        target = new DummyTarget(name);
    }
    
    public void testExecute0() throws Exception {
        Request request = new Request("getID", null);
        request.setTarget(target);
        Result result = request.execute();
        assertTrue(result.isSuccess());
        assertEquals(name, result.getResult());
    }
    
    public void testExecute1() throws Exception {
        Request request = new Request("doesNotExist", null);
        request.setTarget(target);
        Result result = request.execute();
        assertFalse(result.isSuccess());
        assertTrue(result.getException() instanceof NoSuchMethodException);
    }
    
    public void testExecute2() throws Exception {
        Request request = new Request("raiseISException", null);
        request.setTarget(target);
        Result result = request.execute();
        assertFalse(result.isSuccess());
        assertTrue(result.getException() instanceof IllegalStateException);
    }

    private static class DummyTarget {
        private final Object id;
        private DummyTarget(Object anID) {
            id = anID;
        }
        public Object getID() {
            return id;
        }
        public void raiseISException() {
            throw new IllegalStateException();
        }
        public void raiseCheckedException() throws IOException {
            throw new IOException();
        }
    }
    
}
