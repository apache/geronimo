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

package org.apache.geronimo.web.deployment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.dconfigbean.DConfigBeanSupport;
import org.apache.geronimo.deployment.service.jsr88.EnvironmentData;
import org.apache.geronimo.naming.deployment.ENCHelper;
import org.apache.geronimo.naming.deployment.jsr88.EjbLocalRef;
import org.apache.geronimo.naming.deployment.jsr88.EjbRef;
import org.apache.geronimo.naming.deployment.jsr88.MessageDestination;
import org.apache.geronimo.naming.deployment.jsr88.ResourceEnvRef;
import org.apache.geronimo.naming.deployment.jsr88.ResourceRef;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerAbstractSecurityType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerServiceRefType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * @version $Rev$ $Date$
 */
public class WebAppDConfigBean extends DConfigBeanSupport {
    private final ENCHelper encHelper;
    private EnvironmentData environment;
    private EjbRef[] ejbRefs = new EjbRef[0];
    private EjbLocalRef[] ejbLocalRefs = new EjbLocalRef[0];
    private ResourceRef[] resourceRefs = new ResourceRef[0];
    private ResourceEnvRef[] resourceEnvRefs = new ResourceEnvRef[0];
    private MessageDestination[] messageDestinations = new MessageDestination[0];

    WebAppDConfigBean(DDBean ddBean, GerWebAppType webApp) {
        super(ddBean, webApp);

        ENCHelper.XmlEnvRefs envRefs = new ENCHelper.XmlEnvRefs(webApp.getEjbRefArray(), webApp.getEjbLocalRefArray(), webApp.getResourceRefArray(), webApp.getResourceEnvRefArray()); 

        //which version are we dealing with?
        String version = ddBean.getRoot().getAttributeValue("version");
        if ("2.4".equals(version)) {
            encHelper = new ENCHelper(ddBean, envRefs, getXPathsForJ2ee_1_4(ENCHelper.ENC_XPATHS), getXPathsForJ2ee_1_4(ENCHelper.NAME_XPATHS));
        } else {
            encHelper = new ENCHelper(ddBean, envRefs, getXPathsWithPrefix(null, ENCHelper.ENC_XPATHS), getXPathsWithPrefix(null, ENCHelper.NAME_XPATHS));
        }

    }

    GerWebAppType getWebApp() {
        return (GerWebAppType) getXmlObject();
    }

    public String getContextRoot() {
        return getWebApp().getContextRoot();
    }

    public void setContextRoot(String contextRoot) {
        pcs.firePropertyChange("contextRoot", getContextRoot(), contextRoot);
        getWebApp().setContextRoot(contextRoot);
    }

    public DConfigBean getDConfigBean(DDBean ddBean) throws ConfigurationException {
        return encHelper.getDConfigBean(ddBean);
    }

    public void removeDConfigBean(DConfigBean dcBean) throws BeanNotFoundException {
        encHelper.removeDConfigBean(dcBean);
    }

    public String[] getXpaths() {
        return getXPathsForJ2ee_1_4(ENCHelper.ENC_XPATHS);
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return WebAppDConfigRoot.SCHEMA_TYPE_LOADER;
    }

    // ----------------------- JavaBean Properties for web-app ----------------------

