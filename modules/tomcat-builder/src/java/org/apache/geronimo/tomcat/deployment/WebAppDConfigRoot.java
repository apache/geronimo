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

package org.apache.geronimo.tomcat.deployment;

import java.io.IOException;
import java.io.InputStream;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanRootSupport;
import org.apache.geronimo.xbeans.geronimo.tomcat.TomcatWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.tomcat.TomcatWebAppType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;

/**
 * @version $Rev: 56771 $ $Date: 2004-11-06 12:58:54 -0700 (Sat, 06 Nov 2004) $
 */
public class WebAppDConfigRoot extends DConfigBeanRootSupport {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(TomcatWebAppDocument.class.getClassLoader())
    });

    private static String[] XPATHS = {
        "web-app"
    };

    private WebAppDConfigBean webAppBean;

    public WebAppDConfigRoot(DDBeanRoot ddBean) {
        super(ddBean, TomcatWebAppDocument.Factory.newInstance());
        TomcatWebAppType webApp = getWebAppDocument().addNewWebApp();
        replaceWebAppDConfigBean(webApp);
    }

    private void replaceWebAppDConfigBean(TomcatWebAppType webApp) {
        DDBean ddBean = getDDBean();
        webAppBean = new WebAppDConfigBean(ddBean.getChildBean(XPATHS[0])[0], webApp);
    }

    TomcatWebAppDocument getWebAppDocument() {
        return (TomcatWebAppDocument) getXmlObject();
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
