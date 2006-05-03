/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.client.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.client.AppClientContainer;
import org.apache.geronimo.client.StaticJndiContextPlugin;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.j2ee.deployment.AppClientModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEAppClientModuleImpl;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientDocument;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientType;
import org.apache.geronimo.xbeans.geronimo.client.GerResourceType;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientDocument;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


/**
 * @version $Rev:385232 $ $Date$
 */
public class AppClientModuleBuilder implements ModuleBuilder {
    private static final Log log = LogFactory.getLog(AppClientModuleBuilder.class);
    private static final String LINE_SEP = System.getProperty("line.separator");
    
    private final Environment defaultClientEnvironment;
    private final Environment defaultServerEnvironment;
    private final AbstractNameQuery corbaGBeanObjectName;

    private final AbstractNameQuery transactionContextManagerObjectName;
    private final AbstractNameQuery connectionTrackerObjectName;
    private final SingleElementCollection ejbReferenceBuilder;
    private final SingleElementCollection connectorModuleBuilder;
    private final SingleElementCollection resourceReferenceBuilder;
    private final SingleElementCollection serviceReferenceBuilder;
    private static final String GERAPPCLIENT_NAMESPACE = GerApplicationClientDocument.type.getDocumentElementName().getNamespaceURI();

    public AppClientModuleBuilder(Environment defaultClientEnvironment,
            Environment defaultServerEnvironment,
            AbstractNameQuery transactionContextManagerObjectName,
            AbstractNameQuery connectionTrackerObjectName,
            AbstractNameQuery corbaGBeanObjectName,
            EJBReferenceBuilder ejbReferenceBuilder,
            ModuleBuilder connectorModuleBuilder,
            ResourceReferenceBuilder resourceReferenceBuilder,
            ServiceReferenceBuilder serviceReferenceBuilder) {
        this(defaultClientEnvironment,
                defaultServerEnvironment,
                transactionContextManagerObjectName,
                connectionTrackerObjectName,
                corbaGBeanObjectName,
                new SingleElementCollection(ejbReferenceBuilder),
                new SingleElementCollection(connectorModuleBuilder),
                new SingleElementCollection(resourceReferenceBuilder),
                new SingleElementCollection(serviceReferenceBuilder));
    }

    public AppClientModuleBuilder(AbstractNameQuery transactionContextManagerObjectName,
            AbstractNameQuery connectionTrackerObjectName,
            AbstractNameQuery corbaGBeanObjectName,
            Collection ejbReferenceBuilder,
            Collection connectorModuleBuilder,
            Collection resourceReferenceBuilder,
            Collection serviceReferenceBuilder,
            Environment defaultClientEnvironment,
            Environment defaultServerEnvironment
    ) {
        this(defaultClientEnvironment,
                defaultServerEnvironment,
                transactionContextManagerObjectName,
                connectionTrackerObjectName,
                corbaGBeanObjectName,
                new SingleElementCollection(ejbReferenceBuilder),
                new SingleElementCollection(connectorModuleBuilder),
                new SingleElementCollection(resourceReferenceBuilder),
                new SingleElementCollection(serviceReferenceBuilder));
    }

    private AppClientModuleBuilder(Environment defaultClientEnvironment,
            Environment defaultServerEnvironment,
            AbstractNameQuery transactionContextManagerObjectName,
            AbstractNameQuery connectionTrackerObjectName,
            AbstractNameQuery corbaGBeanObjectName,
            SingleElementCollection ejbReferenceBuilder,
            SingleElementCollection connectorModuleBuilder,
            SingleElementCollection resourceReferenceBuilder,
            SingleElementCollection serviceReferenceBuilder) {
        this.defaultClientEnvironment = defaultClientEnvironment;
        this.defaultServerEnvironment = defaultServerEnvironment;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.connectorModuleBuilder = connectorModuleBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.serviceReferenceBuilder = serviceReferenceBuilder;
    }

    private EJBReferenceBuilder getEjbReferenceBuilder() {
        return (EJBReferenceBuilder) ejbReferenceBuilder.getElement();
    }

    private ModuleBuilder getConnectorModuleBuilder() {
        return (ModuleBuilder) connectorModuleBuilder.getElement();
    }

    private ResourceReferenceBuilder getResourceReferenceBuilder() {
        return (ResourceReferenceBuilder) resourceReferenceBuilder.getElement();
    }

