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
package org.apache.geronimo.connector.deployment.jsr88;

import java.io.InputStream;
import java.io.IOException;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.geronimo.deployment.plugin.DConfigBeanRootSupport;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 * Represents "/" in a Geronimo Connector deployment plan (geronimo-ra.xml).
 * The only function here is to navigate to an appropriate "Connector"
 * DConfigBean.
 *
 * @version $Rev$ $Date$
 */
public class Connector15DCBRoot extends DConfigBeanRootSupport {
    // This may be overcomplicated -- if we don't refer to J2EE types in our schemas
    // then we should only need to use the GerConnectorDocument loader
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(GerConnectorDocument.class.getClassLoader())
    });

    private ConnectorDCB connector;

    public Connector15DCBRoot(DDBeanRoot ddBean) {
        super(ddBean, null);
        setXmlObject(loadDefaultData(ddBean));
    }

    private XmlObject loadDefaultData(DDBeanRoot root) {
        InputStream in = root.getDeployableObject().getEntry("META-INF/geronimo-ra.xml");
        if(in == null) {
            GerConnectorDocument doc = GerConnectorDocument.Factory.newInstance();
            DDBean[] list = root.getChildBean("connector");
            if(list.length > 0) {
                connector = new ConnectorDCB(list[0], doc.addNewConnector());
            }
            return doc;
        } else {
            try {
                GerConnectorDocument result =  GerConnectorDocument.Factory.parse(in);
                in.close();
                DDBean[] list = root.getChildBean("connector");
                if(list.length > 0) {
                    connector = new ConnectorDCB(list[0], result.getConnector());
                }
                return result;
            } catch (XmlException e) {
                throw new RuntimeException("Unable to load default Geronimo RA data", e);
            } catch (IOException e) {
                throw new RuntimeException("Unable to load default Geronimo RA data", e);
            }
        }
    }

    GerConnectorDocument getConnectorDocument() {
        return (GerConnectorDocument) getXmlObject();
    }

    public String[] getXpaths() {
        return getXPathsForJ2ee_1_4(new String[][]{{"connector",},});
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        if (getXpaths()[0].equals(bean.getXpath())) { // "connector"
            return connector;
        } else {
            throw new ConfigurationException("No DConfigBean matching DDBean "+bean.getXpath());
        }
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }
}
