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

import java.util.Arrays;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;

import org.apache.geronimo.naming.deployment.EJBRefDConfigBean;
import org.apache.geronimo.naming.deployment.EJBLocalRefDConfigBean;
import org.apache.geronimo.deployment.tools.loader.WebDeployable;

/**
 *
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/20 07:19:13 $
 */
public class WebAppDConfigTest extends DeployerTestCase {
    private DeploymentConfiguration config;
    private WebDeployable deployable;
    private DDBeanRoot ddBeanRoot;
    private WebAppDConfigRoot configRoot;

    public void testWebAppRoot() throws Exception {
        assertNotNull(configRoot);
        assertTrue(Arrays.equals(new String[]{"web-app"}, configRoot.getXpaths()));
        assertNotNull(configRoot.getDConfigBean(ddBeanRoot.getChildBean("web-app")[0]));
        assertNull(configRoot.getDConfigBean(ddBeanRoot.getChildBean("web-app/description")[0]));
    }

    public void testWebApp() throws Exception {
        DDBean ddBean = ddBeanRoot.getChildBean("web-app")[0];
        WebAppDConfigBean webApp = (WebAppDConfigBean) configRoot.getDConfigBean(ddBean);
        assertNotNull(webApp);
/*
        String[] xpaths = webApp.getXpaths();
        assertTrue(Arrays.equals(
                new String[]{
                    "ejb-ref",
                    "ejb-local-ref",
                    "message-destination-ref",
                    "resource-env-ref",
                    "resource-ref",
                },
                xpaths)
        );
*/
    }

/*
    public void testEJBRef() throws Exception {
        DDBean ddBean = ddBeanRoot.getChildBean("web-app")[0];
        WebAppDConfigBean webApp = (WebAppDConfigBean) configRoot.getDConfigBean(ddBean);
        DDBean[] ddBeans = ddBean.getChildBean(webApp.getXpaths()[0]);
        assertEquals(2, ddBeans.length);
        assertEquals("fake-ejb-ref", ddBeans[0].getChildBean("ejb-ref-name")[0].getText());
        assertEquals("another-ejb-ref", ddBeans[1].getChildBean("ejb-ref-name")[0].getText());

        EJBRefDConfigBean ejbRef0 = (EJBRefDConfigBean) webApp.getDConfigBean(ddBeans[0]);
        EJBRefDConfigBean ejbRef1 = (EJBRefDConfigBean) webApp.getDConfigBean(ddBeans[1]);
        assertNotNull(ejbRef0);
        assertEquals(ddBeans[0], ejbRef0.getDDBean());
        assertNotNull(ejbRef1);
        assertEquals(ddBeans[1], ejbRef1.getDDBean());
        assertTrue(ejbRef0 != ejbRef1);
    }
*/

/*
    public void testEJBLocalRef() throws Exception {
        DDBean ddBean = ddBeanRoot.getChildBean("web-app")[0];
        WebAppDConfigBean webApp = (WebAppDConfigBean) configRoot.getDConfigBean(ddBean);
        DDBean[] ddBeans = ddBean.getChildBean(webApp.getXpaths()[1]);
        assertEquals(2, ddBeans.length);
        assertEquals("fake-ejb-local-ref", ddBeans[0].getChildBean("ejb-ref-name")[0].getText());
        assertEquals("another-ejb-local-ref", ddBeans[1].getChildBean("ejb-ref-name")[0].getText());

        EJBLocalRefDConfigBean ejbRef0 = (EJBLocalRefDConfigBean) webApp.getDConfigBean(ddBeans[0]);
        EJBLocalRefDConfigBean ejbRef1 = (EJBLocalRefDConfigBean) webApp.getDConfigBean(ddBeans[1]);
        assertNotNull(ejbRef0);
        assertEquals(ddBeans[0], ejbRef0.getDDBean());
        assertNotNull(ejbRef1);
        assertEquals(ddBeans[1], ejbRef1.getDDBean());
        assertTrue(ejbRef0 != ejbRef1);
    }
*/

    protected void setUp() throws Exception {
        super.setUp();
        deployable = new WebDeployable(classLoader.getResource("deployables/war1/"));
        config = manager.createConfiguration(deployable);

        ddBeanRoot = deployable.getDDBeanRoot();
        configRoot = (WebAppDConfigRoot) config.getDConfigBeanRoot(ddBeanRoot);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
