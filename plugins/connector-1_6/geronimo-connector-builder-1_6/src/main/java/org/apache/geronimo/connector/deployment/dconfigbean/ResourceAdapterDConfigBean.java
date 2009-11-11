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

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.dconfigbean.DConfigBeanSupport;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.connector.GerOutboundResourceadapterType;
import org.apache.geronimo.xbeans.connector.GerResourceadapterInstanceType;
import org.apache.geronimo.xbeans.connector.GerResourceadapterType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ResourceAdapterDConfigBean extends DConfigBeanSupport {

    private final static String[][] RESOURCE_ADAPTER_XPATHS = {
        {"config-property"},
        {"outbound-resourceadapter", "connection-definition"},
        {"adminobject"}};
    private Map configPropertiesMap = new HashMap();
    private Map connectionDefinitionsMap = new HashMap();
    private Map adminObjectsMap = new HashMap();

    public ResourceAdapterDConfigBean(DDBean ddBean, final GerResourceadapterType resourceadapter) {
        super(ddBean, resourceadapter);
        if (getResourceadapterInstance() == null) {
            resourceadapter.addNewResourceadapterInstance();
        }
        ConfigPropertiesHelper.initializeConfigSettings(ddBean, new ConfigPropertiesHelper.ConfigPropertiesSource() {
            public GerConfigPropertySettingType[] getConfigPropertySettingArray() {
                return getResourceadapterInstance().getConfigPropertySettingArray();
            }

            public GerConfigPropertySettingType addNewConfigPropertySetting() {
                return getResourceadapterInstance().addNewConfigPropertySetting();
            }

            public void removeConfigPropertySetting(int j) {
            }

            public ConfigPropertySettings[] getConfigPropertySettings() {
                return new ConfigPropertySettings[0];
            }

            public void setConfigPropertySettings(ConfigPropertySettings[] configs) {
            }

        }, configPropertiesMap, "config-property", "config-property-name");
        //initialize connection definitions
        GerOutboundResourceadapterType outboundResourceadapter = resourceadapter.getOutboundResourceadapter();
        if (outboundResourceadapter == null) {
            outboundResourceadapter = resourceadapter.addNewOutboundResourceadapter();
        }
        DDBean[] connectionDefinitionDDBeans = ddBean.getChildBean(getXpaths()[1]);
        GerConnectionDefinitionType[] connectionDefinitions = outboundResourceadapter.getConnectionDefinitionArray();

        if (connectionDefinitions.length == 0) {
            //we are new
            for (int i = 0; i < connectionDefinitionDDBeans.length; i++) {
                DDBean connectionDefinitionDdBean = connectionDefinitionDDBeans[i];
                GerConnectionDefinitionType connectionDefinition = outboundResourceadapter.addNewConnectionDefinition();
                String connectionfactoryInterface = connectionDefinitionDdBean.getText("connectionfactory-interface")[0];
                ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean = new ConnectionDefinitionDConfigBean(connectionDefinitionDdBean, connectionDefinition);
                connectionDefinitionsMap.put(connectionfactoryInterface, connectionDefinitionDConfigBean);
            }
        } else {
            //we are read in from xml.  Check correct length
            assert connectionDefinitionDDBeans.length == connectionDefinitions.length;
            for (int i = 0; i < connectionDefinitionDDBeans.length; i++) {
                DDBean connectionDefinitionDdBean = connectionDefinitionDDBeans[i];
                GerConnectionDefinitionType connectionDefinition = connectionDefinitions[i];
                String connectionfactoryInterface = connectionDefinitionDdBean.getText("connectionfactory-interface")[0];
                assert connectionfactoryInterface.equals(connectionDefinition.getConnectionfactoryInterface());
                ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean = new ConnectionDefinitionDConfigBean(connectionDefinitionDdBean, connectionDefinition);
                connectionDefinitionsMap.put(connectionfactoryInterface, connectionDefinitionDConfigBean);
            }
        }

        //admin objects
//        DDBean[] adminObjecDdBeans = ddBean.getChildBean(getXpaths()[2]);
//        GerAdminobjectType[] adminobjectTypes = getResourceadapter().getAdminobjectArray();
//
//        if (adminobjectTypes.length == 0) {
//            //we are new
//            for (int i = 0; i < adminObjecDdBeans.length; i++) {
//                DDBean adminObjectDdBean = adminObjecDdBeans[i];
//                GerAdminobjectType adminobjectType = getResourceadapter().addNewAdminobject();
//                String adminObjectInterface = adminObjectDdBean.getText("adminobject-interface")[0];
//                String adminObjectClass = adminObjectDdBean.getText("adminobject-class")[0];
//                AdminObjectDConfigBean adminObjectDConfigBean = new AdminObjectDConfigBean(adminObjectDdBean, adminobjectType);
//                adminObjectsMap.put(new Key(adminObjectInterface, adminObjectClass), adminObjectDConfigBean);
//            }
//        } else {
//            //we are read in from xml.  Check correct length
//            assert adminObjecDdBeans.length == adminobjectTypes.length;
//            for (int i = 0; i < adminObjecDdBeans.length; i++) {
//                DDBean adminObjectDdBean = adminObjecDdBeans[i];
//                GerAdminobjectType adminobjectType = adminobjectTypes[i];
//                String adminObjectInterface = adminObjectDdBean.getText("adminobject-interface")[0];
//                assert(adminObjectInterface.equals(adminobjectType.getAdminobjectInterface().getStringValue()));
//                String adminObjectClass = adminObjectDdBean.getText("adminobject-class")[0];
//                assert(adminObjectClass.equals(adminobjectType.getAdminobjectClass().getStringValue()));
//                AdminObjectDConfigBean adminObjectDConfigBean = new AdminObjectDConfigBean(adminObjectDdBean, adminobjectType);
//                adminObjectsMap.put(new Key(adminObjectInterface, adminObjectClass), adminObjectDConfigBean);
//
//            }
//        }

    }

    GerResourceadapterType getResourceadapter() {
        return (GerResourceadapterType) getXmlObject();
    }

    private GerResourceadapterInstanceType getResourceadapterInstance() {
        return getResourceadapter().getResourceadapterInstance();
    }

    public String getResourceAdapterName() {
        return getResourceadapterInstance().getResourceadapterName();
    }

    public void setResourceAdapterName(String resourceAdapterName) {
        getResourceadapterInstance().setResourceadapterName(resourceAdapterName);
    }

    public String getWorkManager() {
        if(getResourceadapterInstance() == null || getResourceadapterInstance().getWorkmanager() == null) {
            return null;
        }
        return getResourceadapterInstance().getWorkmanager().getGbeanLink();
    }

    public void setWorkManager(String workManager) {
        if(getResourceadapterInstance() == null) {
            getResourceadapter().addNewResourceadapterInstance();
        }
        if(getResourceadapterInstance().getWorkmanager() == null) {
            getResourceadapterInstance().addNewWorkmanager();
        }
        getResourceadapterInstance().getWorkmanager().setGbeanLink(workManager);
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        String xpath = bean.getXpath();
        String[] xpaths = getXpaths();
        if (xpath.equals(xpaths[0])) {
            //resource adapter config property
            String configPropertyName = bean.getText("config-property-name")[0];
            ConfigPropertySettingDConfigBean configPropertySetting = (ConfigPropertySettingDConfigBean) configPropertiesMap.get(configPropertyName);
            assert configPropertySetting != null;
            return configPropertySetting;
        }
        if (xpath.equals(xpaths[1])) {
            //connection definition
            String connectionFactoryInterface = bean.getText("connectionfactory-interface")[0];
            ConnectionDefinitionDConfigBean connectionDefinition = (ConnectionDefinitionDConfigBean) connectionDefinitionsMap.get(connectionFactoryInterface);
            assert connectionDefinition != null;
            return connectionDefinition;
        }
        if (xpath.equals(xpaths[2])) {
            //admin objects
            String adminObjectInterface = bean.getText("adminobject-interface")[0];
            String adminObjectClass = bean.getText("adminobject-class")[0];
            AdminObjectDConfigBean adminObject = (AdminObjectDConfigBean) adminObjectsMap.get(new Key(adminObjectInterface, adminObjectClass));
            assert adminObject != null;
            return adminObject;
        }
        return null;
    }


    public String[] getXpaths() {
        return getXPathsForJ2ee_1_4(RESOURCE_ADAPTER_XPATHS);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ResourceAdapterDConfigRoot.SCHEMA_TYPE_LOADER;
    }


    //from doubleKeyedHashMap, currently in transaction module
    private final static class Key {
        private final Object part1;
        private final Object part2;

        public Key(Object part1, Object part2) {
            this.part1 = part1;
            this.part2 = part2;
        }

        public int hashCode() {
            return part1.hashCode() ^ part2.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                Key other = (Key) obj;
                return this.part1.equals(other.part1) && this.part2.equals(other.part2);
            } else {
                return false;
            }
        }
    }
}
