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

import java.net.URI;
import java.util.Map;
import java.util.List;
import java.util.Set;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.xbeans.j2ee.ServiceRefHandlerType;

/**
 * @version $Rev$ $Date$
 */
public interface ServiceReferenceBuilder {

    //it could return a Service or a Reference, we don't care
    Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException;

    //TODO a locate port method for links.

    public class HandlerInfoInfo {
        private final Set portNames;
        private final Class handlerClass;
        private final Map handlerConfig;
        private final QName[] soapHeaders;
        private final Set soapRoles;

        public HandlerInfoInfo(Set portNames, Class handlerClass, Map handlerConfig, QName[] soapHeaders, Set soapRoles) {
            this.portNames = portNames;
            this.handlerClass = handlerClass;
            this.handlerConfig = handlerConfig;
            this.soapHeaders = soapHeaders;
            this.soapRoles = soapRoles;
        }

        public Set getPortNames() {
            return portNames;
        }

        public Class getHandlerClass() {
            return handlerClass;
        }

        public Map getHandlerConfig() {
            return handlerConfig;
        }

        public QName[] getSoapHeaders() {
            return soapHeaders;
        }

        public Set getSoapRoles() {
            return soapRoles;
        }
    }
}
