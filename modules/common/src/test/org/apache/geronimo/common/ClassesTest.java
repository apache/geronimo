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

package org.apache.geronimo.common;

import junit.framework.TestCase;

/**
 * Unit test for {@link Classes} class.
 *
 * @version $Revision: 1.3 $ $Date: 2003/09/27 20:18:37 $
 */
public class ClassesTest
    extends TestCase
{
    protected Class loadClass(final String name)
    {
        Class type = null;

        try {
            type = Classes.loadClass(name);
        }
        catch (ClassNotFoundException e) {
            fail("Class should have been found: " + e);
        }

        assertNotNull(type);

        return type;
    }

    public void testLoadClass_Null()
    {
        try {
            Classes.loadClass(null);
            fail("Expected NullArgumentException");
        }
        catch(NullArgumentException ignore) {
        }
        catch (ClassNotFoundException e) {
            fail("Class should have been found: " + e);
        }

        try {
            Classes.loadClass("org.apache.geronimo.common.Classes",null);
            fail("Expected NullArgumentException");
        }
        catch(NullArgumentException ignore) {
        }
        catch (ClassNotFoundException e) {
            fail("Class should have been found: " + e);
        }
    }

    public void testLoadClass_Simple()
    {
        String className = "org.apache.geronimo.common.Classes";
        Class type = loadClass(className);
        assertEquals(className, type.getName());
    }

    public void testLoadClass_Missing()
    {
        String className = "some.class.that.does.not.Exist";
        try {
            Classes.loadClass(className);
            fail("Expected ClassNotFoundException: " + className);
        }
        catch (ClassNotFoundException ignore) {}
    }

    public void testLoadClass_Primitives()
    {
        String className = "boolean";
        Class type = loadClass(className);
        assertEquals(className, type.getName());
    }

    public void testLoadClass_VMPrimitives()
    {
        String className = "B";
        Class type = loadClass(className);
        assertEquals(byte.class, type);
    }

    public void testLoadClass_VMClassSyntax()
    {
        String className = "org.apache.geronimo.common.Classes";
        Class type = loadClass("L" + className + ";");
        assertEquals(className, type.getName());
    }

    public void testLoadClass_VMArraySyntax()
    {
        String className = "[B";
        Class type = loadClass(className);
        assertEquals(byte[].class, type);

        className = "[java.lang.String";
        type = loadClass(className);
        assertEquals(String[].class, type);
    }

    public void testLoadClass_UserFriendlySyntax()
    {
        String className = "I[]";
        Class type = loadClass(className);
        assertEquals(int[].class, type);

        className = "I[][][]";
        type = loadClass(className);
        assertEquals(int[][][].class, type);
    }

    public void testgetClassName() throws ClassNotFoundException {
        Class t;
        Class y;
        String x;

        t= String.class;
        x = Classes.getClassName(t);
        y = loadClass(x);
        assertEquals(t,y);

        t= int.class;
        x = Classes.getClassName(t);
        y = loadClass(x);
        assertEquals(t,y);

        t= String[].class;
        x = Classes.getClassName(t);
        y = loadClass(x);
        assertEquals(t,y);

        t= int[].class;
        x = Classes.getClassName(t);
        y = loadClass(x);
        assertEquals(t,y);

        t= String[][].class;
        x = Classes.getClassName(t);
        y = loadClass(x);
        assertEquals(t,y);

        t= int[][].class;
        x = Classes.getClassName(t);
        y = loadClass(x);
        assertEquals(t,y);

    }

    public void testGetPrimitiveWrapper() {
        try {
            Classes.getPrimitiveWrapper(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }

        try {
            Classes.getPrimitiveWrapper(String.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        assertEquals(Boolean.class,Classes.getPrimitiveWrapper(Boolean.TYPE));
    }

    public void testIsPrimitiveWrapper() {
        try {
            Classes.isPrimitiveWrapper(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }
        assertTrue(Classes.isPrimitiveWrapper(Boolean.TYPE));
        assertFalse(Classes.isPrimitiveWrapper(String.class));
    }

    public void testIsPrimitive() {
        try {
            Classes.isPrimitive(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }
        assertTrue(Classes.isPrimitive(Boolean.TYPE));
        assertFalse(Classes.isPrimitive(String.class));
    }
}
