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

package org.apache.geronimo.connector.outbound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Hashtable;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;

import junit.framework.TestCase;
import org.apache.geronimo.connector.mock.MockConnection;
import org.apache.geronimo.connector.mock.MockConnectionFactory;
import org.apache.geronimo.connector.mock.MockManagedConnectionFactory;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.deployment.RefAdapter;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class ManagedConnectionFactoryWrapperTest extends TestCase {

    private Kernel kernel;
    private ObjectName managedConnectionFactoryName;
    private ObjectName ctcName;
    private ObjectName cmfName;
    private static final String GLOBAL_NAME = "GLOBAL_NAME";
    private static final String KERNEL_NAME = "testKernel";
    private static final String TARGET_NAME = "testCFName";

    public void testProxy() throws Exception {
        Object proxy = kernel.invoke(managedConnectionFactoryName, "getProxy");
        assertNotNull(proxy);
        assertTrue(proxy instanceof ConnectionFactory);
        Connection connection = ((ConnectionFactory) proxy).getConnection();
        assertNotNull(connection);
        kernel.stopGBean(managedConnectionFactoryName);
        try {
            ((ConnectionFactory) proxy).getConnection();
            fail();
        } catch (IllegalStateException ise) {
        }
        kernel.startGBean(managedConnectionFactoryName);
        ((ConnectionFactory) proxy).getConnection();
    }

    public void testSerialization() throws Exception {
        ConnectionFactory proxy = (ConnectionFactory) kernel.invoke(managedConnectionFactoryName, "getProxy");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(proxy);
        oos.flush();
        byte[] bytes = baos.toByteArray();
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object proxy2 = ois.readObject();
        assertNotNull(proxy2);
        assertTrue(proxy instanceof ConnectionFactory);
        Connection connection = proxy.getConnection();
        assertNotNull(connection);
        kernel.stopGBean(managedConnectionFactoryName);
        ObjectInputStream ois2 = new ObjectInputStream(new ByteArrayInputStream(bytes));
        ConnectionFactory proxy3 = (ConnectionFactory) ois2.readObject();
        try {
            proxy3.getConnection();
            fail();
        } catch (IllegalStateException ise) {
        }
        kernel.startGBean(managedConnectionFactoryName);
        proxy3.getConnection();

    }

    public void testGlobalLookup() throws Exception {
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put("java.naming.factory.url.pkgs", "org.apache.geronimo.naming");
        env.put("java.naming.provider.url", "rmi://localhost:1099");

        Context ctx = new InitialContext(env);
        ConnectionFactory cf = (ConnectionFactory) ctx.lookup("geronimo:" + GLOBAL_NAME);
        assertNotNull(cf);
        kernel.stopGBean(managedConnectionFactoryName);
        try {
            ctx.lookup("geronimo:" + GLOBAL_NAME);
            fail();
        } catch (NamingException ne) {
        }
        kernel.startGBean(managedConnectionFactoryName);
        ConnectionFactory cf2 = (ConnectionFactory) ctx.lookup("geronimo:" + GLOBAL_NAME);
        assertNotNull(cf2);
    }

    public void testLocalLookup() throws Exception {
        JMXReferenceFactory referenceFactory = new JMXReferenceFactory();
        ComponentContextBuilder builder = new ComponentContextBuilder(referenceFactory);
        builder.addResourceRef("resourceref", ConnectionFactory.class, new RefAdapter() {
            public XmlObject getXmlObject() {
                return null;
            }

            public void setXmlObject(XmlObject xmlObject) {
            }

            public String getRefName() {
                return "resourceref";
            }

            public void setRefName(String name) {
            }

            public String getServerName() {
                return null;
            }

            public void setServerName(String serverName) {
            }

            public String getKernelName() {
                return KERNEL_NAME;
            }

            public void setKernelName(String kernelName) {
            }

            public String getTargetName() {
                return TARGET_NAME;
            }

            public void setTargetName(String targetName) {
            }

            public String getExternalUri() {
                return null;
            }

            public void setExternalUri(String externalURI) {
            }

        });
        ReadOnlyContext roc = builder.getContext();
        Object o = roc.lookup("env/resourceref");
        assertNotNull(o);
        assertTrue(o instanceof ConnectionFactory);
    }

    protected void setUp() throws Exception {
        kernel = new Kernel(KERNEL_NAME, "test.domain");
        kernel.boot();
        GBeanMBean ctc = new GBeanMBean(MockConnectionTrackingCoordinator.getGBeanInfo());
        ctcName = ObjectName.getInstance("test:role=ConnectionTrackingCoordinator");
        kernel.loadGBean(ctcName, ctc);
        GBeanMBean cmf = new GBeanMBean(GenericConnectionManager.getGBeanInfo());
        cmf.setAttribute("transactionSupport", NoTransactions.INSTANCE);
        cmf.setAttribute("pooling", new NoPool());
        cmf.setAttribute("name", "TestCF");
        cmf.setReferencePatterns("ConnectionTracker", Collections.singleton(ctcName));
        cmfName = ObjectName.getInstance("test:role=ConnectionManagerFactory");
        kernel.loadGBean(cmfName, cmf);

        managedConnectionFactoryName = ObjectName.getInstance(JMXReferenceFactory.BASE_MANAGED_CONNECTION_FACTORY_NAME + TARGET_NAME);

        GBeanMBean mcfw = new GBeanMBean(ManagedConnectionFactoryWrapper.getGBeanInfo());
        mcfw.setAttribute("managedConnectionFactoryClass", MockManagedConnectionFactory.class);
        mcfw.setAttribute("connectionFactoryInterface", ConnectionFactory.class);
        mcfw.setAttribute("connectionFactoryImplClass", MockConnectionFactory.class);
        mcfw.setAttribute("connectionInterface", Connection.class);
        mcfw.setAttribute("connectionImplClass", MockConnection.class);
        mcfw.setAttribute("globalJNDIName", GLOBAL_NAME);
        //"ResourceAdapterWrapper",
        mcfw.setReferencePatterns("ConnectionManagerFactory", Collections.singleton(cmfName));
        //"ManagedConnectionFactoryListener",
        kernel.loadGBean(managedConnectionFactoryName, mcfw);

        kernel.startGBean(ctcName);
        kernel.startGBean(cmfName);
        kernel.startGBean(managedConnectionFactoryName);
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(managedConnectionFactoryName);
        kernel.shutdown();
    }

    public static class MockConnectionTrackingCoordinator implements ConnectionTracker {
        public void handleObtained(ConnectionTrackingInterceptor connectionTrackingInterceptor,
                ConnectionInfo connectionInfo) {
        }

        public void handleReleased(ConnectionTrackingInterceptor connectionTrackingInterceptor,
                ConnectionInfo connectionInfo) {
        }

        public void setEnvironment(ConnectionInfo connectionInfo, String key) {
        }

        static final GBeanInfo GBEAN_INFO;

        static {
            GBeanInfoFactory infoFactory = new GBeanInfoFactory(MockConnectionTrackingCoordinator.class);
            infoFactory.addInterface(ConnectionTracker.class);
            GBEAN_INFO = infoFactory.getBeanInfo();
        }

        public static GBeanInfo getGBeanInfo() {
            return GBEAN_INFO;
        }
    }
}
