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

import java.security.Permission;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import junit.framework.TestCase;

/**
 *
 * @version $Rev$ $Date$
 */
public class WebResourcePermissionTest extends TestCase {

    public void testSerialization() throws Exception {
        WebResourcePermission permission = new WebResourcePermission("/bar/*:/bar/stool", "GET,POST");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(permission);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        assertEquals(permission, o);
    }

    /*
     * Testing WebResourcePermission(java.lang.String, java.lang.String)
     */
    public void testConstructorStringString() {

        WebResourcePermission permission = new WebResourcePermission("/foo", "GET,POST");

        assertTrue(permission.equals(permission));
        assertEquals(permission.getName(), "/foo");
        assertEquals(permission.getActions(), "GET,POST");
        
        permission = new WebResourcePermission("/foo", "GET,POST,POST,GET");
        assertEquals(permission.getActions(), "GET,POST");
        
        permission = new WebResourcePermission("/", "GET,POST");
        permission = new WebResourcePermission("/:/foo", "GET,POST");
        permission = new WebResourcePermission("/:*.asp", "GET,POST");
        permission = new WebResourcePermission("/:/foo:*.asp", "GET,POST");
        permission = new WebResourcePermission("/bar/*", "GET,POST");
        permission = new WebResourcePermission("", "GET,POST");
        permission = new WebResourcePermission("/*", "GET,POST");
        permission = new WebResourcePermission("/*:/bar/stool", "GET,POST");
        permission = new WebResourcePermission("/bar/*:/bar/stool", "GET,POST");

        // bad HTTP method
        try {
            permission = new WebResourcePermission("/foo", "GET,POST,BAR");
            fail("Bad HTTP method");
        } catch(IllegalArgumentException iae) {
        }

        // bad HTTP method for a WebResourcePermission
        try {
            permission = new WebResourcePermission("/foo", "GET,POST:INTEGRAL");
            fail("Bad HTTP method for a WebResourcePermission");
        } catch(IllegalArgumentException iae) {
        }

        // null URLPatternSpec for a WebResourcePermission
        try {
            permission = new WebResourcePermission(null, "GET,POST");
            fail("null URLPatternSpec for a WebResourcePermission");
        } catch(IllegalArgumentException iae) {
        }

        // missing qualifiers
        try {
            permission = new WebResourcePermission("/foo:", "GET,POST");
            fail("/foo:");
        } catch(IllegalArgumentException iae) {
        }

        // qualifer provided when first pattern isn't path-prefix
        try {
            permission = new WebResourcePermission("/foo:/foo/bar", "GET,POST");
            fail("/foo:/foo/bar");
        } catch(IllegalArgumentException iae) {
        }

        try {
            permission = new WebResourcePermission("/foo/*:*.asp", "GET,POST");
            fail("/foo/*:*.asp");
        } catch(IllegalArgumentException iae) {
        }

        try {
            permission = new WebResourcePermission("/foo:/", "GET,POST");
            fail("/foo:/");
        } catch(IllegalArgumentException iae) {
        }

        try {
            permission = new WebResourcePermission("/bar/*:/cat/stool/*", "GET,POST");
            fail("/bar/*:/cat/stool/*");
        } catch(IllegalArgumentException iae) {
        }

        try {
            permission = new WebResourcePermission("/bar/*:/*", "GET,POST");
            fail("/bar/*:/");
        } catch(IllegalArgumentException iae) {
        }

        try {
            permission = new WebResourcePermission("/bar/stool/*:/bar", "GET,POST");
            fail("/bar/stool/*:/bar");
        } catch(IllegalArgumentException iae) {
        }
        
    }

    public void testImpliesStringString() {

        // The argument is an instanceof WebResourcePermission 
        Permission pA = new WebResourcePermission("/foo", "");
        Permission pB = new WebUserDataPermission("/foo", "");
        
        assertFalse(pA.implies(pB));
        assertFalse(pB.implies(pA));
    
        pA = new WebResourcePermission("/foo", "");
        pB = new WebResourcePermission("/foo", "GET,POST");
        
        assertTrue(pA.implies(pB));
        assertFalse(pB.implies(pA));
        
        pA = new WebResourcePermission("/foo/*:/foo/bar", "");
        pB = new WebResourcePermission("/foo/bar", "");
        
        assertFalse(pA.implies(pB));
        assertFalse(pB.implies(pA));

        pA = new WebResourcePermission("/foo/bar/*:/foo/bar/cat/dog", "");
        pB = new WebResourcePermission("/foo/bar/*:/foo/bar/cat/*", "");
        
        assertTrue(pA.implies(pB));
        assertFalse(pB.implies(pA));
    }

    /*
     * Testing WebResourcePermission(String, String[])
     */
    public void testConstructorStringStringArray() {
    }
    
    public void testImpliesStringStringArray() {
    }

    /*
     * Testing WebResourcePermission(HttpServletRequest)
     */
    public void testConstructorHttpServletRequest() {
    }
    
    public void testImpliesHttpServletRequest() {
    }
    
    public static void main(String[] args) {
        WebResourcePermissionTest test = new WebResourcePermissionTest();
        test.testConstructorStringString();
        test.testImpliesStringString();
        test.testConstructorStringStringArray();
        test.testImpliesStringStringArray();
        test.testConstructorHttpServletRequest();
        test.testImpliesHttpServletRequest();
    }
}

