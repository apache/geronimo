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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import javax.management.ObjectName;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.JarUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.configuration.LocalConfigStore;
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
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
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
        String j2eeDomainName = "geronimo.server";
        String j2eeServerName = "TestGeronimoServer";
        String j2eeModuleName = "org/apache/geronimo/j2ee/deployment/test";
        String j2eeApplicationName = "null";
        ObjectName connectionTrackerName = new ObjectName("geronimo.connector:service=ConnectionTracker");

        ConnectorModuleBuilder moduleBuilder = new ConnectorModuleBuilder();
        File rarFile = action.getRARFile();

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = new URLClassLoader(new URL[]{rarFile.toURL()}, oldCl);

        Thread.currentThread().setContextClassLoader(cl);

        JarFile rarJarFile = JarUtil.createJarFile(rarFile);
        Module module = moduleBuilder.createModule(action.getVendorDD(), rarJarFile, j2eeModuleName, action.getSpecDD(), null);
        if (module == null) {
            throw new DeploymentException("Was not a connector module");
        }

        File carFile = File.createTempFile("RARTest", ".car");
        try {
            EARContext earContext = new EARContext(new JarOutputStream(new FileOutputStream(carFile)),
                    module.getConfigId(),
                    module.getType(),
                    module.getParentId(),
                    null,
                    j2eeDomainName,
                    j2eeServerName,
                    j2eeApplicationName,
                    null,
                    connectionTrackerName,
                    null,
                    null,
                    null);

            action.install(moduleBuilder, earContext, module);
            earContext.getClassLoader(null);
            moduleBuilder.initContext(earContext, module, cl);
            moduleBuilder.addGBeans(earContext, module, cl);
            earContext.close();

            File tempdir = new File(System.getProperty("java.io.tmpdir"));
            File unpackedDir = new File(tempdir, "OpenEJBTest-Unpacked");
            LocalConfigStore.unpack(unpackedDir, new FileInputStream(carFile));

            verifyDeployment(unpackedDir, oldCl, j2eeDomainName, j2eeServerName, j2eeApplicationName, j2eeModuleName);
        } finally {
            carFile.delete();
        }
    }

    public void testBuildEar() throws Exception {
        String j2eeDomainName = "geronimo.server";
        String j2eeServerName = "TestGeronimoServer";
        ObjectName connectionTrackerName = new ObjectName("geronimo.connector:service=ConnectionTracker");
        ObjectName j2eeServer = new ObjectName(j2eeDomainName + ":name=" + j2eeServerName);
        Kernel kernel = new Kernel("blah");
        kernel.boot();
        try {
            EARConfigBuilder configBuilder = new EARConfigBuilder(j2eeServer, null, connectionTrackerName, null, null, null, null, null, null, new ConnectorModuleBuilder(), null, kernel);
            JarFile rarFile = JarUtil.createJarFile(new File(basedir, "target/test-ear-noger.ear"));
            File outFile = File.createTempFile("EARTest", ".car");
            try {
                File planFile = new File(basedir, "src/test-data/data/external-application-plan.xml");
                Object plan = configBuilder.getDeploymentPlan(planFile, rarFile);
                configBuilder.buildConfiguration(outFile, null, plan, rarFile);
            } finally {
                outFile.delete();
            }
        } finally {
            kernel.shutdown();
        }
    }


    private void verifyDeployment(File unpackedDir, ClassLoader cl, String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName) throws Exception {
        DataSource ds = null;
        Kernel kernel = null;
        try {
            GBeanMBean config = loadConfig(unpackedDir);

            kernel = new Kernel("blah");
            kernel.boot();

            GBeanMBean serverInfoGBean = new GBeanMBean(ServerInfo.GBEAN_INFO);
            serverInfoGBean.setAttribute("baseDirectory", ".");
            ObjectName serverInfoObjectName = ObjectName.getInstance(j2eeDomainName + ":type=ServerInfo");
            kernel.loadGBean(serverInfoObjectName, serverInfoGBean);
            kernel.startGBean(serverInfoObjectName);
            assertRunning(kernel, serverInfoObjectName);

            GBeanMBean j2eeServerGBean = new GBeanMBean(J2EEServerImpl.GBEAN_INFO);
            j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));
            ObjectName j2eeServerObjectName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName);
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

            ObjectName applicationObjectName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=J2EEApplication,name=" + j2eeApplicationName + ",J2EEServer=" + j2eeServerName);
            if (!j2eeApplicationName.equals("null")) {
                assertRunning(kernel, applicationObjectName);
            } else {
                Set applications = kernel.getMBeanServer().queryNames(applicationObjectName, null);
                assertTrue("No application object should be registered for a standalone module", applications.isEmpty());
            }

            ObjectName moduleName = ObjectName.getInstance(j2eeDomainName + ":j2eeType=ResourceAdapterModule,J2EEServer=" + j2eeServerName + ",J2EEApplication=" + j2eeApplicationName + ",name=" + j2eeModuleName);
            assertRunning(kernel, moduleName);

            // FirstTestOutboundConnectionFactory
            ObjectName firstConnectionManagerFactory = new ObjectName(j2eeDomainName +
                    ":j2eeType=ConnectionManager" +
                    ",J2EEServer=" + j2eeServerName +
                    ",name=FirstTestOutboundConnectionFactory");
            assertRunning(kernel, firstConnectionManagerFactory);


            ObjectName firstOutCF = new ObjectName(j2eeDomainName +
                    ":j2eeType=JCAConnectionFactory" +
                    ",J2EEServer=" + j2eeServerName +
                    ",JCAResource=null" +
                    ",name=FirstTestOutboundConnectionFactory");
            assertRunning(kernel, firstOutCF);

            ObjectName firstOutSecurity = new ObjectName("geronimo.security:service=Realm,type=PasswordCredential,name=FirstTestOutboundConnectionFactory");
            assertRunning(kernel, firstOutSecurity);

            ObjectName firstOutMCF = new ObjectName(j2eeDomainName +
                    ":j2eeType=JCAManagedConnectionFactory" +
                    ",J2EEServer=" + j2eeServerName +
                    ",name=FirstTestOutboundConnectionFactory");
            assertRunning(kernel, firstOutMCF);

            // SecondTestOutboundConnectionFactory
            ObjectName secondConnectionManagerFactory = new ObjectName(j2eeDomainName +
                    ":j2eeType=ConnectionManager" +
                    ",J2EEServer=" + j2eeServerName +
                    ",name=SecondTestOutboundConnectionFactory");
            assertRunning(kernel, secondConnectionManagerFactory);


            ObjectName secondOutCF = new ObjectName(j2eeDomainName +
                    ":j2eeType=JCAConnectionFactory" +
                    ",J2EEServer=" + j2eeServerName +
                    ",JCAResource=null" +
                    ",name=SecondTestOutboundConnectionFactory");
            assertRunning(kernel, secondOutCF);

            ObjectName secondOutMCF = new ObjectName(j2eeDomainName +
                    ":j2eeType=JCAManagedConnectionFactory" +
                    ",J2EEServer=" + j2eeServerName +
                    ",name=SecondTestOutboundConnectionFactory");
            assertRunning(kernel, secondOutMCF);

            // ThirdTestOutboundConnectionFactory
            ObjectName thirdConnectionManagerFactory = new ObjectName(j2eeDomainName +
                    ":j2eeType=ConnectionManager" +
                    ",J2EEServer=" + j2eeServerName +
                    ",name=ThirdTestOutboundConnectionFactory");
            assertRunning(kernel, thirdConnectionManagerFactory);


            ObjectName thirdOutCF = new ObjectName(j2eeDomainName +
                    ":j2eeType=JCAConnectionFactory" +
                    ",J2EEServer=" + j2eeServerName +
                    ",JCAResource=null" +
                    ",name=ThirdTestOutboundConnectionFactory");
            assertRunning(kernel, thirdOutCF);

            ObjectName thirdOutMCF = new ObjectName(j2eeDomainName +
                    ":j2eeType=JCAManagedConnectionFactory" +
                    ",J2EEServer=" + j2eeServerName +
                    ",name=ThirdTestOutboundConnectionFactory");
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

    private GBeanMBean loadConfig(File unpackedCar) throws Exception {
        InputStream in = new FileInputStream(new File(unpackedCar, "META-INF/config.ser"));
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(in));
            GBeanInfo gbeanInfo = Configuration.GBEAN_INFO;
            GBeanMBean config = new GBeanMBean(gbeanInfo);
            Configuration.loadGMBeanState(config, ois);
            return config;
        } finally {
            in.close();
        }
    }

    protected void setUp() throws Exception {
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
            moduleBuilder.installModule(JarUtil.createJarFile(getRARFile()), earContext, module);
        }
    }

}
