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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.client.AppClientContainer;
import org.apache.geronimo.client.StaticJndiContextPlugin;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.AppClientModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.NamingContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEAppClientModuleImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientDocument;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientType;
import org.apache.geronimo.xbeans.geronimo.client.GerResourceType;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientDocument;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class AppClientModuleBuilder implements ModuleBuilder {

    private final URI defaultClientParentId;
    private final URI defaultServerParentId;
    private final ObjectName corbaGBeanObjectName;
    private final Kernel kernel;
    private final Repository repository;
    private final ConfigurationStore store;

    private final String clientApplicationName = "client-application";
    private final ObjectName transactionContextManagerObjectName;
    private final ObjectName connectionTrackerObjectName;
    private final EJBReferenceBuilder ejbReferenceBuilder;
    private final ModuleBuilder connectorModuleBuilder;
    private final ResourceReferenceBuilder resourceReferenceBuilder;
    private final ServiceReferenceBuilder serviceReferenceBuilder;

    public AppClientModuleBuilder(URI defaultClientParentId,
                                  URI defaultServerParentId,
                                  ObjectName transactionContextManagerObjectName,
                                  ObjectName connectionTrackerObjectName,
                                  ObjectName corbaGBeanObjectName,
                                  EJBReferenceBuilder ejbReferenceBuilder,
                                  ModuleBuilder connectorModuleBuilder,
                                  ResourceReferenceBuilder resourceReferenceBuilder,
                                  ServiceReferenceBuilder serviceReferenceBuilder,
                                  ConfigurationStore store,
                                  Repository repository,
                                  Kernel kernel) {
        this.defaultClientParentId = defaultClientParentId;
        this.defaultServerParentId = defaultServerParentId;
        this.corbaGBeanObjectName = corbaGBeanObjectName;
        this.kernel = kernel;
        this.repository = repository;
        this.store = store;
        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.connectorModuleBuilder = connectorModuleBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.serviceReferenceBuilder = serviceReferenceBuilder;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "app-client", null, null, true);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, earConfigId, false);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, boolean standAlone) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";

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
            XmlObject xmlObject = SchemaConversionUtils.parse(specDD);
            ApplicationClientDocument appClientDoc = SchemaConversionUtils.convertToApplicationClientSchema(xmlObject);
            appClient = appClientDoc.getApplicationClient();
        } catch (XmlException e) {
            throw new DeploymentException("Unable to parse application-client.xml", e);
        }

        // parse vendor dd
        GerApplicationClientType gerAppClient = getGeronimoAppClient(plan, moduleFile, standAlone, targetPath, appClient, earConfigId);

        // get the ids from either the application plan or for a stand alone module from the specific deployer
        URI configId = null;
        try {
            configId = new URI(gerAppClient.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + gerAppClient.getConfigId(), e);
        }

        URI parentId = null;
        if (gerAppClient.isSetParentId()) {
            try {
                parentId = new URI(gerAppClient.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + gerAppClient.getParentId(), e);
            }
        } else {
            parentId = defaultServerParentId;
        }

        return new AppClientModule(standAlone, configId, parentId, moduleFile, targetPath, appClient, gerAppClient, specDD);
    }

    GerApplicationClientType getGeronimoAppClient(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, ApplicationClientType appClient, URI earConfigId) throws DeploymentException {
        GerApplicationClientType gerAppClient = null;
        try {
            // load the geronimo-application-client.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    gerAppClient = (GerApplicationClientType) SchemaConversionUtils.getNestedObjectAsType((XmlObject) plan,
                            "application-client",
                            GerApplicationClientType.type);
                } else {
                    GerApplicationClientDocument gerAppClientDoc = null;
                    if (plan != null) {
                        gerAppClientDoc = GerApplicationClientDocument.Factory.parse((File) plan);
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/geronimo-application-client.xml");
                        gerAppClientDoc = GerApplicationClientDocument.Factory.parse(path);
                    }
                    if (gerAppClientDoc != null) {
                        gerAppClient = gerAppClientDoc.getApplicationClient();
                    }
                }
            } catch (IOException e) {
            }

            // if we got one extract the validate it otherwise create a default one
            if (gerAppClient != null) {
                gerAppClient = (GerApplicationClientType) SchemaConversionUtils.convertToGeronimoSecuritySchema(gerAppClient);
                gerAppClient = (GerApplicationClientType) SchemaConversionUtils.convertToGeronimoNamingSchema(gerAppClient);
                gerAppClient = (GerApplicationClientType) SchemaConversionUtils.convertToGeronimoServiceSchema(gerAppClient);
                SchemaConversionUtils.validateDD(gerAppClient);
            } else {
                String path;
                if (standAlone) {
                    // default configId is based on the moduleFile name
                    path = new File(moduleFile.getName()).getName();
                } else {
                    // default configId is based on the module uri from the application.xml
                    path = targetPath;
                }
                gerAppClient = createDefaultPlan(path, appClient, standAlone, earConfigId);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }
        return gerAppClient;
    }

    private GerApplicationClientType createDefaultPlan(String name, ApplicationClientType appClient, boolean standAlone, URI earConfigId) {
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

        // set the parentId and configId
        if (standAlone) {
            geronimoAppClient.setClientConfigId(id);
            geronimoAppClient.setConfigId(id + "/server");
        } else {
            geronimoAppClient.setClientConfigId(earConfigId.getPath() + "/" + id);
            // not used but we need to have a value
            geronimoAppClient.setConfigId(id);
        }
        return geronimoAppClient;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        // extract the app client jar file into a standalone packed jar file and add the contents to the output
        JarFile moduleFile = module.getModuleFile();
        try {
            earContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName());
        }
        ((AppClientModule) module).setEarFile(earFile);
    }

    public void initContext(EARContext earContext, Module clientModule, ClassLoader cl) {
        // application clients do not add anything to the shared context
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader earClassLoader) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();

        AppClientModule appClientModule = (AppClientModule) module;

        ApplicationClientType appClient = (ApplicationClientType) appClientModule.getSpecDD();
        GerApplicationClientType geronimoAppClient = (GerApplicationClientType) appClientModule.getVendorDD();

        // get the app client main class
        JarFile moduleFile = module.getModuleFile();
        String mainClasss = null;
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
        ObjectName appClientModuleName = null;
        try {
            //TODO consider constructing a module context
            appClientModuleName = NameFactory.getModuleName(null, null, null, NameFactory.APP_CLIENT_MODULE, appClientModule.getName(), earJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct module name", e);
        }

        // create a gbean for the app client module and add it to the ear
        Map componentContext;
        GBeanData appClientModuleGBeanData = new GBeanData(appClientModuleName, J2EEAppClientModuleImpl.GBEAN_INFO);
        try {
            appClientModuleGBeanData.setReferencePatterns("J2EEServer", Collections.singleton(earContext.getServerObjectName()));
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                appClientModuleGBeanData.setReferencePatterns("J2EEApplication", Collections.singleton(earContext.getApplicationObjectName()));
            }
            appClientModuleGBeanData.setAttribute("deploymentDescriptor", appClientModule.getOriginalSpecDD());

        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
        }
        earContext.addGBean(appClientModuleGBeanData);

        // create another child configuration within the config store for the client application
        EARContext appClientDeploymentContext = null;
        File appClientDir = null;
        ConfigurationData appClientConfigurationData = null;
        try {
            try {
                appClientDir = store.createNewConfigurationDir();

                // construct the app client deployment context... this is the same class used by the ear context
                try {

                    URI clientConfigId = URI.create(geronimoAppClient.getClientConfigId());
                    URI clientParentId;
                    if (geronimoAppClient.isSetClientParentId()) {
                        clientParentId = URI.create(geronimoAppClient.getClientParentId());
                    } else {
                        clientParentId = defaultClientParentId;
                    }
                    appClientDeploymentContext = new EARContext(appClientDir,
                            clientConfigId,
                            ConfigurationModuleType.APP_CLIENT,
                            clientParentId,
                            kernel,
                            clientApplicationName,
                            transactionContextManagerObjectName,
                            connectionTrackerObjectName,
                            null,
                            null,
                            corbaGBeanObjectName,
                            RefContext.derivedClientRefContext(earContext.getRefContext(), ejbReferenceBuilder, resourceReferenceBuilder, serviceReferenceBuilder));
                } catch (Exception e) {
                    throw new DeploymentException("Could not create a deployment context for the app client", e);
                }

                // extract the client Jar file into a standalone packed jar file and add the contents to the output
                URI moduleBase = new URI(appClientModule.getTargetPath());
                try {
                    appClientDeploymentContext.addIncludeAsPackedJar(moduleBase, moduleFile);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName());
                }

                // add the includes
                DependencyType[] includes = geronimoAppClient.getIncludeArray();
                ServiceConfigBuilder.addIncludes(appClientDeploymentContext, includes, repository);

                // add the dependencies
                DependencyType[] dependencies = geronimoAppClient.getDependencyArray();
                ServiceConfigBuilder.addDependencies(appClientDeploymentContext, dependencies, repository);

                // add manifest class path entries to the app client context
                addManifestClassPath(appClientDeploymentContext, appClientModule.getEarFile(), moduleFile, moduleBase);

                // get the classloader
                ClassLoader appClientClassLoader = appClientDeploymentContext.getClassLoader(repository);

                // pop in all the gbeans declared in the geronimo app client file
                if (geronimoAppClient != null) {
                    GbeanType[] gbeans = geronimoAppClient.getGbeanArray();
                    ServiceConfigBuilder.addGBeans(gbeans, appClientClassLoader, appClientDeploymentContext.getJ2eeContext(), appClientDeploymentContext);
                    //deploy the resource adapters specified in the geronimo-application.xml
                    Collection resourceModules = new ArrayList();
                    try {
                        GerResourceType[] resources = geronimoAppClient.getResourceArray();
                        for (int i = 0; i < resources.length; i++) {
                            GerResourceType resource = resources[i];
                            String path;
                            JarFile connectorFile;
                            if (resource.isSetExternalRar()) {
                                path = resource.getExternalRar();
                                URI pathURI = null;
                                try {
                                    pathURI = new URI(path);
                                } catch (URISyntaxException e) {
                                    throw new DeploymentException("Bad path to external rar", e);
                                }
                                if (!repository.hasURI(pathURI)) {
                                    throw new DeploymentException("Missing rar in repository: " + path);
                                }
                                URL pathURL = null;
                                try {
                                    pathURL = repository.getURL(pathURI);
                                } catch (MalformedURLException e) {
                                    throw new DeploymentException("Could not locate external rar in repository", e);
                                }
                                try {
                                    connectorFile = new JarFile(pathURL.getFile());
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
                            Module connectorModule = connectorModuleBuilder.createModule(connectorPlan, connectorFile, path, null, null, null);
                            resourceModules.add(connectorModule);
                            connectorModuleBuilder.installModule(connectorFile, appClientDeploymentContext, connectorModule);
                        }
                        //the install step could have added more dependencies... we need a new cl.
                        appClientClassLoader = appClientDeploymentContext.getClassLoader(repository);
                        for (Iterator iterator = resourceModules.iterator(); iterator.hasNext();) {
                            Module connectorModule = (Module) iterator.next();
                            connectorModuleBuilder.initContext(appClientDeploymentContext, connectorModule, appClientClassLoader);
                        }

                        for (Iterator iterator = resourceModules.iterator(); iterator.hasNext();) {
                            Module connectorModule = (Module) iterator.next();
                            connectorModuleBuilder.addGBeans(appClientDeploymentContext, connectorModule, appClientClassLoader);
                        }
                    } finally {
                        for (Iterator iterator = resourceModules.iterator(); iterator.hasNext();) {
                            Module connectorModule = (Module) iterator.next();
                            connectorModule.close();
                        }
                    }
                }

                // add the app client static jndi provider
                ObjectName jndiContextName = ObjectName.getInstance("geronimo.client:type=StaticJndiContext");
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
                ObjectName appClientContainerName = ObjectName.getInstance("geronimo.client:type=ClientContainer");
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
                appClientConfigurationData = appClientDeploymentContext.getConfigurationData();
            } finally {
                if (appClientDeploymentContext != null) {
                    try {
                        appClientDeploymentContext.close();
                    } catch (IOException e) {
                    }
                }
            }

            try {
                // todo this should be handled in the Deployer class
                store.install(appClientDeploymentContext.getConfigurationData(), appClientDir);
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
            earContext.addChildConfiguration(appClientConfigurationData);
        } catch (Throwable e) {
            DeploymentUtil.recursiveDelete(appClientDir);
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

    public void addManifestClassPath(DeploymentContext deploymentContext, JarFile earFile, JarFile jarFile, URI jarFileLocation) throws DeploymentException {
        Manifest manifest = null;
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

                JarFile classPathJarFile = null;
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

    private Map buildComponentContext(EARContext appClientContext, NamingContext ejbContext, AppClientModule appClientModule, ApplicationClientType appClient, GerApplicationClientType geronimoAppClient, ClassLoader cl) throws DeploymentException {

        return ENCConfigBuilder.buildComponentContext(appClientContext,
                ejbContext,
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(AppClientModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultClientParentId", URI.class, true);
        infoBuilder.addAttribute("defaultServerParentId", URI.class, true);
        infoBuilder.addAttribute("transactionContextManagerObjectName", ObjectName.class, true);
        infoBuilder.addAttribute("connectionTrackerObjectName", ObjectName.class, true);
        infoBuilder.addAttribute("corbaGBeanObjectName", ObjectName.class, true);
        infoBuilder.addReference("EJBReferenceBuilder", EJBReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ConnectorModuleBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ResourceReferenceBuilder", ResourceReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("ServiceReferenceBuilder", ServiceReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("Store", ConfigurationStore.class, NameFactory.CONFIGURATION_STORE);
        infoBuilder.addReference("Repository", Repository.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{"defaultClientParentId",
                                                "defaultServerParentId",
                                                "transactionContextManagerObjectName",
                                                "connectionTrackerObjectName",
                                                "corbaGBeanObjectName",
                                                "EJBReferenceBuilder",
                                                "ConnectorModuleBuilder",
                                                "ResourceReferenceBuilder",
                                                "ServiceReferenceBuilder",
                                                "Store",
                                                "Repository",
                                                "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
