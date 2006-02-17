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
package org.apache.geronimo.kernel.config;

import java.io.File;
import java.io.Serializable;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.URI;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.Enumeration;

import junit.framework.TestCase;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev$ $Date$
 */
public class MultiParentClassLoaderTest extends TestCase {
    private static final String CLASS_NAME = "TestClass";
    private static final String ENTRY_NAME = "foo";
    private static final String ENTRY_VALUE = "bar";
    private File[] files;
    private static final String NON_EXISTANT_RESOURCE = "non-existant-resource";
    private static final String NON_EXISTANT_CLASS = "NonExistant.class";
    private URLClassLoader[] parents;
    private File myFile;
    private MultiParentClassLoader classLoader;
    private static final Artifact NAME = new Artifact("test", "fake", "1.0", "car", true);

    /**
     * Verify that the test jars are valid
     * @throws Exception
     */
    public void testTestJars() throws Exception {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            JarFile jarFile = new JarFile(files[i]);
            String urlString = "jar:" + file.toURL() + "!/" + ENTRY_NAME;
            URL url = new URL(files[i].toURL(), urlString);
            assertStreamContains(ENTRY_VALUE + i, url.openStream());
            jarFile.close();

            URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { file.toURL() } );
            // clazz shared by all
            Class clazz = urlClassLoader.loadClass(CLASS_NAME);
            assertNotNull(clazz);
            assertTrue(clazz instanceof Serializable);

            // clazz specific to this jar
            clazz = urlClassLoader.loadClass(CLASS_NAME + i);
            assertNotNull(clazz);
            assertTrue(clazz instanceof Serializable);

            // resource shared by all jars
            InputStream in = urlClassLoader.getResourceAsStream(ENTRY_NAME );
            assertStreamContains("Should have found value from parent " + i, ENTRY_VALUE + i, in);

