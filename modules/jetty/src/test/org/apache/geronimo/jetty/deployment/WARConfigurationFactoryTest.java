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
package org.apache.geronimo.jetty.deployment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;

import org.apache.geronimo.deployment.tools.loader.WebDeployable;
import org.apache.geronimo.xbeans.geronimo.deployment.jetty.JettyContextRootType;
import org.apache.geronimo.xbeans.geronimo.deployment.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.deployment.jetty.JettyWebAppType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/06 08:55:49 $
 */
public class WARConfigurationFactoryTest extends DeployerTestCase {

    public void testFactory() throws Exception {
        WebDeployable deployable = new WebDeployable(classLoader.getResource("deployables/war1/"));
        DeploymentConfiguration config = manager.createConfiguration(deployable);
        assertEquals(deployable, config.getDeployableObject());
    }

    public void testConfig() throws Exception {
        WebDeployable deployable = new WebDeployable(classLoader.getResource("deployables/war1/"));
        WARConfiguration config = (WARConfiguration) warFactory.createConfiguration(deployable);
        assertNotNull(config);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        config.save(baos);
        byte[] bytes = baos.toByteArray();
        String output = new String(bytes);
        System.out.println(output);
        Document doc = parser.parse(new ByteArrayInputStream(baos.toByteArray()));
        Element root = doc.getDocumentElement();
        assertEquals("jet:web-app", root.getNodeName());
        //assertNull(XMLUtil.getChild(root, "jet:context-root"));
    }

    public void testConfigSet() throws Exception {
        WebDeployable deployable = new WebDeployable(classLoader.getResource("deployables/war1/"));
        WARConfiguration config = (WARConfiguration) warFactory.createConfiguration(deployable);
        DConfigBeanRoot configRoot = config.getDConfigBeanRoot(deployable.getDDBeanRoot());
        WebAppDConfigBean contextBean = (WebAppDConfigBean) configRoot.getDConfigBean(deployable.getChildBean("/web-app")[0]);
        contextBean.setContextRoot("/test");
        contextBean.setContextPriorityClassLoader(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        config.save(baos);
        byte[] bytes = baos.toByteArray();
        String output = new String(bytes);
        System.out.println(output);

        JettyWebAppDocument webAppDoc = JettyWebAppDocument.Factory.parse(new ByteArrayInputStream(baos.toByteArray()));
        JettyWebAppType webApp = webAppDoc.getWebApp();
        assertEquals("/test", webApp.getContextRoot().getStringValue());
        assertEquals(false, webApp.getContextPriorityClassloader());
    }

    public void testConfigSaveRestore() throws Exception {
        WebDeployable deployable = new WebDeployable(classLoader.getResource("deployables/war1/"));
        WARConfiguration config = (WARConfiguration) warFactory.createConfiguration(deployable);
        DConfigBeanRoot configRoot = config.getDConfigBeanRoot(deployable.getDDBeanRoot());
        WebAppDConfigBean contextBean = (WebAppDConfigBean) configRoot.getDConfigBean(deployable.getChildBean("/web-app")[0]);
        contextBean.setContextRoot("/test");
        contextBean.setContextPriorityClassLoader(true);
        checkContents(((WebAppDConfigRoot)configRoot).getWebAppDocument());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        config.save(baos);
        byte[] bytes = baos.toByteArray();
        String output = new String(bytes);
        System.out.println(output);

        config = (WARConfiguration) warFactory.createConfiguration(deployable);
        configRoot = config.getDConfigBeanRoot(deployable.getDDBeanRoot());
        contextBean = (WebAppDConfigBean) configRoot.getDConfigBean(deployable.getChildBean("/web-app")[0]);

        assertEquals("", contextBean.getContextRoot());

        config.restore(new ByteArrayInputStream(baos.toByteArray()));
        configRoot = config.getDConfigBeanRoot(deployable.getDDBeanRoot());
        checkContents(((WebAppDConfigRoot)configRoot).getWebAppDocument());


        contextBean = (WebAppDConfigBean) configRoot.getDConfigBean(deployable.getChildBean("/web-app")[0]);
        assertEquals("/test", contextBean.getContextRoot());
        assertEquals(true, contextBean.getContextPriorityClassLoader());
    }

    private void checkContents(JettyWebAppDocument webAppDoc) {
        JettyWebAppType webApp = webAppDoc.getWebApp();
        assertEquals("/test", webApp.getContextRoot().getStringValue());
        assertEquals(true, webApp.getContextPriorityClassloader());
    }

    public void testSanity() throws Exception {
        JettyWebAppDocument webAppDoc = JettyWebAppDocument.Factory.newInstance();
        JettyWebAppType webApp = webAppDoc.addNewWebApp();
        webAppDoc.setWebApp(webApp);
        JettyContextRootType contextRoot = webApp.addNewContextRoot();
        webApp.setContextRoot(contextRoot);
        contextRoot.setStringValue("/test");
        webApp.setContextPriorityClassloader(true);
        checkContents(webAppDoc);
        assertEquals("/test", webApp.getContextRoot().getStringValue());
        assertEquals("/test", contextRoot.getStringValue());
        assertEquals(true, webApp.getContextPriorityClassloader());
    }

    protected void setUp() throws Exception {
        super.setUp();
    }
}
