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
package org.apache.geronimo.deployment.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.JarFile;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.deployment.BatchDeployer;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/16 22:53:28 $
 */
public class ServiceDeployerTest extends TestCase {
    private static final URI MODULE_ID = URI.create("service");
    private static final Set serviceURIs = new HashSet();

    static {
        serviceURIs.add(URI.create("service/classes/test-resource.dat"));
    }

    private DocumentBuilder parser;
    private ServiceDeployer deployer;
    private File workDir;
    private BatchDeployer batcher;
    private File configFile;

    public void testResource() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL resource = cl.getResource("services/service1.xml");
        URLInfo urlInfo = new URLInfo(resource, URLType.RESOURCE);
        ServiceModule module = (ServiceModule) deployer.getModule(urlInfo, MODULE_ID);
        module.generateClassPath(new ConfigurationCallback() {
            public void addFile(URI path, InputStream is) throws IOException {
                fail();
            }

            public void addToClasspath(URI uri) {
                fail();
            }

            public void addGBean(ObjectName name, GBeanMBean gbean) {
                fail();
            }
        });
        module.defineGBeans(new ConfigurationCallback() {
            public void addFile(URI path, InputStream is) throws IOException {
                fail();
            }

            public void addToClasspath(URI uri) {
                fail();
            }

            public void addGBean(ObjectName name, GBeanMBean gbean) {
                try {
                    assertEquals(new ObjectName("geronimo.test:name=MyMockGMBean"), name);
                    assertEquals("1234", gbean.getAttribute("Value"));
                } catch (Exception e) {
                    fail();
                }
            }
        }, ClassLoader.getSystemClassLoader());
    }

    public void testPacked() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL resource = cl.getResource("services/service2.jar");
        URLInfo urlInfo = new URLInfo(resource, URLType.PACKED_ARCHIVE);

        ServiceModule module = (ServiceModule) deployer.getModule(urlInfo, MODULE_ID);
        final Set paths = new HashSet();
        module.generateClassPath(new ConfigurationCallback() {
            public void addFile(URI path, InputStream is) throws IOException {
                paths.add(path);
            }

            public void addToClasspath(URI uri) {
                assertEquals(URI.create("service/classes/"), uri);
            }

            public void addGBean(ObjectName name, GBeanMBean gbean) {
                fail();
            }
        });
        assertEquals(serviceURIs, paths);
        module.defineGBeans(new ConfigurationCallback() {
            public void addFile(URI path, InputStream is) throws IOException {
                fail();
            }

            public void addToClasspath(URI uri) {
                fail();
            }

            public void addGBean(ObjectName name, GBeanMBean gbean) {
                try {
                    assertEquals(new ObjectName("geronimo.test:name=MyMockGMBean"), name);
                    assertEquals("1234", gbean.getAttribute("Value"));
                } catch (Exception e) {
                    fail();
                }
            }
        }, ClassLoader.getSystemClassLoader());
    }

    public void testUnpacked() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL resource = cl.getResource("services/service3/");
        URLInfo urlInfo = new URLInfo(resource, URLType.UNPACKED_ARCHIVE);

        ServiceModule module = (ServiceModule) deployer.getModule(urlInfo, MODULE_ID);
        final Set paths = new HashSet();
        module.generateClassPath(new ConfigurationCallback() {
            public void addFile(URI path, InputStream is) throws IOException {
                paths.add(path);
            }

            public void addToClasspath(URI uri) {
                assertEquals(URI.create("service/classes/"), uri);
            }

            public void addGBean(ObjectName name, GBeanMBean gbean) {
                fail();
            }
        });
        assertEquals(serviceURIs, paths);
        module.defineGBeans(new ConfigurationCallback() {
            public void addFile(URI path, InputStream is) throws IOException {
                fail();
            }

            public void addToClasspath(URI uri) {
                fail();
            }

            public void addGBean(ObjectName name, GBeanMBean gbean) {
                try {
                    assertEquals(new ObjectName("geronimo.test:name=MyMockGMBean"), name);
                    assertEquals("1234", gbean.getAttribute("Value"));
                } catch (Exception e) {
                    fail();
                }
            }
        }, ClassLoader.getSystemClassLoader());
    }

    public void testBatchResource() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL resource = cl.getResource("services/service1.xml");
        URLInfo urlInfo = new URLInfo(resource, URLType.RESOURCE);
        batcher.addSource(urlInfo);
        batcher.deploy();
        JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(configFile)));
        batcher.saveConfiguration(jos);
        jos.close();
        JarFile jar = new JarFile(configFile);
        Set jarEntries = new HashSet();
        for (Enumeration e = jar.entries(); e.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            jarEntries.add(entry.getName());
        }
        Set result = new HashSet();
        result.add("META-INF/config.ser");
        assertEquals(result, jarEntries);
    }

    public void XtestBatchPacked() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL resource = cl.getResource("services/service2.jar");
        URLInfo urlInfo = new URLInfo(resource, URLType.PACKED_ARCHIVE);
        batcher.addSource(urlInfo);
        batcher.deploy();
        JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(configFile)));
        batcher.saveConfiguration(jos);
        jos.close();
        JarFile jar = new JarFile(configFile);
        Set jarEntries = new HashSet();
        for (Enumeration e = jar.entries(); e.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            jarEntries.add(entry.getName());
        }
        Set result = new HashSet();
        result.add("META-INF/config.ser");
        result.add("service2.jar/classes/test-resource.dat");
        assertEquals(result, jarEntries);
    }

    protected void setUp() throws Exception {
        parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        deployer = new ServiceDeployer(parser);
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        workDir = new File(tmpDir, "test.car.work");
        workDir.mkdir();
        configFile = new File(tmpDir, "test.car");
        batcher = new BatchDeployer(URI.create("test"), Collections.singletonList(deployer), workDir);
    }

    protected void tearDown() throws Exception {
        recursiveDelete(workDir);
        configFile.delete();
    }

    private static void recursiveDelete(File root) throws Exception {
        File[] files = root.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    recursiveDelete(file);
                } else {
                    file.delete();
                }
            }
        }
        root.delete();
    }
}
