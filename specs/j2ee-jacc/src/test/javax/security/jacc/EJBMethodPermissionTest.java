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

package javax.security.jacc;

import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 *
 * @version $Rev$ $Date$
 */
public class EJBMethodPermissionTest extends TestCase {
    protected Method method;

    public void setUp() {
        try {
            method = TestClass.class.getDeclaredMethod("cat", new Class[] { Integer.class, Float.class, Long.class, Double.class });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (SecurityException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }

    /*
     * Testing EJBMethodPermission(java.lang.String, java.lang.String)
     */
    public void testConstructorStringString() {

        // methodSpec ::= null
        EJBMethodPermission permission = new EJBMethodPermission("foo", null);

        // methodSpec ::= methodNameSpec
        permission = new EJBMethodPermission("foo", "");
        permission = new EJBMethodPermission("foo", "cat");

        // methodSpec ::= methodNameSpec comma methodInterface
        permission = new EJBMethodPermission("foo", ",ServiceEndpoint");
        permission = new EJBMethodPermission("foo", "cat,ServiceEndpoint");

        // methodSpec ::= methodNameSpec comma methodInterfaceSpec comma methodParamsSpec
        permission = new EJBMethodPermission("foo", ",,");
        permission = new EJBMethodPermission("foo", "cat,,");
        permission = new EJBMethodPermission("foo", ",Home,");
        permission = new EJBMethodPermission("foo", "cat,Home,");
        permission = new EJBMethodPermission("foo", ",,a,b,c,d");
        permission = new EJBMethodPermission("foo", "cat,,a,b,c,d");
        permission = new EJBMethodPermission("foo", ",Home,a,b,c,d");
        permission = new EJBMethodPermission("foo", "cat,Home,a,b,c,d");


        // methodInterface ::= "Home" | "LocalHome" | "Remote" | "Local" | "ServiceEndpoint"
        permission = new EJBMethodPermission("foo", "cat,Home,a,b,c,d");
        permission = new EJBMethodPermission("foo", "cat,LocalHome,a,b,c,d");
        permission = new EJBMethodPermission("foo", "cat,Remote,a,b,c,d");
        permission = new EJBMethodPermission("foo", "cat,Local,a,b,c,d");
        permission = new EJBMethodPermission("foo", "cat,ServiceEndpoint,a,b,c,d");

        assertEquals(permission.getName(), "foo");
        assertEquals(permission.getActions(), "cat,ServiceEndpoint,a,b,c,d");


        // bad methodInterface
        try {
            permission = new EJBMethodPermission("foo", "cat,Interface,a,b,c,d");
            fail("Bad method interface");
        } catch(IllegalArgumentException iae) {
        }

        // no production produces "emptyString,emptyString"
        try {
            permission = new EJBMethodPermission("foo", ",");
            fail("Empty method interface");
        } catch(IllegalArgumentException iae) {
        }

        // no production produces "methodName ,emptyString"
        try {
            permission = new EJBMethodPermission("foo", "cat,");
            fail("Empty method interface");
        } catch(IllegalArgumentException iae) {
        }

        // no production produces an empty method parameter
        try {
            permission = new EJBMethodPermission("foo", ",,,");
            fail("Empty method parameter");
        } catch(IllegalArgumentException iae) {
        }

        // no production produces an empty method parameter
        try {
            permission = new EJBMethodPermission("foo", ",,,,,,");
            fail("Empty method parameter");
        } catch(IllegalArgumentException iae) {
        }
    }

    public void testImpliesStringString() {
        EJBMethodPermission permissionFooEEE = new EJBMethodPermission("foo", "");
        EJBMethodPermission permissionFooMIP = new EJBMethodPermission("foo", "cat,LocalHome,a,b,c,d");
        EJBMethodPermission permissionBarEEE = new EJBMethodPermission("bar", "");
        EJBMethodPermission permissionFooEIP = new EJBMethodPermission("foo", ",LocalHome,a,b,c,d");
        EJBMethodPermission permissionFooEIE = new EJBMethodPermission("foo", ",LocalHome,");
        EJBMethodPermission permissionFooEI  = new EJBMethodPermission("foo", ",LocalHome");

        assertTrue(permissionFooEEE.implies(permissionFooEEE));
        assertTrue(permissionFooEEE.implies(permissionFooMIP));
        assertTrue(permissionFooEEE.implies(permissionFooEIP));
        assertTrue(permissionFooEEE.implies(permissionFooEIE));
        assertTrue(permissionFooEEE.implies(permissionFooEI));
        assertFalse(permissionFooMIP.implies(permissionFooEEE));

        assertTrue(permissionFooEEE.equals(permissionFooEEE));
        assertFalse(permissionFooEEE.equals(permissionFooMIP));
        assertFalse(permissionFooMIP.equals(permissionFooEEE));

        assertFalse(permissionFooEEE.implies(permissionBarEEE));
        assertFalse(permissionBarEEE.implies(permissionFooEEE));

        assertFalse(permissionFooEEE.equals(permissionBarEEE));
        assertFalse(permissionBarEEE.equals(permissionFooEEE));

        assertTrue(permissionFooEIP.implies(permissionFooMIP));
        assertFalse(permissionFooEIE.implies(permissionFooMIP));
        assertTrue(permissionFooEI.implies(permissionFooMIP));
        assertTrue(permissionFooEI.implies(permissionFooEIP));
        assertTrue(permissionFooEI.implies(permissionFooEIE));

        assertFalse(permissionFooEEE.hashCode() == permissionBarEEE.hashCode());
    }

    /*
     * Testing EJBMethodPermission(String, String, String, String[])
     */
    public void testConstructorStringStringStringStringArray() {

        // methodSpec ::= null
        EJBMethodPermission permission = new EJBMethodPermission("foo", null, null, null);

        // methodSpec ::= methodNameSpec
        permission = new EJBMethodPermission("foo", "", "", null);
        permission = new EJBMethodPermission("foo", "cat", "", null);

        // methodSpec ::= methodNameSpec comma methodInterface
        permission = new EJBMethodPermission("foo", "", "ServiceEndpoint", null);
        permission = new EJBMethodPermission("foo", "cat", "ServiceEndpoint", null);

        // methodSpec ::= methodNameSpec comma methodInterfaceSpec comma methodParamsSpec
        permission = new EJBMethodPermission("foo", "", "", new String[]{});
        permission = new EJBMethodPermission("foo", "cat", "", new String[]{});
        permission = new EJBMethodPermission("foo", "", "Home", new String[]{});
        permission = new EJBMethodPermission("foo", "cat", "Home", new String[] {});
        permission = new EJBMethodPermission("foo", "", "", new String[] { "a", "b", "c", "d" });
        permission = new EJBMethodPermission("foo", "cat", "", new String[] { "a", "b", "c", "d" });
        permission = new EJBMethodPermission("foo", "", "Home", new String[] { "a", "b", "c", "d" });
        permission = new EJBMethodPermission("foo", "cat", "Home", new String[] { "a", "b", "c", "d" });


        // methodInterface ::= "Home" | "LocalHome" | "Remote" | "Local" | "ServiceEndpoint"
        permission = new EJBMethodPermission("foo", "cat", "Home", new String[] { "a", "b", "c", "d" });
        permission = new EJBMethodPermission("foo", "cat", "LocalHome", new String[] { "a", "b", "c", "d" });
        permission = new EJBMethodPermission("foo", "cat", "Remote", new String[] { "a", "b", "c", "d" });
        permission = new EJBMethodPermission("foo", "cat", "Local", new String[] { "a", "b", "c", "d" });
        permission = new EJBMethodPermission("foo", "cat", "ServiceEndpoint", new String[] { "a", "b", "c", "d" });

        assertEquals(permission.getName(), "foo");
        assertEquals(permission.getActions(), "cat,ServiceEndpoint,a,b,c,d");


        // bad methodInterface
        try {
            permission = new EJBMethodPermission("foo", "cat", "Interface", new String[] { "a", "b", "c", "d" });
            fail("Bad method interface");
        } catch(IllegalArgumentException iae) {
        }
    }

    public void testImpliesStringStringStringStringArray() {
        EJBMethodPermission permissionFooEEE = new EJBMethodPermission("foo", "", "", null);
        EJBMethodPermission permissionFooMIP = new EJBMethodPermission("foo", "cat", "LocalHome", new String[] { "a", "b", "c", "d" });
        EJBMethodPermission permissionBarEEE = new EJBMethodPermission("bar", "", "", new String[] {});
        EJBMethodPermission permissionFooEIP = new EJBMethodPermission("foo", "", "LocalHome", new String[] { "a", "b", "c", "d" });
        EJBMethodPermission permissionFooEIE = new EJBMethodPermission("foo", "", "LocalHome", new String[] {});
        EJBMethodPermission permissionFooEI  = new EJBMethodPermission("foo", "", "LocalHome", null);

        assertTrue(permissionFooEEE.implies(permissionFooEEE));
        assertTrue(permissionFooEEE.implies(permissionFooMIP));
        assertTrue(permissionFooEEE.implies(permissionFooEIP));
        assertTrue(permissionFooEEE.implies(permissionFooEIE));
        assertTrue(permissionFooEEE.implies(permissionFooEI));
        assertFalse(permissionFooMIP.implies(permissionFooEEE));

        assertTrue(permissionFooEEE.equals(permissionFooEEE));
        assertFalse(permissionFooEEE.equals(permissionFooMIP));
        assertFalse(permissionFooMIP.equals(permissionFooEEE));

        assertFalse(permissionFooEEE.implies(permissionBarEEE));
        assertFalse(permissionBarEEE.implies(permissionFooEEE));

        assertFalse(permissionFooEEE.equals(permissionBarEEE));
        assertFalse(permissionBarEEE.equals(permissionFooEEE));

        assertTrue(permissionFooEIP.implies(permissionFooMIP));
        assertFalse(permissionFooEIE.implies(permissionFooMIP));
        assertTrue(permissionFooEI.implies(permissionFooMIP));
        assertTrue(permissionFooEI.implies(permissionFooEIP));
        assertTrue(permissionFooEI.implies(permissionFooEIE));

        assertFalse(permissionFooEEE.hashCode() == permissionBarEEE.hashCode());
    }

    /*
     * Testing EJBMethodPermission(String, String, Method)
     */
    public void testConstructorStringStringMethod() {

        EJBMethodPermission permission = new EJBMethodPermission("foo", "ServiceEndpoint", method);

        permission = new EJBMethodPermission("foo", null, method);
        permission = new EJBMethodPermission("foo", "", method);

        permission = new EJBMethodPermission("foo", "Home", method);
        permission = new EJBMethodPermission("foo", "LocalHome", method);
        permission = new EJBMethodPermission("foo", "Remote", method);
        permission = new EJBMethodPermission("foo", "Local", method);
        permission = new EJBMethodPermission("foo", "ServiceEndpoint", method);

        assertEquals(permission.getName(), "foo");
        assertEquals(permission.getActions(), "cat,ServiceEndpoint,java.lang.Integer,java.lang.Float,java.lang.Long,java.lang.Double");

        // Parameter method must not be null
        try {
            permission = new EJBMethodPermission("foo", "ServiceEndpoint", null);
            fail("Parameter method must not be null");
        } catch(IllegalArgumentException iae) {
        }
    }

    public void testImpliesStringStringMethod() {
    }

    class TestClass {
        public Object cat(Integer a, Float b, Long c, Double d) {
            return null;
        }
    }
}

