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
//import org.apache.geronimo.naming.deployment.ENCHelper;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.12 $ $Date: 2004/02/25 09:57:44 $
 */
public class WebAppDConfigBean extends DConfigBeanSupport {

//    private ENCHelper encHelper;

    WebAppDConfigBean(DDBean ddBean, JettyWebAppType webApp) {
        super(ddBean, webApp);
/*
        encHelper = new ENCHelper(ddBean, new ENCHelper.XmlEnvRefs() {
            public GerEjbRefType[] getEjbRefs() {
                return getWebApp().getEjbRefArray();
            }

            public GerEjbRefType addNewEjbRef() {
                return getWebApp().addNewEjbRef();
            }

            public void removeEjbRef(int i) {
                getWebApp().removeEjbRef(i);
            }

            public GerEjbLocalRefType[] getEjbLocalRefs() {
                return getWebApp().getEjbLocalRefArray();
            }

            public GerEjbLocalRefType addNewEjbLocalRef() {
                return getWebApp().addNewEjbLocalRef();
            }

            public void removeEjbLocalRef(int i) {
                getWebApp().removeEjbLocalRef(i);
            }

            public GerMessageDestinationRefType[] getMessageDestinationRefs() {
                return getWebApp().getMessageDestinationRefArray();
            }

            public GerMessageDestinationRefType addNewMessageDestinationRef() {
                return getWebApp().addNewMessageDestinationRef();
            }

            public void removeMessageDestinationRef(int i) {
                getWebApp().removeMessageDestinationRef(i);
            }

            public GerResourceEnvRefType[] getResourceEnvRefs() {
                return getWebApp().getResourceEnvRefArray();
            }

            public GerResourceEnvRefType addNewResourceEnvRef() {
                return getWebApp().addNewResourceEnvRef();
            }

            public void removeResourceEnvRef(int i) {
                getWebApp().removeResourceEnvRef(i);
            }

            public GerResourceRefType[] getResourceRefs() {
                return getWebApp().getResourceRefArray();
            }

            public GerResourceRefType addNewResourceRef() {
                return getWebApp().addNewResourceRef();
            }

            public void removeResourceRef(int i) {
                getWebApp().removeResourceRef(i);
            }

        });
*/
    }

    JettyWebAppType getWebApp() {
        return (JettyWebAppType)getXmlObject();
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
        return null;
//        return encHelper.getDConfigBean(ddBean);
    }

    public void removeDConfigBean(DConfigBean dcBean) throws BeanNotFoundException {
        //encHelper.removeDConfigBean(dcBean);
    }

    public String[] getXpaths() {
        return null;
//        return ENCHelper.ENC_XPATHS;
    }

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return WebAppDConfigRoot.SCHEMA_TYPE_LOADER;
    }

}
