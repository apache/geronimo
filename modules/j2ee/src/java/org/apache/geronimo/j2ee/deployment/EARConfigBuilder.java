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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
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
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.management.impl.J2EEApplicationImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationDocument;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerDependencyType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerGbeanType;
import org.apache.geronimo.xbeans.j2ee.ApplicationDocument;
import org.apache.geronimo.xbeans.j2ee.ApplicationType;
import org.apache.geronimo.xbeans.j2ee.ModuleType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 1.14 $ $Date: 2004/07/22 03:22:53 $
 */
public class EARConfigBuilder implements ConfigurationBuilder {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(GerApplicationDocument.class.getClassLoader())
    });



    private static final String PARENT_ID = "org/apache/geronimo/Server";

    private final Kernel kernel;
    private final Repository repository;
    private final ModuleBuilder ejbConfigBuilder;
    private final ModuleBuilder webConfigBuilder;
    private final ModuleBuilder connectorConfigBuilder;
    private final String j2eeServerName;
    private final String j2eeDomainName;
    private final ObjectName j2eeServer;
    private final ObjectName transactionContextManagerObjectName;
    private final ObjectName connectionTrackerObjectName;
    private final ObjectName transactionalTimerObjectName;
    private final ObjectName nonTransactionalTimerObjectName;


    public EARConfigBuilder(
            ObjectName j2eeServer, ObjectName transactionContextManagerObjectName, ObjectName connectionTrackerObjectName, ObjectName transactionalTimerObjectName, ObjectName nonTransactionalTimerObjectName, Repository repository, ModuleBuilder ejbConfigBuilder, ModuleBuilder webConfigBuilder, ModuleBuilder connectorConfigBuilder, Kernel kernel
            ) {
        this.kernel = kernel;
        this.repository = repository;
        this.j2eeServer = j2eeServer;
        j2eeServerName = j2eeServer.getKeyProperty("name");
        j2eeDomainName = j2eeServer.getDomain();

        this.ejbConfigBuilder = ejbConfigBuilder;
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
        return (SchemaTypeLoader[])typeLoaders.toArray(new SchemaTypeLoader[typeLoaders.size()]);
    }

    public XmlObject getDeploymentPlan(URL module) throws XmlException {

        try {
            URL moduleBase;
            if (module.toString().endsWith("/")) {
                moduleBase = module;
            } else {
                moduleBase = new URL("jar:" + module.toString() + "!/");
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
            XmlObject plan = webConfigBuilder.getDeploymentPlan(module);
            if (plan != null) {
                return plan;
            }
        }

        if (ejbConfigBuilder != null) {
            XmlObject plan = ejbConfigBuilder.getDeploymentPlan(module);
            if (plan != null) {
                return plan;
            }
        }

        if (connectorConfigBuilder != null) {
            XmlObject plan = connectorConfigBuilder.getDeploymentPlan(module);
            if (plan != null) {
                return plan;
            }
        }

        return null;
    }

    private GerApplicationDocument createDefaultPlan(URL moduleBase) throws XmlException {
        // load the web.xml
        URL applicationXmlUrl = null;
        try {
            applicationXmlUrl = new URL(moduleBase, "META-INF/application.xml");
        } catch (MalformedURLException e) {
            return null;
        }
        ApplicationDocument applicationDoc = (ApplicationDocument) XmlBeansUtil.getXmlObject(applicationXmlUrl, ApplicationDocument.type);
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
            id = moduleBase.getFile();
            if (id.endsWith("!/")) {
                id = id.substring(0, id.length() - 2);
            }
            if (id.endsWith(".ear")) {
                id = id.substring(0, id.length() - 4);
            }
            id = id.substring(id.lastIndexOf('/') + 1);
        }

        gerApplication.setConfigId(id);
        return gerApplicationDocument;
    }


    public void buildConfiguration(File outfile, Manifest manifest, InputStream is, XmlObject plan) throws IOException, DeploymentException {
        File tmp = FileUtil.toTempFile(is);
        buildConfiguration(outfile, manifest, new JarFile(tmp), plan);
    }

    public void buildConfiguration(File outfile, Manifest manifest, File module, XmlObject plan) throws IOException, DeploymentException {
        if (module.isDirectory()) {
            throw new DeploymentException("Unpacked ear files are not currently supported");
        }
        buildConfiguration(outfile, manifest, new JarFile(module), plan);
    }

    public void buildConfiguration(File outfile, Manifest manifest, JarFile earFile, XmlObject plan) throws IOException, DeploymentException {
        FileOutputStream fos = new FileOutputStream(outfile);
        try {
            // get the ids from either the application plan or for a stand alone module from the specific deployer
            URI configId = getConfigId(plan);
            ConfigurationModuleType type = getType(plan);
            URI parentId = getParentId(plan);

            // get the modules either the application plan or for a stand alone module from the specific deployer
            Set moduleLocations = new HashSet();
            Set modules = new LinkedHashSet();
            ApplicationType application = addModules(configId, plan, earFile, moduleLocations, modules);
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
                        nonTransactionalTimerObjectName);
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
                } catch (IOException e) {
                    // ignore
                }
            }
            fos.close();
        }
    }

    private ApplicationType addModules(URI configId, XmlObject plan, JarFile earFile, Set moduleLocations, Set modules) throws DeploymentException, IOException {
        ApplicationType application;
        if (plan instanceof GerApplicationDocument) {
            try {
                JarEntry appXMLEntry = earFile.getJarEntry("META-INF/application.xml");
                if (appXMLEntry == null) {
                    throw new DeploymentException("Did not find META-INF/application.xml in earFile");
                }
                ApplicationDocument doc = (ApplicationDocument) XmlBeansUtil.parse(earFile.getInputStream(appXMLEntry), ApplicationDocument.type);
                application = doc.getApplication();
            } catch (XmlException e) {
                throw new DeploymentException("Unable to parse application.xml");
            }

            // get a set containing all of the files in the ear that are actually modules
            ModuleType[] moduleTypes = application.getModuleArray();
            Set ejbModules = new HashSet();
            Set connectorModules = new HashSet();
            Set webModules = new HashSet();

            for (int i = 0; i < moduleTypes.length; i++) {
                ModuleType module = moduleTypes[i];
                if (module.isSetEjb()) {
                    URI uri = URI.create(module.getEjb().getStringValue());
                    EJBModule ejbModule = new EJBModule(uri.toString(), uri);
                    if (ejbConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy ejb application; No ejb deployer defined: " + ejbModule.getURI());
                    }
                    moduleLocations.add(uri.toString());
                    ejbModules.add(ejbModule);
                } else if (module.isSetWeb()) {
                    org.apache.geronimo.xbeans.j2ee.WebType web = module.getWeb();
                    URI uri = URI.create(web.getWebUri().getStringValue());
                    String contextRoot = web.getContextRoot().getStringValue();
                    WebModule webModule = new WebModule(uri.toString(), uri, contextRoot);
                    if (webConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy web application; No war deployer defined: " + webModule.getURI());
                    }
                    moduleLocations.add(uri.toString());
                    webModules.add(webModule);
                } else if (module.isSetConnector()) {
                    URI uri = URI.create(module.getConnector().getStringValue());
                    ConnectorModule connectorModule = new ConnectorModule(uri.toString(), uri);
                    if (connectorConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy resource adapter; No rar deployer defined: " + connectorModule.getURI());
                    }
                    moduleLocations.add(uri.toString());
                    connectorModules.add(connectorModule);
                }
            }
            modules.addAll(connectorModules);
            modules.addAll(ejbModules);
            modules.addAll(webModules);
        } else if (webConfigBuilder != null && webConfigBuilder.canHandlePlan(plan)) {
            modules.add(webConfigBuilder.createModule(configId.toString(), plan));
            application = null;
        } else if (ejbConfigBuilder != null && ejbConfigBuilder.canHandlePlan(plan)) {
            modules.add(ejbConfigBuilder.createModule(configId.toString(), plan));
            application = null;
        } else if (connectorConfigBuilder != null && connectorConfigBuilder.canHandlePlan(plan)) {
            modules.add(connectorConfigBuilder.createModule(configId.toString(), plan));
            application = null;
        } else {
            throw new DeploymentException("Could not build module list; Unknown plan type");
        }
        return application;
    }

    private ModuleBuilder getBuilder(Module module) {
        if (module instanceof EJBModule) {
            return ejbConfigBuilder;
        } else if (module instanceof WebModule) {
            return webConfigBuilder;
        } else if (module instanceof ConnectorModule) {
            return connectorConfigBuilder;
        }
        throw new IllegalArgumentException("Unknown module type: " + module.getClass().getName());
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

        throw new DeploymentException("Could not determine config id");
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