    private ServiceReferenceBuilder getServiceReferenceBuilder() {
        return (ServiceReferenceBuilder) serviceReferenceBuilder.getElement();
    }

    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "app-client", null, null, null, naming, idBuilder);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, earName, naming, idBuilder);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";
        assert (earName == null) == (earEnvironment == null): "if earName is not null you must supply earEnvironment as well";

        String specDD;
        ApplicationClientType appClient;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "META-INF/application-client.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);
        } catch (Exception e) {
            //no application-client.xml, not for us.
            return null;
        }
        //we found application-client.xml, if it won't parse it's an error.
        try {
            // parse it
            XmlObject xmlObject = XmlBeansUtil.parse(specDD);
            ApplicationClientDocument appClientDoc = SchemaConversionUtils.convertToApplicationClientSchema(xmlObject);
            appClient = appClientDoc.getApplicationClient();
        } catch (XmlException e) {
            throw new DeploymentException("Unable to parse application-client.xml", e);
        }

        // parse vendor dd
        boolean standAlone = earEnvironment == null;
        GerApplicationClientType gerAppClient = getGeronimoAppClient(plan, moduleFile, standAlone, targetPath, appClient, earEnvironment);


        EnvironmentType clientEnvironmentType = gerAppClient.getClientEnvironment();
        Environment clientEnvironment = EnvironmentBuilder.buildEnvironment(clientEnvironmentType, defaultClientEnvironment);
        if(standAlone) {
            String name = new File(moduleFile.getName()).getName();
            idBuilder.resolve(clientEnvironment, name+"_"+name, "jar");
        } else {
            Artifact earConfigId = earEnvironment.getConfigId();
            idBuilder.resolve(clientEnvironment, earConfigId.getArtifactId() + "_" + new File(moduleFile.getName()).getName(), "jar");
        }
        EnvironmentType serverEnvironmentType = gerAppClient.getServerEnvironment();
        Environment serverEnvironment = EnvironmentBuilder.buildEnvironment(serverEnvironmentType, defaultServerEnvironment);
        if (!standAlone) {
            EnvironmentBuilder.mergeEnvironments(earEnvironment, serverEnvironment);
            serverEnvironment = earEnvironment;
            if(!serverEnvironment.getConfigId().isResolved()) {
                throw new IllegalStateException("Server environment module ID should be fully resolved (not "+serverEnvironment.getConfigId()+")");
            }
        } else {
            idBuilder.resolve(serverEnvironment, new File(moduleFile.getName()).getName(), "jar");
        }

        AbstractName moduleName;
        if (earName == null) {
            earName = naming.createRootName(earEnvironment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, earEnvironment.getConfigId().toString(), NameFactory.APP_CLIENT_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.APP_CLIENT_MODULE);
        }

        return new AppClientModule(standAlone, moduleName, serverEnvironment, clientEnvironment, moduleFile, targetPath, appClient, gerAppClient, specDD);
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
                        rawPlan = XmlBeansUtil.parse(path);
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
            throw new DeploymentException(e);
        }
        return gerAppClient;
    }

    private GerApplicationClientType createDefaultPlan(String name, ApplicationClientType appClient, boolean standAlone, Environment environment) {
        String id = appClient.getId();
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
        JarFile moduleFile = module.getModuleFile();
        try {
            earContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName());
        }
        AppClientModule appClientModule = (AppClientModule) module;
        appClientModule.setEarFile(earFile);
        //create the ear context for the app client.
        Environment clientEnvironment = appClientModule.getClientEnvironment();
        if (!appClientModule.isStandAlone() || clientEnvironment.getConfigId() == null) {
            Artifact earConfigId = earContext.getConfigID();
            Artifact configId = new Artifact(earConfigId.getGroupId(), earConfigId.getArtifactId() + "_" + module.getTargetPath(), earConfigId.getVersion(), "car");
            clientEnvironment.setConfigId(configId);
        }

        AbstractName clientBaseName = earContext.getNaming().createRootName(clientEnvironment.getConfigId(), clientEnvironment.getConfigId().toString(), NameFactory.J2EE_APPLICATION);
        File appClientDir;
        try {
            appClientDir = targetConfigurationStore.createNewConfigurationDir(clientEnvironment.getConfigId());
        } catch (ConfigurationAlreadyExistsException e) {
            throw new DeploymentException(e);
        }

        // construct the app client deployment context... this is the same class used by the ear context
        try {

            EARContext appClientDeploymentContext = new EARContext(appClientDir,
                    null,
                    clientEnvironment,
                    ConfigurationModuleType.CAR,
                    earContext.getNaming(),
                    earContext.getConfigurationManager(),
                    null, //no server name needed on client
                    clientBaseName,
                    transactionContextManagerObjectName,
                    connectionTrackerObjectName,
                    null,
                    null,
                    corbaGBeanObjectName,
                    new RefContext(getEjbReferenceBuilder(), getResourceReferenceBuilder(), getServiceReferenceBuilder()));
            appClientModule.setEarContext(appClientDeploymentContext);
        } catch (DeploymentException e) {
            cleanupAppClientDir(appClientDir);
            throw e;
        }

    }

    public void initContext(EARContext earContext, Module clientModule, ClassLoader cl) {
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader earClassLoader, Collection repositories) throws DeploymentException {

        AppClientModule appClientModule = (AppClientModule) module;

        ApplicationClientType appClient = (ApplicationClientType) appClientModule.getSpecDD();
        GerApplicationClientType geronimoAppClient = (GerApplicationClientType) appClientModule.getVendorDD();

        // get the app client main class
        JarFile moduleFile = module.getModuleFile();
        String mainClasss;
//        String classPath = null;
        try {
            Manifest manifest = moduleFile.getManifest();
            if (manifest == null) {
                throw new DeploymentException("App client module jar does not contain a manifest: " + moduleFile.getName());
            }
            mainClasss = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClasss == null) {
                throw new DeploymentException("App client module jar does not have Main-Class defined in the manifest: " + moduleFile.getName());
            }
            String classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if (module.isStandAlone() && classPath != null) {
                throw new DeploymentException("Manifest class path entry is not allowed in a standalone jar (J2EE 1.4 Section 8.2)");
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not get manifest from app client module: " + moduleFile.getName());
        }

        // generate the object name for the app client
        AbstractName appClientModuleName = appClientModule.getModuleName();

        // create a gbean for the app client module and add it to the ear
        Map componentContext;
        GBeanData appClientModuleGBeanData = new GBeanData(appClientModuleName, J2EEAppClientModuleImpl.GBEAN_INFO);
        try {
            appClientModuleGBeanData.setReferencePatterns("J2EEServer", Collections.singleton(earContext.getServerName()));
            if (!module.isStandAlone()) {
                appClientModuleGBeanData.setReferencePatterns("J2EEApplication", Collections.singleton(earContext.getModuleName()));
            }
            appClientModuleGBeanData.setAttribute("deploymentDescriptor", appClientModule.getOriginalSpecDD());

        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
        }
        try {
            earContext.addGBean(appClientModuleGBeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add application client module gbean to configuration", e);
        }

        EARContext appClientDeploymentContext = appClientModule.getEarContext();
//        ConfigurationData appClientConfigurationData = null;

        // Create a Module ID Builder defaulting to similar settings to use for any children we create
        ModuleIDBuilder idBuilder = new ModuleIDBuilder();
        idBuilder.setDefaultGroup(appClientModule.getClientEnvironment().getConfigId().getGroupId());
        idBuilder.setDefaultVersion(appClientModule.getClientEnvironment().getConfigId().getVersion());
        try {
            try {

                //register the message destinations in the app client ear context.
                MessageDestinationType[] messageDestinations = appClient.getMessageDestinationArray();
                GerMessageDestinationType[] gerMessageDestinations = geronimoAppClient.getMessageDestinationArray();

                ENCConfigBuilder.registerMessageDestinations(appClientDeploymentContext.getRefContext(), appClientModule.getName(), messageDestinations, gerMessageDestinations);
                // extract the client Jar file into a standalone packed jar file and add the contents to the output
                URI moduleBase = new URI(appClientModule.getTargetPath());
                try {
                    appClientDeploymentContext.addIncludeAsPackedJar(moduleBase, moduleFile);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName());
                }

                // add manifest class path entries to the app client context
                addManifestClassPath(appClientDeploymentContext, appClientModule.getEarFile(), moduleFile, moduleBase);

                // get the classloader
                ClassLoader appClientClassLoader = appClientDeploymentContext.getClassLoader();

                // pop in all the gbeans declared in the geronimo app client file
                if (geronimoAppClient != null) {
                    GbeanType[] gbeans = geronimoAppClient.getGbeanArray();
                    ServiceConfigBuilder.addGBeans(gbeans, appClientClassLoader, appClientDeploymentContext.getModuleName(), appClientDeploymentContext);
                    //deploy the resource adapters specified in the geronimo-application.xml
                    Collection resourceModules = new ArrayList();
                    try {
                        GerResourceType[] resources = geronimoAppClient.getResourceArray();
                        for (int i = 0; i < resources.length; i++) {
                            GerResourceType resource = resources[i];
                            String path;
                            JarFile connectorFile;
                            if (resource.isSetExternalRar()) {
                                path = resource.getExternalRar().trim();
                                Artifact artifact = Artifact.create(path);
                                File file = null;
                                for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
                                    Repository repository = (Repository) iterator.next();
                                    if (repository.contains(artifact)) {
                                        file = repository.getLocation(artifact);
                                        break;
                                    }
                                }
                                if (file == null) {
                                    throw new DeploymentException("Missing rar in repositories: " + path);
                                }
                                try {
                                    connectorFile = new JarFile(file);
                                } catch (IOException e) {
                                    throw new DeploymentException("Could not access rar contents", e);
                                }
                            } else {
                                path = resource.getInternalRar();
                                try {
                                    connectorFile = new NestedJarFile(appClientModule.getEarFile(), path);
                                } catch (IOException e) {
                                    throw new DeploymentException("Could not locate connector inside ear", e);
                                }
                            }
                            XmlObject connectorPlan = resource.getConnector();
                            Module connectorModule = getConnectorModuleBuilder().createModule(connectorPlan, connectorFile, path, null, appClientModule.getClientEnvironment(), null, appClientDeploymentContext.getModuleName(), earContext.getNaming(), idBuilder);
                            resourceModules.add(connectorModule);
                            //TODO configStore == null is fishy, consider moving these stages for connectors into the corresponding stages for this module.
                            getConnectorModuleBuilder().installModule(connectorFile, appClientDeploymentContext, connectorModule, null, null, repositories);
                        }
                        //the install step could have added more dependencies... we need a new cl.
                        appClientClassLoader = appClientDeploymentContext.getClassLoader();
                        for (Iterator iterator = resourceModules.iterator(); iterator.hasNext();) {
                            Module connectorModule = (Module) iterator.next();
                            getConnectorModuleBuilder().initContext(appClientDeploymentContext, connectorModule, appClientClassLoader);
                        }

                        for (Iterator iterator = resourceModules.iterator(); iterator.hasNext();) {
                            Module connectorModule = (Module) iterator.next();
                            getConnectorModuleBuilder().addGBeans(appClientDeploymentContext, connectorModule, appClientClassLoader, repositories);
                        }
                    } finally {
                        for (Iterator iterator = resourceModules.iterator(); iterator.hasNext();) {
                            Module connectorModule = (Module) iterator.next();
                            connectorModule.close();
                        }
                    }
                }

                // add the app client static jndi provider
                AbstractName jndiContextName = earContext.getNaming().createChildName(appClientDeploymentContext.getModuleName(), "StaticJndiContext", "StaticJndiContext");
                GBeanData jndiContextGBeanData = new GBeanData(jndiContextName, StaticJndiContextPlugin.GBEAN_INFO);
                try {
                    componentContext = buildComponentContext(appClientDeploymentContext, earContext, appClientModule, appClient, geronimoAppClient, appClientClassLoader);
                    jndiContextGBeanData.setAttribute("context", componentContext);
                } catch (DeploymentException e) {
                    throw e;
                } catch (Exception e) {
                    throw new DeploymentException("Unable to construct jndi context for AppClientModule GBean", e);
                }
                appClientDeploymentContext.addGBean(jndiContextGBeanData);

                // finally add the app client container
//                AbstractName appClientContainerName = earContext.getNaming().createChildName(appClientDeploymentContext.getModuleName(), "ClientContainer", "ClientContainer");
                AbstractName appClientContainerName = appClientDeploymentContext.getModuleName();
                GBeanData appClientContainerGBeanData = new GBeanData(appClientContainerName, AppClientContainer.GBEAN_INFO);
                try {
                    appClientContainerGBeanData.setAttribute("mainClassName", mainClasss);
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
                    } else if (geronimoAppClient.isSetDefaultPrincipal()) {
                        DefaultPrincipal defaultPrincipal = SecurityBuilder.buildDefaultPrincipal(geronimoAppClient.getDefaultPrincipal());
                        appClientContainerGBeanData.setAttribute("defaultPrincipal", defaultPrincipal);
                    }
                    appClientContainerGBeanData.setReferencePattern("JNDIContext", jndiContextName);
                    appClientContainerGBeanData.setReferencePattern("TransactionContextManager", transactionContextManagerObjectName);
                } catch (Exception e) {
                    throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
                }
                appClientDeploymentContext.addGBean(appClientContainerGBeanData);

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

    public String getSchemaNamespace() {
        return GERAPPCLIENT_NAMESPACE;
    }

    public void addManifestClassPath(DeploymentContext deploymentContext, JarFile earFile, JarFile jarFile, URI jarFileLocation) throws DeploymentException {
        Manifest manifest;
        try {
            manifest = jarFile.getManifest();
        } catch (IOException e) {
            throw new DeploymentException("Could not read manifest: " + jarFileLocation);
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
                throw new DeploymentException("Invalid manifest classpath entry: jarFile=" + jarFileLocation + ", path=" + path);
            }

            if (!pathUri.getPath().endsWith(".jar")) {
                throw new DeploymentException("Manifest class path entries must end with the .jar extension (J2EE 1.4 Section 8.2): jarFile=" + jarFileLocation + ", path=" + path);
            }
            if (pathUri.isAbsolute()) {
                throw new DeploymentException("Manifest class path entries must be relative (J2EE 1.4 Section 8.2): jarFile=" + jarFileLocation + ", path=" + path);
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
                    throw new DeploymentException("Manifest class path entries must be a valid jar file (J2EE 1.4 Section 8.2): jarFile=" + jarFileLocation + ", path=" + path, e);
                }

                // add the client jars of this class path jar
                addManifestClassPath(deploymentContext, earFile, classPathJarFile, classPathJarLocation);
            }
        }
    }

    private Map buildComponentContext(EARContext appClientContext, EARContext ejbContext, AppClientModule appClientModule, ApplicationClientType appClient, GerApplicationClientType geronimoAppClient, ClassLoader cl) throws DeploymentException {

        return ENCConfigBuilder.buildComponentContext(appClientContext,
                ejbContext.getConfiguration(),
                appClientModule,
                null, //no user transaction yet
                appClient.getEnvEntryArray(),
                appClient.getEjbRefArray(), geronimoAppClient.getEjbRefArray(),
                new EjbLocalRefType[0], null,
                appClient.getResourceRefArray(), geronimoAppClient.getResourceRefArray(),
                appClient.getResourceEnvRefArray(), geronimoAppClient.getResourceEnvRefArray(),
                appClient.getMessageDestinationRefArray(),
                appClient.getServiceRefArray(), geronimoAppClient.getServiceRefArray(),
                cl);

    }

    private boolean cleanupAppClientDir(File configurationDir)
    {
        LinkedList cannotBeDeletedList = new LinkedList();
               
        if (!DeploymentUtil.recursiveDelete(configurationDir,cannotBeDeletedList)) {
            // Output a message to help user track down file problem
            log.warn("Unable to delete " + cannotBeDeletedList.size() + 
                    " files while recursively deleting directory " 
                    + configurationDir + LINE_SEP +
                    "The first file that could not be deleted was:" + LINE_SEP + "  "+
                    ( !cannotBeDeletedList.isEmpty() ? cannotBeDeletedList.getFirst() : "") );
            return false;
        }
        return true;
    }  
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(AppClientModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultClientEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("defaultServerEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("transactionContextManagerObjectName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("connectionTrackerObjectName", AbstractNameQuery.class, true);
        infoBuilder.addAttribute("corbaGBeanObjectName", AbstractNameQuery.class, true);
        infoBuilder.addReference("EJBReferenceBuilder", EJBReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ConnectorModuleBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ResourceReferenceBuilder", ResourceReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceReferenceBuilder", ServiceReferenceBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{"transactionContextManagerObjectName",
                "connectionTrackerObjectName",
                "corbaGBeanObjectName",
                "EJBReferenceBuilder",
                "ConnectorModuleBuilder",
                "ResourceReferenceBuilder",
                "ServiceReferenceBuilder",
                "defaultClientEnvironment",
                "defaultServerEnvironment",
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
