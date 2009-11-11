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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.enterprise.deploy.model.DDBean;
import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * Represents /connector/resourceadapter/outbound-resourceadapter/connection-definition
 * in the Geronimo Connector deployment plan.  A Geronimo connection definition
 * corresponds to a ra.xml connection definition (though there may be several
 * Geronimo CDs for each ra.xml CD so this cannot be a DConfigBean [which would
 * require a 1:1 mapping]).  Each Geronimo connection definition may have one
 * or more instances with different config property settings, etc.
 *
 * @version $Rev$ $Date$
 */
public class ConnectionDefinition extends XmlBeanSupport {
    private DDBean resourceAdapter;
    private ConnectionDefinitionInstance[] instances = new ConnectionDefinitionInstance[0];

    public ConnectionDefinition() {
        super(null);
    }

    public ConnectionDefinition(DDBean resourceAdapter, GerConnectionDefinitionType definition) {
        super(null);
        configure(resourceAdapter, definition);
    }

    protected GerConnectionDefinitionType getConnectionDefinition() {
        return (GerConnectionDefinitionType) getXmlObject();
    }

    void configure(DDBean resourceAdapter, GerConnectionDefinitionType definition) {
        this.resourceAdapter = resourceAdapter;
        setXmlObject(definition);
        //todo: handle unmatched interfaces below
        instances = new ConnectionDefinitionInstance[definition.getConnectiondefinitionInstanceArray().length];
        DDBean[] beans = resourceAdapter.getChildBean("outbound-resourceadapter/connection-definition");
        DDBean match = null;
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            if(bean.getText("connectionfactory-interface")[0].equals(definition.getConnectionfactoryInterface())) {
                match = bean;
                break;
            }
        }
        for (int i = 0; i < instances.length; i++) {
            GerConnectiondefinitionInstanceType gerInstance = definition.getConnectiondefinitionInstanceArray()[i];
            instances[i] = new ConnectionDefinitionInstance(match, gerInstance);
        }
    }

    // ----------------------- JavaBean Properties for connection-definition ----------------------

    //todo: instead of String, make this an Enum type aware of the interfaces available in the J2EE DD
    public String getConnectionFactoryInterface() {
        return getConnectionDefinition().getConnectionfactoryInterface();
    }

    public void setConnectionFactoryInterface(String iface) {
        String old = getConnectionFactoryInterface();
        getConnectionDefinition().setConnectionfactoryInterface(iface);
        DDBean match = getConnectionDefinitionDDBean();
        for (int i = 0; i < instances.length; i++) {
            ConnectionDefinitionInstance instance = instances[i];
            if(instance.getDDBean() != match) {
                instance.configure(match, instance.getConnectionInstance());
            }
        }
        pcs.firePropertyChange("connectionFactoryInterface", old, iface);
    }

    public ConnectionDefinitionInstance[] getConnectionInstances() {
        return instances;
    }

    public void setConnectionInstance(ConnectionDefinitionInstance[] instances) {
        ConnectionDefinitionInstance[] old = this.instances;
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        this.instances = instances;
        // Handle current or new resource adapters
        for (int i = 0; i < instances.length; i++) {
            ConnectionDefinitionInstance instance = instances[i];
            if(instance.getConnectionInstance() == null) {
                instance.configure(getConnectionDefinitionDDBean(), getConnectionDefinition().addNewConnectiondefinitionInstance());
            } else {
                before.remove(instance);
            }
        }
        // Handle removed resource adapters
        for (Iterator it = before.iterator(); it.hasNext();) {
            ConnectionDefinitionInstance instance = (ConnectionDefinitionInstance) it.next();
            GerConnectiondefinitionInstanceType all[] = getConnectionDefinition().getConnectiondefinitionInstanceArray();
            for (int i = 0; i < all.length; i++) {
                if(all[i] == instance) {
                    getConnectionDefinition().removeConnectiondefinitionInstance(i);
                    break;
                }
            }
        }
        pcs.firePropertyChange("connectionInstance", old, instances);

    }


    // ----------------------- End of JavaBean Properties ----------------------

    /**
     * Look up the J2EE connection definition corresponding to this one (based on connectionfactory-interface)
     */
    private DDBean getConnectionDefinitionDDBean() {
        String iface = getConnectionFactoryInterface();
        if(iface == null || iface.equals("")) {
            return null;
        }
        DDBean list[] = resourceAdapter.getChildBean("outbound-resourceadapter/connection-definition");
        for (int i = 0; i < list.length; i++) {
            DDBean bean = list[i];
            String[] test = bean.getText("connectionfactory-interface");
            if(test.length > 0) {
                String myface = test[0];
                if(myface.equals(iface)) {
                    return bean;
                }
            }
        }
        return null;
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
