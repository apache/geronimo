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

package org.apache.geronimo.connector.deployment.dconfigbean;

import java.io.InputStream;
import java.io.IOException;

import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanRootSupport;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerVersionType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;

/**
 *
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/25 09:57:09 $
 *
 * */
public class ResourceAdapterDConfigRoot extends DConfigBeanRootSupport {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(GerConnectorDocument.class.getClassLoader())
    });

    private static String[][] XPATHS = {
        {"connector", "resourceadapter"}
    };

    private ResourceAdapterDConfigBean resourceAdapterDConfigBean;

    public ResourceAdapterDConfigRoot(DDBeanRoot ddBean) {
        super(ddBean, GerConnectorDocument.Factory.newInstance());
        GerResourceadapterType resourceAdapter = getConnectorDocument().addNewConnector().addNewResourceadapter();
        getConnectorDocument().getConnector().setVersion(GerVersionType.X_1_5);
        replaceResourceAdapterDConfigBean(resourceAdapter);
    }

    private void replaceResourceAdapterDConfigBean(GerResourceadapterType resourceAdapter) {
        DDBean ddBean = getDDBean();
        DDBean childDDBean = ddBean.getChildBean(getXpaths()[0])[0];
        resourceAdapterDConfigBean = new ResourceAdapterDConfigBean(childDDBean, resourceAdapter);
    }

    GerConnectorDocument getConnectorDocument() {
        return (GerConnectorDocument) getXmlObject();
    }

    public String[] getXpaths() {
        return getXPathsForJ2ee_1_4(XPATHS);
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        if (getXpaths()[0].equals(bean.getXpath())) {
            return resourceAdapterDConfigBean;
        }
        return null;
    }

    public void fromXML(InputStream inputStream) throws XmlException, IOException {
        super.fromXML(inputStream);
        if (!getConnectorDocument().getConnector().getVersion().equals(GerVersionType.X_1_5)) {
            throw new IllegalStateException("Wrong version, expected 1.5");
        }
        replaceResourceAdapterDConfigBean(getConnectorDocument().getConnector().getResourceadapter());
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }
}
