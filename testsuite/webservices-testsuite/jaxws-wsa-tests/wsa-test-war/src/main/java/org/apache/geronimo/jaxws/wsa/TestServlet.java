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
package org.apache.geronimo.jaxws.wsa;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import junit.framework.Assert;

import org.apache.geronimo.calculator.Calculator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
           
public abstract class TestServlet extends HttpServlet {
              
    private static final String NAMESPACE = 
        "http://geronimo.apache.org/calculator";
    
    private static final QName PORT = 
        new QName(NAMESPACE, "CalculatorPort");
    
    private static final QName SERVICE = 
        new QName(NAMESPACE, "CalculatorService");
        
    private static final String BASE_ACTION = 
        "http://geronimo.apache.org/calculator/Calculator";
    
    private static final String MULTIPLY_REQUEST_ACTION =
        "http://geronimo.apache.org/calculator/Calculator/multiplyRequest";
    
    private static final String ADD_REQUEST_ACTION =
        "http://geronimo.apache.org/calculator/Calculator/addRequest";
        
    public static final String MSG1 = 
        "<?xml version=\"1.0\"?><S:Envelope " +
        "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        "<S:Body>\n" +
        "<p:add xmlns:p=\"http://geronimo.apache.org/calculator\">\n" +
        "  <arg0>10</arg0>\n" +
        "  <arg1>10</arg1>\n" +
        "</p:add>\n" +
        "</S:Body></S:Envelope>";
    
    public static final String MSG2 = 
        "<?xml version=\"1.0\"?><S:Envelope " +
        "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
        "xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
        "<S:Header>" +
        "<wsa:To>{0}</wsa:To>\n" +
        "<wsa:MessageID>uuid:{1}</wsa:MessageID>\n" +
        "<wsa:Action>{2}</wsa:Action>\n" +
        "</S:Header>\n" +
        "<S:Body>\n" +
        "<p:add xmlns:p=\"http://geronimo.apache.org/calculator\">\n" +
        "  <arg0>{3}</arg0>\n" +
        "  <arg1>{3}</arg1>\n" +
        "</p:add>\n" +
        "</S:Body></S:Envelope>";
    
    public static final String REF_PARAM = 
        "<foo3:BarKey3 xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" wsa:IsReferenceParameter=\"1\" xmlns:foo3=\"http://geronimo.apache.org/calculator\">FooBar</foo3:BarKey3>\n";
    
    public static final String MSG3 = 
        "<S:Envelope " +
        "xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
        "xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
        "<S:Header>\n" +
        "<wsa:To>{0}</wsa:To>\n" +
        "<wsa:MessageID>uuid:{1}</wsa:MessageID>\n" +
        "<wsa:Action>{2}</wsa:Action>\n" +
        "<wsa:ReplyTo>\n" +
        "  <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
        "  <wsa:ReferenceParameters>\n" +
        "    <foo1:BarKey1 xmlns:foo1=\"http://geronimo.apache.org/calculator\">FooBarReply</foo1:BarKey1>\n" +
        "  </wsa:ReferenceParameters>" +
        "</wsa:ReplyTo>\n" +
        "<wsa:FaultTo>\n" +
        "  <wsa:Address>http://www.w3.org/2005/08/addressing/anonymous</wsa:Address>\n" +
        "  <wsa:ReferenceParameters>\n" +
        "    <foo2:BarKey2 xmlns:foo2=\"http://geronimo.apache.org/calculator\">FooBarFault</foo2:BarKey2>\n" +
        "  </wsa:ReferenceParameters>" +
        "</wsa:FaultTo>\n" +
        REF_PARAM +
        "</S:Header>\n" +
        "<S:Body>\n" +
        "<p:multiply xmlns:p=\"http://geronimo.apache.org/calculator\">\n" +
        "  <arg0>{3}</arg0>\n" +
        "  <arg1>{3}</arg1>\n" +
        "</p:multiply>\n" +
        "</S:Body></S:Envelope>";
    
    protected String address;
    
    protected Service service;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
        String testName = request.getParameter("test");
        
        System.out.println(testName);
        
        if (testName == null || !testName.startsWith("test")) {
            throw new ServletException("Invalid test name");
        }
        
