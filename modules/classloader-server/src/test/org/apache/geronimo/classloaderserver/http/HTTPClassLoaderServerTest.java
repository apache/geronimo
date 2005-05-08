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

package org.apache.geronimo.classloaderserver.http;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.classloaderserver.ClassLoaderInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.jetty.JettyContainerImpl;
import org.apache.geronimo.jetty.connector.HTTPConnector;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;


/**
 *
 * @version $Rev: 109957 $ $Date: 2004-12-06 18:52:06 +1100 (Mon, 06 Dec 2004) $
 */
public class HTTPClassLoaderServerTest extends TestCase {
    private ClassLoader cl = this.getClass().getClassLoader();
    private ObjectName containerName;
    private ObjectName connectorName;
    private ObjectName classLoaderServerName;
    private MockClassLoaderInfo clInfo;
    private Kernel kernel;
    
    public void testURLs() throws Exception {
        assertEquals(1, clInfo.urls.length);
    }
    
    public void testOK() throws Exception {
        URLClassLoader urlCL = new URLClassLoader(clInfo.urls, null);
        String className = HTTPClassLoaderServerTest.class.getName();
        Class clazz = urlCL.loadClass(className);
        assertEquals(className, clazz.getName());
    }
    
    public void testNOK() throws Exception {
        URLClassLoader urlCL = new URLClassLoader(clInfo.urls, null);
        try {
            urlCL.loadClass("Test");
            fail();
        } catch (ClassNotFoundException e) {
        }
    }
    
    private void start(GBeanData instance) throws Exception {
        kernel.loadGBean(instance, cl);
        kernel.startGBean(instance.getName());
    }

    private void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    protected void setUp() throws Exception {
        containerName = new ObjectName("geronimo.jetty:role=Container");
        Set containerPatterns = new HashSet();
        containerPatterns.add(containerName);
        connectorName = new ObjectName("geronimo.jetty:role=Connector");
        Set connectorPatterns = new HashSet();
        connectorPatterns.add(connectorName);
        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();
        GBeanData container = new GBeanData(containerName, JettyContainerImpl.GBEAN_INFO);
        start(container);
        
        int port = 5678;
        GBeanData connector = new GBeanData(connectorName, HTTPConnector.GBEAN_INFO);
        connector.setAttribute("port", new Integer(port));
        connector.setReferencePatterns("JettyContainer", containerPatterns);
        start(connector);
        
        classLoaderServerName = new ObjectName("geronimo.jetty:role=ClassLoaderServer");
        GBeanData classLoaderServer = new GBeanData(classLoaderServerName, HTTPClassLoaderServer.GBEAN_INFO);
        classLoaderServer.setReferencePatterns("JettyContainer", containerPatterns);
        classLoaderServer.setReferencePatterns("JettyConnector", connectorPatterns);
        start(classLoaderServer);
        
        clInfo = new MockClassLoaderInfo(new URI("test"), getClass().getClassLoader());
        kernel.invoke(classLoaderServerName, "export", new Object[] {clInfo}, new String[] {ClassLoaderInfo.class.getName()});
    }

    protected void tearDown() throws Exception {
        stop(containerName);
        stop(connectorName);
        stop(classLoaderServerName);
        kernel.shutdown();
    }
    
    private static class MockClassLoaderInfo extends ClassLoader implements ClassLoaderInfo {
        private final Object id;
        private final ClassLoader delegate;
        private URL[] urls;
        
        private MockClassLoaderInfo(Object id, ClassLoader delegate) {
            this.id = id;
            this.delegate = delegate;
        }
        
        public InputStream getResourceAsStream(String name) {
            return delegate.getResourceAsStream(name);
        }
        
        public ClassLoader getClassLoader() {
            return this;
        }

        public Object getID() {
            return id;
        }

        public void setClassLoaderServerURLs(URL[] urls) {
            this.urls = urls;
        }
    }
}

