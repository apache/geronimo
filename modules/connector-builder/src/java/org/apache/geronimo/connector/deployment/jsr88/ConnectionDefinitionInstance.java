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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.XpathEvent;
import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
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

    void configure(DDBean connectionDefinition, GerConnectiondefinitionInstanceType definition) {
        if(this.connectionDefinition != null) {
            this.connectionDefinition.removeXpathListener("config-property", xpathListener);
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
                list.add(new ConfigPropertySetting(ddBean, setting));
            } else {
                System.out.println("Ignoring connectiondefinition-instance/config-setting "+setting.getName()+" (no matching config-property in J2EE DD)");
                //todo: delete it from the XMLBeans tree
            }
        }
        for (Iterator it = byName.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            DDBean bean = (DDBean) byName.get(name);
System.out.println("Creating DDBean "+name+" "+bean.getText("config-property-name")[0]);
            list.add(new ConfigPropertySetting(bean, getConnectionInstance().addNewConfigPropertySetting()));
        }
        settings = (ConfigPropertySetting[]) list.toArray(new ConfigPropertySetting[list.size()]);
        if(connectionDefinition != null) {
            connectionDefinition.addXpathListener("config-property", xpathListener);
        }
        // todo: Prepare the ConnectionManager

    }

    DDBean getDDBean() {
        return connectionDefinition;
    }

    //todo: the following properties
    // connection-manager

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
}
