/**
 *
 * Copyright 2004 The Apache Software Foundation
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
import org.apache.geronimo.naming.deployment.RefAdapter;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyLocalRefType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyRemoteRefType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 *
 *
 * @version $Revision: 1.13 $ $Date: 2004/03/09 18:03:52 $
 */
public class WebAppDConfigBean extends DConfigBeanSupport {

    private ENCHelper encHelper;

    WebAppDConfigBean(DDBean ddBean, JettyWebAppType webApp) {
        super(ddBean, webApp);

        ENCHelper.XmlEnvRefs envRefs = new ENCHelper.XmlEnvRefs() {
            public RefAdapter[] getEjbRefs() {
                return wrapArray(getWebApp().getEjbRefArray());
            }

            public RefAdapter addNewEjbRef() {
                return new JettyRefAdapter(getWebApp().addNewEjbRef());
            }

            public void setEjbRef(int i, RefAdapter refAdapter) {
                getWebApp().setEjbRefArray(i, (JettyRemoteRefType) refAdapter.getXmlObject());
                refAdapter.setXmlObject(getWebApp().getEjbRefArray(i));
            }

            public void removeEjbRef(int i) {
                getWebApp().removeEjbRef(i);
            }

            public RefAdapter[] getEjbLocalRefs() {
                return wrapArray(getWebApp().getEjbLocalRefArray());
            }

            public RefAdapter addNewEjbLocalRef() {
                return new JettyRefAdapter(getWebApp().addNewEjbLocalRef());
            }

            public void setEjbLocalRef(int i, RefAdapter refAdapter) {
                getWebApp().setEjbLocalRefArray(i, (JettyLocalRefType) refAdapter.getXmlObject());
                refAdapter.setXmlObject(getWebApp().getEjbLocalRefArray(i));
            }

            public void removeEjbLocalRef(int i) {
                getWebApp().removeEjbLocalRef(i);
            }

            public RefAdapter[] getMessageDestinationRefs() {
                return wrapArray(getWebApp().getMessageDestinationRefArray());
            }

            public RefAdapter addNewMessageDestinationRef() {
                return new JettyRefAdapter(getWebApp().addNewMessageDestinationRef());
            }

            public void setMessageDestinationRef(int i, RefAdapter refAdapter) {
                getWebApp().setMessageDestinationRefArray(i, (JettyLocalRefType) refAdapter.getXmlObject());
                refAdapter.setXmlObject(getWebApp().getMessageDestinationRefArray(i));
            }

            public void removeMessageDestinationRef(int i) {
                getWebApp().removeMessageDestinationRef(i);
            }

            public RefAdapter[] getResourceEnvRefs() {
                return wrapArray(getWebApp().getResourceEnvRefArray());
            }

            public RefAdapter addNewResourceEnvRef() {
                return new JettyRefAdapter(getWebApp().addNewResourceEnvRef());
            }

            public void setResourceEnvRef(int i, RefAdapter refAdapter) {
                getWebApp().setResourceEnvRefArray(i, (JettyLocalRefType) refAdapter.getXmlObject());
                refAdapter.setXmlObject(getWebApp().getResourceEnvRefArray(i));
            }

            public void removeResourceEnvRef(int i) {
                getWebApp().removeResourceEnvRef(i);
            }

            public RefAdapter[] getResourceRefs() {
                return wrapArray(getWebApp().getResourceRefArray());
            }

            public RefAdapter addNewResourceRef() {
                return new JettyRefAdapter(getWebApp().addNewResourceRef());
            }

            public void setResourceRef(int i, RefAdapter refAdapter) {
                getWebApp().setResourceRefArray(i, (JettyLocalRefType) refAdapter.getXmlObject());
                refAdapter.setXmlObject(getWebApp().getResourceRefArray(i));
            }

            public void removeResourceRef(int i) {
                getWebApp().removeResourceRef(i);
            }

            private RefAdapter[] wrapArray(JettyRemoteRefType[] refs) {
                RefAdapter[] wrapped = new RefAdapter[refs.length];
                for (int i = 0; i < refs.length; i++) {
                    wrapped[i] = new JettyRefAdapter(refs[i]);
                }
                return wrapped;
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
