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
import org.apache.geronimo.xbeans.connector.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.connector.GerAdminobjectType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class AdminObjectDConfigBean extends DConfigBeanSupport {
    private AdminObjectInstance[] instances = new AdminObjectInstance[0];

    public AdminObjectDConfigBean(DDBean ddBean, GerAdminobjectType adminObject) {
        super(ddBean, adminObject);
        String adminObjectInterface = ddBean.getText("adminobject-interface")[0];
        if (adminObject.getAdminobjectInterface() == null) {
            adminObject.setAdminobjectInterface(adminObjectInterface);
        } else {
            assert adminObjectInterface.equals(adminObject.getAdminobjectInterface());
        }
        String adminObjectClass = ddBean.getText("adminobject-class")[0];
        if (adminObject.getAdminobjectClass() == null) {
            adminObject.setAdminobjectClass(adminObjectClass);
        } else {
            assert adminObjectClass.equals(adminObject.getAdminobjectClass());
        }
        // Get initial list of instances
        GerAdminobjectInstanceType[] xmlInstances = getAdminObject().getAdminobjectInstanceArray();
        instances = new AdminObjectInstance[xmlInstances.length];
        for (int i = 0; i < instances.length; i++) {
            instances[i] = new AdminObjectInstance();
            instances[i].initialize(xmlInstances[i], this);
        }
    }

    GerAdminobjectType getAdminObject() {
        return (GerAdminobjectType) getXmlObject();
    }

    public AdminObjectInstance[] getAdminObjectInstance() {
        return instances;
    }

    public void setAdminObjectInstance(AdminObjectInstance[] instances) {
        AdminObjectInstance[] old = getAdminObjectInstance();
        this.instances = instances;
        for (int i = 0; i < instances.length; i++) { // catch additions
            AdminObjectInstance instance = instances[i];
            if (!instance.hasParent()) {
                GerAdminobjectInstanceType xmlObject = getAdminObject().addNewAdminobjectInstance();
                instance.initialize(xmlObject, this);
            }
        }
        for (int i = 0; i < old.length; i++) { // catch removals
            AdminObjectInstance instance = old[i];
            boolean found = false;
            for (int j = 0; j < instances.length; j++) {
                if (instances[j] == instance) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // remove the XmlBean
                for (int j = 0; j < getAdminObject().getAdminobjectInstanceArray().length; j++) {
                    GerAdminobjectInstanceType test = getAdminObject().getAdminobjectInstanceArray(j);
                    if (test == instance.getAdminobjectInstance()) {
                        getAdminObject().removeAdminobjectInstance(j);
                        break;
                    }
                }
                // clean up the removed JavaBean
                instance.dispose();
            }
        }
        pcs.firePropertyChange("adminObjectInstance", old, instances);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ResourceAdapterDConfigRoot.SCHEMA_TYPE_LOADER;
    }

}