        Method testMethod = null;
        try {
            testMethod = getClass().getMethod(testName, new Class [] {});
        } catch (Exception e1) {
            throw new ServletException("No such test: " + testName);        
        }
        
        try {
            testMethod.invoke(this, (Object[])null);
        } catch (IllegalArgumentException e) {
            throw new ServletException("Error invoking test: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ServletException("Error invoking test: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable root = e.getTargetException();
            ServletException ex = new ServletException("Test '" + testName + "' failed");
            ex.initCause(root);
            throw ex;
        }
  
        response.setContentType("text/plain");
        response.getWriter().println("Test '" + testName + "' passed");
    }

    public void testReferencePropertiesDispatch() throws Exception {
        Dispatch<SOAPMessage> dispatch = null;
        SOAPMessage request = null;
        SOAPMessage response = null;
        
        // test request and ReplyTo reference properties using Dispatch API
        dispatch = service.createDispatch(PORT, SOAPMessage.class, Service.Mode.MESSAGE); 
        request = TestUtils.createMessage(MessageFormat.format(MSG3, address, UUID.randomUUID(), MULTIPLY_REQUEST_ACTION, 5));
        response = dispatch.invoke(request);
        testMultiplyResponse(response, 25, false);
        TestUtils.testReferenceProperties(response, "BarKey1", "FooBarReply");
    }
    
    public void testReferenceProperties() throws Exception {
        // test request reference properties        
        W3CEndpointReferenceBuilder builder = createERPBuilder();
        Document refParam = TestUtils.createDocument(REF_PARAM);
        builder.referenceParameter(refParam.getDocumentElement());
        EndpointReference epr = builder.build();
        
        Calculator calc = service.getPort(epr, Calculator.class, new AddressingFeature());
        Assert.assertEquals(36, calc.multiply(6, 6));
    }
        
    public void testDispatch() throws Exception {
        Dispatch<SOAPMessage> dispatch = null;
        SOAPMessage request = null;
        SOAPMessage response = null;
        
        // make a call without Addressing support
        dispatch = service.createDispatch(PORT, SOAPMessage.class, Service.Mode.MESSAGE);
        request = TestUtils.createMessage(MSG1);
        try {
            response = dispatch.invoke(request);
            throw new ServletException("Did not throw exception");
        } catch (SOAPFaultException e) {
            // that's what we expect
        }
                
        // make a call with Addressing support 
        dispatch = service.createDispatch(PORT, SOAPMessage.class, Service.Mode.MESSAGE); 
        request = TestUtils.createMessage(MessageFormat.format(MSG2, address, UUID.randomUUID(), ADD_REQUEST_ACTION, 5));
        response = dispatch.invoke(request);
        testAddResponse(response, 10, false);
        
        // make a call with Addressing support and MTOM
        dispatch = service.createDispatch(PORT, SOAPMessage.class, Service.Mode.MESSAGE, new MTOMFeature());
        request = TestUtils.createMessage(MessageFormat.format(MSG2, address, UUID.randomUUID(), ADD_REQUEST_ACTION, 10));
        response = dispatch.invoke(request);
        testAddResponse(response, 20, true);
        
        EndpointReference epr = null;
        
        // make a call with Addressing support using EPR 
        epr = createEPR();

        service.createDispatch(epr, SOAPMessage.class, Service.Mode.MESSAGE);
        request = TestUtils.createMessage(MessageFormat.format(MSG2, address, UUID.randomUUID(), ADD_REQUEST_ACTION, 15));
        response = dispatch.invoke(request);
        testAddResponse(response, 30, false);
        
        // make a call with Addressing support using EPR and MTOM
        epr = createEPR();

        service.createDispatch(epr, SOAPMessage.class, Service.Mode.MESSAGE, new MTOMFeature());
        request = TestUtils.createMessage(MessageFormat.format(MSG2, address, UUID.randomUUID(), ADD_REQUEST_ACTION, 20));
        response = dispatch.invoke(request);
        testAddResponse(response, 40, true);
    }
   
