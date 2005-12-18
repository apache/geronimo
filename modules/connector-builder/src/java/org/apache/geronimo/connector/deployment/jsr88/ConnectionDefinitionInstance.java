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

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.XpathEvent;
import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * Represents connection-definition/connectiondefinition-instance in the
 * Geronimo Connector deployment plan.
 *
 * @version $Rev$ $Date$
 */
public class ConnectionDefinitionInstance extends XmlBeanSupport {
    final XpathListener xpathListener = new XpathListener() {
                    public void fireXpathEvent(XpathEvent event) {
                        if(event.isAddEvent()) {
                            //todo: add new config-property-setting, fire change event
                        } else if(event.isRemoveEvent()) {
                            //todo: remove config-property-setting, fire change event
                        } else if(event.isChangeEvent()) {
                            if(event.getChangeEvent().getPropertyName().equals("config-property-name")) {
                                String old = (String) event.getChangeEvent().getOldValue();
                                for (int i = 0; i < settings.length; i++) {
                                    ConfigPropertySetting setting = settings[i];
                                    if(setting.getName().equals(old)) {
                                        setting.setName((String) event.getChangeEvent().getNewValue());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                };
    private DDBean connectionDefinition;
    private ConfigPropertySetting[] settings = new ConfigPropertySetting[0];
    private ConnectionManager manager;

    public ConnectionDefinitionInstance() {
        super(null);
    }

    public ConnectionDefinitionInstance(DDBean connectionDefinition, GerConnectiondefinitionInstanceType instance) {
        super(null);
        configure(connectionDefinition, instance);
    }

    protected GerConnectiondefinitionInstanceType getConnectionInstance() {
        return (GerConnectiondefinitionInstanceType) getXmlObject();
    }

    void clearNullSettings() {
        List list = new ArrayList();
        for (int i = 0; i < settings.length; i++) {
            ConfigPropertySetting setting = settings[i];
            if(setting.getValue() != null) {
                list.add(setting);
            }
        }
        settings = (ConfigPropertySetting[]) list.toArray(new ConfigPropertySetting[list.size()]);
        GerConnectiondefinitionInstanceType instance = getConnectionInstance();
        for (int i = instance.getConfigPropertySettingArray().length-1; i>=0; --i) {
            GerConfigPropertySettingType type = instance.getConfigPropertySettingArray(i);
            if(type.isNil() || type.getStringValue() == null) {
                instance.removeConfigPropertySetting(i);
            }
        }
    }

    void reconfigure() {
        configure(connectionDefinition, getConnectionInstance());
    }

    void configure(DDBean connectionDefinition, GerConnectiondefinitionInstanceType definition) {
        ConfigPropertySetting[] old = null;
        if(this.connectionDefinition != null) {
            this.connectionDefinition.removeXpathListener("config-property", xpathListener);
            old = settings;
        }
        this.connectionDefinition = connectionDefinition;
        setXmlObject(definition);

        // Prepare the ConfigPropertySetting array
        List list = new ArrayList();
        DDBean[] all = connectionDefinition == null ? new DDBean[0] : connectionDefinition.getChildBean("config-property");
        Map byName = new HashMap();
        for (int i = 0; i < all.length; i++) {
            DDBean ddBean = all[i];
            byName.put(ddBean.getText("config-property-name")[0], ddBean);
        }
        GerConfigPropertySettingType[] previous = definition.getConfigPropertySettingArray();
        for (int i = 0; i < previous.length; i++) {
            GerConfigPropertySettingType setting = previous[i];
            DDBean ddBean = (DDBean) byName.remove(setting.getName());
            if(ddBean != null) {
                list.add(new ConfigPropertySetting(ddBean, setting, false));
            } else {
                System.out.println("Ignoring connectiondefinition-instance/config-setting "+setting.getName()+" (no matching config-property in J2EE DD)");
                //todo: delete it from the XMLBeans tree
            }
        }
        for (Iterator it = byName.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            DDBean bean = (DDBean) byName.get(name);
            list.add(new ConfigPropertySetting(bean, getConnectionInstance().addNewConfigPropertySetting(), true));
        }
        settings = (ConfigPropertySetting[]) list.toArray(new ConfigPropertySetting[list.size()]);
        if(old != null) {
            pcs.firePropertyChange("configPropertySetting", old, settings);
        }
        if(connectionDefinition != null) {
            connectionDefinition.addXpathListener("config-property", xpathListener);
        }
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

    public ConfigPropertySetting[] getConfigPropertySetting() {
        return settings;
    }

    public ConfigPropertySetting getConfigPropertySetting(int index) {
        return settings[index];
    }

    public ConnectionManager getConnectionManager() {
        return manager;
    }


    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
