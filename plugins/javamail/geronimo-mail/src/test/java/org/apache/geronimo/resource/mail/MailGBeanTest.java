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
package org.apache.geronimo.resource.mail;

import java.util.Collections;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.apache.geronimo.kernel.osgi.MockBundleContext;


/**
 * @version $Rev$ $Date$
 */
public class MailGBeanTest extends TestCase {
    public void testNotheing() {}

    public void XtestProperties() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "testStore");
        properties.put("mail.transport.protocol", "testTransport");

        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);
        MailGBean mail = new MailGBean("test:name=mail", null, Boolean.TRUE, properties, null, null, null, null, null, null, null, bundleContext);
        mail.doStart();
        Object proxy = mail.$getResource();

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

    }

    public void XtestDefaultOverrides() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "POOKIE");
        properties.put("mail.transport.protocol", "BEAR");

        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);
        MailGBean mail = new MailGBean("test:name=mail", null, Boolean.TRUE, properties, null, "test", "test", null, null, null, null, bundleContext);
        mail.doStart();
        Object proxy = mail.$getResource();

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

    }

    public void XtestSMTPOverrides() throws Exception {
        // these are defaults, all to be overridden
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "POOKIE");
        properties.put("mail.transport.protocol", "BEAR");
        properties.put("mail.smtp.ehlo", "false");

        // this is done in the property bundle for the transport.
        Properties bundle = new Properties();
        bundle.put("mail.smtp.ehlo", "true");
        bundle.put("mail.smtp.quitwait", "true");

        SMTPTransportGBean protocol = new SMTPTransportGBean("test:name=smtp", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        protocol.doStart();

        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);
        MailGBean mail = new MailGBean("test:name=mail", Collections.<ProtocolGBean>singleton(protocol), Boolean.TRUE, properties, null, "test", "test", null, null, null, null, bundleContext);
        mail.doStart();
        Object proxy = mail.$getResource();

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

        TestTransport testTransport = (TestTransport) transport;
        assertFalse(testTransport.isEHLO());

    }

    public void XtestPOP3Overrides() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "POOKIE");
        properties.put("mail.transport.protocol", "BEAR");
        properties.put("mail.pop3.ehlo", "true");

        POP3StoreGBean protocol = new POP3StoreGBean("test:name=pop3", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        protocol.doStart();

        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);
        MailGBean mail = new MailGBean("test:name=mail", Collections.<ProtocolGBean>singleton(protocol), Boolean.TRUE, properties, null, "test", "test", null, null, null, null, bundleContext);
        mail.doStart();
        Object proxy = mail.$getResource();

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

    }

    public void XtestIMAPOverrides() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "POOKIE");
        properties.put("mail.transport.protocol", "BEAR");
        properties.put("mail.imap.ehlo", "true");

        IMAPStoreGBean protocol = new IMAPStoreGBean("test:name=imap", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        protocol.doStart();

        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, null);
        MailGBean mail = new MailGBean("test:name=mail", Collections.<ProtocolGBean>singleton(protocol), Boolean.TRUE, properties, null, "test", "test", null, null, null, null, bundleContext);
        mail.doStart();
        Object proxy = mail.$getResource();


        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

    }

}
