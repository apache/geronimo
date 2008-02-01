/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.zip.ZipEntry;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.ClassPathList;
import org.apache.geronimo.deployment.ModuleList;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.j2ee.ApplicationInfo;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEApplicationImpl;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationDocument;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerExtModuleType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerModuleType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.geronimo.xbeans.javaee.ApplicationDocument;
import org.apache.geronimo.xbeans.javaee.ApplicationType;
import org.apache.geronimo.xbeans.javaee.ModuleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EARConfigBuilder implements ConfigurationBuilder, CorbaGBeanNameSource {

    private static final Log log = LogFactory.getLog(EARConfigBuilder.class);
    private static final String LINE_SEP = System.getProperty("line.separator");

    private final static QName APPLICATION_QNAME = GerApplicationDocument.type.getDocumentElementName();

    private final ConfigurationManager configurationManager;
    private final Collection<Repository> repositories;
    private final SingleElementCollection ejbConfigBuilder;
    private final SingleElementCollection webConfigBuilder;
    private final SingleElementCollection connectorConfigBuilder;
    private final SingleElementCollection appClientConfigBuilder;
    private final SingleElementCollection resourceReferenceBuilder;
    private final NamespaceDrivenBuilderCollection securityBuilders;
    private final NamespaceDrivenBuilderCollection serviceBuilders;
    private final Collection<ModuleBuilderExtension> persistenceUnitBuilders;

    private final Environment defaultEnvironment;
    private final AbstractNameQuery serverName;
    private final AbstractNameQuery transactionManagerObjectName;
    private final AbstractNameQuery connectionTrackerObjectName;
    private final AbstractNameQuery transactionalTimerObjectName;
    private final AbstractNameQuery nonTransactionalTimerObjectName;
    private final AbstractNameQuery corbaGBeanObjectName;
    private final Naming naming;
    private final Collection<ArtifactResolver> artifactResolvers;

    public EARConfigBuilder(Environment defaultEnvironment,
            AbstractNameQuery transactionManagerAbstractName,
            AbstractNameQuery connectionTrackerAbstractName,
            AbstractNameQuery transactionalTimerAbstractName,
            AbstractNameQuery nonTransactionalTimerAbstractName,
            AbstractNameQuery corbaGBeanAbstractName,
            AbstractNameQuery serverName,
            Collection<Repository> repositories,
            Collection ejbConfigBuilder,
            Collection webConfigBuilder,
            Collection connectorConfigBuilder,
            Collection resourceReferenceBuilder,
            Collection appClientConfigBuilder,
            Collection securityBuilders,
            Collection serviceBuilders,
            Collection<ModuleBuilderExtension> persistenceUnitBuilders,
            Collection<ArtifactResolver> artifactResolvers,
            Kernel kernel) {
        this(defaultEnvironment,
                transactionManagerAbstractName,
                connectionTrackerAbstractName,
                transactionalTimerAbstractName,
                nonTransactionalTimerAbstractName,
                corbaGBeanAbstractName,
                serverName,
                ConfigurationUtil.getConfigurationManager(kernel),
                repositories,
                new SingleElementCollection(ejbConfigBuilder),
                new SingleElementCollection(webConfigBuilder),
                new SingleElementCollection(connectorConfigBuilder),
                new SingleElementCollection(resourceReferenceBuilder),
                new SingleElementCollection(appClientConfigBuilder),
                securityBuilders,
                serviceBuilders,
                persistenceUnitBuilders,
                kernel.getNaming(), artifactResolvers);
    }

    public EARConfigBuilder(Environment defaultEnvironment,
            AbstractNameQuery transactionManagerAbstractName,
            AbstractNameQuery connectionTrackerAbstractName,
            AbstractNameQuery transactionalTimerAbstractName,
            AbstractNameQuery nonTransactionalTimerAbstractName,
            AbstractNameQuery corbaGBeanAbstractName,
            AbstractNameQuery serverName,
            Collection<Repository> repositories,
            ModuleBuilder ejbConfigBuilder,
            ModuleBuilder webConfigBuilder,
            ModuleBuilder connectorConfigBuilder,
            ActivationSpecInfoLocator activationSpecInfoLocator,
            ModuleBuilder appClientConfigBuilder,
            NamespaceDrivenBuilder securityBuilder,
            NamespaceDrivenBuilder serviceBuilder,
            ModuleBuilderExtension persistenceUnitBuilder,
            Naming naming,
            Collection<ArtifactResolver> artifactResolvers) {
        this(defaultEnvironment,
                transactionManagerAbstractName,
                connectionTrackerAbstractName,
                transactionalTimerAbstractName,
                nonTransactionalTimerAbstractName,
                corbaGBeanAbstractName,
                serverName,
                null,
                repositories,
                new SingleElementCollection(ejbConfigBuilder),
                new SingleElementCollection(webConfigBuilder),
                new SingleElementCollection(connectorConfigBuilder),
                new SingleElementCollection(activationSpecInfoLocator),
                new SingleElementCollection(appClientConfigBuilder),
                securityBuilder == null ? Collections.EMPTY_SET : Collections.singleton(securityBuilder),
                serviceBuilder == null ? Collections.EMPTY_SET : Collections.singleton(serviceBuilder),
                persistenceUnitBuilder == null ? Collections.EMPTY_SET : Collections.singleton(persistenceUnitBuilder),
                naming,
                artifactResolvers);
    }

    private EARConfigBuilder(Environment defaultEnvironment,
             AbstractNameQuery transactionManagerAbstractName,
             AbstractNameQuery connectionTrackerAbstractName,
             AbstractNameQuery transactionalTimerAbstractName,
             AbstractNameQuery nonTransactionalTimerAbstractName,
             AbstractNameQuery corbaGBeanAbstractName,
             AbstractNameQuery serverName,
             ConfigurationManager configurationManager,
             Collection<Repository> repositories,
             SingleElementCollection ejbConfigBuilder,
             SingleElementCollection webConfigBuilder,
             SingleElementCollection connectorConfigBuilder,
             SingleElementCollection resourceReferenceBuilder,
             SingleElementCollection appClientConfigBuilder,
             Collection securityBuilders,
             Collection serviceBuilders,
             Collection<ModuleBuilderExtension> persistenceUnitBuilders,
             Naming naming,
             Collection<ArtifactResolver> artifactResolvers) {
        this.configurationManager = configurationManager;
        this.repositories = repositories;
        this.defaultEnvironment = defaultEnvironment;

        this.ejbConfigBuilder = ejbConfigBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.webConfigBuilder = webConfigBuilder;
        this.connectorConfigBuilder = connectorConfigBuilder;
        this.appClientConfigBuilder = appClientConfigBuilder;
        this.securityBuilders = new NamespaceDrivenBuilderCollection(securityBuilders, GerSecurityDocument.type.getDocumentElementName());
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders, GBeanBuilder.SERVICE_QNAME);
        this.persistenceUnitBuilders = persistenceUnitBuilders;
        
        this.transactionManagerObjectName = transactionManagerAbstractName;
        this.connectionTrackerObjectName = connectionTrackerAbstractName;
        this.transactionalTimerObjectName = transactionalTimerAbstractName;
        this.nonTransactionalTimerObjectName = nonTransactionalTimerAbstractName;
        this.corbaGBeanObjectName = corbaGBeanAbstractName;
        this.serverName = serverName;
        this.naming = naming;
        this.artifactResolvers = artifactResolvers;
    }


    public AbstractNameQuery getCorbaGBeanName() {
        return corbaGBeanObjectName;
    }

    private ModuleBuilder getEjbConfigBuilder() {
        return (ModuleBuilder) ejbConfigBuilder.getElement();
    }

    private ModuleBuilder getWebConfigBuilder() {
        return (ModuleBuilder) webConfigBuilder.getElement();
    }

    private ModuleBuilder getConnectorConfigBuilder() {
        return (ModuleBuilder) connectorConfigBuilder.getElement();
    }

    private ModuleBuilder getAppClientConfigBuilder() {
        return (ModuleBuilder) appClientConfigBuilder.getElement();
    }

    public Object getDeploymentPlan(File planFile, JarFile jarFile, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (planFile == null && jarFile == null) {
            return null;
        }
        ApplicationInfo plan = getEarPlan(planFile, jarFile, idBuilder);
        if (plan != null) {
            return plan;
        }
        //Only "synthetic" ears with only external modules can have no jar file.
        if (jarFile == null) {
            return null;
        }

        // get the modules either the application plan or for a stand alone module from the specific deployer
        Module module = null;
        if (getWebConfigBuilder() != null) {
            module = getWebConfigBuilder().createModule(planFile, jarFile, naming, idBuilder);
        }
        if (module == null && getEjbConfigBuilder() != null) {
            module = getEjbConfigBuilder().createModule(planFile, jarFile, naming, idBuilder);
        }
        if (module == null && getConnectorConfigBuilder() != null) {
            module = getConnectorConfigBuilder().createModule(planFile, jarFile, naming, idBuilder);
        }
        if (module == null && getAppClientConfigBuilder() != null) {
            module = getAppClientConfigBuilder().createModule(planFile, jarFile, naming, idBuilder);
        }
        if (module == null) {
            return null;
        }

        if (module instanceof ApplicationInfo) {
            return module;
        }
        
        return new ApplicationInfo(module.getType(),
                module.getEnvironment(),
                module.getModuleName(),
                jarFile,
                null,
                null,
                new LinkedHashSet<Module>(Collections.singleton(module)),
                new ModuleList(),
                null);
    }

    private ApplicationInfo getEarPlan(File planFile, JarFile earFile, ModuleIDBuilder idBuilder) throws DeploymentException {
        String specDD;
        ApplicationType application = null;
        if (earFile != null) {
            try {
                URL applicationXmlUrl = DeploymentUtil.createJarURL(earFile, "META-INF/application.xml");
                specDD = DeploymentUtil.readAll(applicationXmlUrl);
                //we found something called application.xml in the right place, if we can't parse it it's an error
                XmlObject xmlObject = XmlBeansUtil.parse(specDD);
                application = convertToApplicationSchema(xmlObject).getApplication();
            } catch (XmlException e) {
                throw new DeploymentException("Could not parse application.xml", e);
            } catch (Exception e) {
                //ee5 spec allows optional application.xml, continue with application == null
                if (!earFile.getName().endsWith(".ear")) {
                    return null;
                }
                //TODO return application.xml that we can make metadata complete?
            }
        }

        GerApplicationType gerApplication = null;
        try {
            // load the geronimo-application.xml from either the supplied plan or from the earFile
            XmlObject rawPlan;
            try {
                if (planFile != null) {
                    rawPlan = XmlBeansUtil.parse(planFile.toURL(), getClass().getClassLoader());
                    gerApplication = (GerApplicationType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, APPLICATION_QNAME, GerApplicationType.type);
                    if (gerApplication == null) {
                        return null;
                    }
                } else {
                    URL path = DeploymentUtil.createJarURL(earFile, "META-INF/geronimo-application.xml");
                    rawPlan = XmlBeansUtil.parse(path, getClass().getClassLoader());
                    gerApplication = (GerApplicationType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, APPLICATION_QNAME, GerApplicationType.type);
                }
            } catch (IOException e) {
                //TODO isn't this an error?
            }

            // if we got one extract the validate it otherwise create a default one
            if (gerApplication == null) {
                gerApplication = createDefaultPlan(application, earFile);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        EnvironmentType environmentType = gerApplication.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        idBuilder.resolve(environment, earFile == null ? planFile.getName() : new File(earFile.getName()).getName(), "ear");
        // Make this EAR's settings the default for child modules
        idBuilder.setDefaultGroup(environment.getConfigId().getGroupId());
        idBuilder.setDefaultVersion(environment.getConfigId().getVersion());

        Artifact artifact = environment.getConfigId();
        AbstractName earName = naming.createRootName(artifact, artifact.toString(), NameFactory.J2EE_APPLICATION);

        // get the modules either the application plan or for a stand alone module from the specific deployer
        // todo change module so you can extract the real module path back out.. then we can eliminate
        // the moduleLocations and have addModules return the modules
        ModuleList moduleLocations = new ModuleList();
        LinkedHashSet<Module> modules = new LinkedHashSet<Module>();
        try {
            addModules(earFile, application, gerApplication, moduleLocations, modules, environment, earName, idBuilder);
            if (application == null && modules.isEmpty()) {
                //if no application.xml and no modules detected, return null for stand-alone module processing
                return null;
            }
        } catch (Throwable e) {
            // close all the modules
            for (Module module : modules) {
                module.close();
            }

            if (e instanceof DeploymentException) {
                throw (DeploymentException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            }
            throw new DeploymentException(e);
        }

        return new ApplicationInfo(ConfigurationModuleType.EAR,
                environment,
                earName,
                earFile,
                application,
                gerApplication,
                modules,
                moduleLocations,
                application == null ? null : application.toString());
    }


    private GerApplicationType createDefaultPlan(ApplicationType application, JarFile module) {
        // construct the empty geronimo-application.xml
        GerApplicationType gerApplication = GerApplicationType.Factory.newInstance();
        EnvironmentType environmentType = gerApplication.addNewEnvironment();
        ArtifactType artifactType = environmentType.addNewModuleId();

        artifactType.setGroupId(Artifact.DEFAULT_GROUP_ID);

        // set the configId
        String id = application != null ? application.getId() : null;
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

        artifactType.setArtifactId(id);
        artifactType.setVersion("" + System.currentTimeMillis());
        artifactType.setType("car");
        return gerApplication;
    }

    static ApplicationDocument convertToApplicationSchema(XmlObject xmlObject) throws XmlException {
        if (ApplicationDocument.type.equals(xmlObject.schemaType())) {
            XmlBeansUtil.validateDD(xmlObject);
            return (ApplicationDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor moveable = xmlObject.newCursor();
        String schemaLocationURL = "http://java.sun.com/xml/ns/javaee/application_5.xsd";
        String version = "5";
        try {
            cursor.toStartDoc();
            cursor.toFirstChild();
            if ("http://java.sun.com/xml/ns/j2ee".equals(cursor.getName().getNamespaceURI())) {
                SchemaConversionUtils.convertSchemaVersion(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version);
                XmlObject result = xmlObject.changeType(ApplicationDocument.type);
                XmlBeansUtil.validateDD(result);
                return (ApplicationDocument) result;
            }

            // otherwise assume DTD
            SchemaConversionUtils.convertToSchema(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version);
            cursor.toStartDoc();
            cursor.toChild(SchemaConversionUtils.JAVAEE_NAMESPACE, "application");
            cursor.toFirstChild();
            SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
        } finally {
            cursor.dispose();
            moveable.dispose();
        }
        XmlObject result = xmlObject.changeType(ApplicationDocument.type);
        if (result != null) {
            XmlBeansUtil.validateDD(result);
            return (ApplicationDocument) result;
        }
        XmlBeansUtil.validateDD(xmlObject);
        return (ApplicationDocument) xmlObject;
    }

    public Artifact getConfigurationID(Object plan, JarFile module, ModuleIDBuilder idBuilder) throws IOException, DeploymentException {
        ApplicationInfo applicationInfo = (ApplicationInfo) plan;
        Artifact test = applicationInfo.getEnvironment().getConfigId();
        if (!test.isResolved()) {
            throw new IllegalStateException("Module ID should be fully resolved by now (not " + test + ")");
        }
        return test;
    }

    public DeploymentContext buildConfiguration(boolean inPlaceDeployment, Artifact configId, Object plan, JarFile earFile, Collection configurationStores, ArtifactResolver artifactResolver, ConfigurationStore targetConfigurationStore) throws IOException, DeploymentException {
        assert plan != null;
        ApplicationInfo applicationInfo = (ApplicationInfo) plan;

        EARContext earContext = null;
        ConfigurationModuleType applicationType = applicationInfo.getType();
        applicationInfo.getEnvironment().setConfigId(configId);
        File configurationDir = null;
        try {
            try {
                configurationDir = targetConfigurationStore.createNewConfigurationDir(configId);
            } catch (ConfigurationAlreadyExistsException e) {
                throw new DeploymentException(e);
            }

            ConfigurationManager configurationManager = this.configurationManager;
            if (configurationManager == null) {
                configurationManager = new SimpleConfigurationManager(configurationStores, artifactResolver, repositories);
            }

            // Create the output ear context
            earContext = new EARContext(configurationDir,
                    inPlaceDeployment ? DeploymentUtil.toFile(earFile) : null,
                    applicationInfo.getEnvironment(),
                    applicationType,
                    naming,
                    configurationManager,
                    repositories,
                    serverName,
                    applicationInfo.getModuleName(),
                    transactionManagerObjectName,
                    connectionTrackerObjectName,
                    transactionalTimerObjectName,
                    nonTransactionalTimerObjectName,
                    corbaGBeanObjectName
            );
            applicationInfo.setEarContext(earContext);
            applicationInfo.setRootEarContext(earContext);
            earContext.getGeneralData().put(ModuleList.class, applicationInfo.getModuleLocations());

            // Copy over all files that are _NOT_ modules (e.g. META-INF and APP-INF files)
            Set moduleLocations = applicationInfo.getModuleLocations();
            ClassPathList libClasspath = new ClassPathList();
            if (ConfigurationModuleType.EAR == applicationType && earFile != null) {
                //get the value of the library-directory element in spec DD
                ApplicationType specDD = (ApplicationType) applicationInfo.getSpecDD();
                String libDir = getLibraryDirectory(specDD);
                for (Enumeration<JarEntry> e = earFile.entries(); e.hasMoreElements();) {
                    ZipEntry entry = e.nextElement();
                    String entryName = entry.getName();
                    boolean addEntry = true;
                    for (Object moduleLocation : moduleLocations) {
                        String location = (String) moduleLocation;
                        if (entryName.startsWith(location)) {
                            addEntry = false;
                            break;
                        }
                    }
                    if (libDir != null && entry.getName().startsWith(libDir) && entry.getName().endsWith(".jar")) {
                        NestedJarFile library = new NestedJarFile(earFile, entry.getName());
                        earContext.addIncludeAsPackedJar(URI.create(entry.getName()), library);
                        libClasspath.add(entry.getName());
                    } else if (addEntry) {
                        earContext.addFile(URI.create(entry.getName()), earFile, entry);
                    }
                }
                if (!libClasspath.isEmpty()) {
                    earContext.getGeneralData().put(ClassPathList.class, libClasspath);
                }
            }

            GerApplicationType geronimoApplication = (GerApplicationType) applicationInfo.getVendorDD();

            // each module installs it's files into the output context.. this is different for each module type
            LinkedHashSet modules = applicationInfo.getModules();
            for (Object module2 : modules) {
                Module module = (Module) module2;
                getBuilder(module).installModule(earFile, earContext, module, configurationStores, targetConfigurationStore, repositories);
            }

            earContext.flush();

            // give each module a chance to populate the earContext now that a classloader is available
            ClassLoader cl = earContext.getClassLoader();
            for (Object module3 : modules) {
                Module module = (Module) module3;
                getBuilder(module).initContext(earContext, module, cl);
            }

            // add gbeans declared in the geronimo-application.xml
            if (geronimoApplication != null) {
                securityBuilders.build(geronimoApplication, earContext, earContext);
                serviceBuilders.build(geronimoApplication, earContext, earContext);
            }
            
            if (ConfigurationModuleType.EAR == applicationType) {
                // process persistence unit in EAR library directory
                earContext.getGeneralData().put(ClassPathList.class, libClasspath);
                for (ModuleBuilderExtension mbe: persistenceUnitBuilders) {
                    mbe.initContext(earContext, applicationInfo, earContext.getClassLoader());
                }
                
                // Create the J2EEApplication managed object
                GBeanData gbeanData = new GBeanData(earContext.getModuleName(), J2EEApplicationImpl.GBEAN_INFO);
                try {
                    String originalSpecDD = applicationInfo.getOriginalSpecDD();
                    if (originalSpecDD == null) {
                        originalSpecDD = "Synthetic EAR";
                    }
                    gbeanData.setAttribute("deploymentDescriptor", originalSpecDD);
                } catch (Exception e) {
                    throw new DeploymentException("Error initializing J2EEApplication managed object", e);
                }
                gbeanData.setReferencePatterns("Server", new ReferencePatterns(new AbstractNameQuery(J2EEServer.class.getName())));

                Map<String, String> thisApp = Collections.singletonMap(NameFactory.J2EE_APPLICATION, earContext.getModuleName().getNameProperty(NameFactory.J2EE_NAME));
                LinkedHashSet<AbstractNameQuery> resourcePatterns = new LinkedHashSet<AbstractNameQuery>();
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JAVA_MAIL_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JCA_CONNECTION_FACTORY), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JDBC_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JDBC_DRIVER), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JMS_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JNDI_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JTA_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.RMI_IIOP_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.URL_RESOURCE), J2EEResource.class.getName()));
                gbeanData.setReferencePatterns("Resources", resourcePatterns);

                gbeanData.setReferencePatterns("AppClientModules", new ReferencePatterns(new AbstractNameQuery(null, thisApp, org.apache.geronimo.management.AppClientModule.class.getName())));
                gbeanData.setReferencePatterns("EJBModules", new ReferencePatterns(new AbstractNameQuery(null, thisApp, org.apache.geronimo.management.EJBModule.class.getName())));
                gbeanData.setReferencePatterns("ResourceAdapterModules", new ReferencePatterns(new AbstractNameQuery(null, thisApp, org.apache.geronimo.management.geronimo.ResourceAdapterModule.class.getName())));
                gbeanData.setReferencePatterns("WebModules", new ReferencePatterns(new AbstractNameQuery(null, thisApp, org.apache.geronimo.management.geronimo.WebModule.class.getName())));
                earContext.addGBean(gbeanData);
            }

            // each module can now add it's GBeans
            for (Object module1 : modules) {
                Module module = (Module) module1;
                getBuilder(module).addGBeans(earContext, module, cl, repositories);
            }

            // it's the caller's responsibility to close the context...
            return earContext;
        } catch (GBeanAlreadyExistsException e) {
            cleanupContext(earContext, configurationDir);
            throw new DeploymentException(e);
        } catch (IOException e) {
            cleanupContext(earContext, configurationDir);
            throw e;
        } catch (DeploymentException e) {
            cleanupContext(earContext, configurationDir);
            throw e;
        } catch (RuntimeException e) {
            cleanupContext(earContext, configurationDir);
            throw e;
        } catch (Error e) {
            cleanupContext(earContext, configurationDir);
            throw e;
        } finally {
            for (Object module1 : applicationInfo.getModules()) {
                Module module = (Module) module1;
                module.close();
            }
        }
    }

    private String getLibraryDirectory(ApplicationType specDD) {
        if (specDD == null || !specDD.isSetLibraryDirectory()) {
            //value 'lib' is used if element not set or ear does not contain a dd
            return "lib";
        }

        //only set if not empty value, empty value implies no library directory
        String value = specDD.getLibraryDirectory().getStringValue();
        return value.trim().length() > 0 ? value : null;
    }

    private void cleanupContext(EARContext earContext, File configurationDir) {
        List<ConfigurationData> configurations = new ArrayList<ConfigurationData>();
        if (earContext != null) {
            configurations.addAll(earContext.getAdditionalDeployment());
            try {
                earContext.close();
            } catch (IOException ioe) {
                // ignore any cleanup problems
            } catch (DeploymentException de) {
                // ignore any cleanup problems
            }
        }
        // configurationDir is created before we create an EARContext
        if (configurationDir != null) {
            cleanupConfigurationDir(configurationDir);
        }
        // cleanup any other configurations generated (e.g. AppClient config dirs)
        for (ConfigurationData configurationData : configurations) {
            cleanupConfigurationDir(configurationData.getConfigurationDir());
        }
    }

    private boolean cleanupConfigurationDir(File configurationDir) {
        LinkedList<String> cannotBeDeletedList = new LinkedList<String>();

        if (!DeploymentUtil.recursiveDelete(configurationDir, cannotBeDeletedList)) {
            // Output a message to help user track down file problem
            log.warn("Unable to delete " + cannotBeDeletedList.size() +
                    " files while recursively deleting directory "
                    + configurationDir.getAbsolutePath() + LINE_SEP +
                    "The first file that could not be deleted was:" + LINE_SEP + "  " +
                    (!cannotBeDeletedList.isEmpty() ? cannotBeDeletedList.getFirst() : ""));
            return false;
        }
        return true;
    }

    private static Map<String, String> filter(Map<String, String> original, String key, String value) {
        LinkedHashMap<String, String> filter = new LinkedHashMap<String, String>(original);
        filter.put(key, value);
        return filter;
    }

    private void addModules(JarFile earFile, ApplicationType application, GerApplicationType gerApplication, ModuleList moduleLocations, LinkedHashSet<Module> modules, Environment environment, AbstractName earName, ModuleIDBuilder idBuilder) throws DeploymentException {
        Map<String, Object> altVendorDDs = new HashMap<String, Object>();
        try {
            mapVendorPlans(gerApplication, altVendorDDs, earFile);
            if (earFile != null) {
                if (application != null) {
                    ModuleType[] moduleTypes = application.getModuleArray();

                    //get a set containing all of the files in the ear that are actually modules
                    for (ModuleType moduleXml : moduleTypes) {
                        String modulePath;
                        ModuleBuilder builder;

                        Object moduleContextInfo = null;
                        String moduleTypeName;
                        if (moduleXml.isSetEjb()) {
                            modulePath = moduleXml.getEjb().getStringValue();
                            builder = getEjbConfigBuilder();
                            if (builder == null) {
                                throw new DeploymentException("Cannot deploy ejb application; No ejb deployer defined: " + modulePath);
                            }
                            moduleTypeName = "an EJB";
                        } else if (moduleXml.isSetWeb()) {
                            modulePath = moduleXml.getWeb().getWebUri().getStringValue();
                            if (getWebConfigBuilder() == null) {
                                throw new DeploymentException("Cannot deploy web application; No war deployer defined: " + modulePath);
                            }
                            builder = getWebConfigBuilder();
                            moduleTypeName = "a war";
                            moduleContextInfo = moduleXml.getWeb().getContextRoot().getStringValue().trim();
                        } else if (moduleXml.isSetConnector()) {
                            modulePath = moduleXml.getConnector().getStringValue();
                            if (getConnectorConfigBuilder() == null) {
                                throw new DeploymentException("Cannot deploy resource adapter; No rar deployer defined: " + modulePath);
                            }
                            builder = getConnectorConfigBuilder();
                            moduleTypeName = "a connector";
                        } else if (moduleXml.isSetJava()) {
                            modulePath = moduleXml.getJava().getStringValue();
                            if (getAppClientConfigBuilder() == null) {
                                throw new DeploymentException("Cannot deploy app client; No app client deployer defined: " + modulePath);
                            }
                            builder = getAppClientConfigBuilder();
                            moduleTypeName = "an application client";
                        } else {
                            throw new DeploymentException("Could not find a module builder for module: " + moduleXml);
                        }

                        moduleLocations.add(modulePath);

                        NestedJarFile moduleFile;
                        try {
                            moduleFile = new NestedJarFile(earFile, modulePath);
                        } catch (IOException e) {
                            throw new DeploymentException("Invalid moduleFile: " + modulePath, e);
                        }

                        Module module = builder.createModule(altVendorDDs.get(modulePath),
                                moduleFile,
                                modulePath,
                                getAltSpecDDURL(earFile, moduleXml),
                                environment,
                                moduleContextInfo,
                                earName,
                                naming, idBuilder);

                        if (module == null) {
                            throw new DeploymentException("Module was not " + moduleTypeName + ": " + modulePath);
                        }

                        modules.add(module);
                    }
                } else {
                    //no application.xml available, must inspect ear to locate and process modules
                    Enumeration<JarEntry> entries = earFile.entries();
                    while (entries.hasMoreElements()) {
                        ModuleBuilder builder;
                        Object moduleContextInfo = null;
                        String moduleTypeName;
                        ZipEntry entry = entries.nextElement();
                        if (entry.getName().endsWith(".war")) {
                            if (getWebConfigBuilder() == null) {
                                throw new DeploymentException("Cannot deploy web application; No war deployer defined: " + entry.getName());
                            }
                            builder = getWebConfigBuilder();
                            moduleTypeName = "a war";
                            moduleContextInfo = entry.getName().split(".war")[0];
                        } else if (entry.getName().endsWith(".rar")) {
                            if (getConnectorConfigBuilder() == null) {
                                throw new DeploymentException("Cannot deploy resource adapter; No rar deployer defined: " + entry.getName());
                            }
                            builder = getConnectorConfigBuilder();
                            moduleTypeName = "a connector";
                        } else if (entry.getName().endsWith(".jar") && !isLibraryEntry(application, entry)) {
                            try {
                                NestedJarFile moduleFile = new NestedJarFile(earFile, entry.getName());
                                Manifest mf = moduleFile.getManifest();

                                if (mf.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS) != null) {
                                    if (getAppClientConfigBuilder() == null) {
                                        throw new DeploymentException("Cannot deploy app client; No app client deployer defined: " + entry.getName());
                                    }
                                    builder = getAppClientConfigBuilder();
                                    moduleTypeName = "an application client";
                                } else {
                                    //ask the ejb builder if its an ejb module
                                    builder = getEjbConfigBuilder();
                                    if (builder == null) {
//                                        throw new DeploymentException("Cannot deploy ejb application; No ejb deployer defined: " + entry.getName());
                                        continue;
                                    }

                                    Module module = builder.createModule(altVendorDDs.get(entry.getName()),
                                            moduleFile,
                                            entry.getName(),
                                            null,
                                            environment,
                                            moduleContextInfo,
                                            earName,
                                            naming, idBuilder);

                                    if (module != null) {
                                        moduleLocations.add(entry.getName());
                                        modules.add(module);
                                    }
                                    continue;
                                }
                                //TODO if no ejb-jar.xml inspect classes for EJB component annotations to identify as EJBJar module
                            } catch (IOException e) {
                                throw new DeploymentException("Invalid moduleFile: " + entry.getName(), e);
                            }
                        } else {
                            continue;
                        }

                        moduleLocations.add(entry.getName());

                        NestedJarFile moduleFile;
                        try {
                            moduleFile = new NestedJarFile(earFile, entry.getName());
                        } catch (IOException e) {
                            throw new DeploymentException("Invalid moduleFile: " + entry.getName(), e);
                        }

                        Module module = builder.createModule(altVendorDDs.get(entry.getName()),
                                moduleFile,
                                entry.getName(),
                                null,
                                environment,
                                moduleContextInfo,
                                earName,
                                naming, idBuilder);

                        if (module == null) {
                            throw new DeploymentException("Module was not " + moduleTypeName + ": " + entry.getName());
                        }

                        modules.add(module);
                    }
                }
            }

            //all the modules in the geronimo plan should have been found by now.
            if (!moduleLocations.containsAll(altVendorDDs.keySet())) {
                throw new DeploymentException("Geronimo ear plan contains modules that aren't in the ear: " + new HashSet<String>(moduleLocations).removeAll(altVendorDDs.keySet()));
            }
            //deploy the extension modules
            for (GerExtModuleType gerExtModule : gerApplication.getExtModuleArray()) {
                String moduleName;
                ModuleBuilder builder;
                Object moduleContextInfo = null;
                String moduleTypeName;

                if (gerExtModule.isSetEjb()) {
                    moduleName = gerExtModule.getEjb().getStringValue();
                    builder = getEjbConfigBuilder();
                    if (builder == null) {
                        throw new DeploymentException("Cannot deploy ejb application; No ejb deployer defined: " + moduleName);
                    }
                    moduleTypeName = "an EJB";
                } else if (gerExtModule.isSetWeb()) {
                    moduleName = gerExtModule.getWeb().getStringValue();
                    if (getWebConfigBuilder() == null) {
                        throw new DeploymentException("Cannot deploy web application; No war deployer defined: " + moduleName);
                    }
                    builder = getWebConfigBuilder();
                    moduleTypeName = "a war";
                    //ext modules must use vendor plan to set context-root
                } else if (gerExtModule.isSetConnector()) {
                    moduleName = gerExtModule.getConnector().getStringValue();
                    if (getConnectorConfigBuilder() == null) {
                        throw new DeploymentException("Cannot deploy resource adapter; No rar deployer defined: " + moduleName);
                    }
                    builder = getConnectorConfigBuilder();
                    moduleTypeName = "a connector";
                } else if (gerExtModule.isSetJava()) {
                    moduleName = gerExtModule.getJava().getStringValue();
                    if (getAppClientConfigBuilder() == null) {
                        throw new DeploymentException("Cannot deploy app client; No app client deployer defined: " + moduleName);
                    }
                    builder = getAppClientConfigBuilder();
                    moduleTypeName = "an application client";
                } else {
                    throw new DeploymentException("Could not find a module builder for module: " + gerExtModule);
                }
                //dd is included explicitly
                XmlObject[] anys = gerExtModule.selectChildren(GerExtModuleType.type.qnameSetForWildcardElements());
                if (anys.length != 1) {
                    throw new DeploymentException("Unexpected count of xs:any elements in embedded vendor plan " + anys.length + " qnameset: " + GerExtModuleType.type.qnameSetForWildcardElements());
                }
                Object vendorDD = anys[0];

                JarFile moduleFile;
                if (gerExtModule.isSetInternalPath()) {
                    String modulePath = gerExtModule.getInternalPath().trim();
                    moduleLocations.add(modulePath);
                    try {
                        moduleFile = new NestedJarFile(earFile, modulePath);
                    } catch (IOException e) {
                        throw new DeploymentException("Invalid moduleFile: " + modulePath, e);
                    }
                } else {
                    PatternType patternType = gerExtModule.getExternalPath();
                    String groupId = trim(patternType.getGroupId());
                    String artifactId = trim(patternType.getArtifactId());
                    String version = trim(patternType.getVersion());
                    String type = trim(patternType.getType());
                    Artifact artifact = new Artifact(groupId, artifactId, version, type);
                    try {
                        artifact = getArtifactResolver().resolveInClassLoader(artifact);
                    } catch (MissingDependencyException e) {
                        throw new DeploymentException("Could not resolve external rar location in repository: " + artifact, e);
                    }
                    File location = null;
                    for (Repository repository : repositories) {
                        if (repository.contains(artifact)) {
                            location = repository.getLocation(artifact);
                            break;
                        }
                    }
                    if (location == null) {
                        throw new DeploymentException(moduleTypeName + " is missing in repositories: " + artifact);
                    }
                    try {
                        moduleFile = new JarFile(location);
                    } catch (IOException e) {
                        throw new DeploymentException("Could not access contents of " + moduleTypeName, e);
                    }

                }

                Module module = builder.createModule(vendorDD,
                        moduleFile,
                        moduleName,
                        null, //TODO implement an alt-spec-dd element
                        environment,
                        moduleContextInfo,
                        earName,
                        naming, idBuilder);

                if (module == null) {
                    throw new DeploymentException("Module was not " + moduleTypeName + ": " + moduleName);
                }

                modules.add(module);
            }
        } finally {
            // delete all the temp files created for alt vendor dds
            for (Object altVendorDD : altVendorDDs.values()) {
                if (altVendorDD instanceof File) {
                    ((File) altVendorDD).delete();
                }
            }
        }
    }

    private ArtifactResolver getArtifactResolver() throws DeploymentException {
        if (artifactResolvers == null || artifactResolvers.isEmpty()) {
            throw new DeploymentException("No artifact resolver supplied to resolve external module");
        }
        return artifactResolvers.iterator().next();
    }

    private String trim(String s) {
        if (s == null) {
            return null;
        }
        return s.trim();
    }

    private boolean isLibraryEntry(ApplicationType application, ZipEntry entry) {
        String libDir = getLibraryDirectory(application);
        if (libDir != null && entry.getName().startsWith(libDir)) {
            return true;
        }
        return false;
    }

    private void mapVendorPlans(GerApplicationType gerApplication, Map<String, Object> altVendorDDs, JarFile earFile) throws DeploymentException {
        //build map from module path to alt vendor dd
        for (GerModuleType gerModule : gerApplication.getModuleArray()) {
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
//            if (!paths.contains(path)) {
//                throw new DeploymentException("Geronimo deployment plan refers to module '" + path + "' but that was not defined in the META-INF/application.xml");
//            }

            if (gerModule.isSetAltDd()) {
                // the the url of the alt dd
                try {
                    altVendorDDs.put(path, DeploymentUtil.toTempFile(earFile, gerModule.getAltDd().getStringValue()));
                } catch (IOException e) {
                    throw new DeploymentException("Invalid alt vendor dd url: " + gerModule.getAltDd().getStringValue(), e);
                }
            } else {
                //dd is included explicitly
                XmlObject[] anys = gerModule.selectChildren(GerModuleType.type.qnameSetForWildcardElements());
                if (anys.length != 1) {
                    throw new DeploymentException("Unexpected count of xs:any elements in embedded vendor plan " + anys.length + " qnameset: " + GerModuleType.type.qnameSetForWildcardElements());
                }
                altVendorDDs.put(path, anys[0]);
            }
        }
    }

    private URL getAltSpecDDURL(JarFile earFile, ModuleType moduleXml) throws DeploymentException {
        if (moduleXml != null && moduleXml.isSetAltDd()) {
            try {
                return DeploymentUtil.createJarURL(earFile, moduleXml.getAltDd().getStringValue());
            } catch (MalformedURLException e) {
                throw new DeploymentException("Invalid alt sped dd url: " + moduleXml.getAltDd().getStringValue(), e);
            }
        }
        return null;
    }

    private ModuleBuilder getBuilder(Module module) throws DeploymentException {
        if (module instanceof EJBModule) {
            if (getEjbConfigBuilder() == null) {
                throw new DeploymentException("Cannot deploy ejb application; No ejb deployer defined: " + module.getModuleURI());
            }
            return getEjbConfigBuilder();
        } else if (module instanceof WebModule) {
            if (getWebConfigBuilder() == null) {
                throw new DeploymentException("Cannot deploy web application; No war deployer defined: " + module.getModuleURI());
            }
            return getWebConfigBuilder();
        } else if (module instanceof ConnectorModule) {
            if (getConnectorConfigBuilder() == null) {
                throw new DeploymentException("Cannot deploy resource adapter; No rar deployer defined: " + module.getModuleURI());
            }
            return getConnectorConfigBuilder();
        } else if (module instanceof AppClientModule) {
            if (getAppClientConfigBuilder() == null) {
                throw new DeploymentException("Cannot deploy app client; No app client deployer defined: " + module.getModuleURI());
            }
            return getAppClientConfigBuilder();
        }
        throw new IllegalArgumentException("Unknown module type: " + module.getClass().getName());
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EARConfigBuilder.class, NameFactory.CONFIG_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("transactionManagerAbstractName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("connectionTrackerAbstractName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("transactionalTimerAbstractName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("nonTransactionalTimerAbstractName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("corbaGBeanAbstractName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("serverName", AbstractNameQuery.class, true);

        infoBuilder.addReference("Repositories", Repository.class, "Repository");
        infoBuilder.addReference("EJBConfigBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("WebConfigBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ConnectorConfigBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ActivationSpecInfoLocator", ActivationSpecInfoLocator.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("AppClientConfigBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("SecurityBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("PersistenceUnitBuilders", ModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ArtifactResolvers", ArtifactResolver.class, "ArtifactResolver");

        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "transactionManagerAbstractName",
                "connectionTrackerAbstractName",
                "transactionalTimerAbstractName",
                "nonTransactionalTimerAbstractName",
                "corbaGBeanAbstractName",
                "serverName",
                "Repositories",
                "EJBConfigBuilder",
                "WebConfigBuilder",
                "ConnectorConfigBuilder",
                "ActivationSpecInfoLocator",
                "AppClientConfigBuilder",
                "SecurityBuilders",
                "ServiceBuilders",
                "PersistenceUnitBuilders",
                "ArtifactResolvers",
                "kernel"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
