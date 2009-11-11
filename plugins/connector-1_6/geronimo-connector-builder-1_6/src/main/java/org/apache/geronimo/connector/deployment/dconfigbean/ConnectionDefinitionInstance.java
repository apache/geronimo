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

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathListener;

import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.connector.GerConnectionmanagerType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 * @version $Revision 1.0$  $Date$
 */
public class ConnectionDefinitionInstance extends XmlBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();
    private ConfigPropertySettings[] configs = new ConfigPropertySettings[0];
    private ConnectionDefinitionDConfigBean parent;
    private XpathListener configListener;

    public ConnectionDefinitionInstance() {
        super(null);
    }

    void initialize(GerConnectiondefinitionInstanceType xmlObject, ConnectionDefinitionDConfigBean parent) {
        setXmlObject(xmlObject);
        this.parent = parent;
        DDBean parentDDBean = parent.getDDBean();
        configListener = ConfigPropertiesHelper.initialize(parentDDBean, new ConfigPropertiesHelper.ConfigPropertiesSource() {
            public GerConfigPropertySettingType[] getConfigPropertySettingArray() {
                return getConnectiondefinitionInstance().getConfigPropertySettingArray();
            }

            public GerConfigPropertySettingType addNewConfigPropertySetting() {
                return getConnectiondefinitionInstance().addNewConfigPropertySetting();
            }

            public void removeConfigPropertySetting(int j) {
                getConnectiondefinitionInstance().removeConfigPropertySetting(j);
            }

            public ConfigPropertySettings[] getConfigPropertySettings() {
                return configs;
            }

            public void setConfigPropertySettings(ConfigPropertySettings[] configs) {
                setConfigProperty(configs);
            }

        }, "config-property", "config-property-name");
    }


    boolean hasParent() {
        return parent != null;
    }

    void dispose() {
        if (configs != null) {
            for (int i = 0; i < configs.length; i++) {
                configs[i].dispose();
            }
        }
        if (parent != null) {
            parent.getDDBean().removeXpathListener("config-property", configListener);
        }
        configs = null;
        configListener = null;
        parent = null;
    }

// JavaBean properties for this object (with a couple helper methods)

    GerConnectiondefinitionInstanceType getConnectiondefinitionInstance() {
        return (GerConnectiondefinitionInstanceType) getXmlObject();
    }

    GerConnectionmanagerType getConnectionManager() {
        return getConnectiondefinitionInstance().getConnectionmanager();
    }

    public ConfigPropertySettings[] getConfigProperty() {
        return configs;
    }

    private void setConfigProperty(ConfigPropertySettings[] configs) { // can only be changed by adding a new DDBean
        ConfigPropertySettings[] old = getConfigProperty();
        this.configs = configs;
        pcs.firePropertyChange("configProperty", old, configs);
    }

    public String getName() {
        return getConnectiondefinitionInstance().getName();
    }

    public void setName(String name) {
        String old = getName();
        getConnectiondefinitionInstance().setName(name);
        pcs.firePropertyChange("name", old, name);
    }

//    public String getGlobalJNDIName() {
//        return getConnectiondefinitionInstance().getGlobalJndiName();
//    }
//
//    public void setGlobalJNDIName(String globalJNDIName) {
//        String old = getGlobalJNDIName();
//        getConnectiondefinitionInstance().setGlobalJndiName(globalJNDIName);
//        pcs.firePropertyChange("globalJNDIName", old, globalJNDIName);
//    }

//    public boolean isUseConnectionRequestInfo() {
//        return getConnectionManager().getUseConnectionRequestInfo();
//    }
//
//    public void setUseConnectionRequestInfo(boolean useConnectionRequestInfo) {
//        boolean old = isUseConnectionRequestInfo();
//        getConnectionManager().setUseConnectionRequestInfo(useConnectionRequestInfo);
//        pcs.firePropertyChange("useConnectionRequestInfo", old, useConnectionRequestInfo);
//    }
//
//    public boolean isUseSubject() {
//        return getConnectionManager().getUseSubject();
//    }
//
//    public void setUseSubject(boolean useSubject) {
//        boolean old = isUseSubject();
//        getConnectionManager().setUseSubject(useSubject);
//        pcs.firePropertyChange("useSubject", old, useSubject);
//    }
//
//    public boolean isUseTransactionCaching() {
//        return getConnectionManager().getUseTransactionCaching();
//    }
//
//    public void setUseTransactionCaching(boolean useTransactionCaching) {
//        boolean old = isUseTransactionCaching();
//        getConnectionManager().setUseTransactionCaching(useTransactionCaching);
//        pcs.firePropertyChange("useTransactionCaching", old, useTransactionCaching);
//    }
//
//    public boolean isUseLocalTransactions() {
//        return getConnectionManager().getUseLocalTransactions();
//    }
//
//    public void setUseLocalTransactions(boolean useLocalTransactions) {
//        boolean old = isUseLocalTransactions();
//        getConnectionManager().setUseLocalTransactions(useLocalTransactions);
//        pcs.firePropertyChange("useLocalTransactions", old, useLocalTransactions);
//    }
//
//    public boolean isUseTransactions() {
//        return getConnectionManager().getUseTransactions();
//    }
//
//    public void setUseTransactions(boolean useTransactions) {
//        boolean old = isUseTransactions();
//        getConnectionManager().setUseTransactions(useTransactions);
//        pcs.firePropertyChange("useTransactions", old, useTransactions);
//    }
//
//    public int getMaxSize() {
//        BigInteger test = getConnectionManager().getMaxSize();
//        return test == null ? 0 : test.intValue();
//    }
//
//    public void setMaxSize(int maxSize) {
//        int old = getMaxSize();
//        getConnectionManager().setMaxSize(BigInteger.valueOf(maxSize));
//        pcs.firePropertyChange("maxSize", old, maxSize);
//    }
//
//    public int getBlockingTimeout() {
//        BigInteger test = getConnectionManager().getBlockingTimeout();
//        return test == null ? 0 : test.intValue();
//    }
//
//    public void setBlockingTimeout(int blockingTimeout) {
//        int old = getBlockingTimeout();
//        getConnectionManager().setBlockingTimeout(BigInteger.valueOf(blockingTimeout));
//        pcs.firePropertyChange("blockingTimeout", old, blockingTimeout);
//    }

    public boolean isContainerManagedSecurity() {
        return getConnectionManager().isSetContainerManagedSecurity();
    }

    public void setContainerManagedSecurity(boolean containerManagedSecurity) {
        boolean old = isContainerManagedSecurity();
        if (old && !containerManagedSecurity) {
            getConnectionManager().setContainerManagedSecurity(null);
        } else if (!old && containerManagedSecurity) {
            getConnectionManager().addNewContainerManagedSecurity();
        }
        pcs.firePropertyChange("containerManagedSecurity", old, containerManagedSecurity);
    }

    public String toString() {
        return "Connection "+getName();
    }
}
