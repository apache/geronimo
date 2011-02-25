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
package org.apache.geronimo.j2ee.deployment;

import java.util.Map;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.repository.Environment;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public interface WebServiceBuilder {

   /**
    * Introspect on the module file to locate web service for deployment.
    *
    * @param moduleFile J2EE module
    * @param isEJB is this an EJB archive?
    * @param correctedPortLocations mapping between port locations and paths.
    * @param environment
    * @param sharedContext map of builder-specific key to map of servlet names to port information, or an
    * empty map if no web services found.  Port information is opaque
    * to all except the WebServiceBuilder itself.
    * @throws DeploymentException if error encountered while introspecting the module.
    */
    void findWebServices(Module module, boolean isEJB, Map<String, String> correctedPortLocations, Environment environment, Map sharedContext) throws DeploymentException;

    //obviously these need the deployment descriptors, but I'm not sure in what form yet.
    /**
     * configure the supplied GBeanData to implement the POJO web service described in the deployment descriptor.
     * The GBeanData will be for a ServletHolder like gbean that is adapted to holding a ws stack that talks to a
     * POJO web service.  The web deployer is responsible for filling in the standard servlet info such as init params.
     * @param targetGBean
     * @param servletName
     * @param module
     * @param seiClassName
     * @param context
     * @return true if this builder configured this pojo
     * @throws DeploymentException
     */
    boolean configurePOJO(GBeanData targetGBean, String servletName, Module module, String seiClassName, DeploymentContext context) throws DeploymentException;

    /**
     * configure the supplied EJBContainer gbeandata to implement the ejb web service described in the deployment descriptor
     * N.B. this method is a complete guess and should be replaced by something useable right away!
     * @param targetGBean
     * @param ejbName
     * @param moduleFile
     * @param sharedContext
     * @param classLoader
     * @throws DeploymentException
     */
    boolean configureEJB(GBeanData targetGBean, String ejbName, Module module, Map sharedContext, Bundle bundle) throws DeploymentException;

}
