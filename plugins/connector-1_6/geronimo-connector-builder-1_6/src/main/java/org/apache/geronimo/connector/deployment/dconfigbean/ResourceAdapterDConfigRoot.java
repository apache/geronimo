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

package org.apache.geronimo.connector.deployment.dconfigbean;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.dconfigbean.DConfigBeanRootSupport;
import org.apache.geronimo.xbeans.connector.GerConnectorDocument;
import org.apache.geronimo.xbeans.connector.GerResourceadapterType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ResourceAdapterDConfigRoot extends DConfigBeanRootSupport {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {
//        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.javaee6.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(GerConnectorDocument.class.getClassLoader())
    });

    private static String[][] XPATHS = {
        {"connector", "resourceadapter"}
    };

    private ResourceAdapterDConfigBean resourceAdapterDConfigBean;

    public ResourceAdapterDConfigRoot(DDBeanRoot ddBean) {
        super(ddBean, loadDefaultData(ddBean));
        replaceResourceAdapterDConfigBean(getConnectorDocument().getConnector().getResourceadapterArray()[0]);
    }

    private static XmlObject loadDefaultData(DDBeanRoot root) {
        InputStream in = root.getDeployableObject().getEntry("META-INF/geronimo-ra.xml");
        if(in == null) {
            GerConnectorDocument doc = GerConnectorDocument.Factory.newInstance();
            doc.addNewConnector().addNewResourceadapter();
            return doc;
        } else {
            try {
                XmlObject result =  GerConnectorDocument.Factory.parse(in);
                in.close();
                return result;
            } catch (XmlException e) {
                throw new RuntimeException("Unable to load default Geronimo RA data", e);
            } catch (IOException e) {
                throw new RuntimeException("Unable to load default Geronimo RA data", e);
            }
        }
    }

    private void replaceResourceAdapterDConfigBean(GerResourceadapterType resourceAdapter) {
        DDBean ddBean = getDDBean();
        String path = getXpaths()[0];
//        System.out.println("********** Searching XPath "+path+" -- "+ddBean.getChildBean(path));
        DDBean childDDBean = ddBean.getChildBean(path)[0];
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
        //TODO this is so totally wrong...
        replaceResourceAdapterDConfigBean(getConnectorDocument().getConnector().getResourceadapterArray()[0]);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }
}
