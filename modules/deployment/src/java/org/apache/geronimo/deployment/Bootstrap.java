/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.system.main.CommandLine;
import org.apache.geronimo.system.repository.ReadOnlyRepository;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 * Helper class to bootstrap the Geronimo deployer.
 *
 * @version $Revision: 1.8 $ $Date: 2004/02/24 06:05:36 $
 */
public class Bootstrap {
    public static final URI CONFIG_ID = URI.create("org/apache/geronimo/DeployerSystem");
    private static final ObjectName REPOSITORY_NAME = JMXUtil.getObjectName("geronimo.deployer:role=Repository,root=repository");
    private static final ObjectName SERVICE_BUILDER_NAME = JMXUtil.getObjectName("geronimo.deployer:role=Builder,type=Service,id=" + CONFIG_ID.toString());

    /**
     * Invoked from maven.xml during the build to create the first Deployment Configuration
     * @param car the configuration file to generate
     */
    public static void bootstrap(String car, String baseDir, String store, String planPath, String classPath, String mainGBean, String mainMethod, String configurations) {
        File carfile = new File(car);
        File storeDir = new File(baseDir, store);

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Bootstrap.class.getClassLoader());
        try {
            GBeanMBean deploymentSystemConfig = getDeploymentSystemConfig(new URI(store));

            // create the manifext
            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), CommandLine.class.getName());
            mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), classPath);
            mainAttributes.putValue(CommandLine.MAIN_GBEAN.toString(), mainGBean);
            mainAttributes.putValue(CommandLine.MAIN_METHOD.toString(), mainMethod);
            mainAttributes.putValue(CommandLine.CONFIGURATIONS.toString(), configurations);

            // write the deployer system out to a jar
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(carfile)), manifest);
            try {
                // add the startup jar entry which allows us to locat the startup directory
                jos.putNextEntry(new ZipEntry("META-INF/startup-jar"));
                jos.closeEntry();

                // write the configuration to the jar
                jos.putNextEntry(new ZipEntry("META-INF/config.ser"));
                ObjectOutputStream ois = new ObjectOutputStream(jos);
                Configuration.storeGMBeanState(deploymentSystemConfig, ois);
                ois.flush();
                jos.closeEntry();
            } finally {
                jos.close();
            }

            // install the deployer systen in to the config store
            LocalConfigStore configStore = new LocalConfigStore(storeDir);
            configStore.install(carfile.toURL());

            System.setProperty("geronimo.base.dir", baseDir);
            Kernel kernel = new Kernel("geronimo.bootstrap");
            kernel.boot();

            ConfigurationManager configurationManager = kernel.getConfigurationManager();
            ObjectName deploymentSystemName = configurationManager.load(deploymentSystemConfig, carfile.toURL());
            kernel.startRecursiveGBean(deploymentSystemName);

            GBeanMBean serviceDeployerConfig = getServiceDeployerConfig();
            serviceDeployerConfig.setReferencePatterns("Parent", Collections.singleton(deploymentSystemName));
            ObjectName serviceDeployerName = configurationManager.load(serviceDeployerConfig, carfile.toURL());
            kernel.startRecursiveGBean(serviceDeployerName);

            File tempFile = File.createTempFile("deployer", ".car");
            try {
                URL planURL = new File(planPath).toURL();
                XmlObject plan = XmlBeans.getContextTypeLoader().parse(planURL, null, null);
                kernel.getMBeanServer().invoke(
                        SERVICE_BUILDER_NAME,
                        "buildConfiguration",
                        new Object[]{tempFile, null, plan},
                        new String[]{File.class.getName(), JarInputStream.class.getName(), XmlObject.class.getName()});
                configStore.install(tempFile.toURL());
            } finally {
                tempFile.delete();
            }

            kernel.stopGBean(deploymentSystemName);
            kernel.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    private static GBeanMBean getDeploymentSystemConfig(URI storeDir) throws Exception {
        Map gbeans = new HashMap();

        // Install ServerInfo GBean
        ObjectName serverInfoName = new ObjectName("geronimo.deployer:role=ServerInfo");
        GBeanMBean serverInfo = new GBeanMBean(ServerInfo.getGBeanInfo());
        gbeans.put(serverInfoName, serverInfo);

        // Install LocalConfigStore
        GBeanMBean storeGBean = new GBeanMBean(LocalConfigStore.getGBeanInfo());
        storeGBean.setAttribute("root", storeDir);
        storeGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoName));
        gbeans.put(new ObjectName("geronimo.boot:role=ConfigurationStore"), storeGBean);

        // Install default local Repository
        GBeanMBean localRepo = new GBeanMBean(ReadOnlyRepository.GBEAN_INFO);
        localRepo.setAttribute("Root", URI.create("repository/"));
        localRepo.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoName));
        gbeans.put(REPOSITORY_NAME, localRepo);

        // assemble the deployer system configuration
        GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
        config.setAttribute("ID", CONFIG_ID);
        config.setReferencePatterns("Parent", null);
        config.setAttribute("ClassPath", new ArrayList());
        config.setAttribute("GBeanState", Configuration.storeGBeans(gbeans));
        config.setAttribute("Dependencies", Collections.EMPTY_LIST);
        return config;
    }

    private static GBeanMBean getServiceDeployerConfig() throws Exception {
        Map gbeans = new HashMap();

        // Install ServiceConfigBuilder
        GBeanMBean serviceBuilder = new GBeanMBean(ServiceConfigBuilder.GBEAN_INFO);
        serviceBuilder.setReferencePatterns("Repository", Collections.singleton(REPOSITORY_NAME));
        serviceBuilder.setReferencePatterns("Kernel", Collections.singleton(Kernel.KERNEL));
        gbeans.put(SERVICE_BUILDER_NAME, serviceBuilder);

        // Install Deployer
        ObjectName deployerName = Deployer.getDeployerName(CONFIG_ID);
        GBeanMBean deployer = new GBeanMBean(Deployer.GBEAN_INFO);
        deployer.setReferencePatterns("Builders", Collections.singleton(SERVICE_BUILDER_NAME));
        gbeans.put(deployerName, deployer);

        GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
        config.setAttribute("ID", new URI("temp/deployment/ServiceDeployer"));
        config.setAttribute("ClassPath", new ArrayList());
        config.setAttribute("GBeanState", Configuration.storeGBeans(gbeans));
        config.setAttribute("Dependencies", Collections.EMPTY_LIST);

        return config;
    }
}
