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

import java.util.Properties;

import javax.activation.DataSource;
import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.http.HTTPBinding;

import junit.framework.Assert;

import org.apache.geronimo.jaxws.greeter_provider.common.MessageUtils;
import org.apache.geronimo.jaxws.greeter_provider.ejb.EchoLocal;
import org.apache.geronimo.jaxws.greeter_provider.ejb.EchoRemote;

/**
 * For each type provider, the test items include :
 * a. Invoke the extra method via local interface
 * b. Invoke the extra method via remote interface
 * c. Invoke the invoke method of the Provider interface via local interface
 */
public class TestServletEJB extends TestServlet {

    public static final String ECHO_WORDS = "Geronimo";

    private InitialContext localContext;

    private InitialContext remoteContext;

    @WebServiceRef(name = "services/ejb/Provider")
    private Service myService;

    private void assertEchoInvocation(String beanName) throws Exception {
        EchoLocal echoLocal = lookupEchoLocal(beanName);
        Assert.assertEquals(echoLocal.echo(ECHO_WORDS), ECHO_WORDS);
        EchoRemote echoRemote = null;
        echoRemote = lookupEchoRemote(beanName);
        Assert.assertEquals(echoRemote.echo(ECHO_WORDS), ECHO_WORDS);
    }

    private EchoLocal lookupEchoLocal(String beanName) throws Exception {
        return (EchoLocal) localContext.lookup(beanName + "Local");
    }

    private EchoRemote lookupEchoRemote(String beanName) throws Exception {
        return (EchoRemote) remoteContext.lookup(beanName + "Remote");
    }

    private Provider<?> lookupProvider(String beanName) throws Exception {
        return (Provider<?>) localContext.lookup(beanName + "Local");
    }

    @PostConstruct
    private void myInit() throws Exception {
        this.service = myService;
        Properties localProps = new Properties();
        localProps.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        localContext = new InitialContext(localProps);
        Properties remoteProps = new Properties();
        remoteProps.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.openejb.client.RemoteInitialContextFactory");
        remoteProps.setProperty(Context.PROVIDER_URL, "ejbd://localhost:4201");
        remoteContext = new InitialContext(remoteProps);
    }

    @Override
    public void testHTTPDataSource() throws Exception {
        super.testHTTPDataSource();
        assertEchoInvocation("GreeterHTTPDataSourceProvider");
        Provider<DataSource> provider = (Provider<DataSource>) lookupProvider("GreeterHTTPDataSourceProvider");
        DataSource ejbResponseDataSource = provider.invoke(MessageUtils.createRequestHTTPDataSource());
        AssertUtils.assertResponseHTTPDataSource(ejbResponseDataSource);
    }

    @Override
    public void testHTTPSourceMessageMode() throws Exception {
        super.testHTTPSourceMessageMode();
        assertEchoInvocation("GreeterHTTPSourceMessageModeProvider");
        Provider<Source> provider = (Provider<Source>) lookupProvider("GreeterHTTPSourceMessageModeProvider");
        Source source = provider.invoke(MessageUtils.createRequestHTTPSource());
        AssertUtils.assertResponseHTTPSource(source);
    }

    @Override
    public void testHTTPSourcePayloadMode() throws Exception {
        super.testHTTPSourcePayloadMode();
        assertEchoInvocation("GreeterHTTPSourcePayloadModeProvider");
        Provider<Source> provider = (Provider<Source>) lookupProvider("GreeterHTTPSourcePayloadModeProvider");
        Source source = provider.invoke(MessageUtils.createRequestHTTPSource());
        AssertUtils.assertResponseHTTPSource(source);
    }

    @Override
    public void testSOAP11SOAPMessage() throws Exception {
        super.testSOAP11SOAPMessage();
        assertEchoInvocation("GreeterSOAP11SOAPMessageProvider");
        Provider<SOAPMessage> provider = (Provider<SOAPMessage>) lookupProvider("GreeterSOAP11SOAPMessageProvider");
        SOAPMessage soapMessage = provider.invoke(MessageUtils
                .createRequestSOAPMessage(SOAPConstants.SOAP_1_1_PROTOCOL));
        AssertUtils.assertResponseSOAPMessage(soapMessage, SOAPConstants.SOAP_1_1_PROTOCOL);
    }

    @Override
    public void testSOAP11SourceMessageMode() throws Exception {
        super.testSOAP11SourceMessageMode();
        assertEchoInvocation("GreeterSOAP11SourceMessageModeProvider");
        Provider<Source> provider = (Provider<Source>) lookupProvider("GreeterSOAP11SourceMessageModeProvider");
        Source source = provider.invoke(MessageUtils.createRequestSOAPSource(SOAPConstants.SOAP_1_1_PROTOCOL,
                Service.Mode.MESSAGE));
        AssertUtils.assertResponseSOAPSource(source, SOAPConstants.SOAP_1_1_PROTOCOL, Service.Mode.MESSAGE);
    }

    @Override
    public void testSOAP11SourcePayloadMode() throws Exception {
        super.testSOAP11SourcePayloadMode();
        assertEchoInvocation("GreeterSOAP11SourcePayloadModeProvider");
        Provider<Source> provider = (Provider<Source>) lookupProvider("GreeterSOAP11SourcePayloadModeProvider");
        Source source = provider.invoke(MessageUtils.createRequestSOAPSource(SOAPConstants.SOAP_1_1_PROTOCOL,
                Service.Mode.PAYLOAD));
        AssertUtils.assertResponseSOAPSource(source, SOAPConstants.SOAP_1_1_PROTOCOL, Service.Mode.PAYLOAD);
    }

    @Override
    public void testSOAP12SOAPMessage() throws Exception {
        super.testSOAP12SOAPMessage();
        assertEchoInvocation("GreeterSOAP12SOAPMessageProvider");
        Provider<SOAPMessage> provider = (Provider<SOAPMessage>) lookupProvider("GreeterSOAP12SOAPMessageProvider");
        SOAPMessage soapMessage = provider.invoke(MessageUtils
                .createRequestSOAPMessage(SOAPConstants.SOAP_1_2_PROTOCOL));
        AssertUtils.assertResponseSOAPMessage(soapMessage, SOAPConstants.SOAP_1_2_PROTOCOL);
    }

    @Override
    public void testSOAP12SourceMessageMode() throws Exception {
        super.testSOAP12SourceMessageMode();
        assertEchoInvocation("GreeterSOAP12SourceMessageModeProvider");
        Provider<Source> provider = (Provider<Source>) lookupProvider("GreeterSOAP12SourceMessageModeProvider");
        Source source = provider.invoke(MessageUtils.createRequestSOAPSource(SOAPConstants.SOAP_1_2_PROTOCOL,
                Service.Mode.MESSAGE));
        AssertUtils.assertResponseSOAPSource(source, SOAPConstants.SOAP_1_2_PROTOCOL, Service.Mode.MESSAGE);
    }

    @Override
    public void testSOAP12SourcePayloadMode() throws Exception {
        super.testSOAP12SourcePayloadMode();
        assertEchoInvocation("GreeterSOAP12SourcePayloadModeProvider");
        Provider<Source> provider = (Provider<Source>) lookupProvider("GreeterSOAP12SourcePayloadModeProvider");
        Source source = provider.invoke(MessageUtils.createRequestSOAPSource(SOAPConstants.SOAP_1_2_PROTOCOL,
                Service.Mode.PAYLOAD));
        AssertUtils.assertResponseSOAPSource(source, SOAPConstants.SOAP_1_2_PROTOCOL, Service.Mode.PAYLOAD);
    }
}
