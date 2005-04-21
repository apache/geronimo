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
package org.apache.geronimo.j2ee.deployment;

import java.util.jar.JarFile;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.common.DeploymentException;

/**
 * @version $Rev:  $ $Date:  $
 */
public class UnavailableWebServiceBuilder implements WebServiceBuilder {

    public Map parseWebServiceDescriptor(URL wsDDUrl, JarFile moduleFile, boolean isEJB) throws DeploymentException {
        return new HashMap();
    }

    public void configurePOJO(GBeanData targetGBean, JarFile moduleFile, Object portInfo, String seiClassName, ClassLoader classLoader) throws DeploymentException {
        throw new DeploymentException("Web services are not available in this configuration");
    }

    public void configureEJB(GBeanData targetGBean, Object portInfoObject, String seiClassName) throws DeploymentException {
        throw new DeploymentException("Web services are not available in this configuration");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(UnavailableWebServiceBuilder.class);
        infoBuilder.addInterface(WebServiceBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
