/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package javax.resource.spi.work;

import javax.resource.NotSupportedException;
import junit.framework.TestCase;

/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public class ExecutionContextTest extends TestCase {

    public void testConstructor() {
        ExecutionContext context = new ExecutionContext();
        assertNull(context.getXid());
        assertEquals(-1, context.getTransactionTimeout());
    }

    public void testNegativeTimeout() {
        ExecutionContext context = new ExecutionContext();
        try {
            context.setTransactionTimeout(-1);
        } catch (NotSupportedException nse) {
            assertEquals("Illegal timeout value", nse.getMessage());
            return;
        }
        fail("Expected NotSupportedException.");
    }

    public void testZeroTimeout() {
        ExecutionContext context = new ExecutionContext();
        try {
            context.setTransactionTimeout(0);
        } catch (NotSupportedException nse) {
            assertEquals("Illegal timeout value", nse.getMessage());
            return;
        }
        fail("Expected NotSupportedException.");
    }
}
