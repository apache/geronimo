/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Repository;
import org.openejb.deployment.OpenEJBModuleBuilder;

import javax.management.ObjectName;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class WSConfigBuilder implements ConfigurationBuilder {
    private boolean hasEJB = false;

    private final ConfigurationStore configurationStore;
    private final ObjectName j2eeServer;
    private final ObjectName transactionContextManagerObjectName;
    private final ObjectName transactionalTimerObjectName;
    private final ObjectName nonTransactionalTimerObjectName;
    private final ObjectName trackedConnectionAssocator;

    public WSConfigBuilder(ObjectName j2eeServer,
                           ObjectName transactionContextManagerObjectName,
                           ObjectName connectionTrackerObjectName,
                           ObjectName transactionalTimerObjectName,
                           ObjectName nonTransactionalTimerObjectName,
                           ObjectName trackedConnectionAssocator,
                           Repository repository,
                           Kernel kernel,
                           ConfigurationStore configurationStore) {
        this.j2eeServer = j2eeServer;
        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.transactionalTimerObjectName = transactionalTimerObjectName;
        this.nonTransactionalTimerObjectName = nonTransactionalTimerObjectName;
        this.trackedConnectionAssocator = trackedConnectionAssocator;
        this.configurationStore = configurationStore;
    }

    public void doStart() throws WaitingException, Exception {
    }

    public Object getDeploymentPlan(File planFile, JarFile jarFile) throws DeploymentException {
        return null;
    }

    public List buildConfiguration(Object plan, JarFile earFile, File outfile) throws IOException, DeploymentException {
        return null;
    }

    public List buildConfiguration(Object plan, File earFile, File outfile) throws Exception {
        Enumeration entires = new JarFile(earFile).entries();
        while (entires.hasMoreElements()) {
            ZipEntry zipe = (ZipEntry) entires.nextElement();
            String name = zipe.getName();
            if (name.endsWith("/ejb-jar.xml")) {
                hasEJB = true;
                System.out.println("entry found " + name + " the web service is based on a ejb.");
                //log.info("the web service is based on a ejb.");
                break;
            }
        }
        GBeanMBean[] confBeans = null;
        if (hasEJB) {
            File file = installEJBWebService(earFile, outfile);
            confBeans = loadEJBWebService(file, earFile);
        } else {
            File file = installPOJOWebService(earFile, outfile);
            confBeans = loadPOJOWebService(file);
        }
        
        //TODO: take these from plan
        ObjectName gbeanName1 = new ObjectName("geronimo.test:name=" + earFile.getName());
        ObjectName gbeanName2 = new ObjectName("geronimo.test:name=" + earFile.getName() + "EJB");
        Map gbeans = new HashMap();
        gbeans.put(gbeanName1, confBeans[0]);
        if (confBeans.length > 1)
            gbeans.put(gbeanName2, confBeans[1]);
        byte[] state = Configuration.storeGBeans(gbeans);
        GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
        config.setAttribute("ID", new URI("test"));
        config.setReferencePatterns("Parent", null);
        config.setAttribute("classPath", Collections.EMPTY_LIST);
        config.setAttribute("gBeanState", state);
        config.setAttribute("dependencies", Collections.EMPTY_LIST);
        File sourceFile = null;
        try {
            sourceFile = File.createTempFile("test", ".car");
            URL source = sourceFile.toURL();
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(sourceFile)));
            jos.putNextEntry(new ZipEntry("META-INF/config.ser"));
            ObjectOutputStream oos = new ObjectOutputStream(jos);
            config.getGBeanData().writeExternal(oos);
            oos.flush();
            jos.closeEntry();
            jos.close();
            ArrayList list = new ArrayList(1);
            list.add(configurationStore.install(source));
            return list;
        } finally {
            if (sourceFile != null) {
                sourceFile.delete();
            }
        }
    }

    public GBeanMBean[] loadtheWSConfigurations(File installedLocation, File module, ClassLoader classLoader) throws Exception {
        if (hasEJB) {
            return loadEJBWebService(installedLocation, module);
        } else {
            return loadPOJOWebService(installedLocation);
        }
    }

    public File installWebService(File module, File unpackedDir, ClassLoader classLoader) throws IOException, URISyntaxException, DeploymentException {
        ZipFile zipfile = new ZipFile(module);
        Enumeration entires = zipfile.entries();
        while (entires.hasMoreElements()) {
            ZipEntry zipe = (ZipEntry) entires.nextElement();
            String name = zipe.getName();
            if (name.endsWith("/ejb-jar.xml")) {
                hasEJB = true;
                System.out.println("entry found " + name + " the web service is based on a ejb.");
                break;
            }
        }
        if (hasEJB) {
            return installEJBWebService(module, unpackedDir);
        } else {
            return installPOJOWebService(module, unpackedDir);
        }
    }

    /**
     * @param module      Web Service module generated by EWS
     * @param unpackedDir for WS
     * @return the file to where Module is copied in to
     */
    private File installPOJOWebService(File module, File unpackedDir) throws IOException {
        File out = new File(unpackedDir, module.getName());
        copyTheFile(module, out);
        return out;
    }

    private File installEJBWebService(File module, File unpackedDir) throws IOException, URISyntaxException, DeploymentException {
        /**
         * TODO following code deploy the EJB in the OpenEJB EJB continaer. 
         * The code is borrows from the geronimo openEJB module
         * modules/core/src/test/org/openejb/deployment/EJBConfigBuilderTest.java#testEJBJarDeploy()
         * Method. If this code is broken first the  above test should check. If that change this will broke 
         * But this can quickly fix looking at it.      
         */

        URI defaultParentId = new URI("org/apache/geronimo/Server");
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(defaultParentId, null);
        ResourceReferenceBuilder resourceReferenceBuilder = null;
        EARConfigBuilder earConfigBuilder = new EARConfigBuilder(defaultParentId,
                j2eeServer,
                transactionContextManagerObjectName,
                trackedConnectionAssocator,
                transactionalTimerObjectName,
                nonTransactionalTimerObjectName,
                null, // Repository
                moduleBuilder,
                moduleBuilder,
                null,
                null,
                resourceReferenceBuilder, // webconnector
                null, // app client
                null // kernel
        );
        JarFile jarFile = new JarFile(module);
        Object plan = earConfigBuilder.getDeploymentPlan(null, jarFile);
        earConfigBuilder.buildConfiguration(plan, jarFile, unpackedDir);
        return unpackedDir;
    }

    private GBeanMBean[] loadPOJOWebService(File module) throws Exception {
        GBeanMBean gbean = new GBeanMBean(POJOWSGBean.getGBeanInfo());
        //TODO fill up the POJOWSGBean info
        ArrayList classList = AxisGeronimoUtils.getClassFileList(new ZipFile(module));
        gbean.setAttribute("classList", classList);
        gbean.setAttribute("moduleURL", module.toURL());
        return new GBeanMBean[]{gbean};
    }

    private GBeanMBean[] loadEJBWebService(File installLocation, File module) throws Exception {
        GBeanMBean config = loadConfig(installLocation);
        config.setAttribute("baseURL", installLocation.toURL());
        GBeanMBean gbean = new GBeanMBean(EJBWSGBean.getGBeanInfo());
        ArrayList classList = AxisGeronimoUtils.getClassFileList(new ZipFile(module));
        gbean.setAttribute("classList", classList);
        gbean.setAttribute("ejbConfig", config.getTarget());
        return new GBeanMBean[]{gbean, config};
    }

    private GBeanMBean loadConfig(File unpackedCar) throws Exception {
        InputStream in = new FileInputStream(new File(unpackedCar, "META-INF/config.ser"));
        try {
            ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(in));
            GBeanData config = new GBeanData();
            config.readExternal(ois);
            return new GBeanMBean(config, null);
        } finally {
            in.close();
        }
    }

    private void copyTheFile(File inFile, File outFile) throws IOException {
        if (!outFile.exists())
            outFile.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(outFile);
        FileInputStream in = new FileInputStream(inFile);
        try {
            byte[] buf = new byte[1024];
            int val = in.read(buf);
            while (val > 0) {
                out.write(buf, 0, val);
                val = in.read(buf);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(WSConfigBuilder.class);
        infoFactory.addAttribute("j2eeServer", ObjectName.class, true);
        infoFactory.addAttribute("transactionContextManagerObjectName", ObjectName.class, true);
        infoFactory.addAttribute("connectionTrackerObjectName", ObjectName.class, true);
        infoFactory.addAttribute("transactionalTimerObjectName", ObjectName.class, true);
        infoFactory.addAttribute("nonTransactionalTimerObjectName", ObjectName.class, true);
        infoFactory.addAttribute("trackedConnectionAssocator", ObjectName.class, true);
        infoFactory.addAttribute("configurationStore", ConfigurationStore.class, false);
        infoFactory.addReference("Repository", Repository.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addInterface(ConfigurationBuilder.class);
        infoFactory.setConstructor(new String[]{
            "j2eeServer",
            "transactionContextManagerObjectName",
            "connectionTrackerObjectName",
            "transactionalTimerObjectName",
            "nonTransactionalTimerObjectName",
            "trackedConnectionAssocator",
            "configurationStore",
            "Repository",
            "kernel"
        });
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

