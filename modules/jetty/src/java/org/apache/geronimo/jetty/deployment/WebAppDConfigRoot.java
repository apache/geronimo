/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.jetty.deployment;

import java.io.InputStream;
import java.io.IOException;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanRootSupport;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;

/**
 *
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/25 09:57:44 $
 */
public class WebAppDConfigRoot extends DConfigBeanRootSupport {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(JettyWebAppDocument.class.getClassLoader())
    });

    private static String[] XPATHS = {
        "web-app"
    };

    private WebAppDConfigBean webAppBean;

    public WebAppDConfigRoot(DDBeanRoot ddBean) {
        super(ddBean, JettyWebAppDocument.Factory.newInstance());
        JettyWebAppType webApp = getWebAppDocument().addNewWebApp();
        replaceWebAppDConfigBean(webApp);
    }

    private void replaceWebAppDConfigBean(JettyWebAppType webApp) {
        DDBean ddBean = getDDBean();
        webAppBean = new WebAppDConfigBean(ddBean.getChildBean(XPATHS[0])[0], webApp);
    }

    JettyWebAppDocument getWebAppDocument() {
        return (JettyWebAppDocument)getXmlObject();
    }

    public String[] getXpaths() {
        return XPATHS;
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        if (XPATHS[0].equals(bean.getXpath())) {
            return webAppBean;
        }
        return null;
    }

    public void fromXML(InputStream inputStream) throws XmlException, IOException {
        super.fromXML(inputStream);
        replaceWebAppDConfigBean(getWebAppDocument().getWebApp());
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }


}
