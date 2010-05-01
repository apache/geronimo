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

package org.apache.geronimo.web.deployment;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;

import junit.framework.TestCase;

import org.apache.geronimo.deployment.tools.loader.WebDeployable;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class WebAppDConfigTest extends TestCase {
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
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL warDir = classLoader.getResource("deployables/war1/");
        ClassLoader cl = new URLClassLoader(new URL[] {warDir});
        Bundle bundle = new MockBundle(cl, warDir.toString(), 0L);
        deployable = new WebDeployable(bundle);
        config = new WARConfiguration(deployable);

        ddBeanRoot = deployable.getDDBeanRoot();
        configRoot = (WebAppDConfigRoot) config.getDConfigBeanRoot(ddBeanRoot);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
