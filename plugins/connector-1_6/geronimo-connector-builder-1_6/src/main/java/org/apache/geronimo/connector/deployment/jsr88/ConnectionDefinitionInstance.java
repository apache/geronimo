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

import javax.enterprise.deploy.model.DDBean;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.geronimo.naming.deployment.jsr88.GBeanLocator;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * Represents connection-definition/connectiondefinition-instance in the
 * Geronimo Connector deployment plan.
 *
 * @version $Rev$ $Date$
 */
public class ConnectionDefinitionInstance extends ConfigHolder {
    private DDBean connectionDefinition;
    private ConnectionManager manager;

    public ConnectionDefinitionInstance() {
    }

    public ConnectionDefinitionInstance(DDBean connectionDefinition, GerConnectiondefinitionInstanceType instance) {
        configure(connectionDefinition, instance);
    }

    protected GerConnectiondefinitionInstanceType getConnectionInstance() {
        return (GerConnectiondefinitionInstanceType) getXmlObject();
    }

    public void reconfigure() {
        configure(connectionDefinition, getConnectionInstance());
    }

    void configure(DDBean connectionDefinition, GerConnectiondefinitionInstanceType definition) {
        this.connectionDefinition = connectionDefinition;
        super.configure(connectionDefinition, definition);
        if(connectionDefinition != null) {
            DDBean parent = connectionDefinition.getChildBean("..")[0];
            ConnectionManager oldMgr = manager;
            if(oldMgr == null) {
                if(definition.getConnectionmanager() != null) {
                    manager = new ConnectionManager(parent, definition.getConnectionmanager());
                } else {
                    manager = new ConnectionManager(parent, definition.addNewConnectionmanager());
                }
            } else {
                if(definition.getConnectionmanager() != null) {
                    manager.configure(parent, definition.getConnectionmanager());
                } else {
                    manager.configure(parent, definition.addNewConnectionmanager());
                }
            }
            pcs.firePropertyChange("connectionManager", oldMgr, manager);
        }
    }

    DDBean getDDBean() {
        return connectionDefinition;
    }

    protected GerConfigPropertySettingType createConfigProperty() {
        return getConnectionInstance().addNewConfigPropertySetting();
    }

    protected GerConfigPropertySettingType[] getConfigProperties() {
        return getConnectionInstance().getConfigPropertySettingArray();
    }

    protected void removeConfigProperty(int index) {
        getConnectionInstance().removeConfigPropertySetting(index);
    }

    // ----------------------- JavaBean Properties for /connectiondefinition-instance ----------------------

    public String getName() {
        return getConnectionInstance().getName();
    }

    public void setName(String name) {
        String old = getName();
        getConnectionInstance().setName(name);
        pcs.firePropertyChange("name", old, name);
    }

    public String[] getImplementedInterface() {
        return getConnectionInstance().getImplementedInterfaceArray();
    }

    public String getImplementedInterface(int index) {
        return getConnectionInstance().getImplementedInterfaceArray(index);
    }

    public void setImplementedInterface(String[] list) {
        String[] old = getImplementedInterface();
        getConnectionInstance().setImplementedInterfaceArray(list);
        pcs.firePropertyChange("implementedInterface", old, list);
    }

    public void setImplementedInterface(int index, String iface) {
        String[] old = getImplementedInterface();
        getConnectionInstance().setImplementedInterfaceArray(index, iface);
        pcs.firePropertyChange("implementedInterface", old, getImplementedInterface());
    }

    public ConnectionManager getConnectionManager() {
        return manager;
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
