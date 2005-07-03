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

package org.apache.geronimo.web.deployment;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.naming.deployment.ENCHelper;
import org.apache.geronimo.xbeans.geronimo.web.WebAppType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * @version $Rev: 56771 $ $Date: 2004-11-06 12:58:54 -0700 (Sat, 06 Nov 2004) $
 */
public class WebAppDConfigBean extends DConfigBeanSupport {
    private final ENCHelper encHelper;

    WebAppDConfigBean(DDBean ddBean, WebAppType webApp) {
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

    WebAppType getWebApp() {
        return (WebAppType) getXmlObject();
    }

    public String getContextRoot() {
        return getWebApp().getContextRoot();
    }

    public void setContextRoot(String contextRoot) {
        pcs.firePropertyChange("contextRoot", getContextRoot(), contextRoot);
        getWebApp().setContextRoot(contextRoot);
    }

    /**
     * getContextPriorityClassLoader.
     *
     * @return True if this context should give web application class in preference over the containers
     *         classes, as per the servlet specification recommendations.
     */
    public boolean getContextPriorityClassLoader() {
        return getWebApp().getContextPriorityClassloader();
    }

    /**
     * setContextPriorityClassLoader.
     *
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
