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

package org.apache.geronimo.kernel;

import junit.framework.TestCase;

/**
 * Unit test for {@link org.apache.geronimo.kernel.ClassLoading} class.
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/21 22:24:39 $
 */
public class ClassloadingTest
        extends TestCase {
    protected Class loadClass(final String name) {
        Class type = null;

        try {
            type = ClassLoading.loadClass(name);
        } catch (ClassNotFoundException e) {
            fail("Class should have been found: " + e);
        }

        assertNotNull(type);

        return type;
    }

    public void testLoadClass_Null() {
        try {
            ClassLoading.loadClass(null);
            fail("Expected NullArgumentException");
        } catch (IllegalArgumentException ignore) {
        } catch (ClassNotFoundException e) {
            fail("Class should have been found: " + e);
        }

        try {
            ClassLoading.loadClass("org.apache.geronimo.kernel.ClassLoading", null);
            fail("Expected NullArgumentException");
        } catch (IllegalArgumentException ignore) {
        } catch (ClassNotFoundException e) {
            fail("Class should have been found: " + e);
        }
    }

    public void testLoadClass_Simple() {
        String className = "org.apache.geronimo.kernel.ClassLoading";
        Class type = loadClass(className);
        assertEquals(className, type.getName());
    }

    public void testLoadClass_Missing() {
        String className = "some.class.that.does.not.Exist";
        try {
            ClassLoading.loadClass(className);
            fail("Expected ClassNotFoundException: " + className);
        } catch (ClassNotFoundException ignore) {
        }
    }

    public void testLoadClass_Primitives() {
        String className = "boolean";
        Class type = loadClass(className);
        assertEquals(className, type.getName());
    }

    public void testLoadClass_VMPrimitives() {
        String className = "B";
        Class type = loadClass(className);
        assertEquals(byte.class, type);
    }

    public void testLoadClass_VMClassSyntax() {
        String className = "org.apache.geronimo.kernel.ClassLoading";
        Class type = loadClass("L" + className + ";");
        assertEquals(className, type.getName());
    }

    public void testLoadClass_VMArraySyntax() {
        String className = "[B";
        Class type = loadClass(className);
        assertEquals(byte[].class, type);

        className = "[java.lang.String";
        type = loadClass(className);
        assertEquals(String[].class, type);
    }

    public void testLoadClass_UserFriendlySyntax() {
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

        t = String.class;
        x = ClassLoading.getClassName(t);
        y = loadClass(x);
        assertEquals(t, y);

        t = int.class;
        x = ClassLoading.getClassName(t);
        y = loadClass(x);
        assertEquals(t, y);

        t = String[].class;
        x = ClassLoading.getClassName(t);
        y = loadClass(x);
        assertEquals(t, y);

        t = int[].class;
        x = ClassLoading.getClassName(t);
        y = loadClass(x);
        assertEquals(t, y);

        t = String[][].class;
        x = ClassLoading.getClassName(t);
        y = loadClass(x);
        assertEquals(t, y);

        t = int[][].class;
        x = ClassLoading.getClassName(t);
        y = loadClass(x);
        assertEquals(t, y);

    }
}
