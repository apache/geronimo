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

import org.apache.geronimo.xbeans.connector.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;
import javax.enterprise.deploy.model.DDBean;

/**
 * Represents /connector/adminobject/adminobject-instance in the
 * Geronimo Connector deployment plan.
 *
 * @version $Rev$ $Date$
 */
public class AdminObjectInstance extends ConfigHolder {
    private DDBean adminObject;

    public AdminObjectInstance() {
    }

    public AdminObjectInstance(DDBean adminObject, GerAdminobjectInstanceType instance) {
        configure(adminObject, instance);
    }

    protected GerAdminobjectInstanceType getAdminInstance() {
        return (GerAdminobjectInstanceType) getXmlObject();
    }

    public void reconfigure() {
        configure(adminObject, getAdminInstance());
    }

    void configure(DDBean adminObject, GerAdminobjectInstanceType definition) {
        this.adminObject = adminObject;
        super.configure(adminObject, definition);
    }

    protected GerConfigPropertySettingType createConfigProperty() {
        return getAdminInstance().addNewConfigPropertySetting();
    }

    protected GerConfigPropertySettingType[] getConfigProperties() {
        return getAdminInstance().getConfigPropertySettingArray();
    }

    protected void removeConfigProperty(int index) {
        getAdminInstance().removeConfigPropertySetting(index);
    }

    // ----------------------- JavaBean Properties for /adminobject-instance ----------------------

    public String getMessageDestinationName() {
        return getAdminInstance().getMessageDestinationName();
    }

    public void setMessageDestinationName(String name) {
        String old = getMessageDestinationName();
        getAdminInstance().setMessageDestinationName(name);
        pcs.firePropertyChange("messageDestinationName", old, name);
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
