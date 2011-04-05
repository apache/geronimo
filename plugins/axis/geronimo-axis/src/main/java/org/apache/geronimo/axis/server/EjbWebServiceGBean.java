/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis.server;

import java.net.URI;
import java.util.Collection;
import java.util.Properties;

import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.geronimo.security.jaas.ConfigurationFactory;
import org.apache.geronimo.webservices.SoapHandler;
import org.apache.openejb.server.axis.EjbRpcProvider;

@GBean(j2eeType = NameFactory.WEB_SERVICE_LINK)
public class EjbWebServiceGBean implements GBeanLifecycle {

    private final SoapHandler soapHandler;
    private final URI location;

    public EjbWebServiceGBean(@ParamReference(name = "EjbDeployment") EjbDeployment ejbDeploymentContext,
                              @ParamAttribute(name = "location") URI location,
                              @ParamAttribute(name = "wsdlURI") URI wsdlURI,
                              @ParamAttribute(name = "serviceInfo") ServiceInfo serviceInfo,
                              @ParamReference(name = "WebServiceContainer") Collection<SoapHandler> webContainers,
                              @ParamAttribute(name = "policyContextID") String policyContextID,
                              @ParamReference(name = "ConfigurationFactory") ConfigurationFactory configurationFactory,
                              @ParamAttribute(name = "realmName") String realmName,
                              @ParamAttribute(name = "authMethod") String authMethod,
                              @ParamAttribute(name = "virtualHosts") String[] virtualHosts,
                              @ParamAttribute(name = "properties") Properties properties) throws Exception {
        this.location = location;
        //for use as a template
        if (webContainers == null || webContainers.isEmpty()) {
            soapHandler = null;
            return;
        }
        this.soapHandler = webContainers.iterator().next();

        RPCProvider provider = new EjbRpcProvider(ejbDeploymentContext.getDeploymentInfo(), serviceInfo.getHandlerInfos());
        SOAPService service = new SOAPService(null, provider, null);

        JavaServiceDesc serviceDesc = serviceInfo.getServiceDesc();
        service.setServiceDescription(serviceDesc);

        ClassLoader classLoader = ejbDeploymentContext.getClassLoader();

        Class serviceEndpointInterface =
                classLoader.loadClass(ejbDeploymentContext.getServiceEndpointInterfaceName());

        service.setOption("className", serviceEndpointInterface.getName());
        serviceDesc.setImplClass(serviceEndpointInterface);

        AxisWebServiceContainer axisContainer = new AxisWebServiceContainer(location, wsdlURI, service, serviceInfo.getWsdlMap(), classLoader);
        if (soapHandler != null) {
            soapHandler.addWebService(location.getPath(),
                    virtualHosts,
                    axisContainer,
                    policyContextID,
                    configurationFactory,
                    realmName,
                    authMethod,
                    properties,
                    classLoader);
        }
    }

    public void doStart() throws Exception {

    }

    public void doStop() throws Exception {
        if (soapHandler != null) {
            soapHandler.removeWebService(location.getPath());
        }
    }

    public void doFail() {

    }

}
