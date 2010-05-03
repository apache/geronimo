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

package org.apache.geronimo.kernel.rmi;

import java.net.MalformedURLException;
import java.net.URL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.rmi.RMIClassLoaderSpiImpl;

/**
 * Unit tests for {@link RMIClassLoaderSpiImpl} class.
 *
 * @version $Rev$ $Date$
 */
public class RMIClassLoaderSpiImplTest extends TestCase {
    private String baseURL;
    private String normalizedBaseURL;

    protected void setUp() throws Exception {
        File dir = new File(System.getProperty("user.home"));

        // NOTE:  Do not change this to use toURL().toURI().toString().
        // This messes up Windows tests where directories can have blanks.
        // The URL encoding gets applied twice, resulting in %20 encodings
        // getting converted into invalid %2520 encodings
        baseURL = dir.toURL().toString();
        if (baseURL.endsWith("/")) {
            baseURL = baseURL.substring(0, baseURL.length() - 1);
        }

        normalizedBaseURL = dir.toURI().toURL().toString();
        if (normalizedBaseURL.endsWith("/")) {
            normalizedBaseURL = normalizedBaseURL.substring(0, normalizedBaseURL.length() - 1);
        }
    }

    public void testNormalizeURL() throws MalformedURLException {
        URL url = new URL(baseURL + "/Apache Group/Geronimo");
        URL normal = RMIClassLoaderSpiImpl.normalizeURL(url);
        assertEquals(normalizedBaseURL + "/Apache%20Group/Geronimo", normal.toString());
    }

    public void testNormalizeCodebase() throws MalformedURLException {
        String codebase = baseURL + "/Apache Group/Geronimo " + baseURL + "/Apache Group/Apache2";

        String normal = RMIClassLoaderSpiImpl.normalizeCodebase(codebase);
        assertEquals(normalizedBaseURL + "/Apache%20Group/Geronimo " +
                     normalizedBaseURL + "/Apache%20Group/Apache2", normal);
    }

    public void testGetClassAnnotationWithClassLoaderServerAware() throws Exception {
        String url1 = "http://localhost:8090/Tester1";
        String url2 = "http://localhost:8090/Tester2";
        MockClassLoader cl = new MockClassLoader(getClass().getClassLoader(), new URL[] {new URL(url1), new URL(url2)});

        Class clazz = cl.loadClass(RMIClassLoaderSpiImplTest.class.getName());

        RMIClassLoaderSpiImpl impl = new RMIClassLoaderSpiImpl();
        String annotations = impl.getClassAnnotation(clazz);
        assertEquals(url1 + " " + url2, annotations);
    }

    private static class MockClassLoader extends ClassLoader implements RMIClassLoaderSpiImpl.ClassLoaderServerAware {
        private final ClassLoader delegate;
        private final URL[] urls;

        private MockClassLoader(ClassLoader delegate, URL[] urls) {
            this.delegate = delegate;
            this.urls = urls;
        }

        public Class loadClass(String name) throws ClassNotFoundException {
            if (name.startsWith("java")) {
                return delegate.loadClass(name);
            }
            String resourceName = name.replace('.', '/') + ".class";
            InputStream in = delegate.getResourceAsStream(resourceName);
            try {
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int read = 0;
                try {
                    while (0 < (read = in.read(buffer))) {
                        out.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    fail();
                    return null;
                }
                return defineClass(name, out.toByteArray(), 0, out.size());
            } finally {
                if (in != null)
                {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                        // ignored
                    }
                }
            }
        }

        public URL[] getClassLoaderServerURLs() {
            return urls;
        }
    }
}
