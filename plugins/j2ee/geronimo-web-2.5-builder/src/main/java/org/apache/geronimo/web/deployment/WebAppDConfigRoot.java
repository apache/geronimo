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

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.dconfigbean.DConfigBeanRootSupport;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;

/**
 * @version $Rev$ $Date$
 */
public class WebAppDConfigRoot extends DConfigBeanRootSupport {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{
        XmlBeans.typeLoaderForClassLoader(GerWebAppDocument.class.getClassLoader())
    });

    private static String[] XPATHS = {
        "web-app"
    };

    private WebAppDConfigBean webAppBean;

    public WebAppDConfigRoot(DDBeanRoot ddBean) {
        super(ddBean, GerWebAppDocument.Factory.newInstance());
        GerWebAppType webApp = getWebAppDocument().addNewWebApp();
        replaceWebAppDConfigBean(webApp);
    }

    private void replaceWebAppDConfigBean(GerWebAppType webApp) {
        DDBean ddBean = getDDBean();
        webAppBean = new WebAppDConfigBean(ddBean.getChildBean(XPATHS[0])[0], webApp);
    }

    GerWebAppDocument getWebAppDocument() {
        return (GerWebAppDocument) getXmlObject();
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
