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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.ApplicationInfo;
import org.apache.geronimo.j2ee.deployment.j2eeobjectnames.NameFactory;
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
import org.apache.geronimo.xbeans.j2ee.ApplicationType;
import org.apache.geronimo.xbeans.j2ee.ModuleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EARConfigBuilder implements ConfigurationBuilder {
    private static final String PARENT_ID = "org/apache/geronimo/Server";

    private final Kernel kernel;
    private final Repository repository;
    private final ModuleBuilder ejbConfigBuilder;
    private final ModuleBuilder webConfigBuilder;
    private final ModuleBuilder connectorConfigBuilder;
    private final ModuleBuilder appClientConfigBuilder;
    private final EJBReferenceBuilder ejbReferenceBuilder;
    private final ResourceReferenceBuilder resourceReferenceBuilder;
    private final String j2eeServerName;
    private final String j2eeDomainName;
    private final ObjectName j2eeServer;
    private final ObjectName transactionContextManagerObjectName;
    private final ObjectName connectionTrackerObjectName;
    private final ObjectName transactionalTimerObjectName;
    private final ObjectName nonTransactionalTimerObjectName;


    public EARConfigBuilder(ObjectName j2eeServer, ObjectName transactionContextManagerObjectName, ObjectName connectionTrackerObjectName, ObjectName transactionalTimerObjectName, ObjectName nonTransactionalTimerObjectName, Repository repository, ModuleBuilder ejbConfigBuilder, EJBReferenceBuilder ejbReferenceBuilder, ModuleBuilder webConfigBuilder, ModuleBuilder connectorConfigBuilder, ResourceReferenceBuilder resourceReferenceBuilder, ModuleBuilder appClientConfigBuilder, Kernel kernel) {
        this.kernel = kernel;
        this.repository = repository;
        this.j2eeServer = j2eeServer;
        j2eeServerName = j2eeServer.getKeyProperty("name");
        j2eeDomainName = j2eeServer.getDomain();

        this.ejbConfigBuilder = ejbConfigBuilder;
        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.webConfigBuilder = webConfigBuilder;
        this.connectorConfigBuilder = connectorConfigBuilder;
        this.appClientConfigBuilder = appClientConfigBuilder;
        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.transactionalTimerObjectName = transactionalTimerObjectName;
        this.nonTransactionalTimerObjectName = nonTransactionalTimerObjectName;
    }

    public Object getDeploymentPlan(File planFile, JarFile jarFile) throws DeploymentException {
        if (jarFile == null) {
            return null;
        }

        Object plan = getEarPlan(planFile, jarFile);
        if (plan != null) {
            return plan;
        }

        // get the modules either the application plan or for a stand alone module from the specific deployer
        Module module = null;
        if (webConfigBuilder != null) {
            module = webConfigBuilder.createModule(planFile, jarFile);
        }
        if (module == null && ejbConfigBuilder != null) {
            module = ejbConfigBuilder.createModule(planFile, jarFile);
        }
        if (module == null && connectorConfigBuilder != null) {
            module = connectorConfigBuilder.createModule(planFile, jarFile);
        }
        if (module == null && appClientConfigBuilder != null) {
            module = appClientConfigBuilder.createModule(planFile, jarFile);
        }
        if (module == null) {
            return null;
        }

        return new ApplicationInfo(module.getType(),
                module.getConfigId(),
                module.getParentId(),
                NameFactory.NULL,
                null,
                null,
                Collections.singleton(module),
                Collections.EMPTY_SET,
                null);
    }

    private Object getEarPlan(File planFile, JarFile earFile) throws DeploymentException {
        ApplicationType application;
        try {
            URL applicationXmlUrl = DeploymentUtil.createJarURL(earFile, "META-INF/application.xml");
            XmlObject xmlObject = SchemaConversionUtils.parse(applicationXmlUrl);
            application = SchemaConversionUtils.convertToApplicationSchema(xmlObject).getApplication();
        } catch (Exception e) {
            return null;
        }
        if (application == null) {
            return null;
        }

        GerApplicationType gerApplication = null;
        try {
            // load the geronimo-application.xml from either the supplied plan or from the earFile
            GerApplicationDocument gerApplicationDoc = null;
            try {
                if (planFile != null) {
                    gerApplicationDoc = GerApplicationDocument.Factory.parse(planFile);
                } else {
                    URL path = DeploymentUtil.createJarURL(earFile, "META-INF/geronimo-application.xml");
                    gerApplicationDoc = GerApplicationDocument.Factory.parse(path);
                }
            } catch (IOException e) {
            }

            // if we got one extract the validate it otherwise create a default one
            if (gerApplicationDoc != null) {
                SchemaConversionUtils.validateDD(gerApplicationDoc);
                gerApplication = gerApplicationDoc.getApplication();
            } else {
                gerApplication = createDefaultPlan(application, earFile);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        // get the ids from either the application plan or for a stand alone module from the specific deployer
        URI configId = null;
        try {
            configId = new URI(gerApplication.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + gerApplication.getConfigId(), e);
        }

        URI parentId = null;
        if (gerApplication.isSetParentId()) {
            try {
                parentId = new URI(gerApplication.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + gerApplication.getParentId(), e);
            }
        }

        // get the modules either the application plan or for a stand alone module from the specific deployer
        // todo change module so you can extract the real module path back out.. then we can eliminate
        // the moduleLocations and have addModules return the modules
        Set moduleLocations = new HashSet();
        Set modules = new LinkedHashSet();
        try {
            addModules(earFile, application, gerApplication, moduleLocations, modules);
        } catch (Throwable e) {
            // close al the modules
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                module.close();
            }

            if (e instanceof DeploymentException) {
                throw (DeploymentException)e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else if (e instanceof Error) {
                throw (Error)e;
            }
            throw new DeploymentException(e);
        }

        String applicationName = gerApplication.isSetApplicationName()? gerApplication.getApplicationName(): configId.toString();

        return new ApplicationInfo(ConfigurationModuleType.EAR,
                configId,
                parentId,
                applicationName,
                application,
                gerApplication,
                modules,
                moduleLocations,
                application.toString());
    }


    private GerApplicationType createDefaultPlan(ApplicationType application, JarFile module) {
        // construct the empty geronimo-application.xml
        GerApplicationType gerApplication = GerApplicationType.Factory.newInstance();

        // set the parentId and configId
        gerApplication.setParentId(PARENT_ID);
        String id = application.getId();
        if (id == null) {
            File fileName = new File(module.getName());
            id = fileName.getName();
            if (id.endsWith(".ear")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        gerApplication.setConfigId(id);
        return gerApplication;
    }

    public List buildConfiguration(Object plan, JarFile earFile, File outfile) throws IOException, DeploymentException {
        ApplicationInfo applicationInfo = (ApplicationInfo) plan;
        try {
            List moduleIDs = new LinkedList();

            // Create the output ear context
            EARContext earContext = null;
            ConfigurationModuleType applicationType = applicationInfo.getType();
            try {
                earContext = new EARContext(outfile,
                        applicationInfo.getConfigId(),
                        applicationType,
                        applicationInfo.getParentId(),
                        kernel,
                        j2eeDomainName,
                        j2eeServerName,
                        applicationInfo.getApplicationName(),
                        transactionContextManagerObjectName,
                        connectionTrackerObjectName,
                        transactionalTimerObjectName,
                        nonTransactionalTimerObjectName,
                        new RefContext(ejbReferenceBuilder, resourceReferenceBuilder));
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }

            // Copy over all files that are _NOT_ modules
            Set moduleLocations = applicationInfo.getModuleLocations();
            if (ConfigurationModuleType.EAR == applicationType) {
                for (Enumeration e = earFile.entries(); e.hasMoreElements();) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    if (!moduleLocations.contains(entry.getName())) {
                        earContext.addFile(URI.create(entry.getName()), earFile, entry);
                    }
                }
            }

            // add dependencies declared in the geronimo-application.xml
            GerApplicationType geronimoApplication = (GerApplicationType) applicationInfo.getVendorDD();
            if (geronimoApplication != null) {
                GerDependencyType[] dependencies = geronimoApplication.getDependencyArray();
                for (int i = 0; i < dependencies.length; i++) {
                    earContext.addDependency(getDependencyURI(dependencies[i]));
                }
            }

            // each module installs it's files into the output context.. this is different for each module type
            Set modules = applicationInfo.getModules();
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
            if (geronimoApplication != null) {
                GerGbeanType[] gbeans = geronimoApplication.getGbeanArray();
                for (int i = 0; i < gbeans.length; i++) {
                    GBeanHelper.addGbean(new GerGBeanAdapter(gbeans[i]), cl, earContext);
                }
            }

            // Create the J2EEApplication managed object
            if (ConfigurationModuleType.EAR == applicationType) {
                GBeanMBean gbean = new GBeanMBean(J2EEApplicationImpl.GBEAN_INFO, cl);
                try {
                    gbean.setAttribute("deploymentDescriptor", applicationInfo.getOriginalSpecDD());
                } catch (Exception e) {
                    throw new DeploymentException("Error initializing J2EEApplication managed object");
                }
                gbean.setReferencePatterns("j2eeServer", Collections.singleton(j2eeServer));
                ObjectName applicationName = earContext.getApplicationObjectName();
                earContext.addGBean(applicationName, gbean);
            }

            // each module can now add it's GBeans
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                String moduleID = getBuilder(module).addGBeans(earContext, module, cl);

                // this is a bit weird and should be rethougth but it works
                if (moduleID != null) {
                    moduleIDs.add(moduleID);
                }
            }

            earContext.close();
            return moduleIDs;
        } finally {
            Set modules = applicationInfo.getModules();
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                module.close();
            }
        }
    }

    private void addModules(JarFile earFile, ApplicationType application, GerApplicationType gerApplication, Set moduleLocations, Set modules) throws DeploymentException {
        Map altVendorDDs = new HashMap();
        try {
            // build map from module path to alt vendor dd
            GerModuleType gerModuleTypes[] = gerApplication.getModuleArray();
            for (int i = 0; i < gerModuleTypes.length; i++) {
                GerModuleType gerModule = gerModuleTypes[i];
                String path = null;
                if (gerModule.isSetEjb()) {
                    path = gerModule.getEjb().getStringValue();
                } else if (gerModule.isSetWeb()) {
                    path = gerModule.getWeb().getStringValue();
                } else if (gerModule.isSetConnector()) {
                    path = gerModule.getConnector().getStringValue();
                } else if (gerModule.isSetJava()) {
                    path = gerModule.getJava().getStringValue();
                }

                if (gerModule.isSetAltDd()) {
                    // the the url of the alt dd
                    try {
                        altVendorDDs.put(path, DeploymentUtil.toTempFile(earFile, gerModule.getAltDd().getStringValue()));
                    } catch (IOException e) {
                        throw new DeploymentException("Invalid alt vendor dd url: " + gerModule.getAltDd().getStringValue(), e);
                    }
                } else {
                    //dd is included explicitly
                    XmlCursor cursor = gerModule.newCursor();
                    try {
                        cursor.toFirstChild();
                        cursor.toNextSibling();
                        //should be at the "any" element
                        XmlObject any = cursor.getObject();
                        altVendorDDs.put(path, any);
                    } finally {
                        cursor.dispose();
                    }
                }
            }


            // get a set containing all of the files in the ear that are actually modules
            ModuleType[] moduleTypes = application.getModuleArray();
            for (int i = 0; i < moduleTypes.length; i++) {
                ModuleType moduleXml = moduleTypes[i];

                String modulePath;
                ModuleBuilder builder;

                String moduleTypeName;
                if (moduleXml.isSetEjb()) {
                    modulePath = moduleXml.getEjb().getStringValue();
                    if (ejbConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy ejb application; No ejb deployer defined: " + modulePath);
                    }
                    builder = ejbConfigBuilder;
                    moduleTypeName = "an EJB";
                } else if (moduleXml.isSetWeb()) {
                    modulePath = moduleXml.getWeb().getWebUri().getStringValue();
                    if (webConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy web application; No war deployer defined: " + modulePath);
                    }
                    builder = webConfigBuilder;
                    moduleTypeName = "a war";
                } else if (moduleXml.isSetConnector()) {
                    modulePath = moduleXml.getConnector().getStringValue();
                    if (connectorConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy resource adapter; No rar deployer defined: " + modulePath);
                    }
                    builder = connectorConfigBuilder;
                    moduleTypeName = "a connector";
                } else if (moduleXml.isSetJava()) {
                    modulePath = moduleXml.getJava().getStringValue();
                    if (appClientConfigBuilder == null) {
                        throw new DeploymentException("Can not deploy app client; No app client deployer defined: " + modulePath);
                    }
                    builder = appClientConfigBuilder;
                    moduleTypeName = "an application client";
                } else {
                    throw new DeploymentException("Could not find a module builder for module: " + moduleXml);
                }

                moduleLocations.add(modulePath);

                URL altSpecDD = null;
                if (moduleXml.isSetAltDd()) {
                    try {
                        altSpecDD = DeploymentUtil.createJarURL(earFile, moduleXml.getAltDd().getStringValue());
                    } catch (MalformedURLException e) {
                        throw new DeploymentException("Invalid alt sped dd url: " + moduleXml.getAltDd().getStringValue(), e);
                    }
                }

                NestedJarFile moduleFile = null;
                try {
                    moduleFile = new NestedJarFile(earFile, modulePath);
                } catch (IOException e) {
                    throw new DeploymentException("Invalid moduleFile: " + modulePath, e);
                }

                Module module = builder.createModule(altVendorDDs.get(modulePath),
                        moduleFile,
                        modulePath,
                        altSpecDD,
                        URI.create(gerApplication.getConfigId()));

                if (module == null) {
                    throw new DeploymentException("Module was not " + moduleTypeName + ": " + modulePath);
                }

                if (module instanceof WebModule) {
                    ((WebModule) module).setContextRoot(moduleXml.getWeb().getContextRoot().getStringValue());
                }

                modules.add(module);
            }
        } finally {
            // delete all the temp files created for alt vendor dds
            for (Iterator iterator = altVendorDDs.values().iterator(); iterator.hasNext();) {
                Object altVendorDD = iterator.next();
                if (altVendorDD instanceof File) {
                    ((File)altVendorDD).delete();
                }
            }
        }
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
        } else if (module instanceof AppClientModule) {
            if (appClientConfigBuilder == null) {
                throw new DeploymentException("Can not deploy app client; No app client deployer defined: " + module.getModuleURI());
            }
            return appClientConfigBuilder;
        }
        throw new IllegalArgumentException("Unknown module type: " + module.getClass().getName());
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
        infoFactory.addReference("ResourceReferenceBuilder", ResourceReferenceBuilder.class);
        infoFactory.addReference("AppClientConfigBuilder", ModuleBuilder.class);

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
            "ResourceReferenceBuilder",
            "AppClientConfigBuilder",
            "kernel"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
