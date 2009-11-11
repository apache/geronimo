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
import org.apache.geronimo.xbeans.connector.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class AdminObjectInstance extends XmlBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();
    private AdminObjectDConfigBean parent;
    private ConfigPropertySettings[] configs;
    private XpathListener configListener;

    public AdminObjectInstance() {
        super(null);
    }

    void initialize(GerAdminobjectInstanceType xmlObject, AdminObjectDConfigBean parent) {
        setXmlObject(xmlObject);
        this.parent = parent;
        DDBean parentDDBean = parent.getDDBean();
        configListener = ConfigPropertiesHelper.initialize(parentDDBean, new ConfigPropertiesHelper.ConfigPropertiesSource() {
            public GerConfigPropertySettingType[] getConfigPropertySettingArray() {
                return getAdminobjectInstance().getConfigPropertySettingArray();
            }

            public GerConfigPropertySettingType addNewConfigPropertySetting() {
                return getAdminobjectInstance().addNewConfigPropertySetting();
            }

            public void removeConfigPropertySetting(int j) {
                getAdminobjectInstance().removeConfigPropertySetting(j);
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
    GerAdminobjectInstanceType getAdminobjectInstance() {
        return (GerAdminobjectInstanceType) getXmlObject();
    }

    public ConfigPropertySettings[] getConfigProperty() {
        return configs;
    }

    private void setConfigProperty(ConfigPropertySettings[] configs) { // can only be changed by adding a new DDBean
        ConfigPropertySettings[] old = getConfigProperty();
        this.configs = configs;
        pcs.firePropertyChange("configProperty", old, configs);
    }

    public String getMessageDestinationName() {
        return getAdminobjectInstance().getMessageDestinationName();
    }

    public void setMessageDestinationName(String messageDestinationName) {
        getAdminobjectInstance().setMessageDestinationName(messageDestinationName);
    }

}
