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

import org.apache.geronimo.deployment.dconfigbean.DConfigBeanSupport;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class ConfigPropertySettingDConfigBean extends DConfigBeanSupport {

    public ConfigPropertySettingDConfigBean(DDBean ddBean, GerConfigPropertySettingType configPropertySetting) {
        super(ddBean, configPropertySetting);
        String name = ddBean.getText("config-property-name")[0];
        if (configPropertySetting.getName() == null) {
            configPropertySetting.setName(name);
            String[] values = ddBean.getText("config-property-value");
            if (values != null && values.length == 1) {
                configPropertySetting.setStringValue(values[0]);
            }
        } else {
            assert name.equals(configPropertySetting.getName());
        }
    }

    GerConfigPropertySettingType getConfigPropertySetting() {
        return (GerConfigPropertySettingType) getXmlObject();
    }

    public String getConfigPropertyName() {
        return getConfigPropertySetting().getName();
    }

    //TODO this needs research about if it works.
    public String getConfigPropertyType() {
        return getDDBean().getText("config-property/config-property-type")[0];
    }

    public String getConfigPropertyValue() {
        return getConfigPropertySetting().getStringValue();
    }

    public void setConfigPropertyValue(String configPropertyValue) {
        getConfigPropertySetting().setStringValue(configPropertyValue);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ResourceAdapterDConfigRoot.SCHEMA_TYPE_LOADER;
    }

}

