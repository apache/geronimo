/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.gbean;

import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.rmi.MarshalledObject;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class GBeanNameTest extends TestCase {
    private Properties props;

    public void testPropertyConstruction() {
        String domain = "testDomain";
        props.put("prop1", "value1");
        props.put("prop2", "value2");
        GBeanName name = new GBeanName(domain, props);
        assertEquals("testDomain:prop1=value1,prop2=value2", name.toString());
    }

    public void testNameConstruction() {
        GBeanName name = new GBeanName("testDomain:prop1=value1,prop2=value2");
        assertEquals("testDomain:prop1=value1,prop2=value2", name.toString());

        name = new GBeanName("testDomain:prop2=value2,prop1=value1");
        assertEquals("testDomain:prop2=value2,prop1=value1", name.toString());
    }

    public void testMatches() {
        GBeanName name = new GBeanName("testDomain:prop1=value1,prop2=value2");
        assertTrue(name.matches(null, null));
        assertTrue(name.matches(null, props));
        assertTrue(name.matches("testDomain", null));
        assertTrue(name.matches("testDomain", props));

        assertFalse(name.matches("test", null));
        assertFalse(name.matches("test", props));

        props.setProperty("prop1", "value2");
        assertFalse(name.matches("testDomain", props));
        props.setProperty("prop1", "value1");
        assertTrue(name.matches("testDomain", props));
        props.setProperty("prop2", "value2");
        assertTrue(name.matches("testDomain", props));
        props.setProperty("prop3", "value3");
        assertFalse(name.matches("testDomain", props));
    }

    public void testInvalidNames() {
        try {
            new GBeanName((String) null);
            fail();
        } catch (NullPointerException e) {
        }
        try {
            new GBeanName("");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("foo=bar");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:  ");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:foo");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:x=x,foo");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:x=x,").toString();
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:x=x, ").toString();
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:,x=x");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:x=x,,y=y");
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            new GBeanName("x:x=x,x=x");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testEquals() {
        GBeanName name = new GBeanName("testDomain:prop1=value1,prop2=value2");
        assertEquals(name, name);
        assertEquals(new GBeanName("testDomain:prop2=value2,prop1=value1"), name);
        assertFalse(name.equals(new GBeanName("foo:prop1=value1,prop2=value2")));
        assertFalse(name.equals(new GBeanName("testDomain:prop1=value1")));
        assertFalse(name.equals(new GBeanName("testDomain:prop2=value2")));
        assertFalse(name.equals(new GBeanName("testDomain:prop2=value2")));
        assertFalse(name.equals(new GBeanName("testDomain:prop1=value1,prop2=value2,prop3=value3")));

        Set set = new HashSet();
        set.add(name);
        set.add(name);
        assertEquals(1, set.size());
    }

    public void testSerialization() throws Exception {
        GBeanName name = new GBeanName("testDomain:prop1=value1,prop2=value2");
        MarshalledObject o = new MarshalledObject(name);
        GBeanName name2 = (GBeanName) o.get();
        assertEquals(name, name2);
    }

    protected void setUp() throws Exception {
        props = new Properties();
    }
}
