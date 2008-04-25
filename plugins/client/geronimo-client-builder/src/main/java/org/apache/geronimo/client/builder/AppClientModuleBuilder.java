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
package org.apache.geronimo.client.builder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.LinkedHashSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.client.AppClientContainer;
import org.apache.geronimo.client.StaticJndiContextPlugin;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ClassPathList;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.ModuleList;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.deployment.AppClientModule;
import org.apache.geronimo.j2ee.deployment.ConnectorModule;
import org.apache.geronimo.j2ee.deployment.CorbaGBeanNameSource;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApplicationClient;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEAppClientModuleImpl;
import org.apache.geronimo.j2ee.ApplicationInfo;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientDocument;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientType;
import org.apache.geronimo.xbeans.geronimo.client.GerResourceType;
import org.apache.geronimo.xbeans.geronimo.naming.GerAbstractNamingEntryDocument;
import org.apache.geronimo.xbeans.geronimo.security.GerSubjectInfoType;
import org.apache.geronimo.xbeans.javaee.ApplicationClientDocument;
import org.apache.geronimo.xbeans.javaee.ApplicationClientType;
import org.apache.geronimo.xbeans.javaee.FullyQualifiedClassType;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


/**
 * @version $Rev:385232 $ $Date$
 */
public class AppClientModuleBuilder implements ModuleBuilder, CorbaGBeanNameSource, GBeanLifecycle {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String GERAPPCLIENT_NAMESPACE = GerApplicationClientDocument.type.getDocumentElementName().getNamespaceURI();
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-client", "http://geronimo.apache.org/xml/ns/j2ee/application-client-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-client-1.1", "http://geronimo.apache.org/xml/ns/j2ee/application-client-2.0");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/application-client-1.2", "http://geronimo.apache.org/xml/ns/j2ee/application-client-2.0");
    }

    private final Environment defaultClientEnvironment;
    private final Environment defaultServerEnvironment;
    private final AbstractNameQuery corbaGBeanObjectName;

    private final AbstractNameQuery transactionManagerObjectName;
    private final AbstractNameQuery connectionTrackerObjectName;
    private final AbstractNameQuery credentialStoreName;
    private final SingleElementCollection connectorModuleBuilder;
    private final NamespaceDrivenBuilderCollection serviceBuilder;
    private final NamingBuilderCollection namingBuilders;
    private final Collection<ModuleBuilderExtension> moduleBuilderExtensions;

    private final Collection<Repository> repositories;

    private final ArtifactResolver clientArtifactResolver;

    public AppClientModuleBuilder(Environment defaultClientEnvironment,
                                  Environment defaultServerEnvironment,
                                  AbstractNameQuery transactionManagerObjectName,
                                  AbstractNameQuery connectionTrackerObjectName,
                                  AbstractNameQuery corbaGBeanObjectName,
                                  AbstractNameQuery credentialStoreName,
                                  Collection<Repository> repositories,
                                  ModuleBuilder connectorModuleBuilder,
                                  NamespaceDrivenBuilder serviceBuilder,
                                  Collection<NamingBuilder> namingBuilders,
                                  Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                                  ArtifactResolver clientArtifactResolver) {
        this(defaultClientEnvironment,
                defaultServerEnvironment,
                transactionManagerObjectName,
                connectionTrackerObjectName,
                corbaGBeanObjectName,
                credentialStoreName, repositories, new SingleElementCollection<ModuleBuilder>(connectorModuleBuilder),
                serviceBuilder == null ? Collections.EMPTY_SET : Collections.singleton(serviceBuilder),
                namingBuilders == null ? Collections.EMPTY_SET : namingBuilders,
                moduleBuilderExtensions,
                clientArtifactResolver);
    }

    public AppClientModuleBuilder(AbstractNameQuery transactionManagerObjectName,
                                  AbstractNameQuery connectionTrackerObjectName,
                                  AbstractNameQuery corbaGBeanObjectName,
                                  AbstractNameQuery credentialStoreName,
                                  Collection<Repository> repositories,
                                  Collection<ModuleBuilder> connectorModuleBuilder,
                                  Collection<NamespaceDrivenBuilder> serviceBuilder,
                                  Collection<NamingBuilder> namingBuilders,
                                  Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                                  ArtifactResolver clientArtifactResolver,
                                  Environment defaultClientEnvironment,
                                  Environment defaultServerEnvironment
    ) {
        this(defaultClientEnvironment,
                defaultServerEnvironment,
                transactionManagerObjectName,
                connectionTrackerObjectName,
                corbaGBeanObjectName,
                credentialStoreName, repositories,
                new SingleElementCollection<ModuleBuilder>(connectorModuleBuilder),
                serviceBuilder,
                namingBuilders,
                moduleBuilderExtensions,
                clientArtifactResolver);
    }

    private AppClientModuleBuilder(Environment defaultClientEnvironment,
                                   Environment defaultServerEnvironment,
                                   AbstractNameQuery transactionManagerObjectName,
                                   AbstractNameQuery connectionTrackerObjectName,
                                   AbstractNameQuery corbaGBeanObjectName,
                                   AbstractNameQuery credentialStoreName,
                                   Collection<Repository> repositories,
                                   SingleElementCollection<ModuleBuilder> connectorModuleBuilder,
                                   Collection<NamespaceDrivenBuilder> serviceBuilder,
                                   Collection<NamingBuilder> namingBuilders,
                                   Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                                   ArtifactResolver clientArtifactResolver) {
        this.defaultClientEnvironment = defaultClientEnvironment;
        this.defaultServerEnvironment = defaultServerEnvironment;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.transactionManagerObjectName = transactionManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.credentialStoreName = credentialStoreName;
        this.repositories = repositories;
        this.connectorModuleBuilder = connectorModuleBuilder;
        this.serviceBuilder = new NamespaceDrivenBuilderCollection(serviceBuilder, GBeanBuilder.SERVICE_QNAME);
        this.namingBuilders = new NamingBuilderCollection(namingBuilders, GerAbstractNamingEntryDocument.type.getDocumentElementName());
        this.moduleBuilderExtensions = moduleBuilderExtensions;
        this.clientArtifactResolver = clientArtifactResolver;
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

    private ModuleBuilder getConnectorModuleBuilder() {
        return (ModuleBuilder) connectorModuleBuilder.getElement();
    }

    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "app-client", null, null, null, naming, idBuilder);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, earName, naming, idBuilder);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null : "moduleFile is null";
        assert targetPath != null : "targetPath is null";
        assert !targetPath.endsWith("/") : "targetPath must not end with a '/'";
        assert (earName == null) == (earEnvironment == null) : "if earName is not null you must supply earEnvironment as well";

        boolean standAlone = earEnvironment == null;

        // get the app client main class
        String mainClass;
        try {
            Manifest manifest = moduleFile.getManifest();
            if (manifest == null) {
                throw new DeploymentException("App client module jar does not contain a manifest: " + moduleFile.getName());
            }
            mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClass == null) {
                //not an app client
                return null;
            }
            String classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if (standAlone && classPath != null) {
                throw new DeploymentException("Manifest class path entry is not allowed in a standalone jar (JAVAEE 5 Section 8.2)");
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not get manifest from app client module: " + moduleFile.getName(), e);
        }

        String specDD;
        ApplicationClientType appClient = null;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "META-INF/application-client.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);
        } catch (Exception e) {
            //construct a default spec dd
            ApplicationClientDocument appClientDoc = ApplicationClientDocument.Factory.newInstance();
            appClientDoc.addNewApplicationClient();
            appClient = appClientDoc.getApplicationClient();
            specDD = appClientDoc.xmlText();
        }

        if (appClient == null) {
            //we found application-client.xml, if it won't parse it's an error.
            try {
                // parse it
                XmlObject xmlObject = XmlBeansUtil.parse(specDD);
                ApplicationClientDocument appClientDoc = convertToApplicationClientSchema(xmlObject);
                appClient = appClientDoc.getApplicationClient();
            } catch (XmlException e) {
                throw new DeploymentException("Unable to parse application-client.xml", e);
            }
        }

        // parse vendor dd
        GerApplicationClientType gerAppClient = getGeronimoAppClient(plan, moduleFile, standAlone, targetPath, appClient, earEnvironment);


        EnvironmentType clientEnvironmentType = gerAppClient.getClientEnvironment();
        Environment clientEnvironment = EnvironmentBuilder.buildEnvironment(clientEnvironmentType, defaultClientEnvironment);
        if (standAlone) {
            String name = new File(moduleFile.getName()).getName();
            idBuilder.resolve(clientEnvironment, name + "_" + name, "jar");
        } else {
            Artifact earConfigId = earEnvironment.getConfigId();
            idBuilder.resolve(clientEnvironment, earConfigId.getArtifactId() + "_" + targetPath, "jar");
        }
        EnvironmentType serverEnvironmentType = gerAppClient.getServerEnvironment();
        Environment serverEnvironment = EnvironmentBuilder.buildEnvironment(serverEnvironmentType, defaultServerEnvironment);
        if (!standAlone) {
            EnvironmentBuilder.mergeEnvironments(earEnvironment, serverEnvironment);
            serverEnvironment = earEnvironment;
            if (!serverEnvironment.getConfigId().isResolved()) {
                throw new IllegalStateException("Server environment module ID should be fully resolved (not " + serverEnvironment.getConfigId() + ")");
            }
        } else {
            idBuilder.resolve(serverEnvironment, new File(moduleFile.getName()).getName(), "jar");
        }

        if (earName == null) {
            earName = naming.createRootName(serverEnvironment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
        }

        //always use the artifactId of the app client as the name component of the module name (on the server).
        AbstractName moduleName = naming.createChildName(earName, clientEnvironment.getConfigId().toString(), NameFactory.APP_CLIENT_MODULE);
        AbstractName clientBaseName = naming.createRootName(clientEnvironment.getConfigId(), clientEnvironment.getConfigId().toString(), NameFactory.J2EE_APPLICATION);

        //start installing the resource adapters in the client.
        Collection<ConnectorModule> resourceModules = new ArrayList<ConnectorModule>();
        GerResourceType[] resources = gerAppClient.getResourceArray();
        for (GerResourceType resource : resources) {
            String path;
            JarFile connectorFile;
            if (resource.isSetExternalRar()) {
                PatternType externalRar = resource.getExternalRar();
                String groupId = trim(externalRar.getGroupId());
                String artifactId = trim(externalRar.getArtifactId());
                String version = trim(externalRar.getVersion());
                String type = trim(externalRar.getType());
                Artifact artifact = new Artifact(groupId, artifactId, version, type);
                try {
                    artifact = clientArtifactResolver.resolveInClassLoader(artifact);
                } catch (MissingDependencyException e) {
                    throw new DeploymentException("Could not resolve external rar location in repository: " + artifact, e);
                }
                File file = null;
                for (Repository repository : repositories) {
                    if (repository.contains(artifact)) {
                        file = repository.getLocation(artifact);
                        break;
                    }
                }
                if (file == null) {
                    throw new DeploymentException("Missing external rar in repositories: " + artifact);
                }
                try {
                    connectorFile = new JarFile(file);
                } catch (IOException e) {
                    throw new DeploymentException("Could not access external rar contents for artifact: " + artifact, e);
                }
                path = artifact.toString();
            } else {
                path = resource.getInternalRar();
                try {
                    connectorFile = new NestedJarFile(moduleFile, path);
                } catch (IOException e) {
                    throw new DeploymentException("Could not locate connector inside ear", e);
                }
            }
            XmlObject connectorPlan = resource.getConnector();
            ConnectorModule connectorModule = (ConnectorModule) getConnectorModuleBuilder().createModule(connectorPlan, connectorFile, path, null, clientEnvironment, null, clientBaseName, naming, idBuilder);
            resourceModules.add(connectorModule);
        }

        // Create the AnnotatedApp interface for the AppClientModule
        AnnotatedApplicationClient annotatedApplicationClient = new AnnotatedApplicationClient(appClient, mainClass);

        AppClientModule module = new AppClientModule(standAlone, moduleName, clientBaseName, serverEnvironment, clientEnvironment, moduleFile, targetPath, appClient, mainClass, gerAppClient, specDD, resourceModules, annotatedApplicationClient);
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, plan, moduleFile, targetPath, specDDUrl, clientEnvironment, null, earName, naming, idBuilder);
        }
        if (standAlone) {
            ApplicationInfo appInfo = new ApplicationInfo(ConfigurationModuleType.CAR,
                    serverEnvironment,
                    earName,
                    null,
                    null,
                    null,
                    new LinkedHashSet<Module>(Collections.singleton(module)),
                    new ModuleList(),
                    null);
            return appInfo;
        } else {
            return module;
        }
    }

    private String trim(String s) {
        if (s == null) {
            return null;
        }
        return s.trim();
    }

    GerApplicationClientType getGeronimoAppClient(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, ApplicationClientType appClient, Environment environment) throws DeploymentException {
        GerApplicationClientType gerAppClient;
        XmlObject rawPlan = null;
        try {
            // load the geronimo-application-client.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    rawPlan = (XmlObject) plan;
                } else {
                    if (plan != null) {
                        rawPlan = XmlBeansUtil.parse((File) plan);
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/geronimo-application-client.xml");
                        rawPlan = XmlBeansUtil.parse(path, getClass().getClassLoader());
                    }
                }
            } catch (IOException e) {
                //exception means we create default
            }

            // if we got one extract the validate it otherwise create a default one
            if (rawPlan != null) {
                gerAppClient = (GerApplicationClientType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, GerApplicationClientDocument.type.getDocumentElementName(), GerApplicationClientType.type);
            } else {
                String path;
                if (standAlone) {
                    // default configId is based on the moduleFile name
                    path = new File(moduleFile.getName()).getName();
                } else {
                    // default configId is based on the module uri from the application.xml
                    path = targetPath;
                }
                gerAppClient = createDefaultPlan(path, appClient, standAlone, environment);
            }
        } catch (XmlException e) {
            throw new DeploymentException("Unable to parse application plan", e);
        }
        return gerAppClient;
    }

    private GerApplicationClientType createDefaultPlan(String name, ApplicationClientType appClient, boolean standAlone, Environment environment) {
        String id = appClient == null ? null : appClient.getId();
        if (id == null) {
            id = name;
            if (id.endsWith(".jar")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        GerApplicationClientType geronimoAppClient = GerApplicationClientType.Factory.newInstance();
        EnvironmentType clientEnvironmentType = geronimoAppClient.addNewClientEnvironment();
        EnvironmentType serverEnvironmentType = geronimoAppClient.addNewServerEnvironment();
        //TODO configid fill in environment with configids
        // set the parentId and configId
//        if (standAlone) {
//            geronimoAppClient.setClientConfigId(id);
//            geronimoAppClient.setConfigId(id + "/server");
//        } else {
//            geronimoAppClient.setClientConfigId(earConfigId.getPath() + "/" + id);
//             not used but we need to have a value
//            geronimoAppClient.setConfigId(id);
//        }
        return geronimoAppClient;
    }

    static ApplicationClientDocument convertToApplicationClientSchema(XmlObject xmlObject) throws XmlException {
        if (ApplicationClientDocument.type.equals(xmlObject.schemaType())) {
            XmlBeansUtil.validateDD(xmlObject);
            return (ApplicationClientDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor moveable = xmlObject.newCursor();
        String schemaLocationURL = "http://java.sun.com/xml/ns/javaee/application-client_5.xsd";
        String version = "5";
        try {
            cursor.toStartDoc();
            cursor.toFirstChild();
            if ("http://java.sun.com/xml/ns/j2ee".equals(cursor.getName().getNamespaceURI())) {
                SchemaConversionUtils.convertSchemaVersion(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version);
                XmlObject result = xmlObject.changeType(ApplicationClientDocument.type);
                XmlBeansUtil.validateDD(result);
                return (ApplicationClientDocument) result;
            }

            // otherwise assume DTD
            SchemaConversionUtils.convertToSchema(cursor, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version);
            cursor.toStartDoc();
            cursor.toChild(SchemaConversionUtils.JAVAEE_NAMESPACE, "application-client");
            cursor.toFirstChild();
            SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.JAVAEE_NAMESPACE, cursor, moveable);
        } finally {
            cursor.dispose();
            moveable.dispose();
        }
        XmlObject result = xmlObject.changeType(ApplicationClientDocument.type);
        if (result != null) {
            XmlBeansUtil.validateDD(result);
            return (ApplicationClientDocument) result;
        }
        XmlBeansUtil.validateDD(xmlObject);
        return (ApplicationClientDocument) xmlObject;

    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repositories) throws DeploymentException {
        // extract the app client jar file into a standalone packed jar file and add the contents to the output
        //This duplicates the copy in the app client's own configuration, made below.
        //this should really only be done if there's a manifest classpath reference to the app client jar by another
        //javaee module.
        JarFile moduleFile = module.getModuleFile();
        try {
            earContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName(), e);
        }
        AppClientModule appClientModule = (AppClientModule) module;
        appClientModule.setEarFile(earFile);
        //create the ear context for the app client.
        Environment clientEnvironment = appClientModule.getEnvironment();
//        if (!appClientModule.isStandAlone() || clientEnvironment.getConfigId() == null) {
//            Artifact earConfigId = earContext.getConfigID();
//            Artifact configId = new Artifact(earConfigId.getGroupId(), earConfigId.getArtifactId() + "_" + module.getTargetPath(), earConfigId.getVersion(), "car");
//            clientEnvironment.setConfigId(configId);
//        }

        File appClientDir;
        try {
            appClientDir = targetConfigurationStore.createNewConfigurationDir(clientEnvironment.getConfigId());
        } catch (ConfigurationAlreadyExistsException e) {
            throw new DeploymentException("Unable to create configuration directory for " + clientEnvironment.getConfigId(), e);
        }

        // construct the app client deployment context... this is the same class used by the ear context
        EARContext appClientDeploymentContext;
        try {

            appClientDeploymentContext = new EARContext(appClientDir,
                    null,
                    clientEnvironment,
                    ConfigurationModuleType.CAR,
                    earContext.getNaming(),
                    earContext.getConfigurationManager(),
                    null, //no server name needed on client
                    appClientModule.getAppClientName(),
                    transactionManagerObjectName,
                    connectionTrackerObjectName,
                    null,
                    null,
                    corbaGBeanObjectName,
                    earContext.getMessageDestinations());
            appClientModule.setEarContext(appClientDeploymentContext);
            appClientModule.setRootEarContext(appClientDeploymentContext);

            try {
                appClientDeploymentContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
            } catch (IOException e) {
                throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName(), e);
            }
            ClassPathList libClasspath = (ClassPathList) earContext.getGeneralData().get(ClassPathList.class);
            if (libClasspath != null) {
                for (String libEntryPath : libClasspath) {
                    try {
                        NestedJarFile library = new NestedJarFile(earFile, libEntryPath);
                        appClientDeploymentContext.addIncludeAsPackedJar(URI.create(libEntryPath), library);
                    } catch (IOException e) {
                        throw new DeploymentException("Could not add to app client library classpath: " + libEntryPath, e);
                    }
                }
            }
        } catch (DeploymentException e) {
            cleanupAppClientDir(appClientDir);
            throw e;
        }
        for (ConnectorModule connectorModule : appClientModule.getResourceModules()) {
            getConnectorModuleBuilder().installModule(connectorModule.getModuleFile(), appClientDeploymentContext, connectorModule, configurationStores, targetConfigurationStore, repositories);
        }

        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.installModule(module.getModuleFile(), appClientDeploymentContext, module, configurationStores, targetConfigurationStore, repositories);
        }
    }

    public void initContext(EARContext earContext, Module clientModule, ClassLoader cl) throws DeploymentException {
        namingBuilders.buildEnvironment(clientModule.getSpecDD(), clientModule.getVendorDD(), ((AppClientModule) clientModule).getEnvironment());

        AppClientModule appClientModule = ((AppClientModule) clientModule);
        for (ConnectorModule connectorModule : appClientModule.getResourceModules()) {
            getConnectorModuleBuilder().initContext(appClientModule.getEarContext(), connectorModule, cl);
        }
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.initContext(earContext, clientModule, cl);
        }
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader earClassLoader, Collection repositories) throws DeploymentException {

        AppClientModule appClientModule = (AppClientModule) module;
        JarFile moduleFile = module.getModuleFile();

        ApplicationClientType appClient = (ApplicationClientType) appClientModule.getSpecDD();
        GerApplicationClientType geronimoAppClient = (GerApplicationClientType) appClientModule.getVendorDD();

        // generate the object name for the app client
        AbstractName appClientModuleName = appClientModule.getModuleName();

        // create a gbean for the app client module and add it to the ear
        GBeanData appClientModuleGBeanData = new GBeanData(appClientModuleName, J2EEAppClientModuleImpl.GBEAN_INFO);
        try {
            appClientModuleGBeanData.setReferencePattern("J2EEServer", earContext.getServerName());
            if (!module.isStandAlone()) {
                appClientModuleGBeanData.setReferencePattern("J2EEApplication", earContext.getModuleName());
            }

        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
        }
        try {
            earContext.addGBean(appClientModuleGBeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add application client module gbean to configuration", e);
        }

        EARContext appClientDeploymentContext = appClientModule.getEarContext();
        //Share the ejb info with the ear.
        //TODO this might be too much, but I don't want to impose a dependency on geronimo-openejb to get
        //EjbModuleBuilder.EarData.class
        Map<Object, Object> generalData = earContext.getGeneralData();
        for (Map.Entry<Object, Object> entry : generalData.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof Class && ((Class) key).getName().equals("org.apache.geronimo.openejb.deployment.EjbModuleBuilder$EarData")) {
                appClientDeploymentContext.getGeneralData().put(key, entry.getValue());
                break;
            }
        }

        // Create a Module ID Builder defaulting to similar settings to use for any children we create
        ModuleIDBuilder idBuilder = new ModuleIDBuilder();
        idBuilder.setDefaultGroup(appClientModule.getEnvironment().getConfigId().getGroupId());
        idBuilder.setDefaultVersion(appClientModule.getEnvironment().getConfigId().getVersion());
        try {
            try {

                //register the message destinations in the app client ear context.
                namingBuilders.initContext(appClient, geronimoAppClient, appClientModule);
                // extract the client Jar file into a standalone packed jar file and add the contents to the output
                URI moduleBase = new URI(appClientModule.getTargetPath());
                try {
                    appClientDeploymentContext.addIncludeAsPackedJar(moduleBase, moduleFile);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName(), e);
                }

                // add manifest class path entries to the app client context
                addManifestClassPath(appClientDeploymentContext, appClientModule.getEarFile(), moduleFile, moduleBase);

                // get the classloader
                ClassLoader appClientClassLoader = appClientDeploymentContext.getClassLoader();

                // pop in all the gbeans declared in the geronimo app client file
                if (geronimoAppClient != null) {
                    serviceBuilder.build(geronimoAppClient, appClientDeploymentContext, appClientDeploymentContext);
                    //deploy the resource adapters specified in the geronimo-application.xml

                    for (ConnectorModule connectorModule : appClientModule.getResourceModules()) {
                        getConnectorModuleBuilder().addGBeans(appClientDeploymentContext, connectorModule, appClientClassLoader, repositories);
                    }
                }

                //Holder may be loaded in the "client" module classloader here, whereas
                //NamingBuilder.INJECTION_KEY.get(buildingContext) returns a Holder loaded in the j2ee-server classloader.
                Object holder;
                // add the app client static jndi provider
                //TODO track resource ref shared and app managed security
                AbstractName jndiContextName = earContext.getNaming().createChildName(appClientDeploymentContext.getModuleName(), "StaticJndiContext", "StaticJndiContext");
                GBeanData jndiContextGBeanData = new GBeanData(jndiContextName, StaticJndiContextPlugin.GBEAN_INFO);
                try {
                    Map<NamingBuilder.Key, Object> buildingContext = new HashMap<NamingBuilder.Key, Object>();
                    buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, jndiContextName);
                    Configuration localConfiguration = appClientDeploymentContext.getConfiguration();
                    Configuration remoteConfiguration = earContext.getConfiguration();

                    if (!appClient.getMetadataComplete()) {
                        // Create a classfinder and populate it for the naming builder(s). The absence of a
                        // classFinder in the module will convey whether metadata-complete is set
                        // (or not)
                        appClientModule.setClassFinder(createAppClientClassFinder(appClient, appClientModule));
                    }

                    namingBuilders.buildNaming(appClient, geronimoAppClient, appClientModule, buildingContext);

                    if (!appClient.getMetadataComplete()) {
                        appClient.setMetadataComplete(true);
                        module.setOriginalSpecDD(module.getSpecDD().toString());
                    }

                    appClientModuleGBeanData.setAttribute("deploymentDescriptor", appClientModule.getOriginalSpecDD());
                    holder = NamingBuilder.INJECTION_KEY.get(buildingContext);
                    jndiContextGBeanData.setAttribute("context", NamingBuilder.JNDI_KEY.get(buildingContext));
                } catch (DeploymentException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeploymentException("Unable to construct jndi context for AppClientModule GBean " +
                            appClientModule.getName(), e);
                }
                appClientDeploymentContext.addGBean(jndiContextGBeanData);

                // finally add the app client container
                AbstractName appClientContainerName = appClientDeploymentContext.getModuleName();
                GBeanData appClientContainerGBeanData = new GBeanData(appClientContainerName, AppClientContainer.GBEAN_INFO);
                try {
                    appClientContainerGBeanData.setAttribute("mainClassName", appClientModule.getMainClassName());
                    appClientContainerGBeanData.setAttribute("appClientModuleName", appClientModuleName);
                    String callbackHandlerClassName = null;
                    if (appClient.isSetCallbackHandler()) {
                        callbackHandlerClassName = appClient.getCallbackHandler().getStringValue().trim();
                    }
                    if (geronimoAppClient.isSetCallbackHandler()) {
                        callbackHandlerClassName = geronimoAppClient.getCallbackHandler().trim();
                    }
                    String realmName = null;
                    if (geronimoAppClient.isSetRealmName()) {
                        realmName = geronimoAppClient.getRealmName().trim();
                    }
                    if (callbackHandlerClassName != null && realmName == null) {
                        throw new DeploymentException("You must specify a realm name with the callback handler");
                    }
                    if (realmName != null) {
                        appClientContainerGBeanData.setAttribute("realmName", realmName);
                        appClientContainerGBeanData.setAttribute("callbackHandlerClassName", callbackHandlerClassName);
                    } else if (geronimoAppClient.isSetDefaultSubject()) {
                        GerSubjectInfoType subjectInfoType = geronimoAppClient.getDefaultSubject();
                        SubjectInfo subjectInfo = buildSubjectInfo(subjectInfoType);
                        appClientContainerGBeanData.setAttribute("defaultSubject", subjectInfo);
                        appClientContainerGBeanData.setReferencePattern("CredentialStore", credentialStoreName);
                    } else if (earContext.getSecurityConfiguration() != null) {
                        //beware a linkage error if we cast this to SubjectInfo
                        String realm = ((SecurityConfiguration) earContext.getSecurityConfiguration()).getDefaultSubjectRealm();
                        String id = ((SecurityConfiguration) earContext.getSecurityConfiguration()).getDefaultSubjectId();
                        if (realm != null) {
                            SubjectInfo subjectInfo = new SubjectInfo(realm, id);
                            appClientContainerGBeanData.setAttribute("defaultSubject", subjectInfo);
                            appClientContainerGBeanData.setReferencePattern("CredentialStore", credentialStoreName);
                        }
                    }
                    appClientContainerGBeanData.setReferencePattern("JNDIContext", jndiContextName);
                    appClientContainerGBeanData.setAttribute("holder", holder);

                } catch (Exception e) {
                    throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
                }
                appClientDeploymentContext.addGBean(appClientContainerGBeanData);

                //TODO this may definitely not be the best place for this!
                for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                    mbe.addGBeans(appClientDeploymentContext, appClientModule, appClientClassLoader, repositories);
                }

                // get the configuration data
                earContext.addAdditionalDeployment(appClientDeploymentContext.getConfigurationData());
            } finally {
                if (appClientDeploymentContext != null) {
                    try {
                        appClientDeploymentContext.close();
                    } catch (IOException e) {
                        //nothing we can do
                    }
                }
            }

        } catch (Throwable e) {
            File appClientDir = appClientDeploymentContext.getBaseDir();
            cleanupAppClientDir(appClientDir);
            if (e instanceof Error) {
                throw (Error) e;
            } else if (e instanceof DeploymentException) {
                throw (DeploymentException) e;
            } else if (e instanceof Exception) {
                throw new DeploymentException(e);
            }
            throw new Error(e);
        }
    }


    private ClassFinder createAppClientClassFinder(ApplicationClientType appClient, AppClientModule appClientModule) throws DeploymentException {

        //------------------------------------------------------------------------------------
        // Find the list of classes from the application-client.xml we want to search for
        // annotations in
        //------------------------------------------------------------------------------------
        List<Class> classes = new ArrayList<Class>();

        // Get the classloader from the module's EARContext
        ClassLoader classLoader = appClientModule.getEarContext().getClassLoader();

        // Get the main class from the module
        String mainClass = appClientModule.getMainClassName();
        Class<?> mainClas;
        try {
            mainClas = classLoader.loadClass(mainClass);
        }
        catch (ClassNotFoundException e) {
            throw new DeploymentException("AppClientModuleBuilder: Could not load main class: " + mainClass, e);
        }
        while (mainClas != null && mainClas != Object.class) {
            classes.add(mainClas);
            mainClas = mainClas.getSuperclass();
        }

        // Get the callback-handler from the deployment descriptor
        if (appClient.isSetCallbackHandler()) {
            FullyQualifiedClassType cls = appClient.getCallbackHandler();
            Class<?> clas;
            try {
                clas = classLoader.loadClass(cls.getStringValue().trim());
            }
            catch (ClassNotFoundException e) {
                throw new DeploymentException("AppClientModuleBuilder: Could not load callback-handler class: " + cls.getStringValue(), e);
            }
            classes.add(clas);
        }

        return new ClassFinder(classes);
    }

    private SubjectInfo buildSubjectInfo(GerSubjectInfoType defaultSubject) {
        String realmName = defaultSubject.getRealm().trim();
        String id = defaultSubject.getId().trim();
        return new SubjectInfo(realmName, id);
    }

    public String getSchemaNamespace() {
        return GERAPPCLIENT_NAMESPACE;
    }

    public void addManifestClassPath(DeploymentContext deploymentContext, JarFile earFile, JarFile jarFile, URI jarFileLocation) throws DeploymentException {
        Manifest manifest;
        try {
            manifest = jarFile.getManifest();
        } catch (IOException e) {
            throw new DeploymentException("Could not read manifest: " + jarFileLocation, e);
        }

        if (manifest == null) {
            return;
        }
        String manifestClassPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        if (manifestClassPath == null) {
            return;
        }

        for (StringTokenizer tokenizer = new StringTokenizer(manifestClassPath, " "); tokenizer.hasMoreTokens();) {
            String path = tokenizer.nextToken();

            URI pathUri;
            try {
                pathUri = new URI(path);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid manifest classpath entry: jarFile=" + jarFileLocation + ", path=" + path, e);
            }

            if (!pathUri.getPath().endsWith(".jar")) {
                throw new DeploymentException("Manifest class path entries must end with the .jar extension (JAVAEE 5 Section 8.2): jarFile=" + jarFileLocation + ", path=" + path);
            }
            if (pathUri.isAbsolute()) {
                throw new DeploymentException("Manifest class path entries must be relative (JAVAEE 5 Section 8.2): jarFile=" + jarFileLocation + ", path=" + path);
            }

            // determine the target file
            URI classPathJarLocation = jarFileLocation.resolve(pathUri);
            File classPathFile = deploymentContext.getTargetFile(classPathJarLocation);

            // we only recuse if the path entry is not already in the output context
            // this will work for all current cases, but may not work in the future
            if (!classPathFile.exists()) {
                // check if the path exists in the earFile
                ZipEntry entry = earFile.getEntry(classPathJarLocation.getPath());
                if (entry == null) {
                    throw new DeploymentException("Cound not find manifest class path entry: jarFile=" + jarFileLocation + ", path=" + path);
                }

                try {
                    // copy the file into the output context
                    deploymentContext.addFile(classPathJarLocation, earFile, entry);
                } catch (IOException e) {
                    throw new DeploymentException("Cound not copy manifest class path entry into configuration: jarFile=" + jarFileLocation + ", path=" + path, e);
                }

                JarFile classPathJarFile;
                try {
                    classPathJarFile = new JarFile(classPathFile);
                } catch (IOException e) {
                    throw new DeploymentException("Manifest class path entries must be a valid jar file (JAVAEE 5 Section 8.2): jarFile=" + jarFileLocation + ", path=" + path, e);
                }

                // add the client jars of this class path jar
                addManifestClassPath(deploymentContext, earFile, classPathJarFile, classPathJarLocation);
            }
        }
    }

    private boolean cleanupAppClientDir(File configurationDir) {
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(AppClientModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultClientEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("defaultServerEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("transactionManagerObjectName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("connectionTrackerObjectName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("corbaGBeanObjectName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("credentialStoreName", AbstractNameQuery.class, true);
        infoBuilder.addReference("Repositories", Repository.class, "Repository");
        infoBuilder.addReference("ConnectorModuleBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("NamingBuilders", NamingBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ModuleBuilderExtensions", ModuleBuilderExtension.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ClientArtifactResolver", ArtifactResolver.class, "ArtifactResolver");

        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{"transactionManagerObjectName",
                "connectionTrackerObjectName",
                "corbaGBeanObjectName",
                "credentialStoreName",
                "Repositories",
                "ConnectorModuleBuilder",
                "ServiceBuilders",
                "NamingBuilders",
                "ModuleBuilderExtensions",
                "ClientArtifactResolver",
                "defaultClientEnvironment",
                "defaultServerEnvironment",
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
