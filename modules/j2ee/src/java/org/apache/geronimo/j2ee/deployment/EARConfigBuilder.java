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
package org.apache.geronimo.j2ee.deployment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.UnpackedJarFile;
import org.apache.geronimo.deployment.util.JarUtil;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.management.impl.J2EEApplicationImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationDocument;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerDependencyType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerGbeanType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerModuleType;
import org.apache.geronimo.xbeans.j2ee.ApplicationDocument;
import org.apache.geronimo.xbeans.j2ee.ApplicationType;
import org.apache.geronimo.xbeans.j2ee.ModuleType;
import org.apache.geronimo.xbeans.j2ee.WebType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EARConfigBuilder implements ConfigurationBuilder {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(GerApplicationDocument.class.getClassLoader())
    });


    private static final String PARENT_ID = "org/apache/geronimo/Server";

    private final Kernel kernel;
    private final Repository repository;
    private final ModuleBuilder ejbConfigBuilder;
    private final ModuleBuilder webConfigBuilder;
    private final ModuleBuilder connectorConfigBuilder;
    private final EJBReferenceBuilder ejbReferenceBuilder;
    private final String j2eeServerName;
    private final String j2eeDomainName;
    private final ObjectName j2eeServer;
    private final ObjectName transactionContextManagerObjectName;
    private final ObjectName connectionTrackerObjectName;
    private final ObjectName transactionalTimerObjectName;
    private final ObjectName nonTransactionalTimerObjectName;


    public EARConfigBuilder(ObjectName j2eeServer, ObjectName transactionContextManagerObjectName, ObjectName connectionTrackerObjectName, ObjectName transactionalTimerObjectName, ObjectName nonTransactionalTimerObjectName, Repository repository, ModuleBuilder ejbConfigBuilder, EJBReferenceBuilder ejbReferenceBuilder, ModuleBuilder webConfigBuilder, ModuleBuilder connectorConfigBuilder, Kernel kernel) {
        this.kernel = kernel;
        this.repository = repository;
        this.j2eeServer = j2eeServer;
        j2eeServerName = j2eeServer.getKeyProperty("name");
        j2eeDomainName = j2eeServer.getDomain();

        this.ejbConfigBuilder = ejbConfigBuilder;
        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.webConfigBuilder = webConfigBuilder;
        this.connectorConfigBuilder = connectorConfigBuilder;
        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.transactionalTimerObjectName = transactionalTimerObjectName;
        this.nonTransactionalTimerObjectName = nonTransactionalTimerObjectName;
    }

    public boolean canConfigure(XmlObject plan) {
        if (plan instanceof GerApplicationDocument) {
            return true;
        }
        if (connectorConfigBuilder != null && connectorConfigBuilder.canHandlePlan(plan)) {
            return true;
        }
        if (ejbConfigBuilder != null && ejbConfigBuilder.canHandlePlan(plan)) {
            return true;
        }
        if (webConfigBuilder != null && webConfigBuilder.canHandlePlan(plan)) {
            return true;
        }
        return false;
    }

    public SchemaTypeLoader[] getTypeLoaders() {
        List typeLoaders = new ArrayList();
        typeLoaders.add(SCHEMA_TYPE_LOADER);
        if (connectorConfigBuilder != null) {
            typeLoaders.add(connectorConfigBuilder.getSchemaTypeLoader());
        }
        if (ejbConfigBuilder != null) {
            typeLoaders.add(ejbConfigBuilder.getSchemaTypeLoader());
        }
        if (webConfigBuilder != null) {
            typeLoaders.add(webConfigBuilder.getSchemaTypeLoader());
        }
        return (SchemaTypeLoader[]) typeLoaders.toArray(new SchemaTypeLoader[typeLoaders.size()]);
    }

    public XmlObject getDeploymentPlan(URL deploymentURL) throws XmlException {
        try {
            URL moduleBase;
            if (deploymentURL.toString().endsWith("/")) {
                moduleBase = deploymentURL;
            } else {
                moduleBase = new URL("jar:" + deploymentURL.toString() + "!/");
            }
            GerApplicationDocument gerAppDoc = (GerApplicationDocument) XmlBeansUtil.getXmlObject(new URL(moduleBase, "META-INF/geronimo-application.xml"), GerApplicationDocument.type);
            if (gerAppDoc != null) {
                return gerAppDoc;
            }

            // try to create a default plan (will return null if this is not an ear file)
            GerApplicationDocument defaultPlan = createDefaultPlan(moduleBase);
            if (defaultPlan != null) {
                return defaultPlan;
            }
        } catch (MalformedURLException e) {
        }

        // support a naked modules
        if (webConfigBuilder != null) {
            XmlObject plan = webConfigBuilder.getDeploymentPlan(deploymentURL);
            if (plan != null) {
                return plan;
            }
        }

        if (ejbConfigBuilder != null) {
            XmlObject plan = ejbConfigBuilder.getDeploymentPlan(deploymentURL);
            if (plan != null) {
                return plan;
            }
        }

        if (connectorConfigBuilder != null) {
            XmlObject plan = connectorConfigBuilder.getDeploymentPlan(deploymentURL);
            if (plan != null) {
                return plan;
            }
        }

        return null;
    }

    private GerApplicationDocument createDefaultPlan(URL deploymentURL) throws XmlException {
        // load the web.xml
        URL applicationXmlUrl = null;
        try {
            applicationXmlUrl = new URL(deploymentURL, "META-INF/application.xml");
        } catch (MalformedURLException e) {
            return null;
        }
        ApplicationDocument applicationDoc;
        try {
            InputStream ddInputStream = applicationXmlUrl.openStream();
            applicationDoc = getApplicationDocument(ddInputStream);
        } catch (IOException e) {
            return null;
        } catch (DeploymentException e) {
            return null;
        }
        if (applicationDoc == null) {
            return null;
        }

        // construct the empty geronimo-application.xml
        GerApplicationDocument gerApplicationDocument = GerApplicationDocument.Factory.newInstance();
        GerApplicationType gerApplication = gerApplicationDocument.addNewApplication();

        // set the parentId and configId
        gerApplication.setParentId(PARENT_ID);
        String id = applicationDoc.getApplication().getId();
        if (id == null) {
            id = deploymentURL.getFile();
            if (id.endsWith("!/")) {
                id = id.substring(0, id.length() - 2);
            }
            if (id.endsWith(".ear")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
            id = id.substring(id.lastIndexOf('/') + 1);
        }

        gerApplication.setConfigId(id);
        return gerApplicationDocument;
    }

    public void buildConfiguration(File outfile, Manifest manifest, final File earFolder, final XmlObject plan) throws IOException, DeploymentException {
        JarFile earFile = null;
        if (earFolder.isDirectory()) {
            earFile = new UnpackedJarFile(earFolder);
        } else {
            earFile = new JarFile(earFolder);
        }

        FileOutputStream fos = new FileOutputStream(outfile);
        try {
            // get the ids from either the application plan or for a stand alone module from the specific deployer
            URI configId = getConfigId(plan);
            ConfigurationModuleType type = getType(plan);
            URI parentId = getParentId(plan);

            // get the modules either the application plan or for a stand alone module from the specific deployer
            Set moduleLocations = new HashSet();
            Set modules = new LinkedHashSet();
            ApplicationType application = addModules(earFile, plan, configId, moduleLocations, modules);
            // if this is an ear, the application name is the configId; otherwise application name is "null"
            String applicationName;
            if (application != null) {
                applicationName = configId.toString();
            } else {
                applicationName = "null";
            }

            // Create the output ear context
            JarOutputStream os = new JarOutputStream(new BufferedOutputStream(fos));
            EARContext earContext = null;
            try {
                earContext = new EARContext(os,
                        configId,
                        type,
                        parentId,
                        kernel,
                        j2eeDomainName,
                        j2eeServerName,
                        applicationName,
                        transactionContextManagerObjectName,
                        connectionTrackerObjectName,
                        transactionalTimerObjectName,
                        nonTransactionalTimerObjectName, ejbReferenceBuilder);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }

            // Copy over all files that are _NOT_ modules
            if (application != null) {
                for (Enumeration e = earFile.entries(); e.hasMoreElements();) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    if (!moduleLocations.contains(entry.getName())) {
                        earContext.addFile(URI.create(entry.getName()), earFile.getInputStream(entry));
                    }
                }
            }

            // add dependencies declared in the geronimo-application.xml
            if (plan instanceof GerApplicationDocument) {
                GerApplicationDocument applicationDoc = (GerApplicationDocument) plan;
                GerApplicationType geronimoApplication = applicationDoc.getApplication();
                GerDependencyType[] dependencies = geronimoApplication.getDependencyArray();
                for (int i = 0; i < dependencies.length; i++) {
                    earContext.addDependency(getDependencyURI(dependencies[i]));
                }
            }

            // each module installs it's files into the output context.. this is differenct for each module type
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                getBuilder(module).installModule(earFile, earContext, module);
            }

            // give each module a chance to populate the earContext now that a classloader is available
            ClassLoader cl = earContext.getClassLoader(repository);
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                getBuilder(module).initContext(earContext, module, cl);
            }

            // add gbeans declared in the geronimo-application.xml
            if (plan instanceof GerApplicationDocument) {
                GerApplicationDocument applicationDoc = (GerApplicationDocument) plan;
                GerApplicationType geronimoApplication = applicationDoc.getApplication();
                GerGbeanType[] gbeans = geronimoApplication.getGbeanArray();
                for (int i = 0; i < gbeans.length; i++) {
                    GBeanHelper.addGbean(new GerGBeanAdapter(gbeans[i]), cl, earContext);
                }
            }

            // Create the J2EEApplication managed object
            if (application != null) {
                GBeanMBean gbean = new GBeanMBean(J2EEApplicationImpl.GBEAN_INFO, cl);
                try {
                    gbean.setAttribute("deploymentDescriptor", application.toString());
                } catch (Exception e) {
                    throw new DeploymentException("Error initializing J2EEApplication managed object");
                }
                gbean.setReferencePatterns("j2eeServer", Collections.singleton(j2eeServer));
                earContext.addGBean(earContext.getApplicationObjectName(), gbean);
            }

            // each module can now add it's GBeans
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                getBuilder(module).addGBeans(earContext, module, cl);
            }

            earContext.close();
            os.flush();
        } finally {
            if (earFile != null) {
                try {
                    earFile.close();
                } catch (IOException ignored) {
                }
            }
            fos.close();
        }
    }

    private ApplicationType addModules(final JarFile earFile, XmlObject plan, URI configId, Set moduleLocations, Set modules) throws IOException, DeploymentException {
        if (plan instanceof GerApplicationDocument) {
            ApplicationType application;
            try {
                JarEntry appXMLEntry = earFile.getJarEntry("META-INF/application.xml");
                if (appXMLEntry == null) {
                    throw new DeploymentException("Did not find META-INF/application.xml in earFile");
                }
                InputStream ddInputStream = earFile.getInputStream(appXMLEntry);
                application = getApplicationDocument(ddInputStream).getApplication();
            } catch (XmlException e) {
                throw new DeploymentException("Unable to parse application.xml", e);
            }

            // build map from module path to alt vendor dd
            Map altVendorDDs = new HashMap();
            GerApplicationDocument gerApplication = (GerApplicationDocument) plan;
            GerModuleType gerModuleTypes[] = gerApplication.getApplication().getModuleArray();
            for (int i = 0; i < gerModuleTypes.length; i++) {
                GerModuleType gerModuleType = gerModuleTypes[i];
                if (gerModuleType.isSetAltDd()) {
                    String path = null;
                    if (gerModuleType.isSetEjb()) {
                        path = gerModuleType.getEjb().getStringValue();
                    } else if (gerModuleType.isSetWeb()) {
                        path = gerModuleType.getWeb().getStringValue();
                    } else if (gerModuleType.isSetConnector()) {
                        path = gerModuleType.getConnector().getStringValue();
                    }
                    altVendorDDs.put(path, JarUtil.createJarURL(earFile, gerModuleType.getAltDd().getStringValue()));
                }
            }


            // get a set containing all of the files in the ear that are actually modules
            ModuleType[] moduleTypes = application.getModuleArray();

            for (int i = 0; i < moduleTypes.length; i++) {
                ModuleType moduleXml = moduleTypes[i];

                String modulePath = null;
                ModuleBuilder builder = null;
                String webContextRoot = null;

                if (moduleXml.isSetEjb()) {
                    modulePath = moduleXml.getEjb().getStringValue();
                    if (ejbConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy ejb application; No ejb deployer defined: " + modulePath);
                    }
                    builder = ejbConfigBuilder;
                } else if (moduleXml.isSetWeb()) {
                    WebType web = moduleXml.getWeb();
                    modulePath = web.getWebUri().getStringValue();
                    webContextRoot = web.getContextRoot().getStringValue();
                    if (webConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy web application; No war deployer defined: " + modulePath);
                    }
                    builder = webConfigBuilder;
                } else if (moduleXml.isSetConnector()) {
                    modulePath = moduleXml.getConnector().getStringValue();
                    if (connectorConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy resource adapter; No rar deployer defined: " + modulePath);
                    }
                    builder = connectorConfigBuilder;
                }
                if (builder != null) {
                    moduleLocations.add(modulePath);

                    URL altSpecDD = null;
                    if (moduleXml.isSetAltDd()) {
                        altSpecDD = JarUtil.createJarURL(earFile, moduleXml.getAltDd().getStringValue());
                    }

                    XmlObject vendorDD = null;
                    URL altVendorDD = (URL) altVendorDDs.get(modulePath);
                    if (altVendorDD != null) {
                        try {
                            vendorDD = builder.parseVendorDD(altVendorDD);
                        } catch (XmlException e) {
                            throw new DeploymentException("Unable to parse " + altVendorDD, e);
                        }
                        if (vendorDD == null) {
                            throw new DeploymentException("Invalid alt vendor dd: modulePath=" + modulePath + ", url=" + altVendorDD);
                        }
                    }

                    Module module = builder.createModule(modulePath, URI.create(modulePath), new NestedJarFile(earFile, modulePath), modulePath, vendorDD, altSpecDD);
                    if (module instanceof WebModule) {
                        ((WebModule)module).setContextRoot(webContextRoot);
                    }

                    modules.add(module);
                }
            }
            return application;
        } else if (webConfigBuilder != null && webConfigBuilder.canHandlePlan(plan)) {
            modules.add(webConfigBuilder.createModule(configId.toString(), earFile, plan));
            return null;
        } else if (ejbConfigBuilder != null && ejbConfigBuilder.canHandlePlan(plan)) {
            modules.add(ejbConfigBuilder.createModule(configId.toString(), earFile, plan));
            return null;
        } else if (connectorConfigBuilder != null && connectorConfigBuilder.canHandlePlan(plan)) {
            modules.add(connectorConfigBuilder.createModule(configId.toString(), earFile, plan));
            return null;
        }
        throw new DeploymentException("Could not build module list; Unknown plan type");
    }

    private ModuleBuilder getBuilder(Module module) throws DeploymentException {
        if (module instanceof EJBModule) {
            if (ejbConfigBuilder == null) {
                throw new DeploymentException("Can not deploy ejb application; No ejb deployer defined: " + module.getModuleURI());
            }
            return ejbConfigBuilder;
        } else if (module instanceof WebModule) {
            if (webConfigBuilder == null) {
                throw new DeploymentException("Can not deploy web application; No war deployer defined: " + module.getModuleURI());
            }
            return webConfigBuilder;
        } else if (module instanceof ConnectorModule) {
            if (connectorConfigBuilder == null) {
                throw new DeploymentException("Can not deploy resource adapter; No rar deployer defined: " + module.getModuleURI());
            }
            return connectorConfigBuilder;
        }
        throw new IllegalArgumentException("Unknown module type: " + module.getClass().getName());
    }

    private ApplicationDocument getApplicationDocument(InputStream ddInputStream) throws XmlException, DeploymentException {
        XmlObject dd;
        try {
            dd = SchemaConversionUtils.parse(ddInputStream);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
        ApplicationDocument applicationDocument = SchemaConversionUtils.convertToApplicationSchema(dd);
        return applicationDocument;
    }

    private URI getParentId(XmlObject plan) throws DeploymentException {
        if (plan instanceof GerApplicationDocument) {
            GerApplicationDocument applicationDoc = (GerApplicationDocument) plan;
            GerApplicationType application = applicationDoc.getApplication();
            if (application.isSetParentId()) {
                try {
                    return new URI(application.getParentId());
                } catch (URISyntaxException e) {
                    throw new DeploymentException("Invalid parentId " + application.getParentId(), e);
                }
            } else {
                return null;
            }
        }

        if (webConfigBuilder != null) {
            if (webConfigBuilder.canHandlePlan(plan)) {
                return webConfigBuilder.getParentId(plan);
            }
        }

        if (ejbConfigBuilder != null) {
            if (ejbConfigBuilder.canHandlePlan(plan)) {
                return ejbConfigBuilder.getParentId(plan);
            }
        }

        if (connectorConfigBuilder != null) {
            if (connectorConfigBuilder.canHandlePlan(plan)) {
                return connectorConfigBuilder.getParentId(plan);
            }
        }

        return null;
    }

    private URI getConfigId(XmlObject plan) throws DeploymentException {
        if (plan instanceof GerApplicationDocument) {
            GerApplicationDocument applicationDoc = (GerApplicationDocument) plan;
            GerApplicationType application = applicationDoc.getApplication();
            try {
                return new URI(application.getConfigId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid configId " + application.getConfigId(), e);
            }
        }

        if (webConfigBuilder != null) {
            if (webConfigBuilder.canHandlePlan(plan)) {
                return webConfigBuilder.getConfigId(plan);
            }
        }

        if (ejbConfigBuilder != null) {
            if (ejbConfigBuilder.canHandlePlan(plan)) {
                return ejbConfigBuilder.getConfigId(plan);
            }
        }

        if (connectorConfigBuilder != null) {
            if (connectorConfigBuilder.canHandlePlan(plan)) {
                return connectorConfigBuilder.getConfigId(plan);
            }
        }

        throw new DeploymentException("Could not determine config id");
    }

    private ConfigurationModuleType getType(XmlObject plan) throws DeploymentException {
        if (plan instanceof GerApplicationDocument) {
            return ConfigurationModuleType.EAR;
        }

        if (webConfigBuilder != null) {
            if (webConfigBuilder.canHandlePlan(plan)) {
                return ConfigurationModuleType.WAR;
            }
        }

        if (ejbConfigBuilder != null) {
            if (ejbConfigBuilder.canHandlePlan(plan)) {
                return ConfigurationModuleType.EJB;
            }
        }

        if (connectorConfigBuilder != null) {
            if (connectorConfigBuilder.canHandlePlan(plan)) {
                return ConfigurationModuleType.RAR;
            }
        }

        throw new DeploymentException("Could not determine type");
    }

    private URI getDependencyURI(GerDependencyType dep) throws DeploymentException {
        URI uri;
        if (dep.isSetUri()) {
            try {
                uri = new URI(dep.getUri());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid dependency URI " + dep.getUri(), e);
            }
        } else {
            // @todo support more than just jars
            String id = dep.getGroupId() + "/jars/" + dep.getArtifactId() + '-' + dep.getVersion() + ".jar";
            try {
                uri = new URI(id);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to construct URI for groupId=" + dep.getGroupId() + ", artifactId=" + dep.getArtifactId() + ", version=" + dep.getVersion(), e);
            }
        }
        return uri;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(EARConfigBuilder.class);
        infoFactory.addAttribute("j2eeServer", ObjectName.class, true);
        infoFactory.addAttribute("transactionContextManagerObjectName", ObjectName.class, true);
        infoFactory.addAttribute("connectionTrackerObjectName", ObjectName.class, true);
        infoFactory.addAttribute("transactionalTimerObjectName", ObjectName.class, true);
        infoFactory.addAttribute("nonTransactionalTimerObjectName", ObjectName.class, true);

        infoFactory.addReference("Repository", Repository.class);
        infoFactory.addReference("EJBConfigBuilder", ModuleBuilder.class);
        infoFactory.addReference("EJBReferenceBuilder", EJBReferenceBuilder.class);
        infoFactory.addReference("WebConfigBuilder", ModuleBuilder.class);
        infoFactory.addReference("ConnectorConfigBuilder", ModuleBuilder.class);

        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addInterface(ConfigurationBuilder.class);

        infoFactory.setConstructor(new String[]{
            "j2eeServer",
            "transactionContextManagerObjectName",
            "connectionTrackerObjectName",
            "transactionalTimerObjectName",
            "nonTransactionalTimerObjectName",
            "Repository",
            "EJBConfigBuilder",
            "EJBReferenceBuilder",
            "WebConfigBuilder",
            "ConnectorConfigBuilder",
            "kernel"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
