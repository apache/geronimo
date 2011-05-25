/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jaxws.handler;

import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;

import org.apache.geronimo.jaxws.info.HandlerChainsInfo;
import org.apache.geronimo.kernel.osgi.MockBundle;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.openejb.jee.HandlerChains;
import org.apache.openejb.jee.JaxbJavaee;

public class GeronimoHandlerResolverTest extends TestSupport {

    private HandlerChainsInfoBuilder handlerChainsInfoBuilder = new HandlerChainsInfoBuilder();

    public void testBasic() throws Exception {
        InputStream in = getClass().getResourceAsStream("/handlers.xml");
        assertTrue(in != null);
        HandlerChainsInfo handlerChains = toHandlerChains(in);
        assertEquals(3, handlerChains.handleChains.size());

        GeronimoHandlerResolver resolver = new GeronimoHandlerResolver(new MockBundle(getClass().getClassLoader(), null, 11L), getClass(), handlerChains, null);

        List<Handler> handlers = null;

        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, null));
        assertEquals(3, handlers.size());
    }

    public void testServiceMatching() throws Exception {
        InputStream in = getClass().getResourceAsStream("/handlers_service.xml");
        assertTrue(in != null);
        HandlerChainsInfo handlerChains = toHandlerChains(in);
        assertEquals(4, handlerChains.handleChains.size());

        GeronimoHandlerResolver resolver = new GeronimoHandlerResolver(new MockBundle(getClass().getClassLoader(), null, 11L), getClass(), handlerChains, null);

        List<Handler> handlers = null;

        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, null));
        assertEquals(0, handlers.size());

        QName serviceName1 = new QName("http://foo", "Bar");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, serviceName1));
        assertEquals(1, handlers.size());

        QName serviceName2 = new QName("http://foo", "Foo");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, serviceName2));
        assertEquals(2, handlers.size());

        QName serviceName3 = new QName("http://foo", "FooBar");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, serviceName3));
        assertEquals(1, handlers.size());

        QName serviceName4 = new QName("http://foo", "BarFoo");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, serviceName4));
        assertEquals(0, handlers.size());

        QName serviceName5 = new QName("https://foo", "Bar");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, serviceName5));
        assertEquals(1, handlers.size());
    }

    public void testBindingMatching() throws Exception {
        InputStream in = getClass().getResourceAsStream("/handlers_bindings.xml");
        assertTrue(in != null);
        HandlerChainsInfo handlerChains = toHandlerChains(in);
        assertEquals(4, handlerChains.handleChains.size());

        GeronimoHandlerResolver resolver = new GeronimoHandlerResolver(new MockBundle(getClass().getClassLoader(), null, 11L), getClass(), handlerChains, null);
        List<Handler> handlers = null;

        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, null));
        assertEquals(0, handlers.size());

        handlers = resolver.getHandlerChain(new TestPortInfo("http://foobar", null, null));
        assertEquals(0, handlers.size());

        handlers = resolver.getHandlerChain(new TestPortInfo("##SOAP12_HTTP", null, null));
        assertEquals(0, handlers.size());

        handlers = resolver.getHandlerChain(new TestPortInfo("##SOAP11_HTTP", null, null));
        assertEquals(2, handlers.size());

        handlers = resolver.getHandlerChain(new TestPortInfo("http://schemas.xmlsoap.org/wsdl/soap/http", null, null));
        assertEquals(2, handlers.size());

        handlers = resolver.getHandlerChain(new TestPortInfo("##SOAP11_HTTP_MTOM", null, null));
        assertEquals(1, handlers.size());

        handlers = resolver.getHandlerChain(new TestPortInfo("##XML_HTTP", null, null));
        assertEquals(2, handlers.size());
    }

    public void testPortMatching() throws Exception {
        InputStream in = getClass().getResourceAsStream("/handlers_port.xml");
        assertTrue(in != null);
        HandlerChainsInfo handlerChains = toHandlerChains(in);
        assertEquals(4, handlerChains.handleChains.size());

        GeronimoHandlerResolver resolver = new GeronimoHandlerResolver(new MockBundle(getClass().getClassLoader(), null, 11L), getClass(), handlerChains, null);

        List<Handler> handlers = null;

        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, null));
        assertEquals(0, handlers.size());

        QName portName1 = new QName("http://foo", "Bar");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, portName1, null));
        assertEquals(1, handlers.size());

        QName portName2 = new QName("http://foo", "Foo");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, portName2, null));
        assertEquals(2, handlers.size());

        QName portName3 = new QName("http://foo", "FooBar");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, portName3, null));
        assertEquals(1, handlers.size());

        QName portName4 = new QName("http://foo", "BarFoo");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, portName4, null));
        assertEquals(0, handlers.size());

        QName portName5 = new QName("https://foo", "Bar");
        handlers = resolver.getHandlerChain(new TestPortInfo(null, portName5, null));
        assertEquals(1, handlers.size());
    }

    public void testMixedMatching() throws Exception {
        InputStream in = getClass().getResourceAsStream("/handlers_mixed.xml");
        assertTrue(in != null);
        HandlerChainsInfo handlerChains = toHandlerChains(in);
        assertEquals(3, handlerChains.handleChains.size());

        GeronimoHandlerResolver resolver = new GeronimoHandlerResolver(new MockBundle(getClass().getClassLoader(), null, 11L), getClass(), handlerChains, null);

        List<Handler> handlers = null;

        handlers = resolver.getHandlerChain(new TestPortInfo(null, null, null));
        assertEquals(0, handlers.size());

        QName serviceName1 = new QName("http://foo", "Bar");
        QName portName1 = new QName("http://foo", "FooBar");
        String binding1 = "##XML_HTTP";
        handlers = resolver.getHandlerChain(new TestPortInfo(binding1, portName1, serviceName1));
        assertEquals(3, handlers.size());

        String binding2 = "##SOAP11_HTTP";
        handlers = resolver.getHandlerChain(new TestPortInfo(binding2, portName1, serviceName1));
        assertEquals(2, handlers.size());

        QName serviceName2 = new QName("http://foo", "Baaz");
        QName portName2 = new QName("http://foo", "Baaz");
        handlers = resolver.getHandlerChain(new TestPortInfo(binding1, portName2, serviceName2));
        assertEquals(1, handlers.size());
    }

    private HandlerChainsInfo toHandlerChains(InputStream input) throws Exception {
        return handlerChainsInfoBuilder.build((HandlerChains) JaxbJavaee.unmarshalHandlerChains(HandlerChains.class, input));
    }

    private static class TestPortInfo implements PortInfo {

        private String bindingID;
        private QName portName;
        private QName serviceName;

        public TestPortInfo(String bindingID, QName portName, QName serviceName) {
            this.bindingID = bindingID;
            this.portName = portName;
            this.serviceName = serviceName;
        }

        public String getBindingID() {
            return this.bindingID;
        }

        public QName getPortName() {
            return this.portName;
        }

        public QName getServiceName() {
            return this.serviceName;
        }

    }

}
