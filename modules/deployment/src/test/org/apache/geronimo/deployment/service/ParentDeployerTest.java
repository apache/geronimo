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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.geronimo.deployment.URLDeployer;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/02/04 05:43:31 $
 */
public class ParentDeployerTest extends TestCase {
    private DocumentBuilder parser;
    private ServiceDeployer deployer;
    private File workDir;

    private Kernel kernel;
    private byte[] state;

    public void testParent() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        ObjectName parentName = new ObjectName("geronimo.test:name=Parent");
        GBeanMBean parentGBean = new GBeanMBean(Configuration.GBEAN_INFO);
        List parentPath = new ArrayList();
        parentPath.add(new URI(cl.getResource("services/").toString()));
        parentGBean.setAttribute("ClassPath", parentPath);
        parentGBean.setAttribute("GBeanState", state);
        kernel.load(parentGBean, null, parentName);
        kernel.getMBeanServer().invoke(parentName, "start", null, null);

        ConfigurationParent parent = (ConfigurationParent) MBeanProxyFactory.getProxy(ConfigurationParent.class, kernel.getMBeanServer(), parentName);
        ClassLoader parentCL = parent.getClassLoader();
        assertNotNull(parentCL.getResource("service1.xml"));
        assertNull(parentCL.getResource("test-resource.dat"));

        ObjectName childName = new ObjectName("geronimo.test:name=Child");
        URLDeployer batcher = new URLDeployer(parent, URI.create("test"), Collections.singletonList(deployer), workDir);
        batcher.addSource(new URLInfo(cl.getResource("services/service3/")));
        batcher.deploy();
        GBeanMBean childConfig = batcher.getConfiguration();
        childConfig.setReferencePatterns("Parent", Collections.singleton(new ObjectName("*:name=Parent")));
        kernel.load(childConfig, cl.getResource("services/"), childName);
        kernel.getMBeanServer().invoke(childName, "start", null, null);
        ClassLoader childCL = (ClassLoader) kernel.getMBeanServer().getAttribute(childName, "ClassLoader");
        assertNotNull(childCL.getResource("service1.xml")); // loaded by parent
        assertNotNull(childCL.getResource("test-resource.dat")); // loaded by child
    }

    protected void setUp() throws Exception {
        parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        deployer = new ServiceDeployer(parser);
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        workDir = new File(tmpDir, "test.car.work");
        workDir.mkdir();

        kernel = new Kernel("test.kernel", "geronimo");
        kernel.boot();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.close();
        state = baos.toByteArray();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
        FileUtil.recursiveDelete(workDir);
    }
}
