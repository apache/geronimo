/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.mail;

import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.management.ObjectName;
import java.util.Collection;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;


/**
 * @version $Rev: $ $Date: $
 */
public class MailGBeanTest extends TestCase {

    private Kernel kernel;
    private ObjectName mailName;
    private ObjectName protocolName;
    private static final String KERNEL_NAME = "testKernel";

    public void testProperties() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "testStore");
        properties.put("mail.transport.protocol", "testTransport");

        GBeanMBean cmf = new GBeanMBean(MailGBean.getGBeanInfo());
        cmf.setAttribute("useDefault", new Boolean(true));
        cmf.setAttribute("properties", properties);

        mailName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,J2EEType=JavaMailResource,name=default");

        kernel.loadGBean(mailName, cmf);
        kernel.startGBean(mailName);

        Object proxy = kernel.invoke(mailName, "$getResource");

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

        kernel.stopGBean(mailName);
    }

    public void testDefaultOverrides() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "POOKIE");
        properties.put("mail.transport.protocol", "BEAR");

        GBeanMBean cmf = new GBeanMBean(MailGBean.getGBeanInfo());
        cmf.setAttribute("useDefault", new Boolean(true));
        cmf.setAttribute("properties", properties);
        cmf.setAttribute("storeProtocol", "test");
        cmf.setAttribute("transportProtocol", "test");

        mailName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,J2EEType=JavaMailResource,name=default");

        kernel.loadGBean(mailName, cmf);
        kernel.startGBean(mailName);

        Object proxy = kernel.invoke(mailName, "$getResource");

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

        kernel.stopGBean(mailName);
    }

    public void testSMTPOverrides() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "POOKIE");
        properties.put("mail.transport.protocol", "BEAR");
        properties.put("mail.smtp.ehlo", "true");

        GBeanMBean cmf = new GBeanMBean(MailGBean.getGBeanInfo());
        cmf.setReferencePattern("Protocols", new ObjectName("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,type=JavaMailProtocol,*"));
        cmf.setAttribute("useDefault", new Boolean(true));
        cmf.setAttribute("properties", properties);
        cmf.setAttribute("storeProtocol", "test");
        cmf.setAttribute("transportProtocol", "test");

        mailName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,J2EEType=JavaMailResource,name=default");

        kernel.loadGBean(mailName, cmf);
        kernel.startGBean(mailName);

        GBeanMBean smtp = new GBeanMBean(SMTPTransportGBean.getGBeanInfo());

        protocolName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,type=JavaMailProtocol,name=smtp");

        kernel.loadGBean(protocolName, smtp);
        kernel.startGBean(protocolName);

        Collection protocols = (Collection) kernel.invoke(mailName, "getProtocols");
        int size = protocols.size();
        Object proxy = kernel.invoke(mailName, "$getResource");

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

        kernel.stopGBean(protocolName);
        kernel.stopGBean(mailName);
    }

    public void testPOP3Overrides() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "POOKIE");
        properties.put("mail.transport.protocol", "BEAR");
        properties.put("mail.pop3.ehlo", "true");

        GBeanMBean cmf = new GBeanMBean(MailGBean.getGBeanInfo());
        cmf.setReferencePattern("Protocols", new ObjectName("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,type=JavaMailProtocol,*"));
        cmf.setAttribute("useDefault", new Boolean(true));
        cmf.setAttribute("properties", properties);
        cmf.setAttribute("storeProtocol", "test");
        cmf.setAttribute("transportProtocol", "test");

        mailName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,J2EEType=JavaMailResource,name=default");

        kernel.loadGBean(mailName, cmf);
        kernel.startGBean(mailName);

        GBeanMBean pop3 = new GBeanMBean(SMTPTransportGBean.getGBeanInfo());

        protocolName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,type=JavaMailProtocol,name=pop3");

        kernel.loadGBean(protocolName, pop3);
        kernel.startGBean(protocolName);

        Object proxy = kernel.invoke(mailName, "$getResource");

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

        kernel.stopGBean(protocolName);
        kernel.stopGBean(mailName);
    }

    public void testIMAPOverrides() throws Exception {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "POOKIE");
        properties.put("mail.transport.protocol", "BEAR");
        properties.put("mail.imap.ehlo", "true");

        GBeanMBean cmf = new GBeanMBean(MailGBean.getGBeanInfo());
        cmf.setReferencePattern("Protocols", new ObjectName("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,type=JavaMailProtocol,*"));
        cmf.setAttribute("useDefault", new Boolean(true));
        cmf.setAttribute("properties", properties);
        cmf.setAttribute("storeProtocol", "testStore");
        cmf.setAttribute("transportProtocol", "testTransport");

        mailName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,J2EEType=JavaMailResource,name=default");

        kernel.loadGBean(mailName, cmf);
        kernel.startGBean(mailName);

        GBeanMBean imap = new GBeanMBean(IMAPStoreGBean.getGBeanInfo());

        protocolName = ObjectName.getInstance("geronimo.server:J2EEServer=geronimo,J2EEApplication=null,type=JavaMailProtocol,name=imap");

        kernel.loadGBean(protocolName, imap);
        kernel.startGBean(protocolName);

        Object proxy = kernel.invoke(mailName, "$getResource");

        assertNotNull(proxy);
        assertTrue(proxy instanceof Session);

        Store store = ((Session) proxy).getStore();
        assertNotNull(store);
        assertTrue(store instanceof TestStore);

        Transport transport = ((Session) proxy).getTransport();
        assertNotNull(transport);
        assertTrue(transport instanceof TestTransport);

        kernel.stopGBean(protocolName);
        kernel.stopGBean(mailName);
    }

    protected void setUp() throws Exception {
        kernel = new Kernel(KERNEL_NAME, "test.domain");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
