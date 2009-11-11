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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.geronimo.deployment.dconfigbean.DConfigBeanSupport;
import org.apache.geronimo.deployment.service.jsr88.EnvironmentData;
import org.apache.geronimo.xbeans.connector.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.connector.GerAdminobjectType;
import org.apache.geronimo.xbeans.connector.GerConnectorType;
import org.apache.geronimo.xbeans.connector.GerResourceadapterType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;

/**
 * Represents /connector in a Geronimo Connector deployment plan.
 * Corresponds to /connector in the J2EE deployment plan.
 *
 * @version $Rev$ $Date$
 */
public class ConnectorDCB extends DConfigBeanSupport {
    private DDBean resourceAdapterDDBean;
    private ResourceAdapter[] resourceAdapter = new ResourceAdapter[0];
    private AdminObjectDCB[] adminobjects = new AdminObjectDCB[0];
    private EnvironmentData environment;

    public ConnectorDCB(DDBean connectorDDBean, final GerConnectorType connector) {
        super(connectorDDBean, connector);
        DDBean[] list = connectorDDBean.getChildBean("resourceadapter");
        if(list.length > 0) {
            resourceAdapterDDBean = list[0];
        }
        //todo: do something if resourceAdapterDDBean is null
        loadExistingData(connector);

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

    public String[] getXpaths() {
        return getXPathsForJ2ee_1_4(new String[][]{{"resourceadapter","adminobject",},});
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        if (getXpaths()[0].equals(bean.getXpath())) { // "adminobject"
            String aoInterface = bean.getText("adminobject-interface")[0];
            String aoClass = bean.getText("adminobject-class")[0];
            // Check whether we've seen this one before
            for(int i=0; i<adminobjects.length; i++) {
                if(adminobjects[i].getAdminObjectClass().equals(aoClass) &&
                   adminobjects[i].getAdminObjectInterface().equals(aoInterface)) {
                    return adminobjects[i];
                }
            }
            // Haven't seen it; create a new DConfigBean
            GerAdminobjectType ao = getConnector().addNewAdminobject();
            AdminObjectDCB dcb = new AdminObjectDCB(bean, ao);
            AdminObjectDCB[] list = new AdminObjectDCB[adminobjects.length+1];
            System.arraycopy(adminobjects, 0, list, 0, adminobjects.length);
            list[adminobjects.length] = dcb;
            return dcb;
        } else {
            throw new ConfigurationException("No DConfigBean matching DDBean "+bean.getXpath());
        }
    }

    private void loadExistingData(GerConnectorType connector) {
        //todo: Handle the import children
        //todo: Handle the hidden-classes children
        //todo: Handle the non-overridable-classes children
        // Handle the dependency children
//        ArtifactType[] deps = connector.getDependencyArray();
//        if(deps != null && deps.length > 0) {
//            dependency = new Artifact[deps.length];
//            for (int i = 0; i < deps.length; i++) {
//                dependency[i] = new Artifact(deps[i]);
//            }
//        }
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
        // Handle the AdminObject children
        GerAdminobjectType[] admins = connector.getAdminobjectArray();
        DDBean[] data = getDDBean().getChildBean(getXpaths()[0]);
        List ddBeans = data == null ? Collections.EMPTY_LIST : new ArrayList(Arrays.asList(data)); // resourceadapter/adminobject

        Map dcbs = new LinkedHashMap();
        if(admins == null) {
            adminobjects = new AdminObjectDCB[0];
        } else {
            // Match up each Geronimo adminobject with a ra.xml adminobject and create DConfigBeans accordingly
            for (int i = 0; i < admins.length; i++) {
                GerAdminobjectType admin = admins[i];
                String aoClass = admin.getAdminobjectClass();
                String aoIface = admin.getAdminobjectInterface();
                AdminObjectDCB dcb = (AdminObjectDCB) dcbs.get("class "+aoClass+" iface "+aoIface);
                if(dcb != null) {
                    // this is a second Geronimo adminobject block of the same type; there will not be a matching DDBean any more
                    // merge the adminobject-instance entries instead!!!
                    if(admin.getAdminobjectInstanceArray().length > 0) {
                        GerAdminobjectType old = dcb.getAdminObject();
                        GerAdminobjectInstanceType[] array = admin.getAdminobjectInstanceArray();
                        int oldCount = dcb.getAdminObjectInstance().length;
                        for (int j = 0; j < array.length; j++) {
                            GerAdminobjectInstanceType instance = array[j];
                            XmlCursor source = instance.newCursor();
                            XmlCursor dest = old.newCursor();
                            dest.toEndToken();
                            if(!source.moveXml(dest)) {
                                throw new RuntimeException("Unable to move admin object instance");
                            }
                            source.dispose();
                            dest.dispose();
                            dcb.addAdminObjectInstance(old.getAdminobjectInstanceArray(oldCount+j));
                        }
                    }
                    continue;
                }
                DDBean target = null;
                for (int j = 0; j < ddBeans.size(); j++) {
                    DDBean ddBean = (DDBean) ddBeans.get(j);
                    String ddClass = ddBean.getText("adminobject-class")[0];
                    String ddIface = ddBean.getText("adminobject-interface")[0];
                    if(ddClass.equals(aoClass) && ddIface.equals(aoIface)) {
                        target = ddBean;
                        ddBeans.remove(j);
                        break;
                    }
                }
                if(target == null) {
                    System.out.println("Geronimo connector deployment plan has admin object with interface '"+aoIface+"' and class '"+aoClass+"' but the ra.xml does not have a matching adminobject declared.  Deleting this adminobject from the Geronimo plan.");
                    continue;
                }
                dcb = new AdminObjectDCB(target, admin);
                dcbs.put("class "+aoClass+" iface "+aoIface, dcb);
            }
        }
        // There are some admin object types in ra.xml with no matching instances; create DConfigBeans for those
        for (int i = 0; i < ddBeans.size(); i++) {
            DDBean ddBean = (DDBean) ddBeans.get(i);
            String ddClass = ddBean.getText("adminobject-class")[0];
            String ddIface = ddBean.getText("adminobject-interface")[0];
            GerAdminobjectType admin = connector.addNewAdminobject();
            dcbs.put("class "+ddClass+" iface "+ddIface, new AdminObjectDCB(ddBean, admin));
        }
        List adminResults = new ArrayList();
        for (Iterator it = dcbs.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            DConfigBean value = (DConfigBean) dcbs.get(key);
            adminResults.add(value);
        }
        adminobjects = (AdminObjectDCB[]) adminResults.toArray(new AdminObjectDCB[adminResults.size()]);

        //todo: Handle the GBean children
    }

    GerConnectorType getConnector() {
        return (GerConnectorType) getXmlObject();
    }


    // ----------------------- JavaBean Properties for /connector ----------------------

    //todo: the following child elements
    // gbean*

    public EnvironmentData getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentData environment) {
        EnvironmentData old = this.environment;
        this.environment = environment;
        if((old == null && environment == null) || (old != null&& old == environment)) {
            return;
        }
        if(old != null) {
            getConnector().unsetEnvironment();
        }
        if(environment != null) {
            environment.configure(getConnector().addNewEnvironment());
        }
        pcs.firePropertyChange("environment", old, environment);
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
