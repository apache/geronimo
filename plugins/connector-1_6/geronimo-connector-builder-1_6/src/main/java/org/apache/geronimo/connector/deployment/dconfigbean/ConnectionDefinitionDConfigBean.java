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
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.connector.GerConnectionmanagerType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 **/
public class ConnectionDefinitionDConfigBean extends DConfigBeanSupport {

    private ConnectionDefinitionInstance[] instances = new ConnectionDefinitionInstance[0];

    public ConnectionDefinitionDConfigBean(DDBean ddBean, GerConnectionDefinitionType connectionDefinition) {
        super(ddBean, connectionDefinition);
        String connectionfactoryInterface = ddBean.getText("connectionfactory-interface")[0];
        if (connectionDefinition.getConnectionfactoryInterface() == null) {
            connectionDefinition.setConnectionfactoryInterface(connectionfactoryInterface);
        } else {
            assert connectionfactoryInterface.equals(connectionDefinition.getConnectionfactoryInterface());
        }
        // Get initial list of instances
        instances = new ConnectionDefinitionInstance[getConnectionDefinition().getConnectiondefinitionInstanceArray().length];
        for (int i = 0; i < instances.length; i++) {
            instances[i] = new ConnectionDefinitionInstance();
            instances[i].initialize(getConnectionDefinition().getConnectiondefinitionInstanceArray(i), this);
        }
    }

    GerConnectionDefinitionType getConnectionDefinition() {
        return (GerConnectionDefinitionType) getXmlObject();
    }

    public ConnectionDefinitionInstance[] getConnectionDefinitionInstance() {
        return instances;
    }

    public void setConnectionDefinitionInstance(ConnectionDefinitionInstance[] instances) {
        ConnectionDefinitionInstance[] old = getConnectionDefinitionInstance();
        this.instances = instances;
        for (int i = 0; i < instances.length; i++) { // catch additions
            ConnectionDefinitionInstance instance = instances[i];
            if (!instance.hasParent()) {
                GerConnectiondefinitionInstanceType xmlObject = getConnectionDefinition().addNewConnectiondefinitionInstance();
                xmlObject.setConnectionmanager(GerConnectionmanagerType.Factory.newInstance());
                instance.initialize(xmlObject, this);
            }
        }
        for (int i = 0; i < old.length; i++) { // catch removals
            ConnectionDefinitionInstance instance = old[i];
            boolean found = false;
            for (int j = 0; j < instances.length; j++) {
                if (instances[j] == instance) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // remove the XmlBean
                for (int j = 0; j < getConnectionDefinition().getConnectiondefinitionInstanceArray().length; j++) {
                    GerConnectiondefinitionInstanceType test = getConnectionDefinition().getConnectiondefinitionInstanceArray(j);
                    if (test == instance.getConnectiondefinitionInstance()) {
                        getConnectionDefinition().removeConnectiondefinitionInstance(j);
                        break;
                    }
                }
                // clean up the removed JavaBean
                instance.dispose();
            }
        }
        pcs.firePropertyChange("connectionDefinitionInstance", old, instances);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return ResourceAdapterDConfigRoot.SCHEMA_TYPE_LOADER;
    }

}
