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

package org.apache.geronimo.jetty.deployment;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.naming.deployment.ENCHelper;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerLocalRefType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class WebAppDConfigBean extends DConfigBeanSupport {

    private ENCHelper encHelper;

    WebAppDConfigBean(DDBean ddBean, JettyWebAppType webApp) {
        super(ddBean, webApp);

        ENCHelper.XmlEnvRefs envRefs = new ENCHelper.XmlEnvRefs() {
            public GerRemoteRefType[] getEjbRefs() {
                return getWebApp().getEjbRefArray();
            }

            public GerRemoteRefType addNewEjbRef() {
                return getWebApp().addNewEjbRef();
            }

            public GerRemoteRefType setEjbRef(int i, GerRemoteRefType remoteRef) {
                getWebApp().setEjbRefArray(i, remoteRef);
                return getWebApp().getEjbRefArray(i);
            }

            public void removeEjbRef(int i) {
                getWebApp().removeEjbRef(i);
            }

            public GerLocalRefType[] getEjbLocalRefs() {
                return getWebApp().getEjbLocalRefArray();
            }

            public GerLocalRefType addNewEjbLocalRef() {
                return getWebApp().addNewEjbLocalRef();
            }

            public GerLocalRefType setEjbLocalRef(int i, GerLocalRefType localRef) {
                getWebApp().setEjbLocalRefArray(i, localRef);
                return getWebApp().getEjbLocalRefArray(i);
            }

            public void removeEjbLocalRef(int i) {
                getWebApp().removeEjbLocalRef(i);
            }

            public GerLocalRefType[] getResourceEnvRefs() {
                return getWebApp().getResourceEnvRefArray();
            }

            public GerLocalRefType addNewResourceEnvRef() {
                return getWebApp().addNewResourceEnvRef();
            }

            public GerLocalRefType setResourceEnvRef(int i, GerLocalRefType localRef) {
                getWebApp().setResourceEnvRefArray(i, localRef);
                return getWebApp().getResourceEnvRefArray(i);
            }

            public void removeResourceEnvRef(int i) {
                getWebApp().removeResourceEnvRef(i);
            }

            public GerLocalRefType[] getResourceRefs() {
                return getWebApp().getResourceRefArray();
            }

            public GerLocalRefType addNewResourceRef() {
                return getWebApp().addNewResourceRef();
            }

            public GerLocalRefType setResourceRef(int i, GerLocalRefType localRef) {
                getWebApp().setResourceRefArray(i, localRef);
                return getWebApp().getResourceRefArray(i);
            }

            public void removeResourceRef(int i) {
                getWebApp().removeResourceRef(i);
            }

        };
        //which version are we dealing with?
        String version = ddBean.getRoot().getAttributeValue("version");
        if ("2.4".equals(version)) {
            encHelper = new ENCHelper(ddBean, envRefs, getXPathsForJ2ee_1_4(ENCHelper.ENC_XPATHS), getXPathsForJ2ee_1_4(ENCHelper.NAME_XPATHS));
        } else {
            encHelper = new ENCHelper(ddBean, envRefs, getXPathsWithPrefix(null, ENCHelper.ENC_XPATHS), getXPathsWithPrefix(null, ENCHelper.NAME_XPATHS));
        }

    }

    JettyWebAppType getWebApp() {
        return (JettyWebAppType) getXmlObject();
    }

    public String getContextRoot() {
        return getWebApp().getContextRoot();
    }

    public void setContextRoot(String contextRoot) {
        pcs.firePropertyChange("contextRoot", getContextRoot(), contextRoot);
        getWebApp().setContextRoot(contextRoot);
    }

    /** getContextPriorityClassLoader.
     * @return True if this context should give web application class in preference over the containers
     * classes, as per the servlet specification recommendations.
     */
    public boolean getContextPriorityClassLoader() {
        return getWebApp().getContextPriorityClassloader();
    }

    /** setContextPriorityClassLoader.
     * @param contextPriority True if this context should give web application class in preference over the containers
     * classes, as per the servlet specification recommendations.
     */
    public void setContextPriorityClassLoader(boolean contextPriority) {
        pcs.firePropertyChange("contextPriorityClassLoader", getContextPriorityClassLoader(), contextPriority);
        getWebApp().setContextPriorityClassloader(contextPriority);
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

}
