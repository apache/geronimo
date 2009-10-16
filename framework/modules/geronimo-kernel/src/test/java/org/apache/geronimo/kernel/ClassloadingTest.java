/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.util.Set;
import java.util.LinkedHashSet;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNull;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import junit.framework.TestCase;

/**
 * Unit test for {@link org.apache.geronimo.kernel.ClassLoading} class.
 *
 * @version $Rev$ $Date$
 */

//@RunWith( JUnit4TestRunner.class )
public class ClassloadingTest extends TestCase {

    private BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, null, null);

    @Test
    public void testLoadClass_Null() {
        try {
            ClassLoading.loadClass("org.apache.geronimo.kernel.ClassLoading", (Bundle)null);
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

    public void testGetAllTypes() throws Exception {
        Set allTypes = ClassLoading.getAllTypes(MockGBean.class);
        assertTrue(allTypes.contains(MockGBean.class));
        assertTrue(allTypes.contains(Object.class));
        assertTrue(allTypes.contains(MockEndpoint.class));
        assertTrue(allTypes.contains(MockParentInterface1.class));
        assertTrue(allTypes.contains(MockParentInterface2.class));
        assertTrue(allTypes.contains(MockChildInterface1.class));
        assertTrue(allTypes.contains(MockChildInterface2.class));
        assertFalse(allTypes.contains(Comparable.class));
    }

    public void testReduceInterfaces() throws Exception {
        Set types = new LinkedHashSet();

        // single class
        types.add(MockGBean.class);
        types = ClassLoading.reduceInterfaces(types);
        assertTrue(types.contains(MockGBean.class));
        assertFalse(types.contains(Object.class));
        assertFalse(types.contains(MockEndpoint.class));
        assertFalse(types.contains(MockParentInterface1.class));
        assertFalse(types.contains(MockParentInterface2.class));
        assertFalse(types.contains(MockChildInterface1.class));
        assertFalse(types.contains(MockChildInterface2.class));
        assertFalse(types.contains(Comparable.class));

        // all types
        types = ClassLoading.getAllTypes(MockGBean.class);
        types = ClassLoading.reduceInterfaces(types);
        assertTrue(types.contains(MockGBean.class));
        assertFalse(types.contains(Object.class));
        assertFalse(types.contains(MockEndpoint.class));
        assertFalse(types.contains(MockParentInterface1.class));
        assertFalse(types.contains(MockParentInterface2.class));
        assertFalse(types.contains(MockChildInterface1.class));
        assertFalse(types.contains(MockChildInterface2.class));
        assertFalse(types.contains(Comparable.class));

        // double all types
        types = ClassLoading.getAllTypes(MockGBean.class);
        types.addAll(ClassLoading.getAllTypes(MockGBean.class));
        types = ClassLoading.reduceInterfaces(types);
        assertTrue(types.contains(MockGBean.class));
        assertFalse(types.contains(Object.class));
        assertFalse(types.contains(MockEndpoint.class));
        assertFalse(types.contains(MockParentInterface1.class));
        assertFalse(types.contains(MockParentInterface2.class));
        assertFalse(types.contains(MockChildInterface1.class));
        assertFalse(types.contains(MockChildInterface2.class));
        assertFalse(types.contains(Comparable.class));

        // extra interfaces
        types = ClassLoading.getAllTypes(MockGBean.class);
        types.addAll(ClassLoading.getAllTypes(Kernel.class));
        types.addAll(ClassLoading.getAllTypes(Serializable.class));
        types = ClassLoading.reduceInterfaces(types);
        assertTrue(types.contains(Kernel.class));
        assertTrue(types.contains(Serializable.class));
        assertTrue(types.contains(MockGBean.class));
        assertFalse(types.contains(Object.class));
        assertFalse(types.contains(MockEndpoint.class));
        assertFalse(types.contains(MockParentInterface1.class));
        assertFalse(types.contains(MockParentInterface2.class));
        assertFalse(types.contains(MockChildInterface1.class));
        assertFalse(types.contains(MockChildInterface2.class));
        assertFalse(types.contains(Comparable.class));

        // two different types
        types = ClassLoading.getAllTypes(MockGBean.class);
        try {
            types.addAll(ClassLoading.getAllTypes(BasicKernel.class));
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private Class loadClass(String name) {
        Class type = null;

        try {
            type = ClassLoading.loadClass(name, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            fail("Class should have been found: " + e);
        }

        assertNotNull(type);

        return type;
    }
}
