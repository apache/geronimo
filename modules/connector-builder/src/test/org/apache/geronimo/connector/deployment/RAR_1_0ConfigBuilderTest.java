/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.connector.deployment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import javax.management.ObjectName;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ConnectorDocument10;
import org.apache.xmlbeans.XmlOptions;
import org.tranql.sql.jdbc.JDBCUtil;

/**
 * @version $Rev$ $Date$
 */
public class RAR_1_0ConfigBuilderTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    private URL j2eeDD;
    private URL geronimoDD;
    XmlOptions xmlOptions;
    private List errors;
    private boolean defaultXATransactionCaching = true;
    private boolean defaultXAThreadCaching = false;
    private int defaultMaxSize = 10;
    private int defaultMinSize = 0;
    private int defaultBlockingTimeoutMilliseconds = 5000;
    private int defaultidleTimeoutMinutes = 15;
    private URI defaultParentId;


    public void testLoadJ2eeDeploymentDescriptor() throws Exception {
        InputStream j2eeInputStream = j2eeDD.openStream();
        ConnectorDocument10 connectorDocument = ConnectorDocument10.Factory.parse(j2eeInputStream);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        if (!connectorDocument.validate(xmlOptions)) {
            fail(errors.toString());
        }
    }

    public void testLoadGeronimoDeploymentDescriptor() throws Exception {
        InputStream geronimoInputStream = geronimoDD.openStream();
        GerConnectorDocument connectorDocument = GerConnectorDocument.Factory.parse(geronimoInputStream);
        assertEquals(1, connectorDocument.getConnector().getResourceadapterArray().length);
        if (!connectorDocument.validate(xmlOptions)) {
            fail(errors.toString());
        }

    }

    public void testBuildUnpackedModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(basedir, "target/test-rar-10");
            }
        };
        executeTestBuildModule(action);
    }

    public void testBuildUnpackedAltSpecDDModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getVendorDD() {
                return new File(basedir, "target/test-rar-10/META-INF/geronimo-ra.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(basedir, "target/test-rar-10/dummy.xml").toURL();
            }

            public File getRARFile() {
                return new File(basedir, "target/test-rar-10");
            }
        };
        try {
            executeTestBuildModule(action);
            fail("Spec alt-dd does not exist.");
        } catch (DeploymentException e) {
        }
    }

    public void testBuildUnpackedAltVendorDDModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getVendorDD() {
                // this file does not exist, one expects a DeploymentException.
                return new File(basedir, "target/test-rar-10/dummy.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(basedir, "target/test-rar-10/META-INF/ra.xml").toURL();
            }

            public File getRARFile() {
                return new File(basedir, "target/test-rar-10");
            }
        };
        try {
            executeTestBuildModule(action);
            fail("Vendor alt-dd does not exist.");
        } catch (DeploymentException e) {
        }
    }

    public void testBuildUnpackedAltSpecVendorDDModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getVendorDD() {
                // this file exists
                return new File(basedir, "target/test-rar-10/META-INF/geronimo-ra.xml");
            }

            public URL getSpecDD() throws MalformedURLException {
                return new File(basedir, "target/test-rar-10/META-INF/ra.xml").toURL();
            }

            public File getRARFile() {
                return new File(basedir, "target/test-rar-10");
            }
       };
        executeTestBuildModule(action);
    }

    public void testBuildPackedModule() throws Exception {
        InstallAction action = new InstallAction() {
            public File getRARFile() {
                return new File(basedir, "target/test-rar-10.rar");
            }
        };
        executeTestBuildModule(action);
    }

    private void executeTestBuildModule(InstallAction action) throws Exception {
//        String j2eeDomainName = "geronimo.server";
//        String j2eeServerName = "TestGeronimoServer";
//        String j2eeModuleName = "org/apache/geronimo/j2ee/deployment/test";
//        String j2eeApplicationName = "null";
        J2eeContext j2eeContext = new J2eeContextImpl("test.domain", "testServer", "null", "org/apache/geronimo/j2ee/deployment/test", null, null);
        ObjectName connectionTrackerName = new ObjectName("geronimo.connector:service=ConnectionTracker");

        Kernel kernel = new Kernel("testServer");
        ConnectorModuleBuilder moduleBuilder = new ConnectorModuleBuilder(defaultParentId, defaultMaxSize, defaultMinSize, defaultBlockingTimeoutMilliseconds, defaultidleTimeoutMinutes, defaultXATransactionCaching, defaultXAThreadCaching, kernel);
        File rarFile = action.getRARFile();

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{rarFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);

        JarFile rarJarFile = DeploymentUtil.createJarFile(rarFile);
        Module module = moduleBuilder.createModule(action.getVendorDD(), rarJarFile, j2eeContext.getJ2eeModuleName(), action.getSpecDD(), null);
        if (module == null) {
            throw new DeploymentException("Was not a connector module");
        }

        File tempDir = null;
        try {
            tempDir = DeploymentUtil.createTempDir();
            EARContext earContext = new EARContext(tempDir,
                    module.getConfigId(),
                    module.getType(),
                    module.getParentId(),
                    null,
                    j2eeContext.getJ2eeDomainName(),
                    j2eeContext.getJ2eeServerName(),
                    j2eeContext.getJ2eeApplicationName(),
                    null,
                    connectionTrackerName,
                    null,
                    null,
                    new RefContext(null, moduleBuilder));

            action.install(moduleBuilder, earContext, module);
            earContext.getClassLoader(null);
            moduleBuilder.initContext(earContext, module, cl);
            moduleBuilder.addGBeans(earContext, module, cl);
            earContext.close();

            verifyDeployment(tempDir, j2eeContext, oldCl);
        } finally {
            module.close();
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testBuildEar() throws Exception {
        String j2eeDomainName = "geronimo.server";
        String j2eeServerName = "TestGeronimoServer";
        ObjectName connectionTrackerName = new ObjectName("geronimo.connector:service=ConnectionTracker");
        ObjectName j2eeServer = new ObjectName(j2eeDomainName + ":name=" + j2eeServerName);
        Kernel kernel = new Kernel("blah");
        kernel.boot();
        JarFile rarFile = null;
        try {
            rarFile = DeploymentUtil.createJarFile(new File(basedir, "target/test-ear-noger.ear"));
            EARConfigBuilder configBuilder = new EARConfigBuilder(defaultParentId, j2eeServer, null, connectionTrackerName, null, null, null, null, null, null, new ConnectorModuleBuilder(defaultParentId, defaultMaxSize, defaultMinSize, defaultBlockingTimeoutMilliseconds, defaultidleTimeoutMinutes, defaultXATransactionCaching, defaultXAThreadCaching, kernel), null, null, kernel);
            File tempDir = null;
            try {
                tempDir = DeploymentUtil.createTempDir();
                File planFile = new File(basedir, "src/test-data/data/external-application-plan.xml");
                Object plan = configBuilder.getDeploymentPlan(planFile, rarFile);
                configBuilder.buildConfiguration(plan, rarFile, tempDir);
            } finally {
                DeploymentUtil.recursiveDelete(tempDir);
            }
        } finally {
            kernel.shutdown();
            DeploymentUtil.close(rarFile);
        }
    }


    private void verifyDeployment(File unpackedDir, J2eeContext j2eeContext, ClassLoader cl) throws Exception {
        DataSource ds = null;
        Kernel kernel = null;
        try {
            GBeanMBean config = loadConfig(unpackedDir, cl);

            kernel = new Kernel("blah");
            kernel.boot();

            GBeanMBean serverInfoGBean = new GBeanMBean(ServerInfo.GBEAN_INFO);
            serverInfoGBean.setAttribute("baseDirectory", ".");
            ObjectName serverInfoObjectName = ObjectName.getInstance(j2eeContext.getJ2eeDomainName() + ":type=ServerInfo");
            kernel.loadGBean(serverInfoObjectName, serverInfoGBean);
            kernel.startGBean(serverInfoObjectName);
            assertRunning(kernel, serverInfoObjectName);

            GBeanMBean j2eeServerGBean = new GBeanMBean(J2EEServerImpl.GBEAN_INFO);
            j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));
            ObjectName j2eeServerObjectName = NameFactory.getServerName(null, null, j2eeContext);
            kernel.loadGBean(j2eeServerObjectName, j2eeServerGBean);
            kernel.startGBean(j2eeServerObjectName);
            assertRunning(kernel, j2eeServerObjectName);

            // load the configuration
            ObjectName objectName = ObjectName.getInstance("test:configuration=test-ejb-jar");
            kernel.loadGBean(objectName, config);
            config.setAttribute("baseURL", unpackedDir.toURL());

            // start the configuration
            kernel.startRecursiveGBean(objectName);
            assertRunning(kernel, objectName);

            ObjectName applicationObjectName = NameFactory.getApplicationName(null, null, null, j2eeContext);
            if (!j2eeContext.getJ2eeApplicationName().equals("null")) {
                assertRunning(kernel, applicationObjectName);
            } else {
                Set applications = kernel.getMBeanServer().queryNames(applicationObjectName, null);
                assertTrue("No application object should be registered for a standalone module", applications.isEmpty());
            }

            ObjectName moduleName = NameFactory.getModuleName(null, null, null, null, NameFactory.RESOURCE_ADAPTER_MODULE, j2eeContext);
            assertRunning(kernel, moduleName);

            // FirstTestOutboundConnectionFactory
            ObjectName firstConnectionManagerFactory = NameFactory.getResourceComponentName(null, null, null, null, "FirstTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
            assertRunning(kernel, firstConnectionManagerFactory);


            ObjectName firstOutCF = NameFactory.getResourceComponentName(null, null, null, null, "FirstTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, firstOutCF);

            ObjectName firstOutSecurity = new ObjectName("geronimo.security:service=Realm,type=PasswordCredential,name=FirstTestOutboundConnectionFactory");
            assertRunning(kernel, firstOutSecurity);

            ObjectName firstOutMCF = NameFactory.getResourceComponentName(null, null, null, null, "FirstTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, firstOutMCF);

            // SecondTestOutboundConnectionFactory
            ObjectName secondConnectionManagerFactory = NameFactory.getResourceComponentName(null, null, null, null, "SecondTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
            assertRunning(kernel, secondConnectionManagerFactory);


            ObjectName secondOutCF = NameFactory.getResourceComponentName(null, null, null, null, "SecondTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, secondOutCF);

            ObjectName secondOutMCF = NameFactory.getResourceComponentName(null, null, null, null, "SecondTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, secondOutMCF);

            // ThirdTestOutboundConnectionFactory
            ObjectName thirdConnectionManagerFactory = NameFactory.getResourceComponentName(null, null, null, null, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
            assertRunning(kernel, thirdConnectionManagerFactory);


            ObjectName thirdOutCF = NameFactory.getResourceComponentName(null, null, null, null, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, thirdOutCF);

            ObjectName thirdOutMCF = NameFactory.getResourceComponentName(null, null, null, null, "ThirdTestOutboundConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
            assertRunning(kernel, thirdOutMCF);

            kernel.stopGBean(objectName);
        } finally {
            if (ds != null) {
                Connection connection = null;
                Statement statement = null;
                try {
                    connection = ds.getConnection();
                    statement = connection.createStatement();
                    statement.execute("SHUTDOWN");
                } finally {
                    JDBCUtil.close(statement);
                    JDBCUtil.close(connection);
                }
            }

            if (kernel != null) {
                kernel.shutdown();
            }
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private void assertRunning(Kernel kernel, ObjectName objectName) throws Exception {
        int state = ((Integer) kernel.getAttribute(objectName, "state")).intValue();
        assertEquals(State.RUNNING_INDEX, state);
    }

    private GBeanMBean loadConfig(File unpackedCar, ClassLoader classLoader) throws Exception {
        InputStream in = new FileInputStream(new File(unpackedCar, "META-INF/config.ser"));
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(in));
            GBeanData config = new GBeanData();
            config.readExternal(ois);
            return new GBeanMBean(config, classLoader);
        } finally {
            in.close();
        }
    }

    protected void setUp() throws Exception {
        defaultParentId = null;//new URI("org/apache/geronimo/Server");
        File docDir = new File(basedir, "src/test-data/connector_1_0");
        j2eeDD = new File(docDir, "ra.xml").toURL();
        geronimoDD = new File(docDir, "geronimo-ra.xml").toURL();
        xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
    }

    private abstract class InstallAction {
        public File getVendorDD() {
            return null;
        }

        public URL getSpecDD() throws MalformedURLException {
            return null;
        }

        public abstract File getRARFile();

        public void install(ModuleBuilder moduleBuilder, EARContext earContext, Module module) throws Exception {
            moduleBuilder.installModule(module.getModuleFile(), earContext, module);
        }
    }

}