            // resource specific to this jar
            in = urlClassLoader.getResourceAsStream(ENTRY_NAME + i);
            assertStreamContains("Should have found value from parent " + i, ENTRY_VALUE + i + ENTRY_VALUE, in);
        }
    }

    /**
     * Verify the get name method returns the name provided to the constructor.
     */
    public void testGetName() {
        assertEquals(NAME, classLoader.getId());
    }

    /**
     * Verufy that the getParents method returns a different array from the one passed to the constructor and that the
     * parents are in the same order.
     */
    public void testGetParents() {
        ClassLoader[] actualParents = classLoader.getParents();
        assertNotSame(parents, actualParents);
        assertEquals(parents.length, actualParents.length);
        for (int i = 0; i < actualParents.length; i++) {
            assertEquals(parents[i], actualParents[i]);
        }
    }

    /**
     * Test loadClass loads in preference of the parents, in order, and then the local urls.
     * @throws Exception if a problem occurs
     */
    public void testLoadClass() throws Exception {
        // load class specific to my class loader
        Class clazz = classLoader.loadClass(CLASS_NAME + 33);
        assertNotNull(clazz);
        assertTrue(clazz instanceof Serializable);
        assertEquals(classLoader, clazz.getClassLoader());

        // load class specific to each parent class loader
        for (int i = 0; i < parents.length; i++) {
            URLClassLoader parent = parents[i];
            clazz = classLoader.loadClass(CLASS_NAME + i);
            assertNotNull(clazz);
            assertTrue(clazz instanceof Serializable);
            assertEquals(parent, clazz.getClassLoader());
        }

        // class shared by all class loaders
        clazz = classLoader.loadClass(CLASS_NAME);
        assertNotNull(clazz);
        assertTrue(clazz instanceof Serializable);
        assertEquals(parents[0], clazz.getClassLoader());
    }

    public void testInverseClassLoading() throws Exception {
        File parentJar = createJarFile(0);
        ClassLoader parentCl = new URLClassLoader(new URL[]{parentJar.toURL()});
        File myJar = createJarFile(1);
        ClassLoader cl = new MultiParentClassLoader(NAME, new URL[]{myJar.toURL()}, parentCl);
        Class clazz = cl.loadClass(CLASS_NAME);
        assertSame(parentCl, clazz.getClassLoader());

        cl = new MultiParentClassLoader(NAME, new URL[]{myJar.toURL()}, parentCl, true, new String[0], new String[0]);
        clazz = cl.loadClass(CLASS_NAME);
        assertSame(cl, clazz.getClassLoader());
    }

    public void testHiddenClasses() throws Exception {
        File parentJar = createJarFile(0);
        ClassLoader parentCl = new URLClassLoader(new URL[]{parentJar.toURL()});
        File myJar = createJarFile(1);
        ClassLoader cl = new MultiParentClassLoader(NAME, new URL[]{myJar.toURL()}, parentCl);
        Class clazz = cl.loadClass(CLASS_NAME);
        assertSame(parentCl, clazz.getClassLoader());

        cl = new MultiParentClassLoader(NAME, new URL[]{myJar.toURL()}, parentCl, false, new String[] {CLASS_NAME}, new String[0]);
        clazz = cl.loadClass(CLASS_NAME);
        assertSame(cl, clazz.getClassLoader());
    }

    public void testNonOverridableClasses() throws Exception {
        File parentJar = createJarFile(0);
        ClassLoader parentCl = new URLClassLoader(new URL[]{parentJar.toURL()});
        File myJar = createJarFile(1);
        ClassLoader cl = new MultiParentClassLoader(NAME, new URL[]{myJar.toURL()}, parentCl);
        Class clazz = cl.loadClass(CLASS_NAME);
        assertSame(parentCl, clazz.getClassLoader());

        cl = new MultiParentClassLoader(NAME, new URL[]{myJar.toURL()}, parentCl, true, new String[0], new String[] {CLASS_NAME});
        clazz = cl.loadClass(CLASS_NAME);
        assertSame(parentCl, clazz.getClassLoader());
    }

    /**
     * Test that an attempt to load a non-existant class causes a ClassNotFoundException.
     */
    public void testLoadNonExistantClass() {
        try {
            classLoader.loadClass(NON_EXISTANT_CLASS);
            fail("loadClass should have thrown a ClassNotFoundException");
        } catch (ClassNotFoundException e) {
            // expected
        }
    }

    /**
     * Test getResourceAsStream loads in preference of the parents, in order, and then the local urls.
     * @throws Exception if a problem occurs
     */
    public void testGetResourceAsStream() throws Exception {
        InputStream in = classLoader.getResourceAsStream(ENTRY_NAME + 33);
        assertStreamContains("Should have found value from my file", ENTRY_VALUE + 33 + ENTRY_VALUE, in);

        for (int i = 0; i < parents.length; i++) {
            in = classLoader.getResourceAsStream(ENTRY_NAME + i);
            assertStreamContains("Should have found value from parent " + i, ENTRY_VALUE + i + ENTRY_VALUE, in);
        }

        in = classLoader.getResourceAsStream(ENTRY_NAME);
        assertStreamContains("Should have found value from first parent", ENTRY_VALUE + 0, in);
    }

    /**
     * Test getResourceAsStream returns null when attempt is made to loade a non-existant resource.
     */
    public void testGetNonExistantResourceAsStream() throws Exception {
        InputStream in = classLoader.getResourceAsStream(NON_EXISTANT_RESOURCE);
        assertNull(in);
    }

    /**
     * Test getResource loads in preference of the parents, in order, and then the local urls.
     * @throws Exception if a problem occurs
     */
    public void testGetResource() throws Exception {
        URL resource = classLoader.getResource(ENTRY_NAME + 33);
        assertURLContains("Should have found value from my file", ENTRY_VALUE + 33 + ENTRY_VALUE, resource);

        for (int i = 0; i < parents.length; i++) {
            resource = classLoader.getResource(ENTRY_NAME + i);
            assertURLContains("Should have found value from parent " + i, ENTRY_VALUE + i + ENTRY_VALUE, resource);
        }

        resource = classLoader.getResource(ENTRY_NAME);
        assertURLContains("Should have found value from first parent", ENTRY_VALUE + 0, resource);
    }

    /**
     * Test getResource returns null when attempt is made to loade a non-existant resource.
     */
    public void testGetNonExistantResource() throws Exception {
        URL resource = classLoader.getResource(NON_EXISTANT_RESOURCE);
        assertNull(resource);
    }

    /**
     * Test getResource returns an enumeration in preference of the parents, in order, and then the local urls.
     * @throws Exception if a problem occurs
     */
    public void testGetResources() throws Exception {
        Enumeration resources = classLoader.getResources(ENTRY_NAME);
        assertNotNull(resources);
        assertTrue(resources.hasMoreElements());

        // there should be one entry for each parent
        for (int i = 0; i < parents.length; i++) {
            URL resource = (URL) resources.nextElement();
            assertURLContains("Should have found value from parent " + i, ENTRY_VALUE + i, resource);
        }

        // and one entry from my url
        assertTrue(resources.hasMoreElements());
        URL resource = (URL) resources.nextElement();
        assertURLContains("Should have found value from my file", ENTRY_VALUE + 33, resource);
    }

    /**
     * Test getResources returns an empty enumeration when attempt is made to loade a non-existant resource.
     */
    public void testGetNonExistantResources() throws Exception {
        Enumeration resources = classLoader.getResources(NON_EXISTANT_RESOURCE);
        assertNotNull(resources);
        assertFalse(resources.hasMoreElements());
    }

    private void assertStreamContains(String expectedValue, InputStream in) throws IOException {
        assertStreamContains(null, expectedValue, in);
    }

    private void assertStreamContains(String message, String expectedValue, InputStream in) throws IOException {
        String entryValue;
        try {
            StringBuffer stringBuffer = new StringBuffer();
            byte[] bytes = new byte[4000];
            for (int count = in.read(bytes); count != -1; count = in.read(bytes)) {
                stringBuffer.append(new String(bytes, 0, count));
            }
            entryValue = stringBuffer.toString();
        } finally {
            in.close();
        }
        assertEquals(message, expectedValue, entryValue);
    }

    private void assertURLContains(String message, String expectedValue, URL resource) throws IOException {
        InputStream in;
        assertNotNull(resource);
        in = resource.openStream();
        assertStreamContains(message, expectedValue, in);
    }

    private static void assertFileExists(File file) {
        assertTrue("File should exist: " + file, file.canRead());
    }

    private static void assertFileNotExists(File file) {
        assertTrue("File should not exist: " + file, !file.canRead());
    }

    protected void setUp() throws Exception {
        files = new File[3];
        for (int i = 0; i < files.length; i++) {
            files[i] = createJarFile(i);
        }

        parents = new URLClassLoader[3];
        for (int i = 0; i < parents.length; i++) {
            parents[i] = new URLClassLoader(new URL[]{files[i].toURL()});
        }

        myFile = createJarFile(33);
        classLoader = new MultiParentClassLoader(NAME, new URL[]{myFile.toURL()}, parents);
    }

    private static File createJarFile(int i) throws IOException {
        File file = File.createTempFile("test-" + i + "-", ".jar");

        FileOutputStream out = new FileOutputStream(file);
        JarOutputStream jarOut = new JarOutputStream(out);

        // common class shared by everyone
        jarOut.putNextEntry(new JarEntry(CLASS_NAME + ".class"));
        jarOut.write(createClass(CLASS_NAME));

        // class only available in this jar
        jarOut.putNextEntry(new JarEntry(CLASS_NAME + i + ".class"));
        jarOut.write(createClass(CLASS_NAME + i));

        // common resource shared by everyone
        jarOut.putNextEntry(new JarEntry(ENTRY_NAME));
        jarOut.write((ENTRY_VALUE + i).getBytes());

        // resource only available in this jar
        jarOut.putNextEntry(new JarEntry(ENTRY_NAME + i));
        jarOut.write((ENTRY_VALUE + i + ENTRY_VALUE).getBytes());

        jarOut.close();
        out.close();

        assertFileExists(file);
        return file;
    }

    private static byte[] createClass(final String name) {
        Enhancer enhancer = new Enhancer();
        enhancer.setNamingPolicy(new NamingPolicy() {
            public String getClassName(String prefix, String source, Object key, Predicate names) {
                return name;
            }
        });
        enhancer.setClassLoader(new URLClassLoader(new URL[0]));
        enhancer.setSuperclass(Object.class);
        enhancer.setInterfaces(new Class[]{Serializable.class});
        enhancer.setCallbackTypes(new Class[]{NoOp.class});
        enhancer.setUseFactory(false);
        ByteCode byteCode = new ByteCode();
        enhancer.setStrategy(byteCode);
        enhancer.createClass();

        return byteCode.getByteCode();
    }

    protected void tearDown() throws Exception {
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
//            assertFileNotExists(files[i]);
        }
    }

    private static class ByteCode extends DefaultGeneratorStrategy {
        private byte[] byteCode;

        public byte[] transform(byte[] byteCode) {
            this.byteCode = byteCode;
            return byteCode;
        }

        public byte[] getByteCode() {
            return byteCode;
        }
    }
}