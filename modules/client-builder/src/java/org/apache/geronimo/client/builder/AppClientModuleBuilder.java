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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Reference;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.deployment.util.IOUtil;
import org.apache.geronimo.deployment.util.JarUtil;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.AppClientModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.EJBRefContext;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.management.impl.J2EEAppClientModuleImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientDocument;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientType;
import org.apache.geronimo.xbeans.geronimo.client.GerDependencyType;
import org.apache.geronimo.xbeans.geronimo.client.GerGbeanType;
import org.apache.geronimo.xbeans.geronimo.client.GerResourceType;
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientDocument;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openejb.client.naming.RemoteEJBObjectFactory;
import org.openejb.client.naming.RemoteEJBRefAddr;


/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class AppClientModuleBuilder implements ModuleBuilder {
    private final Kernel kernel;
    private final Repository repository;
    private final ConfigurationStore store;

    private static final URI CLIENT_PARENT_ID = URI.create("org/apache/geronimo/Client");
    private final String clientDomainName = "geronimo.client";
    private final String clientServerName = "client";
    private final String clientApplicationName = "client-application";
    private final ObjectName transactionContextManagerObjectName;
    private final ObjectName connectionTrackerObjectName;
    private final ModuleBuilder connectorModuleBuilder;

    public AppClientModuleBuilder(ObjectName transactionContextManagerObjectName, ObjectName connectionTrackerObjectName, ModuleBuilder connectorModuleBuilder, ConfigurationStore store, Repository repository, Kernel kernel) {
        this.kernel = kernel;
        this.repository = repository;
        this.store = store;
        this.transactionContextManagerObjectName = transactionContextManagerObjectName;
        this.connectionTrackerObjectName = connectionTrackerObjectName;
        this.connectorModuleBuilder = connectorModuleBuilder;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "app-client", null, null, true);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId) throws DeploymentException {
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
                specDDUrl = JarUtil.createJarURL(moduleFile, "META-INF/application-client.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = IOUtil.readAll(specDDUrl);

            // parse it
            XmlObject xmlObject = SchemaConversionUtils.parse(specDD);
            ApplicationClientDocument appClientDoc = SchemaConversionUtils.convertToApplicationClientSchema(xmlObject);
            appClient = appClientDoc.getApplicationClient();
        } catch (XmlException e) {
            throw new DeploymentException("Unable to parse application-client.xml", e);
        } catch (Exception e) {
            return null;
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
        }

        return new AppClientModule(standAlone, configId, parentId, moduleFile, targetPath, appClient, gerAppClient, specDD);
    }

    GerApplicationClientType getGeronimoAppClient(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, ApplicationClientType appClient, URI earConfigId) throws DeploymentException {
        GerApplicationClientType gerAppClient = null;
        try {
            // load the geronimo-application-client.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    gerAppClient = (GerApplicationClientType) SchemaConversionUtils.getNestedObjectAsType(
                            (XmlObject) plan,
                            "application-client",
                            GerApplicationClientType.type);
                } else {
                    GerApplicationClientDocument gerAppClientDoc = null;
                    if (plan != null) {
                        gerAppClientDoc = GerApplicationClientDocument.Factory.parse((File)plan);
                    } else {
                        URL path = JarUtil.createJarURL(moduleFile, "META-INF/geronimo-application-client.xml");
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
                gerAppClient = (GerApplicationClientType) SchemaConversionUtils.convertToGeronimoNamingSchema(gerAppClient);
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
        // extract the ejbJar file into a standalone packed jar file and add the contents to the output
        JarFile moduleFile = module.getModuleFile();
        try {
            File appClientJarFile = JarUtil.extractToPackedJar(moduleFile);
            earContext.addInclude(URI.create(module.getTargetPath()), appClientJarFile.toURL());
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy app client module jar into configuration: " + moduleFile.getName());
        }
        ((AppClientModule)module).setEarFile(earFile);
    }

    public void initContext(EARContext earContext, Module clientModule, ClassLoader cl) {
        // application clients do not add anything to the shared context
    }

    public String addGBeans(EARContext earContext, Module module, ClassLoader earClassLoader) throws DeploymentException {
        AppClientModule appClientModule = (AppClientModule) module;

        ApplicationClientType appClient = (ApplicationClientType) appClientModule.getSpecDD();
        GerApplicationClientType geronimoAppClient = (GerApplicationClientType) appClientModule.getVendorDD();

        // get the app client main class
        JarFile moduleFile = module.getModuleFile();
        String mainClasss = null;
        String classPath = null;
        try {
            Manifest manifest = moduleFile.getManifest();
            if (manifest == null) {
                throw new DeploymentException("App client module jar does not contain a manifest: " + moduleFile.getName());
            }
            mainClasss = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClasss == null) {
                throw new DeploymentException("App client module jar does not have Main-Class defined in the manifest: " + moduleFile.getName());
            }
            classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if (module.isStandAlone() && classPath != null) {
                throw new DeploymentException("Manifest class path entry is not allowed in a standalone jar (J2EE 1.4 Section 8.2)");
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not get manifest from app client module: " + moduleFile.getName());
        }

        // generate the object name for the app client
        Properties nameProps = new Properties();
        nameProps.put("J2EEServer", earContext.getJ2EEServerName());
        nameProps.put("J2EEApplication", earContext.getJ2EEApplicationName());
        nameProps.put("j2eeType", "AppClientModule");
        nameProps.put("name", appClientModule.getName());
        ObjectName appClientModuleName;
        try {
            appClientModuleName = new ObjectName(earContext.getJ2EEDomainName(), nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }

        // create a gbean for the app client module and add it to the ear
        ReadOnlyContext componentContext;
        GBeanMBean appClientModuleGBean = new GBeanMBean(J2EEAppClientModuleImpl.GBEAN_INFO, earClassLoader);
        try {
            appClientModuleGBean.setReferencePatterns("J2EEServer", Collections.singleton(earContext.getServerObjectName()));
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                appClientModuleGBean.setReferencePatterns("J2EEApplication", Collections.singleton(earContext.getApplicationObjectName()));
            }
            appClientModuleGBean.setAttribute("deploymentDescriptor", null);

            componentContext = buildComponentContext(earContext, appClientModule, appClient, geronimoAppClient, earClassLoader);
            appClientModuleGBean.setAttribute("componentContext", componentContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
        }
        earContext.addGBean(appClientModuleName, appClientModuleGBean);

        // create another child configuration within the config store for the client application
        EARContext appClientDeploymentContext = null;
        File appClientConfiguration = null;
        try {
            appClientConfiguration = FileUtil.createTempFile();
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(appClientConfiguration)));

            // construct the app client deployment context... this is the same class used by the ear context
            try {
                EJBReferenceBuilder ejbReferenceBuilder = new EJBReferenceBuilder() {
                    public Reference createEJBLocalReference(String objectName, boolean isSession, String localHome, String local) {
                        throw new UnsupportedOperationException("Application client cannot have a local ejb ref");
                    }

                    public Reference createEJBRemoteReference(String objectName, boolean isSession, String home, String remote) {
                        RemoteEJBRefAddr addr = new RemoteEJBRefAddr(objectName);
                        Reference reference = new Reference(null, addr, RemoteEJBObjectFactory.class.getName(), null);
                        return reference;
                    }
                };

                URI clientConfigId = URI.create(geronimoAppClient.getClientConfigId());
                URI clientParentId;
                if (geronimoAppClient.isSetClientParentId()) {
                    clientParentId = URI.create(geronimoAppClient.getClientParentId());
                } else {
                    clientParentId = CLIENT_PARENT_ID;
                }
                appClientDeploymentContext = new EARContext(jos,
                        clientConfigId,
                        ConfigurationModuleType.APP_CLIENT,
                        clientParentId,
                        kernel,
                        clientDomainName,
                        clientServerName,
                        clientApplicationName,
                        transactionContextManagerObjectName,
                        connectionTrackerObjectName,
                        null,
                        null,
                        new EJBRefContext(earContext.getEJBRefContext(), ejbReferenceBuilder));
            } catch (Exception e) {
                throw new DeploymentException("Could not create a deployment context for the app client", e);
            }

            // extract the client Jar file into a standalone packed jar file and add the contents to the output
            File appClientJarFile = JarUtil.extractToPackedJar(moduleFile);
            URI moduleBase = new URI(appClientModule.getTargetPath());
            appClientDeploymentContext.addInclude(moduleBase, appClientJarFile.toURL());

            // add the includes
            GerDependencyType[] includes = geronimoAppClient.getIncludeArray();
            for (int i = 0; i < includes.length; i++) {
                GerDependencyType include = includes[i];
                URI uri = getDependencyURI(include);
                String name = uri.toString();
                int idx = name.lastIndexOf('/');
                if (idx != -1) {
                    name = name.substring(idx + 1);
                }
                URI path;
                try {
                    path = new URI(name);
                } catch (URISyntaxException e) {
                    throw new DeploymentException("Unable to generate path for include: " + uri, e);
                }
                try {
                    URL url = repository.getURL(uri);
                    appClientDeploymentContext.addInclude(path, url);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to add include: " + uri, e);
                }
            }

            // add the dependencies
            GerDependencyType[] dependencies = geronimoAppClient.getDependencyArray();
            for (int i = 0; i < dependencies.length; i++) {
                appClientDeploymentContext.addDependency(getDependencyURI(dependencies[i]));
            }

            appClientDeploymentContext.addManifestClassPath(appClientModule.getEarFile(), appClientModule, moduleFile, moduleBase);


            // get the classloader
            ClassLoader appClientClassLoader = appClientDeploymentContext.getClassLoader(repository);

            // pop in all the gbeans declared in the geronimo app client file
            if (geronimoAppClient != null) {
                GerGbeanType[] gbeans = geronimoAppClient.getGbeanArray();
                for (int i = 0; i < gbeans.length; i++) {
                    GBeanHelper.addGbean(new AppClientGBeanAdapter(gbeans[i]), appClientClassLoader, appClientDeploymentContext);
                }
                //deploy the resource adapters specified in the geronimo-application.xml
                Collection resourceModules = appClientModule.getResourceModules();
                GerResourceType[] resources = geronimoAppClient.getResourceArray();
                for (int i = 0; i < resources.length; i++) {
                    GerResourceType resource = resources[i];
                    String path;
                    JarFile connectorFile;
                    if (resource.isSetExternalRar()) {
                        path = resource.getExternalRar();
                        URI pathURI = new URI(path);
                        if (!repository.hasURI(pathURI)) {
                            throw new DeploymentException("Missing rar in repository: " + path);
                        }
                        URL pathURL = repository.getURL(pathURI);
                        connectorFile = new JarFile(pathURL.getFile());
                    } else {
                        path = resource.getInternalRar();
                        connectorFile = new NestedJarFile(appClientModule.getEarFile(), path);
                    }
                    XmlObject connectorPlan = resource.getConnector();
                    Module connectorModule = connectorModuleBuilder.createModule(connectorPlan, connectorFile, path, null, null);
                    resourceModules.add(connectorModule);
                    connectorModuleBuilder.installModule(connectorFile, appClientDeploymentContext, connectorModule);
                }
                ClassLoader cl = appClientDeploymentContext.getClassLoader(repository);
                for (Iterator iterator = resourceModules.iterator(); iterator.hasNext();) {
                    Module connectorModule = (Module) iterator.next();
                    connectorModuleBuilder.initContext(appClientDeploymentContext, connectorModule, cl);
                }

                for (Iterator iterator = resourceModules.iterator(); iterator.hasNext();) {
                    Module connectorModule = (Module) iterator.next();
                    connectorModuleBuilder.addGBeans(appClientDeploymentContext, connectorModule, cl);
                }

            }

            // add the app client static jndi provider
            ObjectName jndiContextName = ObjectName.getInstance("geronimo.client:type=StaticJndiContext");
            GBeanMBean jndiContextGBean = new GBeanMBean("org.apache.geronimo.client.StaticJndiContextPlugin", appClientClassLoader);
            try {

                componentContext = buildComponentContext(appClientDeploymentContext, appClientModule, appClient, geronimoAppClient, earClassLoader);
                jndiContextGBean.setAttribute("context", componentContext);
            } catch (Exception e) {
                throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
            }
            appClientDeploymentContext.addGBean(jndiContextName, jndiContextGBean);

            // finally add the app client container
            ObjectName appClienContainerName = ObjectName.getInstance("geronimo.client:type=ClientContainer");
            GBeanMBean appClienContainerGBean = new GBeanMBean("org.apache.geronimo.client.AppClientContainer", appClientClassLoader);
            try {
                appClienContainerGBean.setAttribute("mainClassName", mainClasss);
                appClienContainerGBean.setAttribute("appClientModuleName", appClientModuleName);
                appClienContainerGBean.setReferencePattern("JNDIContext", new ObjectName("geronimo.client:type=StaticJndiContext"));
            } catch (Exception e) {
                throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
            }
            appClientDeploymentContext.addGBean(appClienContainerName, appClienContainerGBean);
        } catch (Exception e) {
            throw new DeploymentException(e);
        } finally {
            if (appClientDeploymentContext != null) {
                try {
                    appClientDeploymentContext.close();
                } catch (IOException e) {
                }
            }
        }

        try {
            return store.install(appClientConfiguration.toURL()).toString();
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private ReadOnlyContext buildComponentContext(EARContext earContext, AppClientModule appClientModule, ApplicationClientType appClient, GerApplicationClientType geronimoAppClient, ClassLoader cl) throws DeploymentException {
        Map ejbRefMap = mapRefs(geronimoAppClient.getEjbRefArray());
        Map resourceRefMap = mapRefs(geronimoAppClient.getResourceRefArray());
        Map resourceEnvRefMap = mapRefs(geronimoAppClient.getResourceEnvRefArray());

        return ENCConfigBuilder.buildComponentContext(earContext,
                appClientModule.getModuleURI(),
                null,
                appClient.getEnvEntryArray(),
                appClient.getEjbRefArray(), ejbRefMap,
                new EjbLocalRefType[0], Collections.EMPTY_MAP,
                appClient.getResourceRefArray(), resourceRefMap,
                appClient.getResourceEnvRefArray(), resourceEnvRefMap,
                appClient.getMessageDestinationRefArray(),
                cl);

    }

    private static Map mapRefs(GerRemoteRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerRemoteRefType ref = refs[i];
                refMap.put(ref.getRefName(), ref);
            }
        }
        return refMap;
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
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AppClientModuleBuilder.class);
        infoFactory.addAttribute("transactionContextManagerObjectName", ObjectName.class, true);
        infoFactory.addAttribute("connectionTrackerObjectName", ObjectName.class, true);
        infoFactory.addReference("ConnectorModuleBuilder", ModuleBuilder.class);
        infoFactory.addReference("Store", ConfigurationStore.class);
        infoFactory.addReference("Repository", Repository.class);

        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addInterface(ModuleBuilder.class);

        infoFactory.setConstructor(new String[] {"transactionContextManagerObjectName", "connectionTrackerObjectName", "ConnectorModuleBuilder", "Store", "Repository", "kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
