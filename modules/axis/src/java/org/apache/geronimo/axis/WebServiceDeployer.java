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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.ews.ws4j2ee.toWs.Ws4J2ee;
import org.apache.geronimo.ews.ws4j2ee.utils.packager.load.PackageModule;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.xmlbeans.XmlObject;
import org.openejb.deployment.OpenEJBModuleBuilder;

/**
 * Class WebServiceDeployer
 */
public class WebServiceDeployer {

    /**
     * Field log
     */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Field j2eeDomainName
     */
    private static final String j2eeDomainName =
            AxisGeronimoConstants.J2EE_DOMAIN_NAME;

    /**
     * Field j2eeServerName
     */
    private static final String j2eeServerName =
            AxisGeronimoConstants.J2EE_SERVER_NAME;

    /**
     * Field transactionManagerObjectName
     */
    private static final ObjectName transactionManagerObjectName =
            JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");

    /**
     * Field connectionTrackerObjectName
     */
    private static final ObjectName connectionTrackerObjectName =
            JMXUtil.getObjectName(j2eeDomainName + ":type=ConnectionTracker");

    /**
     * Field j2eeModuleName
     */
    private String j2eeModuleName;

    /**
     * Field module
     */
    private String module;

    /**
     * Field kernel
     */
    private final Kernel kernel;

    /**
     * Field axisProperties
     */
    private Properties axisProperties;

    /**
     * Field outDir
     */
    private String outDir;

    /**
     * Field properites
     */
    private Properties properites;

    /**
     * Field configStore
     */
    private final File configStore;

    /**
     * Field tempOutDir
     */
    private String tempOutDir;

    /**
     * Field hasEJB
     */
    private boolean hasEJB = false;

    /**
     * Constructor WebServiceDeployer
     *
     * @param tempOutDir
     * @param kernel
     */
    public WebServiceDeployer(String tempOutDir, Kernel kernel) {
        this.tempOutDir = tempOutDir;
        this.kernel = kernel;
        axisProperties = new Properties();
        properites = new Properties();

        log.info("start deployer with the "
                + AxisGeronimoConstants.AXIS_CONFIG_STORE
                + " as the config store.");

        configStore = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
    }

    /**
     * <p>Method deploy is used to do the Deployment based on the j2ee web service module
     * as explaied by the JSR109 specification. But to use this the classapth must contains
     *  the $JAVA_HOME/lib/tools.jar</p>  
     *
     * @param module
     * @param j2eeApplicationName
     * @param j2eeModuleName
     * @throws Exception
     */
    public void deploy(String module, String j2eeApplicationName, String j2eeModuleName)
            throws Exception {
        this.j2eeModuleName = j2eeModuleName;
        this.module = module;

        log.info("start deploymwnt with the " + this.module + ".");

        GeronimoWsDeployContext deployContext =
                new GeronimoWsDeployContext(module, tempOutDir + "/server");
        Ws4J2ee ws4j2ee = new Ws4J2ee(deployContext,
                null);
        ws4j2ee.generate();
        log.info("ews code generation done.");

        PackageModule packageModule = deployContext.getModule();

        if (packageModule.getEjbJarfile() != null) {
            this.hasEJB = true;

            System.out.println("the web service is based on a ejb.");
            //log.info("the web service is based on a ejb.");
        } else if (packageModule.getWebddfile() != null) {
            this.hasEJB = false;

            System.out.println("the web service is based on a java class.");
            //log.info("the web service is based on a java class.");
        } else {
            throw new DeploymentException("the module must have web.xml or ejb-jar.xml file");
        }

        File file = findTheImpl();
        deployTheWebService(file);
        addEntryToAxisDDStatically(file);
    }

    /**
     * <p>Deploy the web service based on the jar file generated by the EWS tool.
     * This is to work around the compilation probleam</p> 
     * @param module
     * @param j2eeApplicationName
     * @param j2eeModuleName
     * @throws Exception
     */
    public void deployEWSModule(String module, String j2eeApplicationName, String j2eeModuleName)
            throws Exception {
        this.j2eeModuleName = j2eeModuleName;
        this.module = module;

        log.info("start deployment with the " + this.module + ".");
        ZipFile zipfile = new ZipFile(module);
        Enumeration entires = zipfile.entries();
        while(entires.hasMoreElements()){
            ZipEntry zipe = (ZipEntry)entires.nextElement();
            String name = zipe.getName();
            if(name.endsWith("/ejb-jar.xml")){
                this.hasEJB = true;
                System.out.println("entry found "+name+" the web service is based on a ejb.");
                //log.info("the web service is based on a ejb.");
                break;
            } 
        }

        File file = new File(module);
        deployTheWebService(file);
        addEntryToAxisDDStatically(file);
        
    }

    /**
     * Method loadPropertyFiles
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void loadPropertyFiles() throws FileNotFoundException, IOException {
        File popertiesfile = new File(configStore, "index.properties");
        int index = 1;

        if (popertiesfile.exists()) {
            properites.load(new FileInputStream(popertiesfile));

            while (properites.containsValue(String.valueOf(index))) {
                index++;
            }
        } else {
            popertiesfile.getParentFile().mkdirs();
            popertiesfile.createNewFile();
        }

        outDir = String.valueOf(index);

        File axisPopertiesfile = new File(configStore, "axis.properties");

        if (axisPopertiesfile.exists()) {
            axisProperties.load(new FileInputStream(axisPopertiesfile));
        } else {
            axisPopertiesfile.createNewFile();
        }
    }

    /**
     * Method storeProperties
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void storeProperties() throws FileNotFoundException, IOException {
        File popertiesfile = new File(configStore, "index.properties");

        properites.store(new FileOutputStream(popertiesfile),
                "ws configuration");

        File axispopertiesfile = new File(configStore, "axis.properties");

        axisProperties.store(new FileOutputStream(axispopertiesfile),
                "ws configuration");
    }

    /**
     * Method deployTheWebService
     *
     * @param earFile
     * @throws DeploymentException
     */
    public void deployTheWebService(File earFile) throws DeploymentException {
        File unpackedDir = null;

        try {
            loadPropertyFiles();
            properites.setProperty(j2eeModuleName, outDir);
            axisProperties.setProperty(outDir, j2eeModuleName);

            unpackedDir = new File(configStore, outDir);

            unpackedDir.mkdirs();

            if (hasEJB) {
                deployEJB(earFile, unpackedDir);
            } else {
                File out = new File(unpackedDir, earFile.getName());
                copyTheFile(earFile, out);
            }
        } catch (Exception e) {
            // if something goes wrong make sure nothing leaves in a middle
            // state
            if (unpackedDir != null) {
                AxisGeronimoUtils.delete(unpackedDir);
            }
            throw new DeploymentException(e);
        }
    }

