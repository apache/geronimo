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

package org.apache.geronimo.axis2;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.BindingTypeAnnot;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceProviderAnnot;
import org.apache.axis2.jaxws.description.builder.WsdlComposite;
import org.apache.axis2.jaxws.description.builder.WsdlGenerator;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;
import org.apache.axis2.jaxws.description.impl.URIResolverImpl;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.axis2.util.SimpleWSDLLocator;
import org.apache.geronimo.jaxws.PortInfo;

//TODO: Handle RPC Style Messaging

/**
 * @version $Rev$ $Date$
 */
public class AxisServiceGenerator {
    private static final Log log = LogFactory.getLog(AxisServiceGenerator.class);
    
    private static String WSDL_ENCODING = "UTF-8";
    
    private MessageReceiver messageReceiver;
    
    public AxisServiceGenerator(){
        this.messageReceiver = new JAXWSMessageReceiver();
    }
    
    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
   
    public AxisService getServiceFromWSDL(PortInfo portInfo, String endpointClassName, URL configurationBaseUrl, ClassLoader classLoader) throws Exception {
        String wsdlFile = portInfo.getWsdlFile();
        if (wsdlFile == null || wsdlFile.equals("")) {
            throw new Exception("WSDL file is required.");
        }
        Definition wsdlDefinition = readWSDL(wsdlFile, configurationBaseUrl, classLoader);

        Map<QName, Service> serviceMap = wsdlDefinition.getServices();
        Service wsdlService = serviceMap.values().iterator().next();

        Map<String, Port> portMap = wsdlService.getPorts();
        Port port = portMap.values().iterator().next();
        Binding binding = port.getBinding();
        List extElements = binding.getExtensibilityElements();
        Iterator extElementsIterator =extElements.iterator();
        String  bindingS = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING; //this is the default.
        while (extElementsIterator.hasNext()) {
            Object o = extElementsIterator.next();
            if (o instanceof SOAPBinding) {
                SOAPBinding sp = (SOAPBinding)o;
                if (sp.getElementType().getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/soap/")) {
                    //todo:  how to we tell if it is MTOM or not.
                    bindingS = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;
                } 
            } else if (o instanceof SOAP12Binding) {
                SOAP12Binding sp = (SOAP12Binding)o;
                if (sp.getElementType().getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/soap12/")) {
                   //todo:  how to we tell if it is MTOM or not.
                    bindingS = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;
                }
            } else if (o instanceof HTTPBinding) {
                HTTPBinding sp = (HTTPBinding)o;
                if (sp.getElementType().getNamespaceURI().equals("http://www.w3.org/2004/08/wsdl/http")) {
                    bindingS = javax.xml.ws.http.HTTPBinding.HTTP_BINDING;
                }               
            }
        }
        
        String portName = portInfo.getWsdlPort() == null ? port.getName() : portInfo.getWsdlPort().getLocalPart();
        QName serviceQName = portInfo.getWsdlService() == null ? wsdlService.getQName() : portInfo.getWsdlService();

        WSDLToAxisServiceBuilder wsdlBuilder = new WSDL11ToAxisServiceBuilder(wsdlDefinition, serviceQName , portName);
        wsdlBuilder.setCustomResolver(new URIResolverImpl(classLoader));
        
        //populate with axis2 objects
        AxisService service = wsdlBuilder.populateService();
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, endpointClassName));
        service.setWsdlFound(true);
        service.setClassLoader(classLoader);
        
        Class endPointClass = classLoader.loadClass(endpointClassName);
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(endPointClass);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();

        DescriptionBuilderComposite dbc = dbcMap.get(endpointClassName);
        dbc.setClassLoader(classLoader);
        dbc.setWsdlDefinition(wsdlDefinition);
        dbc.setClassName(endpointClassName);
        dbc.setCustomWsdlGenerator(new WSDLGeneratorImpl(wsdlDefinition));
        
        if (dbc.getWebServiceAnnot() != null) { //information specified in .wsdl should overwrite annotation.
            WebServiceAnnot serviceAnnot = dbc.getWebServiceAnnot();
            serviceAnnot.setPortName(portName);
            serviceAnnot.setServiceName(service.getName());
            serviceAnnot.setTargetNamespace(service.getTargetNamespace());
            if (dbc.getBindingTypeAnnot() !=null && bindingS != null && !bindingS.equals("")) {
                BindingTypeAnnot bindingAnnot = dbc.getBindingTypeAnnot();
                bindingAnnot.setValue(bindingS);
            }
        } else if (dbc.getWebServiceProviderAnnot() != null) { 
            WebServiceProviderAnnot serviceProviderAnnot = dbc.getWebServiceProviderAnnot(); 
            serviceProviderAnnot.setPortName(portName);
            serviceProviderAnnot.setServiceName(service.getName());
            serviceProviderAnnot.setTargetNamespace(service.getTargetNamespace());
            if (dbc.getBindingTypeAnnot() !=null && bindingS != null && !bindingS.equals("")) {
                BindingTypeAnnot bindingAnnot = dbc.getBindingTypeAnnot();
                bindingAnnot.setValue(bindingS);
            }
        }

        for(Iterator<AxisOperation> opIterator = service.getOperations() ; opIterator.hasNext() ;){
            AxisOperation operation = opIterator.next();
            operation.setMessageReceiver(this.messageReceiver);
            String MEP = operation.getMessageExchangePattern();
            if (!WSDLUtil.isOutputPresentForMEP(MEP)) {
                List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionComposite(operation.getName().toString());
                for(Iterator<MethodDescriptionComposite> mIterator = mdcList.iterator(); mIterator.hasNext();){
                    MethodDescriptionComposite mdc = mIterator.next();
                    //TODO: JAXWS spec says need to check Holder param exist before taking a method as OneWay
                    mdc.setOneWayAnnot(true);
                }
            }
        }
        
        List<ServiceDescription> serviceDescList = DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        if ((serviceDescList != null) && (serviceDescList.size() > 0)) {
            ServiceDescription sd = serviceDescList.get(0);
            Parameter serviceDescription = new Parameter(EndpointDescription.AXIS_SERVICE_PARAMETER, sd.getEndpointDescriptions()[0]);
            service.addParameter(serviceDescription);
        } else {
            log.debug("No ServiceDescriptions found.");
        }

        return service;
    }

    private class WSDLGeneratorImpl implements WsdlGenerator {
        private Definition def;

        public WSDLGeneratorImpl(Definition def) {
            this.def = def;
        }
        
        public WsdlComposite generateWsdl(String implClass, String bindingType) throws WebServiceException {
            // Need WSDL generation code
            WsdlComposite composite = new WsdlComposite();
            composite.setWsdlFileName(implClass);
            HashMap<String, Definition> testMap = new HashMap<String, Definition>();
            testMap.put(composite.getWsdlFileName(), def);
            composite.setWsdlDefinition(testMap);
            return composite;
        }
    }
    
    protected Definition readWSDL(String wsdlLocation, 
                                  URL configurationBaseUrl, 
                                  ClassLoader classLoader)
        throws IOException, WSDLException {
        Definition wsdlDefinition = null;
        URL wsdlURL = getWsdlURL(wsdlLocation, configurationBaseUrl, classLoader);
        InputStream wsdlStream = null;
        try {
            wsdlStream = wsdlURL.openStream();
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            reader.setFeature("javax.wsdl.verbose", false);
            SimpleWSDLLocator wsdlLocator = new SimpleWSDLLocator(wsdlURL.toString());
            wsdlDefinition = reader.readWSDL(wsdlLocator);
        } finally {
            if (wsdlStream != null) {
                wsdlStream.close();
            }
        }
        return wsdlDefinition;
    }
    
    public static URL getWsdlURL(String wsdlFile, URL configurationBaseUrl, ClassLoader classLoader) {
        URL wsdlURL = null;
        if (wsdlFile != null) {
            try {
                wsdlURL = new URL(wsdlFile);
            } catch (MalformedURLException e) {
                // Not a URL, try as a resource
                wsdlURL = classLoader.getResource(wsdlFile);

                if (wsdlURL == null && configurationBaseUrl != null) {
                    // Cannot get it as a resource, try with
                    // configurationBaseUrl
                    try {
                        wsdlURL = new URL(configurationBaseUrl, wsdlFile);
                    } catch (MalformedURLException ee) {
                        // ignore
                    }
                }
            }
        }
        return wsdlURL;
    }
          
}
