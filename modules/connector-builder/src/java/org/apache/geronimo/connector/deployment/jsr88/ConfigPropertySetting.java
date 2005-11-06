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

import javax.enterprise.deploy.model.DDBean;
import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * Represents /connector/resourceadapter/resourceadapter-instance/config-property-setting
 * or /connector/resourceadapter/outbound-resourceadapter/connection-definition/connectiondefinition-instance/config-property-setting
 * or /connector/adminobject/adminobject-instance/config-property-setting in the
 * Geronimo Connector deployment plan.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigPropertySetting extends XmlBeanSupport {
    private DDBean configProperty;

    public ConfigPropertySetting() {
        super(null);
    }

    public ConfigPropertySetting(DDBean configProperty, GerConfigPropertySettingType property) {
        super(null);
        configure(configProperty, property);
    }

    protected GerConfigPropertySettingType getPropertySetting() {
        return (GerConfigPropertySettingType) getXmlObject();
    }

    DDBean getDDBean() {
        return configProperty;
    }

    void configure(DDBean configProperty, GerConfigPropertySettingType property) {
        this.configProperty = configProperty;
        setXmlObject(property);
        final String name = configProperty.getText("config-property-name")[0];
        getPropertySetting().setName(name);
        String[] test = configProperty.getText("config-property-value");
        if(test != null && test.length == 1) {
            getPropertySetting().setStringValue(test[0]);
        }
    }

    // ----------------------- JavaBean Properties for config-property-setting ----------------------

    public String getName() {
        return getPropertySetting().getName();
    }

    // Not public -- should always be kept in sync with matching config-property
    void setName(String name) {
        String old = getName();
        getPropertySetting().setName(name);
        pcs.firePropertyChange("name", old, name);
    }

    public String getValue() {
        return getPropertySetting().getStringValue();
    }

    public void setValue(String value) {
        String old = getValue();
        getPropertySetting().setStringValue(value);
        pcs.firePropertyChange("value", old, value);
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
