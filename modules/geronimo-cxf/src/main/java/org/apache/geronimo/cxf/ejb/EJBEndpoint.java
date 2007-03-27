/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.cxf.ejb;

import java.net.URL;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.geronimo.cxf.CXFEndpoint;
import org.apache.geronimo.cxf.CXFServiceConfiguration;
import org.apache.geronimo.cxf.GeronimoJaxWsImplementorInfo;
import org.apache.geronimo.jaxws.JAXWSUtils;
import org.apache.openejb.DeploymentInfo;

public class EJBEndpoint extends CXFEndpoint {

    public EJBEndpoint(Bus bus,
                       URL configurationBaseUrl,
                       Class instance) {
        super(bus, instance);
                
        String bindingURI = null;
        if (this.portInfo.getProtocolBinding() != null) {
            bindingURI = JAXWSUtils.getBindingURI(this.portInfo.getProtocolBinding());
        }
        implInfo = new GeronimoJaxWsImplementorInfo((Class)implementor, bindingURI);

        serviceFactory = new JaxWsServiceFactoryBean(implInfo);       
        serviceFactory.setBus(bus);

        String wsdlLocation = null;
        if (this.portInfo.getWsdlFile() != null) {
            wsdlLocation = this.portInfo.getWsdlFile();
        } else {
            wsdlLocation = implInfo.getWsdlLocation();
        }        
        URL wsdlURL = getWsdlURL(configurationBaseUrl, wsdlLocation);

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CXFServiceConfiguration configuration = 
            new CXFServiceConfiguration(this.portInfo, wsdlURL);
        serviceFactory.getConfigurations().add(0, configuration);

        service = serviceFactory.create();

        service.put(Message.SCHEMA_VALIDATION_ENABLED, 
                    service.getEnableSchemaValidationForAllPort());

        DeploymentInfo deploymentInfo = (DeploymentInfo)bus.getExtension(DeploymentInfo.class);
        service.setInvoker(new EJBMethodInvoker(deploymentInfo));       
    }
    
    protected Class getImplementorClass() {
        return (Class)this.implementor;
    }
    
}
