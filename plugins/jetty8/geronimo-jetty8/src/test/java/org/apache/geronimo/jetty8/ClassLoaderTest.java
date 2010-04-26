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

package org.apache.geronimo.jetty8;

import java.io.File;
import java.net.URL;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

import org.apache.geronimo.testsupport.TestSupport;

// import org.apache.geronimo.kernel.config.MultiParentClassLoader;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;

/**
 * Tests loading various classes (as classes and URL resources) with different
 * settings for contextPriorityClassLoader to make sure the restrictions on
 * javax.* class loading are honored.
 *
 * @version $Rev$ $Date$
/**
 * \
 *
 */
public class ClassLoaderTest extends TestSupport {
    Artifact configId = new Artifact("foo", "bar", "1", "car");
    ClassLoader cl;
    URL[] urls;
    private static final Set<String> HIDDEN = new HashSet<String>(Arrays.asList("org.apache.geronimo", "org.mortbay", "org.xml", "org.w3c"));
    private static final Set<String> NON_OVERRIDABLE = new HashSet<String>(Arrays.asList("java.", "javax."));
    private final ClassLoadingRules clRules = new ClassLoadingRules();

    public void setUp() throws Exception {
        super.setUp();
        URL url = new File(BASEDIR, "src/test/resources/deployables/cltest/").toURI().toURL();
        urls = new URL[]{url};
        clRules.setInverseClassLoading(false);
        clRules.getHiddenRule().setClassPrefixes(HIDDEN);
        clRules.getNonOverrideableRule().setClassPrefixes(NON_OVERRIDABLE);
    }

    //todo: try more restricted prefixed besides javax.*

