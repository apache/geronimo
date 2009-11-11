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

import org.apache.geronimo.xbeans.connector.GerResourceadapterInstanceType;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.geronimo.naming.deployment.jsr88.GBeanLocator;
import org.apache.xmlbeans.SchemaTypeLoader;
import javax.enterprise.deploy.model.DDBean;

/**
 * Represents /connector/resourceadapter/resourceadapter-instance in the
 * Geronimo Connector deployment plan.  The settings here correspond to
 * /connector/resourceadapter in the J2EE plan, but since there can be
 * several instances per resource adapter, it's not 1:1 and this is not
 * a DConfigBean.
 *
 * @version $Rev$ $Date$
 */
public class ResourceAdapterInstance extends ConfigHolder {
    private DDBean resourceAdapter;
    private GBeanLocator workManager;

    /**
     * Present so a tool can create a new one
     */
    public ResourceAdapterInstance() {
    }

    public ResourceAdapterInstance(DDBean resourceAdapter, GerResourceadapterInstanceType instance) {
        configure(resourceAdapter, instance);
    }

    protected GerResourceadapterInstanceType getResourceAdapterInstance() {
        return (GerResourceadapterInstanceType) getXmlObject();
    }

    public void reconfigure() {
        configure(resourceAdapter, getResourceAdapterInstance());
    }

    void configure(DDBean resourceAdapter, GerResourceadapterInstanceType xml) {
        this.resourceAdapter = resourceAdapter;
        super.configure(resourceAdapter, xml);
    }

    protected GerConfigPropertySettingType createConfigProperty() {
        return getResourceAdapterInstance().addNewConfigPropertySetting();
    }

    protected GerConfigPropertySettingType[] getConfigProperties() {
        return getResourceAdapterInstance().getConfigPropertySettingArray();
    }

    protected void removeConfigProperty(int index) {
        getResourceAdapterInstance().removeConfigPropertySetting(index);
    }

    // ----------------------- JavaBean Properties for /resourceadapter-instance ----------------------

    public String getResourceAdapterName() {
        return getResourceAdapterInstance().getResourceadapterName();
    }

    public void setResourceAdapterName(String name) {
        String old = getResourceAdapterName();
        getResourceAdapterInstance().setResourceadapterName(name);
        pcs.firePropertyChange("resourceAdapterName", old, name);
    }

    public GBeanLocator getWorkManager() {
        return workManager;
    }

    public void setWorkManager(GBeanLocator locator) {
        GBeanLocator old = getWorkManager();
        if(locator != null && !locator.configured()) {
            if(getResourceAdapterInstance().getWorkmanager() != null) {
                locator.configure(getResourceAdapterInstance().getWorkmanager());
            } else {
                locator.configure(getResourceAdapterInstance().addNewWorkmanager());
            }
        }
        workManager = locator;
        pcs.firePropertyChange("workManager", old, workManager);
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
