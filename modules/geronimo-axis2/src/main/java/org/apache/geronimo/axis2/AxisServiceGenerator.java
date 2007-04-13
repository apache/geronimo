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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
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
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL20ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ResponseWrapperAnnot;
import org.apache.axis2.jaxws.description.builder.WebFaultAnnot;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;
import org.apache.axis2.jaxws.description.builder.WebResultAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.builder.WsdlComposite;
import org.apache.axis2.jaxws.description.builder.WsdlGenerator;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
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
    
    private static String WSDL_ENCODING = "UTF-8";
    
    public AxisServiceGenerator(){
        super();
    }
    
    public AxisService getServiceFromWSDL(PortInfo portInfo, String endpointClassName, URL configurationBaseUrl, ClassLoader classLoader) throws Exception {
        WSDLToAxisServiceBuilder wsdlBuilder = null;
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLWriter writer = factory.newWSDLWriter();
        Definition wsdlDefinition = getWSDLDefition(portInfo, configurationBaseUrl, classLoader);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.writeWSDL(wsdlDefinition, out);
        String wsdlContent = out.toString(WSDL_ENCODING); //Will the Axis2 give us this information soon ?
        
        OMNamespace documentElementNS = ((OMElement)XMLUtils.toOM(new StringReader(wsdlContent))).getNamespace();
           
        Map<QName, Service> serviceMap = wsdlDefinition.getServices();
        Service wsdlService = serviceMap.values().iterator().next();
        
        Map<String, Port> portMap = wsdlService.getPorts();
        Port port = portMap.values().iterator().next();
        String portName = port.getName();
        QName serviceQName = wsdlService.getQName();

        //Decide WSDL Version : 
        if(WSDL2Constants.WSDL_NAMESPACE.equals(documentElementNS.getNamespaceURI())){
            wsdlBuilder = new WSDL20ToAxisServiceBuilder(new ByteArrayInputStream(wsdlContent.getBytes()), serviceQName, null);
        }
        else if(Constants.NS_URI_WSDL11.equals(documentElementNS.getNamespaceURI())){
            wsdlBuilder = new WSDL11ToAxisServiceBuilder(wsdlDefinition, serviceQName , portName);
        }
        //populate with axis2 objects
        AxisService service = wsdlBuilder.populateService();
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, endpointClassName));
        service.setWsdlFound(true);
        service.setClassLoader(classLoader);
        
        //Going to create annotations by hand
        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
        dbc.setClassLoader(classLoader);
        HashMap<String, DescriptionBuilderComposite> dbcMap = new HashMap<String, DescriptionBuilderComposite>();
        
        //Service related annotations
        WebServiceAnnot serviceAnnot = WebServiceAnnot.createWebServiceAnnotImpl();
        serviceAnnot.setPortName(portName);
        serviceAnnot.setServiceName(service.getName());
        serviceAnnot.setName(service.getName());
        serviceAnnot.setTargetNamespace(service.getTargetNamespace());
      
        Class endPointClass = classLoader.loadClass(endpointClassName);
        Method[] classMethods = endPointClass.getMethods();
         
        for(Iterator<AxisOperation> opIterator = service.getOperations() ; opIterator.hasNext() ;){
            AxisOperation operation = opIterator.next();
            operation.setMessageReceiver(JAXWSMessageReceiver.class.newInstance());
                
            for(Method method : classMethods){
                String axisOpName = operation.getName().getLocalPart();
                if(method.getName().equals(axisOpName)){
                    fillOperationInformation(method, operation, dbc);
                    break;
                }
            }
        }
        
        dbc.setWebServiceAnnot(serviceAnnot);
        dbc.setWsdlDefinition(wsdlDefinition);
        dbc.setClassName(endpointClassName);
        dbc.setCustomWsdlGenerator(new WSDLGeneratorImpl(wsdlDefinition));
        dbcMap.put(endpointClassName, dbc);
        List<ServiceDescription> serviceDescList = DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        ServiceDescription sd = serviceDescList.get(0);
        Parameter serviceDescription = new Parameter(EndpointDescription.AXIS_SERVICE_PARAMETER, sd.getEndpointDescriptions()[0]);
        service.addParameter(serviceDescription);
        
        return service;
    }
    
    private void fillOperationInformation(Method method, AxisOperation operation, DescriptionBuilderComposite dbc) throws Exception{
        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        WebMethodAnnot webMethodAnnot = WebMethodAnnot.createWebMethodAnnotImpl();
        webMethodAnnot.setOperationName(method.getName());
           
        if(operation.getStyle().equals(AxisOperation.STYLE_DOC)){
            fillDocOperationInfo(method, operation, dbc, mdc, webMethodAnnot);
        }else if(operation.getStyle().equals(AxisOperation.STYLE_RPC)){
            throw new RuntimeException("Not Yet Implemented");
        }else {
        	//TODO:
        }
    }
    
    private void fillDocOperationInfo(Method method, AxisOperation operation, DescriptionBuilderComposite dbc, MethodDescriptionComposite mdc, WebMethodAnnot webMethodAnnot ) throws Exception{
        mdc.setWebMethodAnnot(webMethodAnnot);
        mdc.setMethodName(method.getName());

        String MEP = operation.getMessageExchangePattern();
         
        if (WSDLUtil.isInputPresentForMEP(MEP)) {
            AxisMessage inAxisMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if(inAxisMessage != null){
                 
                XmlSchemaElement element = inAxisMessage.getSchemaElement();
                
                if(element != null){
                	XmlSchemaType schemaType = element.getSchemaType();
                	
                    if(schemaType instanceof XmlSchemaComplexType){

                        XmlSchemaComplexType complexSchemaType = (XmlSchemaComplexType)element.getSchemaType();
                        XmlSchemaParticle particle = complexSchemaType.getParticle();
                         
                        if (particle instanceof XmlSchemaSequence) {
                            XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;
                            Iterator iterator = xmlSchemaSequence.getItems().getIterator();
                            int i = 0;
                            
                            while (iterator.hasNext()) {
                                XmlSchemaElement innerElement = (XmlSchemaElement) iterator.next();
                                XmlSchemaType innerElementSchemaType = innerElement.getSchemaType();
                                 
                                if(!(innerElementSchemaType instanceof XmlSchemaComplexType)){
                                    element = innerElement;
                                    if(element != null){
                                        attachPDC(mdc, method, element, i);
                                    }
                                    i++;
                                    element = null;
                                }else { 
                                    XmlSchemaComplexType innerComplexSchemaType = (XmlSchemaComplexType)innerElementSchemaType;
                                    XmlSchemaParticle innerParticle = innerComplexSchemaType.getParticle();
                                    XmlSchemaSequence innerXmlSchemaSequence = (XmlSchemaSequence) innerParticle;
                                    iterator = innerXmlSchemaSequence.getItems().getIterator();
                                }
                            }
                        }
                    }
                }
                 
                if(element != null){
                    ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
                    WebParamAnnot webParamAnnot = WebParamAnnot.createWebParamAnnotImpl();
                    webParamAnnot.setName(element.getName());
                    pdc.setWebParamAnnot(webParamAnnot);  
                    Class[] paramTypes = method.getParameterTypes();
                    //TODO: I think there is a bug here - the last paramType.getName will overwrite the previous ones.    
                    for(Class paramType : paramTypes){
                        String strParamType = paramType.getName();                        
                        pdc.setParameterType(strParamType);
                    }
                    mdc.addParameterDescriptionComposite(pdc);
                }
            }
        }
         
        if (WSDLUtil.isOutputPresentForMEP(MEP)) {
            AxisMessage outAxisMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
             
            if(outAxisMessage != null){   
                if(!method.getReturnType().toString().equals("void")){
                	String strReturnTempArray = method.getReturnType().getName();
                	
                	mdc.setReturnType(strReturnTempArray);
                     
                    XmlSchemaElement element = outAxisMessage.getSchemaElement();
                    XmlSchemaType schemaType = element.getSchemaType();
                     
                    if(schemaType instanceof XmlSchemaComplexType){

                        XmlSchemaComplexType complexSchemaType = (XmlSchemaComplexType)element.getSchemaType();
                        XmlSchemaParticle particle = complexSchemaType.getParticle();
                         
                        // TODO: What if we have more than one complex type in a sequence ???
                        if (particle instanceof XmlSchemaSequence) {
                            XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;
                            Iterator iterator = xmlSchemaSequence.getItems().getIterator();
                             
                            while (iterator.hasNext()) {
                                XmlSchemaElement innerElement = (XmlSchemaElement) iterator.next();
                                XmlSchemaType innerElementSchemaType = innerElement.getSchemaType();
                                 
                                if(!(innerElementSchemaType instanceof XmlSchemaComplexType)){
                                    element = innerElement;
                                    break;
                                }else { 
                                    XmlSchemaComplexType innerComplexSchemaType = (XmlSchemaComplexType)innerElementSchemaType;
                                    XmlSchemaParticle innerParticle = innerComplexSchemaType.getParticle();
                                    XmlSchemaSequence innerXmlSchemaSequence = (XmlSchemaSequence) innerParticle;
                                    iterator = innerXmlSchemaSequence.getItems().getIterator();
                                }
                            }
                        }
                    }
                     
                    WebResultAnnot webResult = WebResultAnnot.createWebResultAnnotImpl();
                    webResult.setName(element.getName());
                    mdc.setWebResultAnnot(webResult);

                    ResponseWrapperAnnot responseWrap = ResponseWrapperAnnot.createResponseWrapperAnnotImpl();
                    responseWrap.setClassName(getWrapperClassName(outAxisMessage.getElementQName()));
                    responseWrap.setTargetNamespace(outAxisMessage.getElementQName().getNamespaceURI());
                    mdc.setResponseWrapperAnnot(responseWrap);
                } 
            }
        }else {
        	//TODO: JAXWS spec says need to check Holder param exist before taking a method as OneWay
        	mdc.setOneWayAnnot(true); 
        }
         
        List<AxisMessage> faultMessages = operation.getFaultMessages();
        if(faultMessages != null){
        	for(AxisMessage faultMessage: faultMessages){
        		WebFaultAnnot faultAnnot = WebFaultAnnot.createWebFaultAnnotImpl();
        		faultAnnot.setName(faultMessage.getName());
        		dbc.setWebFaultAnnot(faultAnnot);
        	}
        }
         
        mdc.setWebMethodAnnot(webMethodAnnot);
        dbc.addMethodDescriptionComposite(mdc);
    }
    
    //TODO: Has to verify how JAXB default class wrapper class generation logic
    //Note that this only works when the package name is based on the domain name of the namespace.
    private String getWrapperClassName(QName element) throws Exception {
        String localPart = element.getLocalPart();
        String nameSpace = element.getNamespaceURI();
        
        URI nameSpaceURI = new URI(nameSpace);
        
        String host = nameSpaceURI.getHost();
        String path = nameSpaceURI.getPath();
        String[] hostParts = host.split("\\.");
        String[] pathParts = path.split("/");
        String packageName = "";
        
        for (int i = hostParts.length - 1; i > -1; i--) {
            if (i == 0)
                packageName += hostParts[i];
            else 
                packageName += hostParts[i] + ".";
        }
        
        for(String pathPart : pathParts){
            if(!pathPart.equals("")){
                packageName += ("."+pathPart);
            }
        }
        
        String className = localPart;
        if(Character.isLowerCase(localPart.charAt(0))){
            className = (char)Character.toUpperCase(localPart.charAt(0))+className.substring(1, className.length());
        }
                
        return packageName + "." + className;
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
    
    protected Definition getWSDLDefition(PortInfo portInfo, URL configurationBaseUrl, ClassLoader classLoader) throws IOException, WSDLException {
        String wsdlFile = portInfo.getWsdlFile();
        Definition wsdlDefinition = null;
        if(wsdlFile == null || wsdlFile.equals(""))
            return null;
        else {
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
    
    private void attachPDC(MethodDescriptionComposite mdc, Method method, XmlSchemaElement element, int j) {
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        WebParamAnnot webParamAnnot = WebParamAnnot.createWebParamAnnotImpl();
        webParamAnnot.setName(element.getName());
        pdc.setMethodDescriptionCompositeRef(mdc);
        pdc.setWebParamAnnot(webParamAnnot); 
        Class[] paramTypes = method.getParameterTypes();
            
        if (j < paramTypes.length){
            pdc.setParameterType(paramTypes[j].getName());
            pdc.setListOrder(j);
        }
        mdc.addParameterDescriptionComposite(pdc);
    }
}
