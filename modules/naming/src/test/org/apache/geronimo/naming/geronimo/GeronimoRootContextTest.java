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

package org.apache.geronimo.naming.geronimo;

import javax.naming.InitialContext;

import junit.framework.TestCase;
import org.apache.geronimo.naming.geronimo.GeronimoContextManager;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:57 $
 *
 * */
public class GeronimoRootContextTest extends TestCase {

    protected void setUp() throws Exception {
        GeronimoContextManager.bind("one", "one");
        GeronimoContextManager.bind("this/is/a/compound/name", "two");
        GeronimoContextManager.bind("this/is/another/compound/name", "three");
    }

    public void testLookup() throws Exception {
        InitialContext context = new InitialContext();
        assertEquals(context.lookup("geronimo:one"), "one");
        assertEquals(context.lookup("geronimo:this/is/a/compound/name"), "two");
        assertEquals(context.lookup("geronimo:this/is/another/compound/name"), "three");

    }
}
