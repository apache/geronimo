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

package org.apache.geronimo.naming.geronimo;

import javax.naming.NamingException;
import javax.naming.Context;

import junit.framework.TestCase;
import org.apache.geronimo.naming.geronimo.GeronimoContext;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class GeronimoContextTest extends TestCase {
    private GeronimoContext context;

    protected void setUp() throws Exception {
        context = new GeronimoContext();
        context.internalBind("one", "one");
        context.internalBind("this/is/a/compound/name", "two");
        context.internalBind("this/is/another/compound/name", "three");
    }

    public void testLookup() throws Exception {
        assertEquals(context.lookup("one"), "one");
        assertEquals(context.lookup("this/is/a/compound/name"), "two");
        assertEquals(context.lookup("this/is/another/compound/name"), "three");
    }

    public void testLookupSubContext() throws Exception {
        Context context = (Context) this.context.lookup("this/is");
        assertEquals(context.lookup("a/compound/name"), "two");
        assertEquals(context.lookup("another/compound/name"), "three");
    }

    public void testUnbind() throws Exception {
        assertEquals(1, context.internalUnbind("one").size());
        try {
            context.lookup("one");
            fail();
        } catch (NamingException e) {
        }
        assertEquals(3, context.internalUnbind("this/is/a/compound/name").size());
        try {
            context.lookup("this/is/a/compound/name");
            fail();
        } catch (NamingException e) {
        }
        context.lookup("this/is");
        assertEquals(5, context.internalUnbind("this/is/another/compound/name").size());
        try {
            context.lookup("this/is");
            fail();
        } catch (NamingException e) {
        }
    }
}
