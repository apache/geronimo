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
import javax.enterprise.deploy.model.DDBean;
import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.xbeans.connector.GerResourceadapterType;
import org.apache.geronimo.xbeans.connector.GerOutboundResourceadapterType;
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * Represents /connector/resourceadapter in the Geronimo Connector deployment plan.
 * Note: is not a DConfigBean, because there may be more than one ResourceAdapter
 * in the Geronimo plan per ResourceAdapter in the J2EE deployment descriptor.
 *
 * @version $Rev$ $Date$
 */
public class ResourceAdapter extends XmlBeanSupport {
    private DDBean resourceAdapter;
    private ConnectionDefinition[] instances = new ConnectionDefinition[0];
    private ResourceAdapterInstance resourceAdapterInstance;

    public ResourceAdapter() {
        super(null);
    }

    public ResourceAdapter(DDBean resourceAdapter, GerResourceadapterType ra) {
        super(null);
        configure(resourceAdapter, ra);
    }

    protected GerResourceadapterType getResourceAdapter() {
        return (GerResourceadapterType)getXmlObject();
    }

    void configure(DDBean resourceAdapter, GerResourceadapterType ra) {
        this.resourceAdapter = resourceAdapter;
        setXmlObject(ra);
        if(ra.isSetResourceadapterInstance()) {
            resourceAdapterInstance = new ResourceAdapterInstance(resourceAdapter, ra.getResourceadapterInstance());
        } else {
            resourceAdapterInstance = null;
        }
        if(ra.isSetOutboundResourceadapter()) {
            DDBean[] test = resourceAdapter.getChildBean("outbound-resourceadapter");
            if(test != null && test.length > 0) {
                GerOutboundResourceadapterType outbound = ra.getOutboundResourceadapter();
                GerConnectionDefinitionType[] defs = outbound.getConnectionDefinitionArray();
                if(defs != null) {
                    instances = new ConnectionDefinition[defs.length];
                    for (int i = 0; i < defs.length; i++) {
                        GerConnectionDefinitionType def = defs[i];
                        instances[i] = new ConnectionDefinition(resourceAdapter, def);
                    }
                }
            } else {
                //todo: clean up the Geronimo deployment info since there's no J2EE outbound RA
            }
        }
    }

    // ----------------------- JavaBean Properties for /connector/resourceadapter ----------------------

    public ConnectionDefinition[] getConnectionDefinition() {
        return instances;
    }

    public ConnectionDefinition getConnectionDefinition(int index) {
        return instances[index];
    }

    public void setConnectionDefinition(ConnectionDefinition[] definitions) {
        ConnectionDefinition[] old = getConnectionDefinition();

        if(definitions != null && definitions.length > 0) {
            if(!getResourceAdapter().isSetOutboundResourceadapter()) {
                getResourceAdapter().addNewOutboundResourceadapter();
            }
        } else {
            if(getResourceAdapter().isSetOutboundResourceadapter()) {
                getResourceAdapter().unsetOutboundResourceadapter();
            }
        }
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        instances = definitions;
        // Handle current or new definitions
        for (int i = 0; i < definitions.length; i++) {
            ConnectionDefinition definition = definitions[i];
            if(definition.getConnectionDefinition() == null) {
                definition.configure(resourceAdapter, getResourceAdapter().getOutboundResourceadapter().addNewConnectionDefinition());
            } else {
                before.remove(definition);
            }
        }
        // Handle removed definitions
        for (Iterator it = before.iterator(); it.hasNext();) {
            ConnectionDefinition definition = (ConnectionDefinition) it.next();
            GerConnectionDefinitionType all[] = getResourceAdapter().isSetOutboundResourceadapter() ? getResourceAdapter().getOutboundResourceadapter().getConnectionDefinitionArray() : new GerConnectionDefinitionType[0];
            for (int i = 0; i < all.length; i++) {
                if(all[i] == definition) {
                    getResourceAdapter().getOutboundResourceadapter().removeConnectionDefinition(i);
                    break;
                }
            }
        }
        pcs.firePropertyChange("connectionDefinition", old, instances);
    }

    public void setConnectionDefinition(int index, ConnectionDefinition definition) {
        ConnectionDefinition[] old = instances;
        instances[index] = definition;
        if(definition.getConnectionDefinition() == null) {
            definition.configure(resourceAdapter, getResourceAdapter().getOutboundResourceadapter().addNewConnectionDefinition());
        }
        pcs.firePropertyChange("connectionDefinition", old, instances);
    }

    public ResourceAdapterInstance getResourceAdapterInstance() {
        return resourceAdapterInstance;
    }

    public void setResourceAdapterInstance(ResourceAdapterInstance resourceAdapterInstance) {
        ResourceAdapterInstance old = this.resourceAdapterInstance;
        this.resourceAdapterInstance = resourceAdapterInstance;
        if(resourceAdapterInstance.getResourceAdapterInstance() == null) {
            if(getResourceAdapter().isSetResourceadapterInstance()) {
                resourceAdapterInstance.configure(resourceAdapter, getResourceAdapter().getResourceadapterInstance());
            } else {
                resourceAdapterInstance.configure(resourceAdapter, getResourceAdapter().addNewResourceadapterInstance());
            }
        }
        pcs.firePropertyChange("resourceAdapterInstance", old, instances);
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
