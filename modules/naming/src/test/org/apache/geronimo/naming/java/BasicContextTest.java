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

package org.apache.geronimo.naming.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
* Unit tests for basic ops on an {@link javax.naming.InitialContext}.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:09 $
 */
public class BasicContextTest extends AbstractContextTest {

    public void testInitialContext() throws NamingException {
        assertEquals("Hello", initialContext.lookup("java:comp/env/hello"));
        assertEquals("Hello", initialContext.lookup(new CompositeName("java:comp/env/hello")));
    }

    public void testLookup() throws NamingException {
        assertEquals("Hello", envContext.lookup("hello"));
        assertEquals("Hello", compContext.lookup("env/hello"));
        try {
            envContext.lookup("foo");
            fail();
        } catch (NamingException e) {
            // OK
        }
        assertEquals("Hello", envContext.lookup(new CompositeName("hello")));
        assertEquals("Hello", compContext.lookup(new CompositeName("env/hello")));
        assertEquals("Hello", envContext.lookup(new CompoundName("hello", syntax)));
        assertEquals("Hello", compContext.lookup(new CompoundName("env/hello", syntax)));

        assertEquals(envContext, envContext.lookup(""));
    }

    public void testSubContext() throws NamingException {
        assertEquals("long name", initialContext.lookup("java:comp/env/here/there/anywhere"));
        Context intermediate = (Context)initialContext.lookup("java:comp/env/here/there");
        assertNotNull(intermediate);
        assertEquals("long name", intermediate.lookup("anywhere"));
    }

    public void testSchemeLookup() throws NamingException {
//        envContext.lookup("dns:apache.org");
        assertEquals("Hello", envContext.lookup("java:comp/env/hello"));
        assertEquals("Hello", compContext.lookup("java:comp/env/hello"));
    }

    public void testLookupLink() throws NamingException {
        assertEquals("Hello", envContext.lookup("link"));
    }

    public void testComposeName() throws NamingException {
        assertEquals("org/research/user/jane", envContext.composeName("user/jane", "org/research"));
        assertEquals("research/user/jane", envContext.composeName("user/jane", "research"));
        assertEquals(new CompositeName("org/research/user/jane"), envContext.composeName(new CompositeName("user/jane"), new CompositeName("org/research")));
        assertEquals(new CompositeName("research/user/jane"), envContext.composeName(new CompositeName("user/jane"), new CompositeName("research")));
    }

    public void testList() throws NamingException {
        NamingEnumeration enum;
        Map expected;
        Map result;

        expected = new HashMap();
        for (Iterator i = envBinding.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            expected.put(entry.getKey(), entry.getValue().getClass().getName());
        }
        enum = envContext.list("");
        result = new HashMap();
        while (enum.hasMore()) {
            NameClassPair pair = (NameClassPair) enum.next();
            result.put(pair.getName(), pair.getClassName());
        }
        assertEquals(expected, result);

        try {
            enum.next();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
        try {
            enum.nextElement();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
    }

    public void testListBindings() throws NamingException {
        NamingEnumeration enum;
        enum = envContext.listBindings("");
        int count = 0;
        while (enum.hasMore()) {
            count ++;
            Binding pair = (Binding) enum.next();
            assertTrue(envBinding.containsKey(pair.getName()));
            if (! (envBinding.get(pair.getName()) instanceof ReadOnlyContext)) {
                assertEquals(pair.getObject(), envBinding.get(pair.getName()));
            }
        }
        assertEquals(envBinding.size(), count);

        try {
            enum.next();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
        try {
            enum.nextElement();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
    }

    public void testSpeed() throws NamingException {
        Context comp = (Context) initialContext.lookup("java:comp");

        long start = System.currentTimeMillis();
        for (int i=0; i < 1000000; i++) {
            // initialContext.lookup("java:comp/hello"); // this is sloooow due to scheme resolution
            // envContext.lookup("hello");
            comp.lookup("env/hello");
        }

        long end = System.currentTimeMillis();
        System.out.println("lookup(String) milliseconds: " + (end - start));
    }

}
