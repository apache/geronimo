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

import junit.framework.TestCase;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 01:55:13 $
 */
public class WebUserDataPermissionTest extends TestCase {

    /*
     * Testing WebResourcePermission(java.lang.String, java.lang.String)
     */
    public void testConstructorStringString() {

        WebUserDataPermission permission = new WebUserDataPermission("/foo", "GET,POST:INTEGRAL");

        assertEquals(permission.getName(), "/foo");
        assertEquals(permission.getActions(), "GET,POST:INTEGRAL");
        
        permission = new WebUserDataPermission("/foo", "GET,POST,POST,GET:INTEGRAL");
        assertEquals(permission.getActions(), "GET,POST:INTEGRAL");

        // bad HTTP method
        try {
            permission = new WebUserDataPermission("/foo", "GET,POST,BAR:INTEGRAL");
            fail("Bad HTTP method");
        } catch(IllegalArgumentException iae) {
        }

        // If you have a colon, then you must have a transportType
        try {
            permission = new WebUserDataPermission("/foo", "GET,POST,BAR:");
            fail("Missing transportType");
        } catch(IllegalArgumentException iae) {
        }
    }

    public void testImpliesStringString() {
        // An actions string without a transportType is a shorthand for a 
        // actions string with the value "NONE" as its TransportType
        WebUserDataPermission permissionFooGP = new WebUserDataPermission("/foo", "GET,POST:INTEGRAL");
        WebUserDataPermission permissionFooE = new WebUserDataPermission("/foo", "");
        WebUserDataPermission permissionFooGPN = new WebUserDataPermission("/foo", "GET,POST");
        
        assertTrue(permissionFooE.implies(permissionFooGP));
        assertTrue(permissionFooE.implies(permissionFooGPN));
        assertFalse(permissionFooGP.implies(permissionFooE));
        assertFalse(permissionFooGPN.implies(permissionFooE));

        assertTrue(permissionFooGPN.implies(permissionFooGP));
        assertFalse(permissionFooGP.implies(permissionFooGPN));
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
}

