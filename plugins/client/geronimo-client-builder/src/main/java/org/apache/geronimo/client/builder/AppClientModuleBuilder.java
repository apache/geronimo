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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import javax.xml.bind.JAXBException;
import org.apache.geronimo.client.AppClientContainer;
import org.apache.geronimo.client.StaticJndiContextPlugin;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.deployment.AppClientModule;
import org.apache.geronimo.j2ee.deployment.ApplicationInfo;
import org.apache.geronimo.j2ee.deployment.ConnectorModule;
import org.apache.geronimo.j2ee.deployment.CorbaGBeanNameSource;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.NamingBuilderCollection;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.ApplicationJndi;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.j2ee.management.impl.J2EEAppClientModuleImpl;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.kernel.util.NestedJarFile;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.SubjectInfo;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientDocument;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientType;
import org.apache.geronimo.xbeans.geronimo.client.GerResourceType;
import org.apache.geronimo.xbeans.geronimo.security.GerSubjectInfoType;
import org.apache.openejb.jee.ApplicationClient;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @version $Rev:385232 $ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class AppClientModuleBuilder implements ModuleBuilder, CorbaGBeanNameSource, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(AppClientModuleBuilder.class);
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
    private final SingleElementCollection<ModuleBuilder> connectorModuleBuilder;
    private final NamespaceDrivenBuilderCollection serviceBuilder;
    private final NamingBuilderCollection namingBuilders;
    private final Collection<ModuleBuilderExtension> moduleBuilderExtensions;

    private final Collection<Repository> repositories;

    private final ArtifactResolver clientArtifactResolver;
    private final URI uri;
    private AbstractNameQuery globalContextAbstractName;

    public AppClientModuleBuilder(Environment defaultClientEnvironment,
                                  Environment defaultServerEnvironment,
                                  AbstractNameQuery transactionManagerObjectName,
                                  AbstractNameQuery connectionTrackerObjectName,
                                  AbstractNameQuery corbaGBeanObjectName,
                                  AbstractNameQuery credentialStoreName,
                                  AbstractNameQuery globalContextAbstractName,
                                  Collection<Repository> repositories,
                                  ModuleBuilder connectorModuleBuilder,
                                  NamespaceDrivenBuilder serviceBuilder,
                                  Collection<NamingBuilder> namingBuilders,
                                  Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                                  ArtifactResolver clientArtifactResolver,
                                  String host,
                                  int port) throws URISyntaxException{
        this(defaultClientEnvironment,
                defaultServerEnvironment,
                transactionManagerObjectName,
                connectionTrackerObjectName,
                corbaGBeanObjectName,
                credentialStoreName,
                globalContextAbstractName,
                repositories,
                new SingleElementCollection<ModuleBuilder>(connectorModuleBuilder),
                serviceBuilder == null ? Collections.<NamespaceDrivenBuilder>emptySet() : Collections.singleton(serviceBuilder),
                namingBuilders == null ? Collections.<NamingBuilder>emptySet() : namingBuilders,
                moduleBuilderExtensions,
                clientArtifactResolver,
                host,
                port);
    }

    public AppClientModuleBuilder(@ParamAttribute(name = "transactionManagerObjectName") AbstractNameQuery transactionManagerObjectName,
                                  @ParamAttribute(name = "connectionTrackerObjectName") AbstractNameQuery connectionTrackerObjectName,
                                  @ParamAttribute(name = "corbaGBeanObjectName") AbstractNameQuery corbaGBeanObjectName,
                                  @ParamAttribute(name = "credentialStoreName") AbstractNameQuery credentialStoreName,
                                  @ParamAttribute(name = "globalContextAbstractName") AbstractNameQuery globalContextAbstractName,
                                  @ParamReference(name = "Repositories", namingType = "Repository") Collection<Repository> repositories,
                                  @ParamReference(name = "ConnectorModuleBuilder", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilder> connectorModuleBuilder,
                                  @ParamReference(name = "ServiceBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<NamespaceDrivenBuilder> serviceBuilder,
                                  @ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<NamingBuilder> namingBuilders,
                                  @ParamReference(name = "ModuleBuilderExtensions", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                                  @ParamReference(name = "ClientArtifactResolver", namingType = "ArtifactResolver") ArtifactResolver clientArtifactResolver,
                                  @ParamAttribute(name = "defaultClientEnvironment") Environment defaultClientEnvironment,
                                  @ParamAttribute(name = "defaultServerEnvironment") Environment defaultServerEnvironment,
                                  @ParamAttribute(name = "host") String host,
                                  @ParamAttribute(name = "port") int port) throws URISyntaxException{
        this(defaultClientEnvironment,
                defaultServerEnvironment,
                transactionManagerObjectName,
                connectionTrackerObjectName,
                corbaGBeanObjectName,
                credentialStoreName,
                globalContextAbstractName,
                repositories,
                new SingleElementCollection<ModuleBuilder>(connectorModuleBuilder),
                serviceBuilder,
                namingBuilders,
                moduleBuilderExtensions,
                clientArtifactResolver,
                host,
                port);
    }

    private AppClientModuleBuilder(Environment defaultClientEnvironment,
                                   Environment defaultServerEnvironment,
                                   AbstractNameQuery transactionManagerObjectName,
                                   AbstractNameQuery connectionTrackerObjectName,
                                   AbstractNameQuery corbaGBeanObjectName,
                                   AbstractNameQuery credentialStoreName,
                                   AbstractNameQuery globalContextAbstractName,
                                   Collection<Repository> repositories,
                                   SingleElementCollection<ModuleBuilder> connectorModuleBuilder,
                                   Collection<NamespaceDrivenBuilder> serviceBuilder,
                                   Collection<NamingBuilder> namingBuilders,
                                   Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                                   ArtifactResolver clientArtifactResolver,
                                   String host,
                                   int port) throws URISyntaxException {
        this.defaultClientEnvironment = defaultClientEnvironment;
        this.defaultServerEnvironment = defaultServerEnvironment;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.transactionManagerObjectName = transactionManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.credentialStoreName = credentialStoreName;
        this.globalContextAbstractName = globalContextAbstractName;
        this.repositories = repositories;
        this.connectorModuleBuilder = connectorModuleBuilder;
        this.serviceBuilder = new NamespaceDrivenBuilderCollection(serviceBuilder);
        this.namingBuilders = new NamingBuilderCollection(namingBuilders);
        this.moduleBuilderExtensions = moduleBuilderExtensions;
        this.clientArtifactResolver = clientArtifactResolver;
        if (host != null) {
            uri = new URI("ejbd", null, host, port, null, null, null);
        } else {
            uri = null;
        }
    }

    @Override
    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    @Override
    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    @Override
    public void doFail() {
        doStop();
    }

    @Override
    public AbstractNameQuery getCorbaGBeanName() {
        return corbaGBeanObjectName;
    }

    private ModuleBuilder getConnectorModuleBuilder() {
        return connectorModuleBuilder.getElement();
    }

    @Override
    public Module createModule(Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return null;
    }

    @Override
    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "app-client.jar", null, null, null, naming, idBuilder);
    }

    @Override
    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, Module parentModule, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, parentModule, naming, idBuilder);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, Module parentModule, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null : "moduleFile is null";
        assert targetPath != null : "targetPath is null";
        assert !targetPath.endsWith("/") : "targetPath must not end with a '/'";
        assert (parentModule == null) == (earEnvironment == null) : "if earName is not null you must supply earEnvironment as well";

        boolean standAlone = earEnvironment == null;

        // get the app client main class
        String mainClass;
        try {
            Manifest manifest = moduleFile.getManifest();
            if (manifest == null) {
                // if there is no manifest, it's not an app client module
                return null;
            }
            mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClass == null) {
                // if there is no Main-Class header in manifest, it's not an app client module
                return null;
            }
            String classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if (standAlone && classPath != null) {
                throw new DeploymentException("Manifest class path entry is not allowed in a standalone jar (JAVAEE 5 Section 8.2)");
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not get manifest from app client module: " + moduleFile.getName(), e);
        }

        String specDD = null;
        ApplicationClient appClient = null;
        try {
            if (specDDUrl == null) {
                specDDUrl = JarUtils.createJarURL(moduleFile, "META-INF/application-client.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = JarUtils.readAll(specDDUrl);
        } catch (Exception e) {
            //construct a default spec dd
            appClient = new ApplicationClient();
            try {
                specDD = JaxbJavaee.marshal(ApplicationClient.class, appClient);
            } catch (JAXBException e1) {
                //??
            }
        }

        if (appClient == null) {
            //we found application-client.xml, if it won't parse it's an error.
            try {
                // parse it
                InputStream in = specDDUrl.openStream();
                try {
                    appClient = (ApplicationClient) JaxbJavaee.unmarshalJavaee(ApplicationClient.class, in);
                } finally {
                    in.close();
                }
            } catch (Exception e) {
                throw new DeploymentException("Unable to parse application-client.xml", e);
            }
        }

        // parse vendor dd
        GerApplicationClientType gerAppClient = getGeronimoAppClient(plan, moduleFile, standAlone, targetPath, appClient, earEnvironment);


        EnvironmentType clientEnvironmentType = gerAppClient.getClientEnvironment();
        Environment clientEnvironment = EnvironmentBuilder.buildEnvironment(clientEnvironmentType, defaultClientEnvironment);
        if (standAlone) {
            String name = new File(moduleFile.getName()).getName();
            idBuilder.resolve(clientEnvironment, name + "_" + name, "car");
        } else {
            Artifact earConfigId = earEnvironment.getConfigId();
            idBuilder.resolve(clientEnvironment, earConfigId.getArtifactId() + "_" + targetPath, "car");
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
            idBuilder.resolve(serverEnvironment, new File(moduleFile.getName()).getName(), "car");
        }

        AbstractName earName;
        if (parentModule == null) {
            earName = naming.createRootName(serverEnvironment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
        } else {
            earName = parentModule.getModuleName();
        }

        //always use the artifactId of the app client as the name component of the module name (on the server).
        AbstractName moduleName = naming.createChildName(earName, clientEnvironment.getConfigId().toString(), NameFactory.APP_CLIENT_MODULE);
        AbstractName clientBaseName = naming.createRootName(clientEnvironment.getConfigId(), clientEnvironment.getConfigId().toString(), NameFactory.J2EE_APPLICATION);

        // Create the AnnotatedApp interface for the AppClientModule
//        AnnotatedApplicationClient annotatedApplicationClient = new AnnotatedApplicationClient(appClient, mainClass);

        String name = null;
        if (appClient.getModuleName() != null) {
            name = appClient.getModuleName().trim();
        } else if (standAlone) {
            name = FileUtils.removeExtension(new File(moduleFile.getName()).getName(), ".jar");
        } else {
            name = FileUtils.removeExtension(targetPath, ".jar");
        }
        Map<JndiKey, Map<String, Object>> jndiContext = Module.share(Module.APP, parentModule == null? null: parentModule.getJndiContext());

        AppClientModule module = new AppClientModule(standAlone,
                moduleName,
                name,
                clientBaseName,
                serverEnvironment,
                clientEnvironment,
                moduleFile,
                targetPath,
                appClient,
                mainClass,
                gerAppClient,
                specDD,
                jndiContext,
                parentModule);

        //start installing the resource adapters in the client.
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
                    JarUtils.assertTempFile();
                    connectorFile = new NestedJarFile(moduleFile, path);
                } catch (IOException e) {
                    throw new DeploymentException("Could not locate connector inside ear", e);
                }
            }
            XmlObject connectorPlan = resource.getConnector();
            ConnectorModule connectorModule = (ConnectorModule) getConnectorModuleBuilder().createModule(connectorPlan, connectorFile, path, null, clientEnvironment, null, module, naming, idBuilder);
            module.getModules().add(connectorModule);
        }

        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, plan, moduleFile, targetPath, specDDUrl, clientEnvironment, null, earName, naming, idBuilder);
        }
        if (standAlone) {
            Map<JndiKey, Map<String, Object>> appJndiContext = Module.share(Module.APP, module.getJndiContext());

            ApplicationInfo appInfo = new ApplicationInfo(ConfigurationModuleType.CAR,
                    serverEnvironment,
                    earName,
                    name,
                    null,
                    null,
                    null,
                    null,
                    appJndiContext);
            appInfo.getModules().add(module);
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

    GerApplicationClientType getGeronimoAppClient(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, ApplicationClient appClient, Environment environment) throws DeploymentException {
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
                        URL path = JarUtils.createJarURL(moduleFile, "META-INF/geronimo-application-client.xml");
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

    private GerApplicationClientType createDefaultPlan(String name, ApplicationClient appClient, boolean standAlone, Environment environment) {
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

        try {
            targetConfigurationStore.createNewConfigurationDir(clientEnvironment.getConfigId());
        } catch (ConfigurationAlreadyExistsException e) {
            throw new DeploymentException("Unable to create configuration directory for " + clientEnvironment.getConfigId(), e);
        }

        // construct the app client deployment context... this is the same class used by the ear context
        EARContext appClientDeploymentContext = null;

        try {
            //Use a temporary folder to hold the extracted files for analysis use
            File tempDirectory = FileUtils.createTempDir();

            appClientDeploymentContext = new EARContext(tempDirectory,
                    null,
                    clientEnvironment,
                    ConfigurationModuleType.CAR,
                    appClientModule.getAppClientName(),
                    transactionManagerObjectName,
                    connectionTrackerObjectName,
                    corbaGBeanObjectName,
                    earContext);
            appClientModule.setEarContext(appClientDeploymentContext);
            appClientModule.setRootEarContext(earContext);

            if (module.getParentModule() != null) {
                Collection<String> libClasspath = module.getParentModule().getClassPath();
                for (String libEntryPath : libClasspath) {
                    if (libEntryPath.endsWith(".jar")) {
                        try {
                            JarUtils.assertTempFile();
                            NestedJarFile library = new NestedJarFile(earFile, libEntryPath);
                            appClientDeploymentContext.addIncludeAsPackedJar(URI.create(libEntryPath), library);
                        } catch (IOException e) {
                            throw new DeploymentException("Could not add to app client library classpath: " + libEntryPath, e);
                        }
                    }
                }
                module.getClassPath().addAll(libClasspath);
                Enumeration<JarEntry> ear_entries = earFile.entries();
                //Copy non archive files from ear file to appclient configuration. These
                // files are needed when caculating dir classpath in manifest.
                while (ear_entries.hasMoreElements()) {
                    ZipEntry ear_entry = ear_entries.nextElement();
                    URI targetPath = module.getParentModule().resolve(ear_entry.getName());
                    if (!ear_entry.getName().endsWith(".jar") && !ear_entry.getName().endsWith(".war") && !ear_entry.getName().endsWith(".rar") && !ear_entry.getName().startsWith("META-INF")) {
                        appClientDeploymentContext.addFile(targetPath, earFile, ear_entry);
                    }
                }
            }
            Collection<String> appClientModuleClasspaths = module.getClassPath();
            try {
                // extract the client Jar file into a standalone packed jar file and add the contents to the output
                URI moduleBase = new URI(module.getTargetPath());
                appClientDeploymentContext.addIncludeAsPackedJar(moduleBase, moduleFile);
                // add manifest class path entries to the app client context
                addManifestClassPath(appClientDeploymentContext, appClientModule.getEarFile(), moduleFile, moduleBase);
            } catch (IOException e) {
                throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName(), e);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to get app client module base URI " + module.getTargetPath(), e);
            }

            if (module.getParentModule() != null) {
                appClientModuleClasspaths.add(module.getTargetPath());
                EARContext moduleContext = module.getEarContext();
                Collection<String> moduleLocations = module.getParentModule().getModuleLocations();
                URI baseUri = URI.create(module.getTargetPath());
                moduleContext.getCompleteManifestClassPath(module.getDeployable(), baseUri, URI.create("."), appClientModuleClasspaths, moduleLocations);

                for (String classpath : appClientModuleClasspaths) {
                    appClientDeploymentContext.addToClassPath(classpath);

                    //Copy needed jar from ear to appclient configuration.
                    if (classpath.endsWith(".jar")) {
                        JarUtils.assertTempFile();
                        NestedJarFile library = new NestedJarFile(earFile, classpath);
                        appClientDeploymentContext.addIncludeAsPackedJar(URI.create(classpath), library);
                    }
                }
            }
            for (Module connectorModule : appClientModule.getModules()) {
                if (connectorModule instanceof ConnectorModule) {
                    getConnectorModuleBuilder().installModule(connectorModule.getModuleFile(), appClientDeploymentContext, connectorModule, configurationStores, targetConfigurationStore, repositories);
                }
            }
            for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                mbe.installModule(module.getModuleFile(), appClientDeploymentContext, module, configurationStores, targetConfigurationStore, repositories);
            }
        } catch (DeploymentException e) {
            closeAppClientContextOnException(appClientDeploymentContext);
            throw e;
        } catch (IOException e) {
            closeAppClientContextOnException(appClientDeploymentContext);
            throw new DeploymentException(e);
        }
    }

    public void initContext(EARContext earContext, Module clientModule, Bundle bundle) throws DeploymentException {
        try {
            AppClientModule appClientModule = ((AppClientModule) clientModule);
            namingBuilders.buildEnvironment(appClientModule.getSpecDD(), appClientModule.getVendorDD(), clientModule.getEnvironment());

            for (Module connectorModule : appClientModule.getModules()) {
                if (connectorModule instanceof ConnectorModule) {
                    getConnectorModuleBuilder().initContext(appClientModule.getEarContext(), connectorModule, bundle);
                }
            }
            for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                mbe.initContext(earContext, clientModule, bundle);
            }
        } catch (DeploymentException e) {
            closeAppClientContextOnException(clientModule.getEarContext());
            throw e;
        } catch (Exception e) {
            closeAppClientContextOnException(clientModule.getEarContext());
            throw new DeploymentException(e);
        }
    }

    public void addGBeans(EARContext earContext, Module module, Bundle earBundle, Collection repositories) throws DeploymentException {

        AbstractName appJndiName = module.getEarContext().getNaming().createChildName(earContext.getModuleName(), "ApplicationJndi", "ApplicationJndi");
        module.getEarContext().getGeneralData().put(EARContext.APPLICATION_JNDI_NAME_KEY, appJndiName);

        GBeanData appContexts = new GBeanData(appJndiName, ApplicationJndi.class);
        appContexts.setAttribute("globalContextSegment", module.getJndiContext().get(JndiScope.global));
        appContexts.setAttribute("applicationContextMap", module.getJndiContext().get(JndiScope.app));
        appContexts.setReferencePattern("GlobalContext", globalContextAbstractName);
        try {
            module.getEarContext().addGBean(appContexts);
        } catch (GBeanAlreadyExistsException e1) {
            throw new DeploymentException(e1);
        }

        AppClientModule appClientModule = (AppClientModule) module;
        JarFile moduleFile = module.getModuleFile();

        ApplicationClient appClient = appClientModule.getSpecDD();
        GerApplicationClientType geronimoAppClient = (GerApplicationClientType) appClientModule.getVendorDD();
        //First, the silly gbean on the server that says there's an app client
        // generate the object name for the app client
        AbstractName appClientModuleName = appClientModule.getModuleName();

        // create a gbean for the app client module and add it to the ear
        GBeanData appClientModuleGBeanData = new GBeanData(appClientModuleName, J2EEAppClientModuleImpl.class);
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

        //Now, the gbeans for the actual remote app client
        EARContext appClientDeploymentContext = appClientModule.getEarContext();
        //Share the ejb info with the ear.
        //TODO this might be too much, but I don't want to impose a dependency on geronimo-openejb to get
        //EjbModuleBuilder.EarData.class
        Map<EARContext.Key, Object> generalData = earContext.getGeneralData();
        for (Map.Entry<EARContext.Key, Object> entry : generalData.entrySet()) {
            EARContext.Key key = entry.getKey();
            if (key.getClass().getName().startsWith("org.apache.geronimo.openejb.deployment.EjbModuleBuilder$EarData")) {
                appClientDeploymentContext.getGeneralData().put(key, entry.getValue());
                break;
            }
        }

        //Share the messageDestination info with the ear
        if (appClientDeploymentContext.getMessageDestinations() != null && earContext.getMessageDestinations() != null) {
            appClientDeploymentContext.getMessageDestinations().putAll(earContext.getMessageDestinations());
        }

        try {
            try {

                //register the message destinations in the app client ear context.
                namingBuilders.initContext(appClient, geronimoAppClient, appClientModule);

                // get the classloader
                Bundle appClientClassBundle = appClientDeploymentContext.getDeploymentBundle();

                // pop in all the gbeans declared in the geronimo app client file
                if (geronimoAppClient != null) {
                    serviceBuilder.build(geronimoAppClient, appClientDeploymentContext, appClientDeploymentContext);
                    //deploy the resource adapters specified in the geronimo-application.xml

                    for (Module connectorModule : appClientModule.getModules()) {
                        if (connectorModule instanceof ConnectorModule) {
                            getConnectorModuleBuilder().addGBeans(appClientDeploymentContext, connectorModule, appClientClassBundle, repositories);
                        }
                    }
                }

                //Holder may be loaded in the "client" module classloader here, whereas
                //NamingBuilder.INJECTION_KEY.get(buildingContext) returns a Holder loaded in the j2ee-server classloader.
                Object holder;
                // add the app client static jndi provider
                //TODO track resource ref shared and app managed security
                AbstractName jndiContextName = earContext.getNaming().createChildName(appClientDeploymentContext.getModuleName(), "StaticJndiContext", "StaticJndiContext");
                GBeanData jndiContextGBeanData = new GBeanData(jndiContextName, StaticJndiContextPlugin.class);
                jndiContextGBeanData.setAttribute("uri", uri);
                try {
                    Map<EARContext.Key, Object> buildingContext = new HashMap<EARContext.Key, Object>();
                    buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, jndiContextName);
                    Configuration localConfiguration = appClientDeploymentContext.getConfiguration();
                    Configuration remoteConfiguration = earContext.getConfiguration();

                    if (!appClient.isMetadataComplete()) {
                        // Create a classfinder and populate it for the naming builder(s). The absence of a
                        // classFinder in the module will convey whether metadata-complete is set
                        // (or not)
                        appClientModule.setClassFinder(createAppClientClassFinder(appClient, appClientModule));
                    }

                    if (appClient.getMainClass() == null) {
                        //LifecycleMethodBuilder.buildNaming() need the main class info in appClient specDD.
                        appClient.setMainClass(appClientModule.getMainClassName());
                    }

                    String moduleName = module.getName();

                    if (earContext.getSubModuleNames().contains(moduleName)) {
                        log.warn("Duplicated moduleName: '" + moduleName + "' is found ! deployer will rename it to: '" + moduleName
                                + "_duplicated' , please check your modules in application to make sure they don't share the same name");
                        moduleName = moduleName + "_duplicated";
                        earContext.getSubModuleNames().add(moduleName);
                    }

                    earContext.getSubModuleNames().add(moduleName);
                    appClientModule.getJndiScope(JndiScope.module).put("module/ModuleName", moduleName);

                    namingBuilders.buildNaming(appClient, geronimoAppClient, appClientModule, buildingContext);
                    if (!appClient.isMetadataComplete()) {
                        appClient.setMetadataComplete(true);
                        module.setOriginalSpecDD(module.getSpecDD().toString());
                    }
                    //n the server
                    appClientModuleGBeanData.setAttribute("deploymentDescriptor", appClientModule.getOriginalSpecDD());
                    //in the app client
                    holder = NamingBuilder.INJECTION_KEY.get(buildingContext);
                    jndiContextGBeanData.setAttribute("context", appClientModule.getJndiContext());
                } catch (DeploymentException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeploymentException("Unable to construct jndi context for AppClientModule GBean " + appClientModule.getName(), e);
                }
                appClientDeploymentContext.addGBean(jndiContextGBeanData);

                // finally add the app client container
                AbstractName appClientContainerName = appClientDeploymentContext.getModuleName();
                GBeanData appClientContainerGBeanData = new GBeanData(appClientContainerName, AppClientContainer.class);
                try {
                    appClientContainerGBeanData.setAttribute("mainClassName", appClientModule.getMainClassName());
                    appClientContainerGBeanData.setAttribute("appClientModuleName", appClientModuleName);
                    String callbackHandlerClassName = null;
                    if (appClient.getCallbackHandler() != null) {
                        callbackHandlerClassName = appClient.getCallbackHandler().trim();
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
                        log.warn("Configuration of app client default subject from ear security configuration no longer supported.");
                        //beware a linkage error if we cast this to SubjectInfo
                        //String realm = ((SecurityConfiguration) earContext.getSecurityConfiguration()).getDefaultSubjectRealm();
                        //String id = ((SecurityConfiguration) earContext.getSecurityConfiguration()).getDefaultSubjectId();
                        //if (realm != null) {
                        //  SubjectInfo subjectInfo = new SubjectInfo(realm, id);
                        //  appClientContainerGBeanData.setAttribute("defaultSubject", subjectInfo);
                        //  appClientContainerGBeanData.setReferencePattern("CredentialStore", credentialStoreName);
                        //}
                    }
                    appClientContainerGBeanData.setReferencePattern("JNDIContext", jndiContextName);
                    appClientContainerGBeanData.setAttribute("holder", holder);

                } catch (Exception e) {
                    throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
                }
                appClientDeploymentContext.addGBean(appClientContainerGBeanData);

                //TODO this may definitely not be the best place for this!
                for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
                    mbe.addGBeans(appClientDeploymentContext, appClientModule, appClientClassBundle, repositories);
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

    private static Map<String, Object> getJndiContext(Map<JndiKey, Map<String, Object>> contexts, JndiScope scope) {
        Map<String, Object> context = contexts.get(scope);
        if (context == null) {
            context = new HashMap<String, Object>();
            contexts.put(scope, context);
        }
        return context;
    }

    private ClassFinder createAppClientClassFinder(ApplicationClient appClient, AppClientModule appClientModule) throws DeploymentException {

        //------------------------------------------------------------------------------------
        // Find the list of classes from the application-client.xml we want to search for
        // annotations in
        //------------------------------------------------------------------------------------
        List<Class<?>> classes = new ArrayList<Class<?>>();

        // Get the classloader from the module's EARContext
        Bundle bundle = appClientModule.getEarContext().getDeploymentBundle();

        // Get the main class from the module
        String mainClass = appClientModule.getMainClassName();
        Class<?> mainClas;
        try {
            mainClas = bundle.loadClass(mainClass);
        }
        catch (ClassNotFoundException e) {
            throw new DeploymentException("AppClientModuleBuilder: Could not load main class: " + mainClass, e);
        }
        while (mainClas != null && mainClas != Object.class) {
            classes.add(mainClas);
            mainClas = mainClas.getSuperclass();
        }

        // Get the callback-handler from the deployment descriptor
        if (appClient.getCallbackHandler() != null) {
            String cls = appClient.getCallbackHandler();
            Class<?> clas;
            try {
                clas = bundle.loadClass(cls.trim());
            }
            catch (ClassNotFoundException e) {
                throw new DeploymentException("AppClientModuleBuilder: Could not load callback-handler class: " + cls, e);
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

            if (pathUri.isAbsolute()) {
                throw new DeploymentException("Manifest class path entries must be relative (JAVAEE 5 Section 8.2): jarFile=" + jarFileLocation + ", path=" + path);
            }

            Enumeration<JarEntry> ear_entries = earFile.entries();
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
                    if (!entry.getName().endsWith(".jar")) {

                        while (ear_entries.hasMoreElements()) {
                            ZipEntry ear_entry = ear_entries.nextElement();
                            URI targetPath = jarFileLocation.resolve(ear_entry.getName());
                            if (ear_entry.getName().startsWith(classPathJarLocation.getPath()))
                                deploymentContext.addFile(targetPath, earFile, ear_entry);
                        }
                    } else {
                        // copy the file into the output context
                        deploymentContext.addFile(classPathJarLocation, earFile, entry);
                    }
                } catch (IOException e) {
                    throw new DeploymentException("Cound not copy manifest class path entry into configuration: jarFile=" + jarFileLocation + ", path=" + path, e);
                }

                JarFile classPathJarFile;
                if (classPathFile.getName().endsWith(".jar")) {
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
    }

    /**
     * Close the application client context if any exception is thrown in the deployment process
     * This method should only be called while any error occurs, as it will clean the temporary directory.
     * In a successful deployment, the temporary directory will be used to package the final application to repository directory
     * @param appClientContext
     */
    private void closeAppClientContextOnException(EARContext appClientContext) {
        if (appClientContext == null) {
            return;
        }
        try {
            appClientContext.close();
        } catch (Exception e) {
        }
        cleanupAppClientDir(appClientContext.getBaseDir());
    }

    private boolean cleanupAppClientDir(File configurationDir) {
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

}