    public EnvironmentData getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentData environment) {
        EnvironmentData old = this.environment;
        this.environment = environment;
        if ((old == null && environment == null) || (old != null && old == environment)) {
            return;
        }
        if (old != null) {
            getWebApp().unsetEnvironment();
        }
        if (environment != null) {
            environment.configure(getWebApp().addNewEnvironment());
        }
        pcs.firePropertyChange("environment", old, environment);
    }

    public EjbRef[] getEjbRefs() {
        return ejbRefs;
    }

    public void setEjbRefs(EjbRef[] ejbRefs) {
        EjbRef[] old = this.ejbRefs;
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        this.ejbRefs = ejbRefs;
        // Handle current or new ejbRefs
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRef ejbRef = ejbRefs[i];
            //if(ejbRef.getEjbRef() == null) {
                ejbRef.configure(getWebApp().addNewEjbRef());
            //} else {
            //    before.remove(ejbRef);
            //}
        }
        // Handle removed or new ejbRefs
        for (Iterator it = before.iterator(); it.hasNext();) {
            EjbRef adapter = (EjbRef) it.next();
            GerEjbRefType all[] = getWebApp().getEjbRefArray();
            for (int i = 0; i < all.length; i++) {
                if(all[i] == adapter) {
                    getWebApp().removeEjbRef(i);
                    break;
                }
            }
        }
        pcs.firePropertyChange("ejb-ref", old, ejbRefs);
    }

    public EjbLocalRef[] getEjbLocalRefs() {
        return ejbLocalRefs;
    }

    public void setEjbLocalRefs(EjbLocalRef[] ejbLocalRefs) {
        EjbLocalRef[] old = this.ejbLocalRefs;
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        this.ejbLocalRefs = ejbLocalRefs;
        // Handle current or new ejbLocalRefs
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRef ejbLocalRef = ejbLocalRefs[i];
            //if(ejbLocalRef.getEjbLocalRef() == null) {
                ejbLocalRef.configure(getWebApp().addNewEjbLocalRef());
            //} else {
            //    before.remove(ejbLocalRef);
            //}
        }
        // Handle removed or new ejbLocalRefs
        for (Iterator it = before.iterator(); it.hasNext();) {
            EjbLocalRef adapter = (EjbLocalRef) it.next();
            GerEjbLocalRefType all[] = getWebApp().getEjbLocalRefArray();
            for (int i = 0; i < all.length; i++) {
                if(all[i] == adapter) {
                    getWebApp().removeEjbLocalRef(i);
                    break;
                }
            }
        }
        pcs.firePropertyChange("ejb-local-ref", old, ejbLocalRefs);
    }

    public ResourceRef[] getResourceRefs() {
        return resourceRefs;
    }

    public void setResourceRefs(ResourceRef[] resourceRefs) {
        ResourceRef[] old = this.resourceRefs;
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        this.resourceRefs = resourceRefs;
        // Handle current or new resourceRefs
        for (int i = 0; i < resourceRefs.length; i++) {
            ResourceRef resourceRef = resourceRefs[i];
            //if(resourceRef.getResourceRef() == null) {
            resourceRef.configure(getWebApp().addNewResourceRef());
            //} else {
            //    before.remove(resourceRef);
            //}
        }
        // Handle removed or new resourceRefs
        for (Iterator it = before.iterator(); it.hasNext();) {
            ResourceRef adapter = (ResourceRef) it.next();
            GerResourceRefType all[] = getWebApp().getResourceRefArray();
            for (int i = 0; i < all.length; i++) {
                if(all[i] == adapter) {
                    getWebApp().removeResourceRef(i);
                    break;
                }
            }
        }
        pcs.firePropertyChange("resource-ref", old, resourceRefs);
    }

    public ResourceEnvRef[] getResourceEnvRefs() {
        return resourceEnvRefs;
    }

    public void setResourceEnvRefs(ResourceEnvRef[] resourceEnvRefs) {
        ResourceEnvRef[] old = this.resourceEnvRefs;
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        this.resourceEnvRefs = resourceEnvRefs;
        // Handle current or new resourceEnvRefs
        for (int i = 0; i < resourceEnvRefs.length; i++) {
            ResourceEnvRef resourceEnvRef = resourceEnvRefs[i];
            //if(resourceEnvRef.getResourceEnvRef() == null) {
            resourceEnvRef.configure(getWebApp().addNewResourceEnvRef());
            //} else {
            //    before.remove(resourceEnvRef);
            //}
        }
        // Handle removed or new resourceEnvRefs
        for (Iterator it = before.iterator(); it.hasNext();) {
            ResourceEnvRef adapter = (ResourceEnvRef) it.next();
            GerResourceEnvRefType all[] = getWebApp().getResourceEnvRefArray();
            for (int i = 0; i < all.length; i++) {
                if(all[i] == adapter) {
                    getWebApp().removeResourceEnvRef(i);
                    break;
                }
            }
        }
        pcs.firePropertyChange("resource-env-ref", old, resourceEnvRefs);
    }

    public MessageDestination[] getMessageDestinations() {
        return messageDestinations;
    }

    public void setMessageDestinations(MessageDestination[] messageDestinations) {
        MessageDestination[] old = this.messageDestinations;
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        this.messageDestinations = messageDestinations;
        // Handle current or new messageDestinations
        for (int i = 0; i < messageDestinations.length; i++) {
            MessageDestination messageDestination = messageDestinations[i];
            //if(messageDestination.getMessageDestination() == null) {
            messageDestination.configure(getWebApp().addNewMessageDestination());
            //} else {
            //    before.remove(messageDestination);
            //}
        }
        // Handle removed or new messageDestinations
        for (Iterator it = before.iterator(); it.hasNext();) {
            MessageDestination adapter = (MessageDestination) it.next();
            GerMessageDestinationType all[] = getWebApp().getMessageDestinationArray();
            for (int i = 0; i < all.length; i++) {
                if(all[i] == adapter) {
                    getWebApp().removeMessageDestination(i);
                    break;
                }
            }
        }
        pcs.firePropertyChange("message-destination", old, messageDestinations);
    }

    public String getSecurityRealmName() {
        return getWebApp().getSecurityRealmName();
    }

    public void setSecurityRealmName(String securityRealmName) {
        pcs.firePropertyChange("securityRealmName", getSecurityRealmName(), securityRealmName);
        getWebApp().setSecurityRealmName(securityRealmName);
    }

    //TODO Method to be updated once DConfigBean for "security" is available
    public GerAbstractSecurityType getSecurity() {
        return getWebApp().getSecurity();
    }

    //TODO Method to be updated once DConfigBean for "security" is available
    public void setSecurity(GerAbstractSecurityType security) {
        pcs.firePropertyChange("security", getSecurity(), security);
        getWebApp().setSecurity(security);
    }

    //TODO Method to be updated once DConfigBean for "service-ref" is available
    public GerServiceRefType[] getServiceRefs() {
        return getWebApp().getServiceRefArray();
    }

    //TODO Method to be updated once DConfigBean for "service-ref" is available
    public void setServiceRefs(GerServiceRefType[] serviceRefArray){
        pcs.firePropertyChange("service-ref", getServiceRefs(), serviceRefArray);
        getWebApp().setServiceRefArray(serviceRefArray);
    }
    // ----------------------- End of JavaBean Properties ----------------------
}
