/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package javax.security.jacc;

import java.security.Permission;
import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 01:55:13 $
 */
public class WebResourcePermissionTest extends TestCase {

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

