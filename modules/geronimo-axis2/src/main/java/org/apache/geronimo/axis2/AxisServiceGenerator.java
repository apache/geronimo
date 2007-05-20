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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceProviderAnnot;
import org.apache.axis2.jaxws.description.builder.WsdlComposite;
import org.apache.axis2.jaxws.description.builder.WsdlGenerator;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;

//TODO: Handle RPC Style Messaging

/**
 * @version $Rev$ $Date$
 */
public class AxisServiceGenerator {
    private static final Log log = LogFactory.getLog(AxisServiceGenerator.class);
    
    private static String WSDL_ENCODING = "UTF-8";
    
    public AxisServiceGenerator(){
        super();
    }
    

    public AxisService getServiceFromWSDL(PortInfo portInfo, String endpointClassName, URL configurationBaseUrl, ClassLoader classLoader) throws Exception {
        Definition wsdlDefinition = getWSDLDefinition(portInfo, configurationBaseUrl, classLoader);

        Map<QName, Service> serviceMap = wsdlDefinition.getServices();
        Service wsdlService = serviceMap.values().iterator().next();

        Map<String, Port> portMap = wsdlService.getPorts();
        Port port = portMap.values().iterator().next();
        String portName = portInfo.getWsdlPort() == null ? port.getName() : portInfo.getWsdlPort().getLocalPart();
        QName serviceQName = portInfo.getWsdlService() == null ? wsdlService.getQName() : portInfo.getWsdlService();

        WSDLToAxisServiceBuilder wsdlBuilder = new WSDL11ToAxisServiceBuilder(wsdlDefinition, serviceQName , portName);

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
        } else if (dbc.getWebServiceProviderAnnot() != null) { 
            //TODO: can webservice and webservice provider annot co-exist?
            WebServiceProviderAnnot serviceProviderAnnot = dbc.getWebServiceProviderAnnot(); 
            serviceProviderAnnot.setPortName(portName);
            serviceProviderAnnot.setServiceName(service.getName());
            serviceProviderAnnot.setTargetNamespace(service.getTargetNamespace());
        }

        for(Iterator<AxisOperation> opIterator = service.getOperations() ; opIterator.hasNext() ;){
            AxisOperation operation = opIterator.next();
            operation.setMessageReceiver(JAXWSMessageReceiver.class.newInstance());
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
    
    protected Definition getWSDLDefinition(PortInfo portInfo, URL configurationBaseUrl, ClassLoader classLoader) throws IOException, WSDLException {
        String wsdlFile = portInfo.getWsdlFile();
        Definition wsdlDefinition = null;
        if (wsdlFile == null || wsdlFile.equals("")) {
            log.debug("A WSDL file was not supplied.");
            return null;
        } else {
            URL wsdlURL = getWsdlURL(wsdlFile, configurationBaseUrl, classLoader);
            InputStream wsdlStream;
            try {
                wsdlStream = wsdlURL.openStream();
                if(wsdlStream == null){
                    throw new IOException("unable to read descriptor " + wsdlURL);
                }
                else {
                    WSDLFactory factory = WSDLFactory.newInstance();
                    WSDLReader reader = factory.newWSDLReader();
                    reader.setFeature("javax.wsdl.importDocuments", true);
                    reader.setFeature("javax.wsdl.verbose", false);
                    wsdlDefinition = reader.readWSDL(wsdlURL.toString());
                    wsdlStream.close();                   
                }
            } catch (RuntimeException e) {
                throw new RuntimeException("invalid WSDL provided " + wsdlURL);
            } 
            return wsdlDefinition;
        }
    }
    
    private URL getWsdlURL(String wsdlFile, URL configurationBaseUrl, ClassLoader classLoader) {
        URL wsdlURL = null;
        if (wsdlFile != null) {

            try {
                wsdlURL = new URL(wsdlFile);
            } catch (MalformedURLException e) {
                // Not a URL, try as a resource
                wsdlURL = classLoader.getResource("/" + wsdlFile);

                if (wsdlURL == null && configurationBaseUrl != null) {
                    // Cannot get it as a resource, try with
                    // configurationBaseUrl
                    try {
                        wsdlURL = new URL(configurationBaseUrl.toString()
                                + wsdlFile);
                    } catch (MalformedURLException ee) {
                        // ignore
                    }
                }
            }
        }
        return wsdlURL;
    }
}
