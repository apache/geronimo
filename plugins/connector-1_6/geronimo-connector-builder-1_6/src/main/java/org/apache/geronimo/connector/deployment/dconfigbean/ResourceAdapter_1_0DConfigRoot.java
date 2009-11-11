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
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.connector.GerConnectorDocument;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlException;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ResourceAdapter_1_0DConfigRoot extends DConfigBeanRootSupport {

    private static String[] XPATHS = {
        "connector/resourceadapter"
    };

    private ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean;

    public ResourceAdapter_1_0DConfigRoot(DDBeanRoot ddBean) {
        super(ddBean, GerConnectorDocument.Factory.newInstance());
        GerConnectionDefinitionType connectionDefinition = getConnectorDocument().addNewConnector().addNewResourceadapter().addNewOutboundResourceadapter().addNewConnectionDefinition();
        replaceConnectionDefinitionDConfigBean(connectionDefinition);
    }

    private void replaceConnectionDefinitionDConfigBean(GerConnectionDefinitionType connectionDefinition) {
        DDBean ddBean = getDDBean();
        DDBean childDDBean = ddBean.getChildBean(getXpaths()[0])[0];
        connectionDefinitionDConfigBean = new ConnectionDefinitionDConfigBean(childDDBean, connectionDefinition);
    }

    GerConnectorDocument getConnectorDocument() {
        return (GerConnectorDocument) getXmlObject();
    }

    public String[] getXpaths() {
        return XPATHS;
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        if (getXpaths()[0].equals(bean.getXpath())) {
            return connectionDefinitionDConfigBean;
        }
        return null;
    }

    public void fromXML(InputStream inputStream) throws XmlException, IOException {
        super.fromXML(inputStream);
        //TODO this is so totally wrong...
        replaceConnectionDefinitionDConfigBean(getConnectorDocument().getConnector().getResourceadapterArray()[0].getOutboundResourceadapter().getConnectionDefinitionArray(0));
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ResourceAdapterDConfigRoot.SCHEMA_TYPE_LOADER;
    }
}
