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
import javax.enterprise.deploy.model.DDBean;
import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
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
        //todo: initialize connectiondefinition-instance from definition
    }

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
}
