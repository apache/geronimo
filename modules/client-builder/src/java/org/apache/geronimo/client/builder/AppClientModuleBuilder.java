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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.deployment.util.JarUtil;
import org.apache.geronimo.deployment.util.IOUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.AppClientModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
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
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientDocument;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;


/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class AppClientModuleBuilder implements ModuleBuilder {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{
//        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
//        XmlBeans.typeLoaderForClassLoader(GerApplicationClientDocument.class.getClassLoader())
    });

    private final Kernel kernel;
    private final Repository repository;
    private final ConfigurationStore store;
    private static final URI PARENT_ID = URI.create("org/apache/geronimo/Client");

    public AppClientModuleBuilder(Kernel kernel, Repository repository, ConfigurationStore store) {
        this.kernel = kernel;
        this.repository = repository;
        this.store = store;
    }

    public XmlObject parseSpecDD(URL path) throws DeploymentException {
        InputStream in = null;
        try {
            // check if we have an alt spec dd
            in = path.openStream();
//            XmlObject dd = SchemaConversionUtils.parse(in);
//            ApplicationClientDocument applicationClientDoc = SchemaConversionUtils.convertToApplicationClientSchema(dd);
            ApplicationClientDocument applicationClientDoc = ApplicationClientDocument.Factory.parse(path);
            return applicationClientDoc.getApplicationClient();
        } catch (Exception e) {
            throw new DeploymentException("Unable to parse " + path, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    public XmlObject parseSpecDD(String specDD) throws DeploymentException {
        try {
            // check if we have an alt spec dd
            ApplicationClientDocument applicationClientDoc = ApplicationClientDocument.Factory.parse(specDD);
            return applicationClientDoc.getApplicationClient();
        } catch (Exception e) {
            throw new DeploymentException("Unable to parse specDD", e);
        }
    }

    public XmlObject getDeploymentPlan(URL module) throws DeploymentException {
        try {
            URL moduleBase;
            if (module.toString().endsWith("/")) {
                moduleBase = module;
            } else {
                moduleBase = new URL("jar:" + module.toString() + "!/");
            }
            URL path = new URL(moduleBase, "META-INF/geronimo-application-client.xml");
            XmlObject dd = SchemaConversionUtils.parse(path.openStream());

            GerApplicationClientDocument plan = (GerApplicationClientDocument) validateVendorDD(dd);
            if (plan == null) {
                return createDefaultPlan(moduleBase);
            }
            return plan;
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }
    }

    public XmlObject validateVendorDD(XmlObject dd) throws DeploymentException {
        try {
            dd = SchemaConversionUtils.convertToGeronimoNamingSchema(dd);
            return dd.changeType(GerApplicationClientDocument.type);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private GerApplicationClientDocument createDefaultPlan(URL moduleBase) {
        // load the application-client.xml
        URL appClientXmlUrl = null;
        try {
            appClientXmlUrl = new URL(moduleBase, "META-INF/application-client.xml");
        } catch (MalformedURLException e) {
            return null;
        }

        ApplicationClientType appClient;
        try {
            appClient = (ApplicationClientType) parseSpecDD(appClientXmlUrl);
        } catch (DeploymentException e) {
            return null;
        }
        String id = appClient.getId();
        if (id == null) {
            id = moduleBase.getFile();
            if (id.endsWith("!/")) {
                id = id.substring(0, id.length() - 2);
            }
            if (id.endsWith(".jar")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
            id = id.substring(id.lastIndexOf('/') + 1);
        }
        return newGerApplicationClientDocument(appClient, id);
    }

    private GerApplicationClientDocument newGerApplicationClientDocument(ApplicationClientType appClient, String id) {
        GerApplicationClientDocument geronimoAppClientDoc = GerApplicationClientDocument.Factory.newInstance();
        GerApplicationClientType geronimoAppClient = geronimoAppClientDoc.addNewApplicationClient();

        // set the parentId, configId and context root
        if (null != appClient.getId()) {
            id = appClient.getId();
        }
        geronimoAppClient.setConfigId(id);
        return geronimoAppClientDoc;
    }

    public boolean canHandlePlan(XmlObject plan) {
        return plan instanceof GerApplicationClientDocument;
    }

    public URI getParentId(XmlObject plan) throws DeploymentException {
        GerApplicationClientType geronimoAppClient = ((GerApplicationClientDocument) plan).getApplicationClient();
        try {
            return new URI(geronimoAppClient.getParentId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid parentId " + geronimoAppClient.getParentId(), e);
        }
    }

    public URI getConfigId(XmlObject plan) throws DeploymentException {
        GerApplicationClientType geronimoAppClient = ((GerApplicationClientDocument) plan).getApplicationClient();
        try {
            return new URI(geronimoAppClient.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + geronimoAppClient.getConfigId(), e);
        }
    }

    public Module createModule(String name, JarFile moduleFile, XmlObject vendorDD) throws DeploymentException {
        return createModule(name, moduleFile, vendorDD, "app-client", null);
    }

    public Module createModule(String name, JarFile moduleFile, XmlObject vendorDD, String targetPath, URL specDDUrl) throws DeploymentException {
        URI moduleURI;
        if (targetPath != null) {
            moduleURI = URI.create(targetPath);
            if (targetPath.endsWith("/")) {
                throw new DeploymentException("targetPath must not end with a '/'");
            }
        } else {
            targetPath = "app-client";
            moduleURI = URI.create("");
        }

        if (specDDUrl == null) {
            specDDUrl = JarUtil.createJarURL(moduleFile, "META-INF/application-client.xml");
        }
        String specDD;
        try {
            specDD = IOUtil.readAll(specDDUrl);
        } catch (IOException e) {
            throw new DeploymentException("Unable to read specDD: " + specDDUrl.toExternalForm());
        }
        ApplicationClientType appClient = (ApplicationClientType) parseSpecDD(specDD);

        if (vendorDD == null) {
            try {
                JarEntry entry = moduleFile.getJarEntry("META-INF/geronimo-application-client.xml");
                if (entry != null) {
                    InputStream in = moduleFile.getInputStream(entry);
                    if (in != null) {
                        vendorDD = XmlBeansUtil.parse(in, GerApplicationClientDocument.type);
                    }
                }
            } catch (Exception e) {
                throw new DeploymentException("Unable to parse META-INF/geronimo-application-client.xml", e);
            }
        }
        if (vendorDD == null) {
            vendorDD = newGerApplicationClientDocument(appClient, name);
        }

        GerApplicationClientDocument geronimoAppClientDoc = (GerApplicationClientDocument) vendorDD;
        GerApplicationClientType geronimoAppClient = geronimoAppClientDoc.getApplicationClient();

        return new AppClientModule(name, moduleURI, moduleFile, targetPath, appClient, geronimoAppClient);
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
    }

    public void initContext(EARContext earContext, Module webModule, ClassLoader cl) {
        // application clients do not add anything to the shared context
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader earClassLoader) throws DeploymentException {
        AppClientModule appClientModule = (AppClientModule) module;

        ApplicationClientType appClient = (ApplicationClientType) appClientModule.getSpecDD();
        GerApplicationClientType geronimoAppClient = (GerApplicationClientType) appClientModule.getVendorDD();

        // get the app client main class
        JarFile moduleFile = module.getModuleFile();
        String mainClasss = null;
        try {
            Manifest manifest = moduleFile.getManifest();
            if (manifest == null) {
                throw new DeploymentException("App client module jar does not contain a manifest: " + moduleFile.getName());
            }
            mainClasss = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            if (mainClasss == null) {
                throw new DeploymentException("App client module jar does not have Main-Class defined in the manifest: " + moduleFile.getName());
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
        GBeanMBean appClientModuleGBean = new GBeanMBean(J2EEAppClientModuleImpl.GBEAN_INFO, earClassLoader);
        try {
            appClientModuleGBean.setReferencePatterns("J2EEServer", Collections.singleton(earContext.getServerObjectName()));
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                appClientModuleGBean.setReferencePatterns("J2EEApplication", Collections.singleton(earContext.getApplicationObjectName()));
            }
            appClientModuleGBean.setAttribute("deploymentDescriptor", null);

            ReadOnlyContext componentContext = buildComponentContext(earContext, appClientModule, appClient, geronimoAppClient, earClassLoader);
            appClientModuleGBean.setAttribute("componentContext", componentContext);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
        }
        earContext.addGBean(appClientModuleName, appClientModuleGBean);



        // Create a new executable jar within the ear context
        DeploymentContext appClientDeploymentContext = null;
        File appClientConfiguration = null;
        try {
            appClientConfiguration = FileUtil.createTempFile();
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(appClientConfiguration)));

            // construct the app client deployment context... this is the same class used by the ear context
            try {
                URI configId = URI.create(geronimoAppClient.getConfigId());
                appClientDeploymentContext = new DeploymentContext(jos, configId, ConfigurationModuleType.APP_CLIENT, PARENT_ID, kernel);
            } catch (Exception e) {
                throw new DeploymentException("Could not create a deployment context for the app client", e);
            }

            // extract the ejbJar file into a standalone packed jar file and add the contents to the output
            File appClientJarFile = JarUtil.extractToPackedJar(moduleFile);
            appClientDeploymentContext.addInclude(URI.create(module.getTargetPath()), appClientJarFile.toURL());

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

            // get the classloader
            ClassLoader appClientClassLoader = appClientDeploymentContext.getClassLoader(repository);

            // pop in all the gbeans declared in the geronimo app client file
            if (geronimoAppClient != null) {
                GerGbeanType[] gbeans = geronimoAppClient.getGbeanArray();
                for (int i = 0; i < gbeans.length; i++) {
                    GBeanHelper.addGbean(new AppClientGBeanAdapter(gbeans[i]), appClientClassLoader, appClientDeploymentContext);
                }
            }

            // finally add the app client container
            ObjectName appClienContainerName = ObjectName.getInstance("geronimo.client:type=ClientContainer");
            GBeanMBean appClienContainerGBean = new GBeanMBean("org.apache.geronimo.client.AppClientContainer", appClientClassLoader);
            try {
                appClienContainerGBean.setAttribute("mainClassName", mainClasss);
                appClienContainerGBean.setAttribute("appClientModuleName", appClientModuleName);
                appClienContainerGBean.setReferencePattern("JNDIContext", new ObjectName("geronimo.client:type=JNDIContext"));
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
            store.install(appClientConfiguration.toURL());
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    public SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
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
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("Repository", Repository.class);
        infoFactory.addReference("Store", ConfigurationStore.class);
        infoFactory.setConstructor(new String[] {"kernel", "Repository", "Store"});
        infoFactory.addInterface(ModuleBuilder.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
