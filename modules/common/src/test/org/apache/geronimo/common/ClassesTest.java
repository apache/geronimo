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

package org.apache.geronimo.common;

import junit.framework.TestCase;

/**
 * Unit test for {@link Classes} class.
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:27 $
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
