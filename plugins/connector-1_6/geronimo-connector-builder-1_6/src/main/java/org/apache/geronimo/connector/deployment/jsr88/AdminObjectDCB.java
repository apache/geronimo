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

import org.apache.geronimo.deployment.dconfigbean.DConfigBeanSupport;
import org.apache.geronimo.xbeans.connector.GerAdminobjectType;
import org.apache.geronimo.xbeans.connector.GerAdminobjectInstanceType;
import org.apache.xmlbeans.SchemaTypeLoader;

import javax.enterprise.deploy.model.DDBean;

/**
 * Represents /connector/adminobject in a Geronimo Connector deployment plan.
 * Corresponds to /connector/resourceadapter/adminobject in the J2EE deployment plan.
 * Note that in an arbitrary Geronimo connector plan, there can be multiple
 * adminobject entries per adminobject from the J2EE plan.  When we load such
 * a plan, we combine all the adminobject-instances from those adminobjects
 * into a single Geronimo adminobject per J2EE adminobject, so if we write it
 * out again it'll be a little different, but that way this can be a DConfigBean
 * instead of a POJO (the loading code is in ConnectorDCB).
 *
 * @version $Rev$ $Date$
 */
public class AdminObjectDCB extends DConfigBeanSupport {
    private AdminObjectInstance[] adminObjectInstance = new AdminObjectInstance[0];

    public AdminObjectDCB(DDBean adminobjectDDBean, final GerAdminobjectType adminobject) {
        super(adminobjectDDBean, adminobject);
        //todo: listen for property changes on the admin object
        configure(adminobjectDDBean, adminobject);
    }

    private void configure(DDBean adminDDBean, GerAdminobjectType adminXml) {
        adminXml.setAdminobjectClass(adminDDBean.getText("adminobject-class")[0]);
        adminXml.setAdminobjectInterface(adminDDBean.getText("adminobject-interface")[0]);
        GerAdminobjectInstanceType[] xmls = adminXml.getAdminobjectInstanceArray();
        adminObjectInstance = new AdminObjectInstance[xmls.length];
        for (int i = 0; i < xmls.length; i++) {
            adminObjectInstance[i] = new AdminObjectInstance(adminDDBean, xmls[i]);
        }
    }

    GerAdminobjectType getAdminObject() {
        return (GerAdminobjectType) getXmlObject();
    }

    void addAdminObjectInstance(GerAdminobjectInstanceType xml) {
        AdminObjectInstance instance = new AdminObjectInstance(getDDBean(), xml);
        AdminObjectInstance[] result = new AdminObjectInstance[adminObjectInstance.length+1];
        System.arraycopy(adminObjectInstance, 0, result, 0, adminObjectInstance.length);
        result[adminObjectInstance.length] = instance;
        setAdminObjectInstance(result);
    }

    // ----------------------- JavaBean Properties for /adminobject ----------------------

    public String getAdminObjectInterface() {
        return getAdminObject().getAdminobjectInterface();
    }

    public String getAdminObjectClass() {
        return getAdminObject().getAdminobjectClass();
    }

    public AdminObjectInstance[] getAdminObjectInstance() {
        return adminObjectInstance;
    }

    public void setAdminObjectInstance(AdminObjectInstance[] adminObjectInstance) {
        AdminObjectInstance[] old = getAdminObjectInstance();
        //todo: whack all the old ones
        for (int i = 0; i < adminObjectInstance.length; i++) {
            AdminObjectInstance instance = adminObjectInstance[i];
            if(instance.getAdminInstance() == null) {
                instance.configure(getDDBean(), getAdminObject().addNewAdminobjectInstance());
            }
        }
        this.adminObjectInstance = adminObjectInstance;
        pcs.firePropertyChange("adminObjectInstance", old, adminObjectInstance);
    }

    public AdminObjectInstance getAdminObjectInstance(int index) {
        return adminObjectInstance[index];
    }

    public void setAdminObjectInstance(int index, AdminObjectInstance adminObjectInstance) {
        AdminObjectInstance[] old = getAdminObjectInstance();
        //todo: whack the old one
        if(adminObjectInstance.getAdminInstance() == null) {
            adminObjectInstance.configure(getDDBean(), getAdminObject().addNewAdminobjectInstance());
        }
        this.adminObjectInstance[index] = adminObjectInstance;
        //todo: deep copy of array for "old"
        pcs.firePropertyChange("adminObjectInstance", old, adminObjectInstance);
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
