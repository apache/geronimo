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

package org.apache.geronimo.jetty;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

import junit.framework.TestCase;

/**
 * Tests loading various classes (as classes and URL resources) with different
 * settings for contextPriorityClassLoader to make sure the restrictions on
 * javax.* class loading are honored.
 *
 * @version $Rev: 54805 $ $Date: 2004-10-14 17:51:13 -0400 (Thu, 14 Oct 2004) $
 */
public class ClassLoaderTest extends TestCase {
    JettyClassLoader cl;
    URL[] urls;

    public void setUp() throws MalformedURLException {
        URL url = new File("src/test-resources/deployables/cltest/").toURL();
//        URL url = getClass().getClassLoader().getResource("deployables/cltest/");
        System.err.println("URL: "+url);
        urls = new URL[]{url};
    }

    //todo: try more restricted prefixed besides javax.*

    /**
     * Tries to load a javax.* class that's not available from the
     * parent ClassLoader.  This should work.
     */
    public void testFalseNonexistantJavaxClass() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), false);
        try {
            cl.loadClass("javax.foo.Foo");
        } catch(ClassNotFoundException e) {
            fail("Should be able to load a javax.* class that is not defined by my parent CL");
        }
    }

    /**
     * Tries to load a javax.* class that's not available from the
     * parent ClassLoader.  This should work.
     */
    public void testTrueNonexistantJavaxClass() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), true);
        try {
            cl.loadClass("javax.foo.Foo");
        } catch(ClassNotFoundException e) {
            fail("Should be able to load a javax.* class that is not defined by my parent CL");
        }
    }

    /**
     * Tries to load a javax.* class that is avialable from the parent ClassLoader,
     * when there's a different definition available from this ClassLoader too.
     * This should always load the parent's copy.
     */
    public void testFalseExistantJavaxClass() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), false);
        try {
            Class cls = cl.loadClass("javax.servlet.Servlet");
            assertTrue("Loaded wrong class first; expected to find parent CL's copy of javax.servlet.Servlet",cls.getDeclaredMethods().length > 0);
        } catch(ClassNotFoundException e) {
            fail("Problem with test; expecting to have javax.servlet.* on the ClassPath");
        }
    }

    /**
     * Tries to load a javax.* class that is avialable from the parent ClassLoader,
     * when there's a different definition available from this ClassLoader too.
     * This should always load the parent's copy.
     */
    public void testTrueExistantJavaxClass() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), true);
        try {
            Class cls = cl.loadClass("javax.servlet.Servlet");
            assertTrue("Loaded wrong class first; expected to find parent CL's copy of javax.servlet.Servlet",cls.getDeclaredMethods().length > 0);
        } catch(ClassNotFoundException e) {
            fail("Problem with test; expecting to have javax.servlet.* on the ClassPath");
        }
    }

    /**
     * Tries to load a non-javax.* class that is aailable form the parent
     * ClassLoader, when there's a different definition available from this
     * ClassLoader.  This should load the parent's copy when
     * contextPriorityClassLoader is set to false (as here) and the child's
     * copy when the contextPriorityClassLoader is set to true.
     */
    public void testFalseExistantNonJavaxClass() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), false);
        try {
            Class cls = cl.loadClass("mx4j.MBeanDescription");
            assertTrue("Should not have overriden parent CL definition of class mx4j.MBeanDescription", cls.getDeclaredMethods().length > 0);
        } catch(ClassNotFoundException e) {
            fail("Problem with test; expecting to have mx4j.* on the ClassPath");
        }
    }

    /**
     * Tries to load a non-javax.* class that is aailable form the parent
     * ClassLoader, when there's a different definition available from this
     * ClassLoader.  This should load the parent's copy when
     * contextPriorityClassLoader is set to false and the child's copy when
     * the contextPriorityClassLoader is set to true (as here).
     */
    public void testTrueExistantNonJavaxClass() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), true);
        try {
            Class cls = cl.loadClass("mx4j.MBeanDescription");
            assertTrue("Should be able to override a class that is not in java.*, javax.*, etc.", cls.getDeclaredMethods().length == 0);
        } catch(ClassNotFoundException e) {
            fail("Problem with test; expecting to have mx4j.* on the ClassPath");
        }
    }

    /**
     * Tries to load a javax.* class that's not available from the
     * parent ClassLoader.  This should work.
     */
    public void testFalseNonexistantJavaxResource() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), false);
        URL url = cl.getResource("javax/foo/Foo.class");
        if(url == null) {
            fail("Should be able to load a javax.* class that is not defined by my parent CL");
        }
        assertEquals(url.getProtocol(), "file");
    }

    /**
     * Tries to load a javax.* class that's not available from the
     * parent ClassLoader.  This should work.
     */
    public void testTrueNonexistantJavaxResource() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), true);
        URL url = cl.getResource("javax/foo/Foo.class");
        if(url == null) {
            fail("Should be able to load a javax.* class that is not defined by my parent CL");
        }
        assertEquals(url.getProtocol(), "file");
    }

    /**
     * Tries to load a javax.* class that is avialable from the parent ClassLoader,
     * when there's a different definition available from this ClassLoader too.
     * This should always load the parent's copy.
     */
    public void testFalseExistantJavaxResource() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), false);
        URL url = cl.getResource("javax/servlet/Servlet.class");
        if(url == null) {
            fail("Problem with test; expecting to have javax.servlet.* on the ClassPath");
        }
        assertEquals("Loaded wrong class first; expected to find parent CL's copy of javax.servlet.Servlet", url.getProtocol(), "jar");
    }

    /**
     * Tries to load a javax.* class that is avialable from the parent ClassLoader,
     * when there's a different definition available from this ClassLoader too.
     * This should always load the parent's copy.
     */
    public void testTrueExistantJavaxResource() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), true);
        URL url = cl.getResource("javax/servlet/Servlet.class");
        if(url == null) {
            fail("Problem with test; expecting to have javax.servlet.* on the ClassPath");
        }
        assertEquals("Loaded wrong class first; expected to find parent CL's copy of javax.servlet.Servlet",url.getProtocol(),"jar");
    }

    /**
     * Tries to load a non-javax.* class that is aailable form the parent
     * ClassLoader, when there's a different definition available from this
     * ClassLoader.  This should load the parent's copy when
     * contextPriorityClassLoader is set to false (as here) and the child's
     * copy when the contextPriorityClassLoader is set to true.
     */
    public void testFalseExistantNonJavaxResource() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), false);
        URL url = cl.getResource("mx4j/MBeanDescription.class");
        if(url == null) {
            fail("Problem with test; expecting to have mx4j.* on the ClassPath");
        }
        assertEquals("Should not have overriden parent CL definition of class mx4j.MBeanDescription", url.getProtocol(), "jar");
    }

    /**
     * Tries to load a non-javax.* class that is aailable form the parent
     * ClassLoader, when there's a different definition available from this
     * ClassLoader.  This should load the parent's copy when
     * contextPriorityClassLoader is set to false and the child's copy when
     * the contextPriorityClassLoader is set to true (as here).
     */
    public void testTrueExistantNonJavaxResource() {
        cl = new JettyClassLoader(urls, getClass().getClassLoader(), true);
        URL url = cl.getResource("mx4j/MBeanDescription.class");
        if(url == null) {
            fail("Problem with test; expecting to have mx4j.* on the ClassPath");
        }
        assertEquals("Should be able to override a class that is not in java.*, javax.*, etc.", url.getProtocol(), "file");
    }
}
