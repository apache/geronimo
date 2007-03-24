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

package org.apache.geronimo.axis2.client;

import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.HandlerResolver;

import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.axis2.Axis2HandlerResolver;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.jaxws.JAXWSAnnotationProcessor;
import org.apache.geronimo.jaxws.JNDIResolver;
import org.apache.geronimo.jaxws.client.EndpointInfo;
import org.apache.geronimo.jaxws.client.JAXWSServiceReference;
import org.apache.geronimo.xbeans.javaee.HandlerChainsDocument;
import org.apache.geronimo.xbeans.javaee.HandlerChainsType;
import org.apache.xmlbeans.XmlException;

/**
 * @version $Rev$ $Date$
 */
public class Axis2ServiceReference extends JAXWSServiceReference {

    private static final Log log = LogFactory.getLog(Axis2ServiceReference.class);

    public Axis2ServiceReference(String serviceClassName,
                                 String referenceClassName,
                                 URI wsdlURI,
                                 QName serviceQName,
                                 AbstractName name,
                                 String handlerChainsXML,
                                 Map<Object, EndpointInfo> seiInfoMap) {
        super(handlerChainsXML, seiInfoMap, name, serviceQName, wsdlURI, referenceClassName, serviceClassName);
        System.setProperty("javax.xml.ws.spi.Provider", "org.apache.axis2.jaxws.spi.Provider");
    }

    protected HandlerChainsType getHandlerChains() {
        try {
            if (this.handlerChainsXML == null) //handlerChains could be null if they are not specified.
                return null;
            else
                return HandlerChainsDocument.Factory.parse(this.handlerChainsXML).getHandlerChains();
        } catch (XmlException e) {
            log.warn("Failed to deserialize handler chains", e);
            return null;
        }
    }

    protected HandlerResolver getHandlerResolver(Class serviceClass) {
        JAXWSAnnotationProcessor annotationProcessor =
                new JAXWSAnnotationProcessor(new JNDIResolver(), new WebServiceContextImpl());
        Axis2HandlerResolver handlerResolver =
                new Axis2HandlerResolver(classLoader, serviceClass, getHandlerChains(), annotationProcessor);
        return handlerResolver;
    }
}