    /**
     * Tries to load a javax.* class that's not available from the
     * parent ClassLoader.  This should work.
     */
    public void testFalseNonexistantJavaxClass() {
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        try {
//            cl.loadClass("javax.foo.Foo");
//        } catch(ClassNotFoundException e) {
//            fail("Should be able to load a javax.* class that is not defined by my parent CL");
//        }
    }

    /**
     * Tries to load a javax.* class that's not available from the
     * parent ClassLoader.  This should work.
     */
//    public void testTrueNonexistantJavaxClass() {
//        clRules.setInverseClassLoading(true);
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        try {
//            cl.loadClass("javax.foo.Foo");
//        } catch(ClassNotFoundException e) {
//            fail("Should be able to load a javax.* class that is not defined by my parent CL");
//        }
//    }

    /**
     * Tries to load a javax.* class that is avialable from the parent ClassLoader,
     * when there's a different definition available from this ClassLoader too.
     * This should always load the parent's copy.
     */
//    public void testFalseExistantJavaxClass() {
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        try {
//            Class cls = cl.loadClass("javax.servlet.Servlet");
//            assertTrue("Loaded wrong class first; expected to find parent CL's copy of javax.servlet.Servlet",cls.getDeclaredMethods().length > 0);
//        } catch(ClassNotFoundException e) {
//            fail("Problem with test; expecting to have javax.servlet.* on the ClassPath");
//        }
//    }

    /**
     * Tries to load a javax.* class that is avialable from the parent ClassLoader,
     * when there's a different definition available from this ClassLoader too.
     * This should always load the parent's copy.
     */
//    public void testTrueExistantJavaxClass() {
//        clRules.setInverseClassLoading(true);
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        try {
//            Class cls = cl.loadClass("javax.servlet.Servlet");
//            assertTrue("Loaded wrong class first; expected to find parent CL's copy of javax.servlet.Servlet",cls.getDeclaredMethods().length > 0);
//        } catch(ClassNotFoundException e) {
//            fail("Problem with test; expecting to have javax.servlet.* on the ClassPath");
//        }
//    }

    /**
     * Tries to load a non-javax.* class that is aailable form the parent
     * ClassLoader, when there's a different definition available from this
     * ClassLoader.  This should load the parent's copy when
     * contextPriorityClassLoader is set to false (as here) and the child's
     * copy when the contextPriorityClassLoader is set to true.
     */
//    public void xtestFalseExistantNonJavaxClass() {
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        try {
//            Class cls = cl.loadClass("mx4j.MBeanDescription");
//            assertTrue("Should not have overriden parent CL definition of class mx4j.MBeanDescription", cls.getDeclaredMethods().length > 0);
//        } catch(ClassNotFoundException e) {
//            fail("Problem with test; expecting to have mx4j.* on the ClassPath");
//        }
//    }

    /**
     * Tries to load a non-javax.* class that is aailable form the parent
     * ClassLoader, when there's a different definition available from this
     * ClassLoader.  This should load the parent's copy when
     * contextPriorityClassLoader is set to false and the child's copy when
     * the contextPriorityClassLoader is set to true (as here).
     */
//    public void xtestTrueExistantNonJavaxClass() {
//        clRules.setInverseClassLoading(true);
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        try {
//            Class cls = cl.loadClass("mx4j.MBeanDescription");
//            assertTrue("Should be able to override a class that is not in java.*, javax.*, etc.", cls.getDeclaredMethods().length == 0);
//        } catch(ClassNotFoundException e) {
//            fail("Problem with test; expecting to have mx4j.* on the ClassPath");
//        }
//    }

    /**
     * Tries to load a javax.* class that's not available from the
     * parent ClassLoader.  This should work.
     */
//    public void testFalseNonexistantJavaxResource() {
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        URL url = cl.getResource("javax/foo/Foo.class");
//        if(url == null) {
//            fail("Should be able to load a javax.* class that is not defined by my parent CL");
//        }
//        assertEquals(url.getProtocol(), "file");
//    }

    /**
     * Tries to load a javax.* class that's not available from the
     * parent ClassLoader.  This should work.
     */
//    public void testTrueNonexistantJavaxResource() {
//        clRules.setInverseClassLoading(true);
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        URL url = cl.getResource("javax/foo/Foo.class");
//        if(url == null) {
//            fail("Should be able to load a javax.* class that is not defined by my parent CL");
//        }
//        assertEquals(url.getProtocol(), "file");
//    }

    /**
     * Tries to load a javax.* class that is avialable from the parent ClassLoader,
     * when there's a different definition available from this ClassLoader too.
     * This should always load the parent's copy.
     */
//    public void testFalseExistantJavaxResource() {
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        URL url = cl.getResource("javax/servlet/Servlet.class");
//        if(url == null) {
//            fail("Problem with test; expecting to have javax.servlet.* on the ClassPath");
//        }
//        assertEquals("Loaded wrong class first; expected to find parent CL's copy of javax.servlet.Servlet", url.getProtocol(), "jar");
//    }

    /**
     * Tries to load a javax.* class that is avialable from the parent ClassLoader,
     * when there's a different definition available from this ClassLoader too.
     * This should always load the parent's copy.
     */
//    public void testTrueExistantJavaxResource() {
//        clRules.setInverseClassLoading(true);
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        URL url = cl.getResource("javax/servlet/Servlet.class");
//        if(url == null) {
//            fail("Problem with test; expecting to have javax.servlet.* on the ClassPath");
//        }
//        assertEquals("Loaded wrong class first; expected to find parent CL's copy of javax.servlet.Servlet",url.getProtocol(),"jar");
//    }

    /**
     * Tries to load a non-javax.* class that is aailable form the parent
     * ClassLoader, when there's a different definition available from this
     * ClassLoader.  This should load the parent's copy when
     * contextPriorityClassLoader is set to false (as here) and the child's
     * copy when the contextPriorityClassLoader is set to true.
     */
//    public void xtestFalseExistantNonJavaxResource() {
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        URL url = cl.getResource("mx4j/MBeanDescription.class");
//        if(url == null) {
//            fail("Problem with test; expecting to have mx4j.* on the ClassPath");
//        }
//        assertEquals("Should not have overriden parent CL definition of class mx4j.MBeanDescription", url.getProtocol(), "jar");
//    }

    /**
     * Tries to load a non-javax.* class that is aailable form the parent
     * ClassLoader, when there's a different definition available from this
     * ClassLoader.  This should load the parent's copy when
     * contextPriorityClassLoader is set to false and the child's copy when
     * the contextPriorityClassLoader is set to true (as here).
     */
//    public void testTrueExistantNonJavaxResource() {
//        clRules.setInverseClassLoading(true);
//        clRules.getHiddenRule().setClassPrefixes(Collections.<String>emptySet());
//        cl = new MultiParentClassLoader(configId, urls, getClass().getClassLoader(), clRules);
//        URL url = cl.getResource("mx4j/MBeanDescription.class");
//        if(url == null) {
//            fail("Problem with test; expecting to have mx4j.* on the ClassPath");
//        }
//        assertEquals("Should be able to override a class that is not in java.*, javax.*, etc.", url.getProtocol(), "file");
//    }
}
