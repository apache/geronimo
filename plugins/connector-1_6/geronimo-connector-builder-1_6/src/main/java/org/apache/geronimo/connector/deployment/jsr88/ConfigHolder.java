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

import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.xmlbeans.XmlObject;

import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.DDBean;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Base class for beans that hold an array of config property settings.
 *
 * @version $Rev$ $Date$
 */
public abstract class ConfigHolder extends XmlBeanSupport {
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
    private DDBean ddBean;
    private ConfigPropertySetting[] settings = new ConfigPropertySetting[0];

    public ConfigHolder() {
        super(null);
    }

    public void clearNullSettings() {
        List list = new ArrayList();
        Set saved = new HashSet();
        for (int i = 0; i < settings.length; i++) {
            ConfigPropertySetting setting = settings[i];
            if(setting.getValue() != null && !setting.isSetToDefault()) {
                list.add(setting);
                saved.add(setting.getName());
            }
        }
        settings = (ConfigPropertySetting[]) list.toArray(new ConfigPropertySetting[list.size()]);
        GerConfigPropertySettingType[] configs = getConfigProperties();
        for (int i = configs.length-1; i>=0; --i) {
            GerConfigPropertySettingType type = configs[i];
            if(!saved.contains(type.getName())) {
                removeConfigProperty(i);
            }
        }
    }

    protected void configure(DDBean ddBean, XmlObject xml) {
        ConfigPropertySetting[] old = null;
        if(this.ddBean != null) {
            this.ddBean.removeXpathListener("config-property", xpathListener);
            old = settings;
        }
        this.ddBean = ddBean;
        setXmlObject(xml);

        // Prepare the ConfigPropertySetting array
        List list = new ArrayList();
        DDBean[] all = ddBean == null ? new DDBean[0] : ddBean.getChildBean("config-property");
        if(all == null) {
            all = new DDBean[0];
        }
        Map byName = new HashMap();
        for (int i = 0; i < all.length; i++) {
            DDBean item = all[i];
            byName.put(item.getText("config-property-name")[0], item);
        }
        GerConfigPropertySettingType[] previous = getConfigProperties();
        for (int i = 0; i < previous.length; i++) {
            GerConfigPropertySettingType setting = previous[i];
            DDBean item = (DDBean) byName.remove(setting.getName());
            if(item != null) {
                list.add(new ConfigPropertySetting(item, setting, false));
            } else {
                System.out.println("Ignoring connectiondefinition-instance/config-setting "+setting.getName()+" (no matching config-property in J2EE DD)");
                //todo: delete it from the XMLBeans tree
            }
        }
        for (Iterator it = byName.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            DDBean bean = (DDBean) byName.get(name);
            list.add(new ConfigPropertySetting(bean, createConfigProperty(), true));
        }
        settings = (ConfigPropertySetting[]) list.toArray(new ConfigPropertySetting[list.size()]);
        if(old != null) {
            pcs.firePropertyChange("configPropertySetting", old, settings);
        }
        if(ddBean != null) {
            ddBean.addXpathListener("config-property", xpathListener);
        }
    }

    public ConfigPropertySetting[] getConfigPropertySetting() {
        return settings;
    }

    public ConfigPropertySetting getConfigPropertySetting(int index) {
        return settings[index];
    }

    protected abstract GerConfigPropertySettingType createConfigProperty();
    protected abstract GerConfigPropertySettingType[] getConfigProperties();
    protected abstract void removeConfigProperty(int index);
    public abstract void reconfigure();
}
