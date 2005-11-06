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
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.XpathEvent;
import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * Represents /connector in a Geronimo Connector deployment plan.
 * Corresponds to /connector in the J2EE deployment plan.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConnectorDCB extends DConfigBeanSupport {
    private DDBean resourceAdapterDDBean;
    private ResourceAdapter[] resourceAdapter = new ResourceAdapter[0];

    public ConnectorDCB(DDBean connectorDDBean, final GerConnectorType connector) {
        super(connectorDDBean, connector);
        DDBean[] list = connectorDDBean.getChildBean("resourceadapter");
        if(list.length > 0) {
            resourceAdapterDDBean = list[0];
        }
        //todo: do something if resourceAdapterDDBean is null
        loadExistingData(connector);
        //todo: load defaults from J2EE DD /connector/resourceadapter

        // Make sure we're told if /connector/resourceadapter is replaced!
        connectorDDBean.addXpathListener("resourceadapter", new XpathListener() {
            public void fireXpathEvent(XpathEvent event) {
                if(event.isRemoveEvent()) {
                    resourceAdapterDDBean = null; //todo: all our info was just invalidated
                } else if(event.isAddEvent()) {
                    resourceAdapterDDBean = event.getBean(); //todo: reload defaults from DDBean
                } else {
                    System.out.println("Detected change to J2EE DD /connector/resourceadapter property "+event.getChangeEvent().getPropertyName());
                }
            }
        });
    }

    private void loadExistingData(GerConnectorType connector) {
        //todo: Handle the import children
        //todo: Handle the hidden-classes children
        //todo: Handle the non-overridable-classes children
        //todo: Handle the dependency children
        // Handle the resource adapter children
        GerResourceadapterType[] adapters = connector.getResourceadapterArray();
        if(adapters == null || adapters.length == 0) {
            // Make sure there's at least one connector/resourceadapter element
            if(resourceAdapterDDBean != null) {
                resourceAdapter = new ResourceAdapter[1];
                resourceAdapter[0] = new ResourceAdapter(resourceAdapterDDBean, connector.addNewResourceadapter());
            }
        } else {
            resourceAdapter = new ResourceAdapter[adapters.length];
            for (int i = 0; i < adapters.length; i++) {
                GerResourceadapterType adapter = adapters[i];
                resourceAdapter[i] = new ResourceAdapter(resourceAdapterDDBean, adapter);
            }
        }
        //todo: Handle the AdminObject children
        //todo: Handle the GBean children
    }

    GerConnectorType getConnector() {
        return (GerConnectorType) getXmlObject();
    }


    // ----------------------- JavaBean Properties for /connector ----------------------

    //todo: the following child elements
    // import*
    // hidden-classes*
    // non-overridable-classes*
    // dependency*
    // adminobject*
    // gbean*

    public String getConfigID() {
        return getConnector().getConfigId();
    }

    public void setConfigID(String configId) {
        String old = getConfigID();
        getConnector().setConfigId(configId);
        pcs.firePropertyChange("configID", old, configId);
    }

    public String getParentID() {
        return getConnector().getParentId();
    }

    public void setParentID(String parentId) {
        String old = getParentID();
        if(parentId == null) {
            getConnector().unsetParentId();
        } else {
            getConnector().setParentId(parentId);
        }
        pcs.firePropertyChange("parentID", old, parentId);
    }

    public Boolean getSuppressDefaultParentID() {
        return getConnector().isSetSuppressDefaultParentId() ? getConnector().getSuppressDefaultParentId() ? Boolean.TRUE : Boolean.FALSE : null;
    }

    public void setSuppressDefaultParentID(Boolean suppress) {
        Boolean old = getSuppressDefaultParentID();
        if(suppress == null) {
            getConnector().unsetSuppressDefaultParentId();
        } else {
            getConnector().setSuppressDefaultParentId(suppress.booleanValue());
        }
        pcs.firePropertyChange("suppressDefaultParentID", old, suppress);
    }

    public Boolean getInverseClassLoading() {
        return getConnector().isSetInverseClassloading() ? getConnector().getInverseClassloading() ? Boolean.TRUE : Boolean.FALSE : null;
    }

    public void setInverseClassLoading(Boolean inverse) {
        Boolean old = getInverseClassLoading();
        if(inverse == null) {
            getConnector().unsetInverseClassloading();
        } else {
            getConnector().setInverseClassloading(inverse.booleanValue());
        }
        pcs.firePropertyChange("inverseClassLoading", old, inverse);
    }

    public ResourceAdapter[] getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter[] resourceAdapter) {
        ResourceAdapter[] old = this.resourceAdapter;
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        this.resourceAdapter = resourceAdapter;
        // Handle current or new resource adapters
        for (int i = 0; i < resourceAdapter.length; i++) {
            ResourceAdapter adapter = resourceAdapter[i];
            if(adapter.getResourceAdapter() == null) {
                adapter.configure(resourceAdapterDDBean, getConnector().addNewResourceadapter());
            } else {
                before.remove(adapter);
            }
        }
        // Handle removed resource adapters
        for (Iterator it = before.iterator(); it.hasNext();) {
            ResourceAdapter adapter = (ResourceAdapter) it.next();
            GerResourceadapterType all[] = getConnector().getResourceadapterArray();
            for (int i = 0; i < all.length; i++) {
                if(all[i] == adapter) {
                    getConnector().removeResourceadapter(i);
                    break;
                }
            }
        }
        pcs.firePropertyChange("resourceAdapter", old, resourceAdapter);
    }

    public ResourceAdapter getResourceAdapter(int index) {
        return resourceAdapter[index];
    }

    public void setResourceAdapter(int index, ResourceAdapter ra) {
        ResourceAdapter[] old = this.resourceAdapter;
        resourceAdapter[index] = ra;
        if(ra.getResourceAdapter() == null) {
            ra.configure(resourceAdapterDDBean, getConnector().addNewResourceadapter());
        }
        pcs.firePropertyChange("resourceAdapter", old, resourceAdapter);
    }


    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
