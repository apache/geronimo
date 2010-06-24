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
package org.apache.geronimo.connector.deployment.jsr88;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.geronimo.deployment.dconfigbean.DConfigBeanRootSupport;
import org.apache.geronimo.xbeans.connector.GerConnectorDocument;
import org.apache.geronimo.xbeans.connector.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.connector.GerResourceadapterInstanceType;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlCursor;

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
//        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.javaee6.String.class.getClassLoader()),
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

    /**
     * When loaded, reset the cached "connector" child
     */
    public void fromXML(InputStream inputStream) throws XmlException, IOException {
        DDBean ddb = connector.getDDBean();
        super.fromXML(inputStream);
        if(getConnectorDocument().getConnector() != null) {
            connector = new ConnectorDCB(ddb, getConnectorDocument().getConnector());
        } else {
            connector = new ConnectorDCB(ddb, getConnectorDocument().addNewConnector());
        }
        //todo: fire some kind of notification for the DDBeans to catch?
    }

    /**
     * A little trickery -- on a save event, temporarily remove any config-property-setting
     * elements with a null value, and then immediately replace them again.  This is because
     * we don't want to write them out as null, but we also want to keep the objects in
     * sync 1:1 with the config params declared in the J2EE deployment descriptor.
     */
    public void toXML(OutputStream outputStream) throws IOException {
        List parents = new ArrayList();
        clearNulls(parents);
        try {
            super.toXML(outputStream);
        } finally {
            for (int i = 0; i < parents.size(); i++) {
                Object parent = parents.get(i);
                ConfigHolder instance = (ConfigHolder) parent;
                instance.reconfigure();
            }
        }
    }

    private void clearNulls(List parents) {
        ResourceAdapter[] adapters = connector.getResourceAdapter();
        for (int i = 0; i < adapters.length; i++) {
            ResourceAdapter adapter = adapters[i];
            if(adapter.getResourceAdapterInstance() != null) {
                parents.add(adapter.getResourceAdapterInstance());
                adapter.getResourceAdapterInstance().clearNullSettings();
            }
            ConnectionDefinition defs[] = adapter.getConnectionDefinition();
            for (int j = 0; j < defs.length; j++) {
                ConnectionDefinition def = defs[j];
                ConnectionDefinitionInstance instances[] = def.getConnectionInstances();
                for (int k = 0; k < instances.length; k++) {
                    ConnectionDefinitionInstance instance = instances[k];
                    parents.add(instance);
                    instance.clearNullSettings();
                }
            }
        }
        try {
            DDBean[] adminDDBs = connector.getDDBean().getChildBean(connector.getXpaths()[0]);
            if(adminDDBs == null) adminDDBs = new DDBean[0];
            for (int i = 0; i < adminDDBs.length; i++) {
                DDBean ddb = adminDDBs[i];
                AdminObjectDCB dcb = (AdminObjectDCB) connector.getDConfigBean(ddb);
                AdminObjectInstance[] instances = dcb.getAdminObjectInstance();
                for (int j = 0; j < instances.length; j++) {
                    AdminObjectInstance instance = instances[j];
                    parents.add(instance);
                    instance.clearNullSettings();
                }
            }
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }
}