    public void testPort() throws Exception {
        Calculator calc = null;
        
        // make a call without AddressingFeature
        calc = service.getPort(Calculator.class);
        try {
            calc.add(1, 1);
            throw new ServletException("Did not throw exception");
        } catch (SOAPFaultException e) {
            // that's what we expect
        }

        // make a call with AddressingFeature disabled
        calc = service.getPort(Calculator.class, new AddressingFeature(false, false));
        try {
            calc.add(1, 1);
            throw new ServletException("Did not throw exception");
        } catch (SOAPFaultException e) {
            // that's what we expect
        }
                
        // make a call with AddressingFeature enabled
        calc = service.getPort(Calculator.class, new AddressingFeature());
        Assert.assertEquals(4, calc.add(2, 2));
       
        // make a call with AddressingFeature enabled and port        
        calc = service.getPort(PORT, Calculator.class, new AddressingFeature());
        Assert.assertEquals(6, calc.add(3, 3));
                
        EndpointReference epr = null;
        
        // make a call using EPR from Binding
        epr = ((BindingProvider)calc).getEndpointReference();
        
        calc = service.getPort(epr, Calculator.class, new AddressingFeature());
        Assert.assertEquals(8, calc.add(4, 4));
        
        // make a call using EPR from Service
        epr = calc.getEPR();
        
        calc = service.getPort(epr, Calculator.class, new AddressingFeature());
        Assert.assertEquals(10, calc.add(5, 5));
        
        // make a call using created EPR
        epr = createEPR();
        
        calc = service.getPort(epr, Calculator.class, new AddressingFeature());
        Assert.assertEquals(12, calc.add(6, 6));
    }
    
    public void testWSDL() throws Exception {
        URL url = new URL(this.address + "?wsdl");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(30 * 1000);
        conn.setUseCaches(false);

        QName useAddressing = new QName("http://www.w3.org/2006/05/addressing/wsdl", "UsingAddressing");
        QName actionAttr = new QName("http://www.w3.org/2006/05/addressing/wsdl", "Action");
        
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        try {
            Definition def = 
                wsdlReader.readWSDL(null, new InputSource(conn.getInputStream()));
            
            // check for right wsa:Action element
            Assert.assertEquals(1, def.getPortTypes().size());
            PortType portType = (PortType)def.getPortTypes().values().iterator().next();
            Assert.assertEquals(3, portType.getOperations().size());
            Operation operation1 = portType.getOperation("getEPR", null, null);
            Assert.assertNotNull("operation", operation1);
            Input input1 = operation1.getInput();
            Assert.assertNotNull("input", input1);
            Object action = input1.getExtensionAttribute(actionAttr);
            Assert.assertNotNull("action", action);
            Assert.assertEquals("http://geronimo.apache.org/calculator/CalculatorPortType/getmyepr", action.toString());
            
            // check for presence wsaw:UsingAddressing element
            Assert.assertEquals(1, def.getBindings().size());
            Binding binding = (Binding)def.getBindings().values().iterator().next();
            Assert.assertEquals(3, binding.getBindingOperations().size());
            ExtensibilityElement wsa = TestUtils.getExtensibilityElement(binding.getExtensibilityElements(), useAddressing);
            Assert.assertNotNull("UsingAddressing", wsa);
        } finally {
            conn.disconnect();
        }
    }    
    
    private void testAddResponse(SOAPMessage message, int sum, boolean mtom) throws Exception {
        TestUtils.testResponse(message, BASE_ACTION + "/addResponse", "addResponse", sum, mtom);
    }
    
    private void testMultiplyResponse(SOAPMessage message, int sum, boolean mtom) throws Exception {
        TestUtils.testResponse(message, BASE_ACTION + "/multiplyResponse", "multiplyResponse", sum, mtom);
    }
        
    private W3CEndpointReferenceBuilder createERPBuilder() {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder = builder.address(address);
        builder = builder.serviceName(SERVICE);
        builder = builder.endpointName(PORT);
        return builder;
    }
    
    private EndpointReference createEPR() {       
        return createERPBuilder().build();
    }
    
    protected void updateAddress() {
        Calculator calc = service.getPort(Calculator.class);
        BindingProvider binding = (BindingProvider)calc;
        this.address = (String)binding.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        System.out.println("Set address: " + this.address);
    }
    
}
