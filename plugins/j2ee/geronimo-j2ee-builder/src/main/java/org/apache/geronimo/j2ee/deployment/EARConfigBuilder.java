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
import java.io.InputStream;
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
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.ApplicationJndi;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.j2ee.management.impl.J2EEApplicationImpl;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
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
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.kernel.util.NestedJarFile;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationDocument;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerExtModuleType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerModuleType;
import org.apache.openejb.jee.Application;
//import org.apache.openejb.jee.Module;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.Web;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.CONFIG_BUILDER)
@OsgiService
public class EARConfigBuilder implements ConfigurationBuilder, CorbaGBeanNameSource, GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(EARConfigBuilder.class);
    private static final String LINE_SEP = System.getProperty("line.separator");

    private final static QName APPLICATION_QNAME = GerApplicationDocument.type.getDocumentElementName();
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();

    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application", "http://geronimo.apache.org/xml/ns/j2ee/application-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-1.1", "http://geronimo.apache.org/xml/ns/j2ee/application-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-1.2", "http://geronimo.apache.org/xml/ns/j2ee/application-2.0");
    }

    private final ConfigurationManager configurationManager;
    private final Collection<? extends Repository> repositories;
    private final SingleElementCollection ejbConfigBuilder;
    private final SingleElementCollection webConfigBuilder;
    private final SingleElementCollection connectorConfigBuilder;
    private final SingleElementCollection appClientConfigBuilder;
    private final SingleElementCollection resourceReferenceBuilder;
    private final NamespaceDrivenBuilderCollection serviceBuilders;
    private final Collection<ModuleBuilderExtension> BValModuleBuilders;
    private final Collection<ModuleBuilderExtension> persistenceUnitBuilders;
    private final NamingBuilder namingBuilders;

    private final Environment defaultEnvironment;
    private final AbstractNameQuery serverName;
    private final AbstractNameQuery transactionManagerObjectName;
    private final AbstractNameQuery connectionTrackerObjectName;
    private final AbstractNameQuery corbaGBeanObjectName;
    private final Naming naming;
    private final Collection<? extends ArtifactResolver> artifactResolvers;
    private final BundleContext bundleContext;
    private final AbstractNameQuery globalContextAbstractName;

    public static ThreadLocal<Boolean> createPlanMode = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public static ThreadLocal<ApplicationInfo> appInfo = new ThreadLocal<ApplicationInfo>() {
        @Override
        protected ApplicationInfo initialValue() {
            return null;
        }
    };

    public EARConfigBuilder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
                            @ParamAttribute(name = "transactionManagerAbstractName") AbstractNameQuery transactionManagerAbstractName,
                            @ParamAttribute(name = "connectionTrackerAbstractName") AbstractNameQuery connectionTrackerAbstractName,
                            @ParamAttribute(name = "corbaGBeanAbstractName") AbstractNameQuery corbaGBeanAbstractName,
                            @ParamAttribute(name = "globalContextAbstractName") AbstractNameQuery globalContextAbstractName,
                            @ParamAttribute(name = "serverName") AbstractNameQuery serverName,
                            @ParamReference(name = "Repositories", namingType = "Repository") Collection<? extends Repository> repositories,
                            @ParamReference(name = "EJBConfigBuilder", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilder> ejbConfigBuilder,
                            @ParamReference(name = "WebConfigBuilder", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilder> webConfigBuilder,
                            @ParamReference(name = "ConnectorConfigBuilder", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilder> connectorConfigBuilder,
                            @ParamReference(name = "ActivationSpecInfoLocator", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilder> resourceReferenceBuilder,
                            @ParamReference(name = "AppClientConfigBuilder", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilder> appClientConfigBuilder,
                            @ParamReference(name = "ServiceBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<NamespaceDrivenBuilder> serviceBuilders,
                            @ParamReference(name = "BValModuleBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilderExtension> BValModuleBuilders,
                            @ParamReference(name = "PersistenceUnitBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilderExtension> persistenceUnitBuilders,
                            @ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER) NamingBuilder namingBuilders,
                            @ParamReference(name = "ArtifactResolvers", namingType = "ArtifactResolver") Collection<? extends ArtifactResolver> artifactResolvers,
                            @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel,
                            @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws GBeanNotFoundException {
        this(defaultEnvironment,
                transactionManagerAbstractName,
                connectionTrackerAbstractName,
                corbaGBeanAbstractName,
                globalContextAbstractName,
                serverName,
                ConfigurationUtil.getConfigurationManager(kernel),
                repositories,
                new SingleElementCollection<ModuleBuilder>(ejbConfigBuilder),
                new SingleElementCollection<ModuleBuilder>(webConfigBuilder),
                new SingleElementCollection<ModuleBuilder>(connectorConfigBuilder),
                new SingleElementCollection<ModuleBuilder>(resourceReferenceBuilder),
                new SingleElementCollection<ModuleBuilder>(appClientConfigBuilder),
                serviceBuilders,
                BValModuleBuilders,
                persistenceUnitBuilders,
                namingBuilders,
                kernel.getNaming(),
                artifactResolvers,
                bundleContext);
    }

    public EARConfigBuilder(Environment defaultEnvironment,
                            AbstractNameQuery transactionManagerAbstractName,
                            AbstractNameQuery connectionTrackerAbstractName,
                            AbstractNameQuery corbaGBeanAbstractName,
                            AbstractNameQuery serverName,
                            AbstractNameQuery globalContextAbstractName,
                            Collection<? extends Repository> repositories,
                            ModuleBuilder ejbConfigBuilder,
                            ModuleBuilder webConfigBuilder,
                            ModuleBuilder connectorConfigBuilder,
                            ActivationSpecInfoLocator activationSpecInfoLocator,
                            ModuleBuilder appClientConfigBuilder,
                            NamespaceDrivenBuilder serviceBuilder,
                            ModuleBuilderExtension BValModuleBuilder,
                            ModuleBuilderExtension persistenceUnitBuilder,
                            NamingBuilder namingBuilders,
                            Naming naming,
                            Collection<? extends ArtifactResolver> artifactResolvers, BundleContext bundleContext) {
        this(defaultEnvironment,
                transactionManagerAbstractName,
                connectionTrackerAbstractName,
                corbaGBeanAbstractName,
                globalContextAbstractName,
                serverName,
                null,
                repositories,
                new SingleElementCollection<ModuleBuilder>(ejbConfigBuilder),
                new SingleElementCollection<ModuleBuilder>(webConfigBuilder),
                new SingleElementCollection<ModuleBuilder>(connectorConfigBuilder),
                new SingleElementCollection<ActivationSpecInfoLocator>(activationSpecInfoLocator),
                new SingleElementCollection<ModuleBuilder>(appClientConfigBuilder),
                serviceBuilder == null ? Collections.<NamespaceDrivenBuilder>emptySet() : Collections.singleton(serviceBuilder),
                BValModuleBuilder == null ? Collections.<ModuleBuilderExtension>emptySet() : Collections.singleton(BValModuleBuilder),
                persistenceUnitBuilder == null ? Collections.<ModuleBuilderExtension>emptySet() : Collections.singleton(persistenceUnitBuilder),
                namingBuilders,
                naming,
                artifactResolvers,
                bundleContext);
    }

    private EARConfigBuilder(Environment defaultEnvironment,
                             AbstractNameQuery transactionManagerAbstractName,
                             AbstractNameQuery connectionTrackerAbstractName,
                             AbstractNameQuery corbaGBeanAbstractName,
                             AbstractNameQuery globalContextAbstractName,
                             AbstractNameQuery serverName,
                             ConfigurationManager configurationManager,
                             Collection<? extends Repository> repositories,
                             SingleElementCollection ejbConfigBuilder,
                             SingleElementCollection webConfigBuilder,
                             SingleElementCollection connectorConfigBuilder,
                             SingleElementCollection resourceReferenceBuilder,
                             SingleElementCollection appClientConfigBuilder,
                             Collection<NamespaceDrivenBuilder> serviceBuilders,
                             Collection<ModuleBuilderExtension> BValModuleBuilders,
                             Collection<ModuleBuilderExtension> persistenceUnitBuilders,
                             NamingBuilder namingBuilders,
                             Naming naming,
                             Collection<? extends ArtifactResolver> artifactResolvers,
                             BundleContext bundleContext) {
        this.configurationManager = configurationManager;
        this.repositories = repositories;
        this.defaultEnvironment = defaultEnvironment;

        this.ejbConfigBuilder = ejbConfigBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.webConfigBuilder = webConfigBuilder;
        this.connectorConfigBuilder = connectorConfigBuilder;
        this.appClientConfigBuilder = appClientConfigBuilder;
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
        this.BValModuleBuilders = BValModuleBuilders;
        this.persistenceUnitBuilders = persistenceUnitBuilders;
        this.namingBuilders = namingBuilders;

        this.transactionManagerObjectName = transactionManagerAbstractName;
        this.connectionTrackerObjectName = connectionTrackerAbstractName;
        this.corbaGBeanObjectName = corbaGBeanAbstractName;
        this.globalContextAbstractName = globalContextAbstractName;
        this.serverName = serverName;
        this.naming = naming;
        this.artifactResolvers = artifactResolvers;
        this.bundleContext = bundleContext;
    }

    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doFail() {
        doStop();
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

        Map<JndiKey, Map<String, Object>> jndiContext = Module.share(Module.APP, module.getJndiContext());

        ApplicationInfo applicationInfo = new ApplicationInfo(module.getType(),
                module.getEnvironment(),
                module.getModuleName(),
                module.getName(),
                jarFile,
                null,
                null,
                null,
                jndiContext
        );
        applicationInfo.getModules().add(module);
        return applicationInfo;
    }

    private ApplicationInfo getEarPlan(File planFile, JarFile earFile, ModuleIDBuilder idBuilder) throws DeploymentException {
        String specDD;
        Application application = null;
        if (earFile != null) {
            URL applicationXmlUrl = null;
            try {
                applicationXmlUrl = JarUtils.createJarURL(earFile, "META-INF/application.xml");
                specDD = JarUtils.readAll(applicationXmlUrl);
                //we found something called application.xml in the right place, if we can't parse it it's an error
                InputStream in = applicationXmlUrl.openStream();
                try {
                    application = (Application) JaxbJavaee.unmarshalJavaee(Application.class, in);
                 } finally {
                    IOUtils.close(in);
                }
            } catch (ParserConfigurationException e) {
                throw new DeploymentException("Could not parse application.xml", e);
             } catch (SAXException e) {
                throw new DeploymentException("Could not parse application.xml", e);
             } catch (JAXBException e) {
                throw new DeploymentException("Could not parse application.xml", e);
            } catch (Exception e) {
                //ee5 spec allows optional application.xml, continue with application == null
                if (!earFile.getName().endsWith(".ear")) {
                    return null;
                }
                application = new Application();
            }
        }

        GerApplicationType gerApplication = null;
        try {
            // load the geronimo-application.xml from either the supplied plan or from the earFile
            XmlObject rawPlan;
            try {
                if (planFile != null) {
                    rawPlan = XmlBeansUtil.parse(planFile.toURI().toURL(), getClass().getClassLoader());
                    gerApplication = (GerApplicationType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, APPLICATION_QNAME, GerApplicationType.type);
                    if (gerApplication == null) {
                        return null;
                    }
                } else {
                    URL path = JarUtils.createJarURL(earFile, "META-INF/geronimo-application.xml");
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
        namingBuilders.buildEnvironment(application, gerApplication, environment);

        // get the modules either the application plan or for a stand alone module from the specific deployer
        // todo change module so you can extract the real module path back out.. then we can eliminate
        // the moduleLocations and have addModules return the modules
        String applicationName = null;
        if (application!=null && application.getApplicationName() != null) {
            applicationName = application.getApplicationName().trim();
        } else if (earFile != null) {
            applicationName = FileUtils.removeExtension(new File(earFile.getName()).getName(), ".ear");
        } else {
            applicationName = artifact.toString();
        }
        ApplicationInfo applicationInfo = new ApplicationInfo(ConfigurationModuleType.EAR,
                environment,
                earName,
                applicationName,
                earFile,
                application,
                gerApplication,
                application==null ? null : application.toString()
        );
        try {
            addModules(earFile, application, gerApplication, environment, applicationInfo, idBuilder);
            if (applicationInfo.getModules().isEmpty()) {
                //if no application.xml and no modules detected, return null for stand-alone module processing
                return null;
            } else if (application!=null){
                addModulesToDefaultPlan(application, applicationInfo.getModules());
            }
        } catch (Throwable e) {
            // close all the modules
            for (Module module : applicationInfo.getModules()) {
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

        return applicationInfo;
    }

    private void addModulesToDefaultPlan(Application application, Set<Module<?, ?>> modules) {
        for (Module module : modules) {
            ConfigurationModuleType configurationModuleType = module.getType();
            org.apache.openejb.jee.Module newModule = new org.apache.openejb.jee.Module();
            if (configurationModuleType.equals(ConfigurationModuleType.WAR)) {
                WebModule webModule = (WebModule) module;
                Web web = new Web();
                web.setContextRoot(webModule.getContextRoot());
                web.setWebUri(webModule.getTargetPath());
                newModule.setWeb(web);
            } else if (configurationModuleType.equals(ConfigurationModuleType.EJB)) {
                newModule.setEjb(module.getTargetPath());
            } else if (configurationModuleType.equals(ConfigurationModuleType.RAR)) {
                newModule.setConnector(module.getTargetPath());
            } else if (configurationModuleType.equals(ConfigurationModuleType.CAR)) {
                newModule.setJava(module.getTargetPath());
            }
            application.getModule().add(newModule);
        }
    }

    private GerApplicationType createDefaultPlan(Application application, JarFile module) {
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

        if (earFile != null) {
            Manifest mf = earFile.getManifest();
            if (mf != null && mf.getMainAttributes().getValue("Bundle-SymbolicName") != null) {
                log.warn("Application module contains OSGi manifest. The OSGi manifest will be ignored and the application will be deployed as a regular Java EE application.");
            }
        }

        ApplicationInfo applicationInfo = (ApplicationInfo) plan;

        EARContext earContext = null;
        ConfigurationModuleType applicationType = applicationInfo.getType();
        applicationInfo.getEnvironment().setConfigId(configId);
        try {
            try {
                targetConfigurationStore.createNewConfigurationDir(configId);
            } catch (ConfigurationAlreadyExistsException e) {
                throw new DeploymentException(e);
            }

            ConfigurationManager configurationManager = this.configurationManager;
            if (configurationManager == null) {
                configurationManager = new SimpleConfigurationManager(configurationStores, artifactResolver, repositories, bundleContext);
            }
            //Use a temporary folder to hold the extracted files for analysis use
            File tempDirectory = FileUtils.createTempDir();
            // Create the output ear context
            earContext = new EARContext(tempDirectory,
                    inPlaceDeployment ? JarUtils.toFile(earFile) : null,
                    applicationInfo.getEnvironment(),
                    applicationType,
                    naming,
                    configurationManager,
                    bundleContext,
                    serverName,
                    applicationInfo.getModuleName(),
                    transactionManagerObjectName,
                    connectionTrackerObjectName,
                    corbaGBeanObjectName,
                    new HashMap()
            );
            applicationInfo.setEarContext(earContext);
            applicationInfo.setRootEarContext(earContext);
            earContext.getGeneralData().put(EARContext.MODULE_LIST_KEY, applicationInfo.getModuleLocations());

            // Copy over all files that are _NOT_ modules (e.g. META-INF and APP-INF files)
            LinkedHashSet<String> moduleLocations = applicationInfo.getModuleLocations();
            boolean initModulesInDDOrder = false;
            if (ConfigurationModuleType.EAR == applicationType && earFile != null) {
                //get the value of the library-directory element in spec DD
                Application specDD = applicationInfo.getSpecDD();

                if (specDD != null && specDD.getInitializeInOrder() != null) {
                    initModulesInDDOrder = specDD.getInitializeInOrder();
                }

                String libDir = getLibraryDirectory(specDD);
                Collection<String> libClasspath = applicationInfo.getClassPath();
                for (Enumeration<JarEntry> e = earFile.entries(); e.hasMoreElements();) {
                    ZipEntry entry = e.nextElement();
                    String entryName = entry.getName();
                    boolean addEntry = true;
                    for (String moduleLocation : moduleLocations) {
                        if (entryName.startsWith(moduleLocation)) {
                            addEntry = false;
                            break;
                        }
                    }

                    if (addEntry) {
                        earContext.addFile(URI.create(entry.getName()), earFile, entry);
                    }
                }

                for (Enumeration<JarEntry> e = earFile.entries(); e.hasMoreElements();) {
                    ZipEntry entry = e.nextElement();
                    String entryName = entry.getName();
                    //   EAR/lib/sub-dir/*.jar should not be added into lib classpath.
                    if (libDir != null && entry.getName().startsWith(libDir) && entry.getName().endsWith(".jar") && entry.getName().substring(libDir.length()+1).indexOf("/") == -1) {
                        JarUtils.assertTempFile();
                        NestedJarFile library = new NestedJarFile(earFile, entry.getName());
                        earContext.addIncludeAsPackedJar(URI.create(entry.getName()), library);
                        libClasspath.add(entry.getName());
                        earContext.addManifestClassPath(library, URI.create(libDir+"/"), libClasspath);
                    }
                }

            }


            for(String classpath:applicationInfo.getClassPath()){
                earContext.addToClassPath(classpath);
            }

            GerApplicationType geronimoApplication = (GerApplicationType) applicationInfo.getVendorDD();

            // each module installs it's files into the output context.. this is different for each module type

            List<Module<?,?>> modules = new ArrayList<Module<?,?>>();
            modules.addAll(applicationInfo.getModules());
            if (initModulesInDDOrder){
                //Per the xsd description, the application client module could be in any order
                Collections.sort(modules, new Module.AppClientModuleLastComparator());
            } else {
                Collections.sort(modules, new Module.ModulePriorityComparator());
            }

            for (Module<?,?> module : modules) {
                getBuilder(module).installModule(earFile, earContext, module, configurationStores, targetConfigurationStore, repositories);
            }
            //push the module classpaths into the appropriate ear context
            applicationInfo.accumulateClassPath();

            earContext.flush();
            earContext.initializeConfiguration();

            for (Module<?,?> module : modules) {
                if (earContext != module.getEarContext()) {
                    module.getEarContext().initializeConfiguration();
                }
            }

            applicationInfo.getJndiContext().get(JndiScope.app).put("app/AppName", applicationInfo.getName());

            // give each module a chance to populate the earContext now that a classloader is available
            Bundle bundle = earContext.getDeploymentBundle();
            if (ConfigurationModuleType.EAR == applicationType) {
                namingBuilders.initContext(applicationInfo.getSpecDD(), applicationInfo.getVendorDD(), applicationInfo);
            }
            for (Module<?,?> module : modules) {
                if (createPlanMode.get()) {
                    try {
                        getBuilder(module).initContext(earContext, module, bundle);
                    } catch (Exception e) {
                        // ignore any exceptions to continue processing with the rest of the modules;
                        log.warn("Exception during initContext() phase");
                    }
                } else {
                    getBuilder(module).initContext(earContext, module, bundle);
                }
            }

            AbstractName appJndiName = naming.createChildName(earContext.getModuleName(), "ApplicationJndi", "ApplicationJndi");
            earContext.getGeneralData().put(EARContext.APPLICATION_JNDI_NAME_KEY, appJndiName);
            GBeanData appContexts = new GBeanData(appJndiName, ApplicationJndi.class);
            appContexts.setAttribute("globalContextSegment", applicationInfo.getJndiContext().get(JndiScope.global));
            appContexts.setAttribute("applicationContextMap", applicationInfo.getJndiContext().get(JndiScope.app));
            appContexts.setReferencePattern("GlobalContext", globalContextAbstractName);
            if (!initModulesInDDOrder) {
                earContext.addGBean(appContexts);
            }

            // add gbeans declared in the geronimo-application.xml
            if (geronimoApplication != null) {
                serviceBuilders.build(geronimoApplication, earContext, earContext);
            }

            if (ConfigurationModuleType.EAR == applicationType) {

                for (ModuleBuilderExtension mbe : BValModuleBuilders) {
                    mbe.initContext(earContext, applicationInfo, earContext.getDeploymentBundle());
                }

                // process persistence unit in EAR library directory
                for (ModuleBuilderExtension mbe : persistenceUnitBuilders) {
                    mbe.initContext(earContext, applicationInfo, earContext.getDeploymentBundle());
                }
                for (ModuleBuilderExtension mbe : persistenceUnitBuilders) {
                    mbe.addGBeans(earContext, applicationInfo, earContext.getDeploymentBundle(), repositories);
                }

                // Create the J2EEApplication managed object
                GBeanData gbeanData = new GBeanData(earContext.getModuleName(), J2EEApplicationImpl.class);
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

                namingBuilders.buildNaming(applicationInfo.getSpecDD(), applicationInfo.getVendorDD(), applicationInfo, earContext.getGeneralData());
            }

            // each module can now add it's GBeans
            for (Module<?,?> module : modules) {
                if (createPlanMode.get()) {
                    try {
                        getBuilder(module).addGBeans(earContext, module, bundle, repositories);
                    } catch (DeploymentException e) {
                        // ignore any exceptions to continue processing with the rest of the modules;
                        log.warn("Exception during addGBeans() phase", e);
                    }
                } else {
                    getBuilder(module).addGBeans(earContext, module, bundle, repositories);
                }
            }

            for(Module<?, ?> module : modules) {
                module.flushGBeansToContext();
            }

            //ApplicationJndi is added as the last GBean, as it is a common dependency GBean by each EJB GBean and web module GBean
            //With doing this, it  makes the modules in the ear are started in order, while init-order is configured.
            if (initModulesInDDOrder) {
                earContext.addGBean(appContexts);
            }

            if (createPlanMode.get()) {
                EARConfigBuilder.appInfo.set(applicationInfo);
                throw new DeploymentException();
            }

            // it's the caller's responsibility to close the context...
            return earContext;
        } catch (GBeanAlreadyExistsException e) {
            cleanupContext(earContext);
            throw new DeploymentException(e);
        } catch (IOException e) {
            cleanupContext(earContext);
            throw e;
        } catch (DeploymentException e) {
            cleanupContext(earContext);
            throw e;
        } catch (RuntimeException e) {
            cleanupContext(earContext);
            throw e;
        } catch (Error e) {
            cleanupContext(earContext);
            throw e;
        } finally {
            for (Module<?, ?> module : applicationInfo.getModules()) {
                module.close();
            }
        }
    }

    private void cleanupContext(EARContext earContext) {
        if (earContext != null) {
            try {
                earContext.close();
            } catch (IOException ioe) {
                // ignore any cleanup problems
            } catch (DeploymentException de) {
                // ignore any cleanup problems
            }
        }
    }

    private String getLibraryDirectory(Application specDD) {
        if (specDD == null || specDD.getLibraryDirectory() == null) {
            //value 'lib' is used if element not set or ear does not contain a dd
            return "lib";
        }

        //only set if not empty value, empty value implies no library directory
        String value = specDD.getLibraryDirectory();
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

        if (!FileUtils.recursiveDelete(configurationDir, cannotBeDeletedList)) {
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

    private void addModules(JarFile earFile, Application application, GerApplicationType gerApplication, Environment environment, Module applicationInfo, ModuleIDBuilder idBuilder) throws DeploymentException {
        Map<String, Object> altVendorDDs = new HashMap<String, Object>();
        try {
            mapVendorPlans(gerApplication, altVendorDDs, earFile);
            if (earFile != null) {
                if (application.getModule().size() != 0) {
                    List<org.apache.openejb.jee.Module> Modules = application.getModule();

                    //get a set containing all of the files in the ear that are actually modules
                    for (org.apache.openejb.jee.Module moduleXml : Modules) {
                        String modulePath;
                        ModuleBuilder builder;

                        Object moduleContextInfo = null;
                        String moduleTypeName;
                        if (moduleXml.getEjb() != null) {
                            modulePath = moduleXml.getEjb();
                            builder = getEjbConfigBuilder();
                            if (builder == null) {
                                throw new DeploymentException("Cannot deploy ejb application; No ejb deployer defined: " + modulePath);
                            }
                            moduleTypeName = "an EJB";
                        } else if (moduleXml.getWeb() != null) {
                            modulePath = moduleXml.getWeb().getWebUri();
                            if (getWebConfigBuilder() == null) {
                                throw new DeploymentException("Cannot deploy web application; No war deployer defined: " + modulePath);
                            }
                            builder = getWebConfigBuilder();
                            moduleTypeName = "a war";
                            moduleContextInfo = moduleXml.getWeb().getContextRoot().trim();
                        } else if (moduleXml.getConnector() != null) {
                            modulePath = moduleXml.getConnector();
                            if (getConnectorConfigBuilder() == null) {
                                throw new DeploymentException("Cannot deploy resource adapter; No rar deployer defined: " + modulePath);
                            }
                            builder = getConnectorConfigBuilder();
                            moduleTypeName = "a connector";
                        } else if (moduleXml.getJava() != null) {
                            modulePath = moduleXml.getJava();
                            if (getAppClientConfigBuilder() == null) {
                                throw new DeploymentException("Cannot deploy app client; No app client deployer defined: " + modulePath);
                            }
                            builder = getAppClientConfigBuilder();
                            moduleTypeName = "an application client";
                        } else {
                            throw new DeploymentException("Could not find a module builder for module: " + moduleXml);
                        }

                        applicationInfo.getModuleLocations().add(modulePath);

                        NestedJarFile moduleFile;
                        try {
                            JarUtils.assertTempFile();
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
                                applicationInfo,
                                naming, idBuilder);

                        if (module == null) {
                            throw new DeploymentException("Module was not " + moduleTypeName + ": " + modulePath);
                        }

                        applicationInfo.getModules().add(module);
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
                                JarUtils.assertTempFile();
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
                                            applicationInfo,
                                            naming, idBuilder);

                                    if (module != null) {
                                        applicationInfo.getModuleLocations().add(entry.getName());
                                        applicationInfo.getModules().add(module);
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

                        applicationInfo.getModuleLocations().add(entry.getName());

                        NestedJarFile moduleFile;
                        try {
                            JarUtils.assertTempFile();
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
                                applicationInfo,
                                naming, idBuilder);

                        if (module == null) {
                            throw new DeploymentException("Module was not " + moduleTypeName + ": " + entry.getName());
                        }

                        applicationInfo.getModules().add(module);
                    }
                }

                discoverWebBeans(earFile, application,environment, applicationInfo, idBuilder, altVendorDDs);
            }

            //all the modules in the geronimo plan should have been found by now.
            if (!applicationInfo.getModuleLocations().containsAll(altVendorDDs.keySet())) {
                HashSet<String> missingModules = new HashSet<String>(altVendorDDs.keySet());
                missingModules.removeAll(applicationInfo.getModuleLocations());
                throw new DeploymentException("Geronimo ear plan contains modules that are not in the ear: " + missingModules);
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
                    applicationInfo.getModuleLocations().add(modulePath);
                    try {
                        JarUtils.assertTempFile();
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
                        applicationInfo,
                        naming, idBuilder);

                if (module == null) {
                    throw new DeploymentException("Module was not " + moduleTypeName + ": " + moduleName);
                }

                applicationInfo.getModules().add(module);
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

    private void discoverWebBeans(JarFile earFile, Application application, Environment environment, Module applicationInfo, ModuleIDBuilder idBuilder, Map<String, Object> altVendorDDs) throws DeploymentException {
        Enumeration<JarEntry> entries = earFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".jar") && isLibraryEntry(application, entry)) {
                try {
                    JarUtils.assertTempFile();
                    NestedJarFile moduleFile = new NestedJarFile(earFile, entry.getName());

                    if (moduleFile.getEntry("META-INF/beans.xml") == null) continue;

                    //ask the ejb builder if its an ejb module
                    ModuleBuilder builder = getEjbConfigBuilder();
                    if (builder == null) {
                        continue;
                    }

                    Module module = builder.createModule(altVendorDDs.get(entry.getName()),
                            moduleFile,
                            entry.getName(),
                            null,
                            environment,
                            null,
                            applicationInfo,
                            naming, idBuilder);

                    if (module != null) {
                        applicationInfo.getModuleLocations().add(entry.getName());
                        applicationInfo.getModules().add(module);
                    }
                } catch (IOException e) {
                    throw new DeploymentException("Invalid moduleFile: " + entry.getName(), e);
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

    private boolean isLibraryEntry(Application application, ZipEntry entry) {
        String libDir = getLibraryDirectory(application);
        return libDir != null && entry.getName().startsWith(libDir);
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
                    altVendorDDs.put(path, JarUtils.toTempFile(earFile, gerModule.getAltDd().getStringValue()));
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

    private URL getAltSpecDDURL(JarFile earFile, org.apache.openejb.jee.Module moduleXml) throws DeploymentException {
        if (moduleXml != null && moduleXml.getAltDd() != null) {
            try {
                return JarUtils.createJarURL(earFile, moduleXml.getAltDd());
            } catch (MalformedURLException e) {
                throw new DeploymentException("Invalid alt sped dd url: " + moduleXml.getAltDd(), e);
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

}