    /**
     * Method deployEJB
     *
     * @param earFile
     * @param unpackedDir
     * @throws DeploymentException
     */
    private void deployEJB(File earFile, File unpackedDir)
            throws DeploymentException {
        try {
/////////////////////////////////////////////////////////////////////////////////////////////            
          /**
           * TODO following code deploy the EJB in the OpenEJB EJB continaer. 
           * The code is borrows from the geronimo openEJB module
           * modules/core/src/test/org/openejb/deployment/EJBConfigBuilderTest.java#testEJBJarDeploy()
           * Method. If this code is broken first the  above test should check. If that change this will broke 
           * But this can quickly fix looking at it.      
           */

            OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(kernel);
            ClassLoader oldCl =
                    Thread.currentThread().getContextClassLoader();
            ClassLoader cl =
                    new URLClassLoader(new URL[]{earFile.toURL()}, oldCl);

            Thread.currentThread().setContextClassLoader(cl);

            File carFile = File.createTempFile("OpenEJBTest", ".car");
			ObjectName j2eeServerObjectName = ObjectName.getInstance(AxisGeronimoConstants.J2EE_SERVER_OBJECT_NAME);

            try {
                EARConfigBuilder earConfigBuilder 
                    = new EARConfigBuilder(
                        j2eeServerObjectName, 
                        AxisGeronimoConstants.TRANSACTIONCONTEXTMANAGER_NAME, 
                        AxisGeronimoConstants.TRACKEDCONNECTIONASSOCIATOR_NAME, 
                        AxisGeronimoConstants.TRANSACTIONALTIMER_NAME, 
                        AxisGeronimoConstants.NONTRANSACTIONALTIMER_NAME, 
                        null, moduleBuilder, moduleBuilder, null, null, null);
                XmlObject plan =
                        earConfigBuilder.getDeploymentPlan(earFile.toURL());

                earConfigBuilder.buildConfiguration(carFile, null, earFile,
                        plan);
                LocalConfigStore.unpack(unpackedDir,
                        new FileInputStream(carFile));
/////////////////////////////////////////////////////////////////////////////////
                // store the property IFF all goes well
                storeProperties();
            } finally {
                carFile.delete();
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Method findTheImpl
     *
     * @return
     */
    private File findTheImpl() {
        File outDir = new File(tempOutDir + "/server");

        if (outDir.isDirectory()) {
            File[] files = outDir.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getAbsolutePath().endsWith(".jar")) {
                        return files[i];
                    }
                }
            }
        }

        throw new RuntimeException("implementation jar not found");
    }

    /**
     * Method copyTheFile
     *
     * @param inFile
     * @param outFile
     * @throws IOException
     */
    private void copyTheFile(File inFile, File outFile) throws IOException {
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
    /**
     * <p>add the entry to the Axis Confieration file about the web service.
     * This find the coniguration file and and update this. There are two problems.
     * Number one is service is deployed only once the Axis is restarted. And it is 
     * best not to do this while the Axis is running.</p>
     * @param module
     * @throws DeploymentException
     */
    private void addEntryToAxisDDStatically(File module)throws DeploymentException{
//        try {
//            InputStream wsddconf = null;
//            File file = null;
//            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader(); 
//            URL url = contextLoader.getResource("deployables/axis/WEB-INF/server-config.wsdd");
//            if(url != null){
//                file = new File(url.getFile());
//                if(!url.sameFile(file.toURL()))
//                    throw new DeploymentException("can not load the configuaration");
//                wsddconf = new FileInputStream(file);
//            }
//            
//            if(wsddconf == null){
//                wsddconf = contextLoader.getResourceAsStream("org/apache/axis/server/server-config.wsdd");            
//            }
//        
//            ZipFile zipfile = new ZipFile(module);
//            ZipEntry zentry = zipfile.getEntry("deploy.wsdd");
//            InputStream deplydd = zipfile.getInputStream(zentry);
//            
//            Admin admin = new Admin();
//
//            WSDDDocument wsddDoc = new WSDDDocument(XMLUtils.newDocument(wsddconf));
//            WSDDDeployment deployment = wsddDoc.getDeployment();
//            AxisEngine engine = new AxisServer(deployment);
//            engine.setShouldSaveConfig(true);
//            engine.init();
//            MessageContext msgContext = new MessageContext(engine);
//            
//            Document doc = XMLUtils.newDocument(deplydd);
//            Document result = admin.process(msgContext, doc.getDocumentElement());
//            System.out.println(XMLUtils.DocumentToString(result));
//            PrintWriter w = new PrintWriter(new FileWriter(file));
//            deployment.writeToContext(new SerializationContextImpl(w));
//            w.close();
//        } catch (Exception e) {
//            throw new DeploymentException(e);
//        } 
    }
}
