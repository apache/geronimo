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

package org.apache.geronimo.jaxws.greeter_provider;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.activation.DataSource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.geronimo.jaxws.greeter_provider.common.MessageUtils;

public abstract class TestServlet extends HttpServlet {

    protected Service service;

    public static final QName GREETER_SERVICE = new QName("http://geronimo.apache.org/greeter_provider",
            "GreeterService");

    public static final QName GreeterHTTPDataSourcePort = new QName("http://geronimo.apache.org/greeter_provider",
            "GreeterHTTPDataSourcePort");

    public static final QName GreeterHTTPSourceMessageModePort = new QName(
            "http://geronimo.apache.org/greeter_provider", "GreeterHTTPSourceMessageModePort");

    public static final QName GreeterHTTPSourcePayloadModePort = new QName(
            "http://geronimo.apache.org/greeter_provider", "GreeterHTTPSourcePayloadModePort");

    public static final QName GreeterSOAP11SOAPMessagePort = new QName("http://geronimo.apache.org/greeter_provider",
            "GreeterSOAP11SOAPMessagePort");

    public static final QName GreeterSOAP11SourceMessageModePort = new QName(
            "http://geronimo.apache.org/greeter_provider", "GreeterSOAP11SourceMessageModePort");

    public static final QName GreeterSOAP11SourcePayloadModePort = new QName(
            "http://geronimo.apache.org/greeter_provider", "GreeterSOAP11SourcePayloadModePort");

    public static final QName GreeterSOAP12SOAPMessagePort = new QName("http://geronimo.apache.org/greeter_provider",
            "GreeterSOAP12SOAPMessagePort");

    public static final QName GreeterSOAP12SourceMessageModePort = new QName(
            "http://geronimo.apache.org/greeter_provider", "GreeterSOAP12SourceMessageModePort");

    public static final QName GreeterSOAP12SourcePayloadModePort = new QName(
            "http://geronimo.apache.org/greeter_provider", "GreeterSOAP12SourcePayloadModePort");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String testName = request.getParameter("test");
        if (testName == null || !testName.startsWith("test")) {
            throw new ServletException("Invalid test name");
        }
        Method testMethod = null;
        try {
            testMethod = getClass().getMethod(testName, new Class[] {});
        } catch (Exception e1) {
            throw new ServletException("No such test: " + testName);
        }
        try {
            testMethod.invoke(this, (Object[]) null);
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
        
    public void testHTTPDataSource() throws Exception {
        Dispatch<DataSource> dispatch = service.createDispatch(GreeterHTTPDataSourcePort, 
                                                               DataSource.class,
                                                               Service.Mode.MESSAGE);
        DataSource responseDataSource = dispatch.invoke(MessageUtils.createRequestHTTPDataSource());
        AssertUtils.assertResponseHTTPDataSource(responseDataSource);
    }

    public void testHTTPSourceMessageMode() throws Exception {
        Dispatch<Source> dispatch = service.createDispatch(GreeterHTTPSourceMessageModePort, Source.class,
                Service.Mode.MESSAGE);
        Source source = dispatch.invoke(MessageUtils.createRequestHTTPSource());
        AssertUtils.assertResponseHTTPSource(source);
    }

    public void testHTTPSourcePayloadMode() throws Exception {
        Dispatch<Source> dispatch = service.createDispatch(GreeterHTTPSourcePayloadModePort, Source.class,
                Service.Mode.PAYLOAD);
        Source source = dispatch.invoke(MessageUtils.createRequestHTTPSource());
        AssertUtils.assertResponseHTTPSource(source);
    }

    public void testSOAP11SOAPMessage() throws Exception {
        Dispatch<SOAPMessage> dispatch = service.createDispatch(GreeterSOAP11SOAPMessagePort, SOAPMessage.class,
                Service.Mode.MESSAGE);
        SOAPMessage soapMessage = dispatch.invoke(MessageUtils
                .createRequestSOAPMessage(SOAPConstants.SOAP_1_1_PROTOCOL));
        AssertUtils.assertResponseSOAPMessage(soapMessage, SOAPConstants.SOAP_1_1_PROTOCOL);
    }

    public void testSOAP11SourceMessageMode() throws Exception {
        Dispatch<Source> dispatch = service.createDispatch(GreeterSOAP11SourceMessageModePort, Source.class,
                Service.Mode.MESSAGE);
        Source source = dispatch.invoke(MessageUtils.createRequestSOAPSource(SOAPConstants.SOAP_1_1_PROTOCOL,
                Service.Mode.MESSAGE));
        AssertUtils.assertResponseSOAPSource(source, SOAPConstants.SOAP_1_1_PROTOCOL, Service.Mode.MESSAGE);
    }

    public void testSOAP11SourcePayloadMode() throws Exception {
        Dispatch<Source> dispatch = service.createDispatch(GreeterSOAP11SourcePayloadModePort, Source.class,
                Service.Mode.PAYLOAD);
        Source source = dispatch.invoke(MessageUtils.createRequestSOAPSource(SOAPConstants.SOAP_1_1_PROTOCOL,
                Service.Mode.PAYLOAD));
        AssertUtils.assertResponseSOAPSource(source, SOAPConstants.SOAP_1_1_PROTOCOL, Service.Mode.PAYLOAD);
    }

    public void testSOAP12SOAPMessage() throws Exception {
        Dispatch<SOAPMessage> dispatch = service.createDispatch(GreeterSOAP12SOAPMessagePort, SOAPMessage.class,
                Service.Mode.MESSAGE);
        SOAPMessage soapMessage = dispatch.invoke(MessageUtils
                .createRequestSOAPMessage(SOAPConstants.SOAP_1_2_PROTOCOL));
        AssertUtils.assertResponseSOAPMessage(soapMessage, SOAPConstants.SOAP_1_2_PROTOCOL);
    }

    public void testSOAP12SourceMessageMode() throws Exception {
        Dispatch<Source> dispatch = service.createDispatch(GreeterSOAP12SourceMessageModePort, Source.class,
                Service.Mode.MESSAGE);
        Source source = dispatch.invoke(MessageUtils.createRequestSOAPSource(SOAPConstants.SOAP_1_2_PROTOCOL,
                Service.Mode.MESSAGE));
        AssertUtils.assertResponseSOAPSource(source, SOAPConstants.SOAP_1_2_PROTOCOL, Service.Mode.MESSAGE);
    }

    public void testSOAP12SourcePayloadMode() throws Exception {
        Dispatch<Source> dispatch = service.createDispatch(GreeterSOAP12SourcePayloadModePort, Source.class,
                Service.Mode.PAYLOAD);
        Source source = dispatch.invoke(MessageUtils.createRequestSOAPSource(SOAPConstants.SOAP_1_2_PROTOCOL,
                Service.Mode.PAYLOAD));
        AssertUtils.assertResponseSOAPSource(source, SOAPConstants.SOAP_1_2_PROTOCOL, Service.Mode.PAYLOAD);
    }

}
