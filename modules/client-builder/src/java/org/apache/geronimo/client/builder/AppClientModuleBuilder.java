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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.UserTransaction;

import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.FileUtil;
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
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientDocument;
import org.apache.geronimo.xbeans.geronimo.client.GerApplicationClientType;
import org.apache.geronimo.xbeans.geronimo.client.GerDependencyType;
import org.apache.geronimo.xbeans.geronimo.client.GerGbeanType;
import org.apache.geronimo.xbeans.geronimo.client.GerRemoteRefType;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientDocument;
import org.apache.geronimo.xbeans.j2ee.ApplicationClientType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.system.main.CommandLineManifest;
import org.apache.geronimo.client.AppClientContainer;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class AppClientModuleBuilder implements ModuleBuilder {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(GerApplicationClientDocument.class.getClassLoader())
    });

    private static final URI CONFIG_ID = URI.create("application-client");

    private final Kernel kernel;
    private final Repository repository;

    public AppClientModuleBuilder(Kernel kernel, Repository repository) {
        this.kernel = kernel;
        this.repository = repository;
    }

    public XmlObject getDeploymentPlan(URL module) throws XmlException {
        try {
            URL moduleBase;
            if (module.toString().endsWith("/")) {
                moduleBase = module;
            } else {
                moduleBase = new URL("jar:" + module.toString() + "!/");
            }
            GerApplicationClientDocument plan = (GerApplicationClientDocument) XmlBeansUtil.getXmlObject(new URL(moduleBase, "META-INF/geronimo-application-client.xml"), GerApplicationClientDocument.type);
            if (plan == null) {
                return createDefaultPlan(moduleBase);
            }
            return plan;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private GerApplicationClientDocument createDefaultPlan(URL moduleBase) throws XmlException {
        // load the application-client.xml
        URL appClientXmlUrl = null;
        try {
            appClientXmlUrl = new URL(moduleBase, "META-INF/application-client.xml");
        } catch (MalformedURLException e) {
            return null;
        }
        ApplicationClientDocument appClientDoc = null;
        try {
            InputStream ddInputStream = appClientXmlUrl.openStream();
            appClientDoc = getAppClientDocument(ddInputStream);
        } catch (IOException e) {
            return null;
        } catch (DeploymentException e) {
            return null;
        }
        if (appClientDoc == null) {
            return null;
        }

        ApplicationClientType appClient = appClientDoc.getApplicationClient();
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
        // construct the empty geronimo-jetty.xml
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

    public Module createModule(String name, XmlObject plan) throws DeploymentException {
        GerApplicationClientType geronimoAppClient = ((GerApplicationClientDocument) plan).getApplicationClient();
        AppClientModule module = new AppClientModule(name, URI.create("/"));
        module.setVendorDD(geronimoAppClient);
        return module;
    }

    public void installModule(File earFolder, EARContext earContext, Module appClientModule) throws DeploymentException {
        File appClientFolder = new File(earFolder, appClientModule.getURI().toString());

        InstallCallback callback;
        if (appClientFolder.isDirectory()) {
            callback = new UnPackedInstallCallback(appClientModule, appClientFolder);
        } else {
            JarFile appClientFile;
            try {
                appClientFile = new JarFile(appClientFolder);
            } catch (IOException e) {
                throw new DeploymentException("Can not create application File file " + appClientFolder, e);
            }
            callback = new PackedInstallCallback(appClientModule, appClientFile);
        }
        installModule(callback, earContext, appClientModule);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module appClientModule) throws DeploymentException {
        JarFile appClientFile;
        try {
            if (!appClientModule.getURI().equals(URI.create("/"))) {
                ZipEntry appClientEntry = earFile.getEntry(appClientModule.getURI().toString());
                if (null == appClientEntry) {
                    throw new DeploymentException("Can not find application client file " + appClientModule.getURI());
                }
                // Unpack the nested JAR
                File tempFile = FileUtil.toTempFile(earFile.getInputStream(appClientEntry));
                appClientFile = new JarFile(tempFile);
            } else {
                appClientFile = earFile;
            }
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying applicaiton client", e);
        }
        InstallCallback callback = new PackedInstallCallback(appClientModule, appClientFile);
        installModule(callback, earContext, appClientModule);
    }

    private void installModule(InstallCallback callback, EARContext earContext, Module appClientModule) throws DeploymentException {
        URI moduleBase;
        if (!appClientModule.getURI().equals(URI.create("/"))) {
            moduleBase = URI.create(appClientModule.getURI() + "/");
        } else {
            moduleBase = URI.create("war/");
        }
        try {
            // load the application-client.xml file
            ApplicationClientType appClient;
            try {
                // todo what is this install callback stuff
                InputStream ddInputStream = callback.getWebDD();
                appClient = getAppClientDocument(ddInputStream).getApplicationClient();
                appClientModule.setSpecDD(appClient);
            } catch (XmlException e) {
                throw new DeploymentException("Unable to parse " +
                        (null == appClientModule.getAltSpecDD() ?
                        "WEB-INF/web.xml" :
                        appClientModule.getAltSpecDD().toString()), e);
            }

            // load the geronimo-application-client.xml file
            GerApplicationClientType geronimoAppClient = (GerApplicationClientType) appClientModule.getVendorDD();
            if (geronimoAppClient == null) {
                try {
                    // todo what is this install callback stuff
                    InputStream geronimoAppClientDDInputStream = callback.getGeronimoJettyDD();
                    GerApplicationClientDocument geronimoAppClientDoc;
                    if (geronimoAppClientDDInputStream != null) {
                        geronimoAppClientDoc = (GerApplicationClientDocument) XmlBeansUtil.parse(geronimoAppClientDDInputStream, GerApplicationClientDocument.type);
                    } else {
                        geronimoAppClientDoc = newGerApplicationClientDocument(appClient, moduleBase.toString());
                    }
                    geronimoAppClient = geronimoAppClientDoc.getApplicationClient();
                    appClientModule.setVendorDD(geronimoAppClient);
                } catch (XmlException e) {
                    throw new DeploymentException("Unable to parse " +
                            (null == appClientModule.getAltVendorDD() ?
                            "WEB-INF/geronimo-jetty.xml" :
                            appClientModule.getAltVendorDD().toString()), e);
                }
            }

            // add the applicationClient's content to the configuration
            callback.installInEARContext(earContext, moduleBase);

            // add the dependencies declared in the geronimo-jetty.xml file
            GerDependencyType[] dependencies = geronimoAppClient.getDependencyArray();
            for (int i = 0; i < dependencies.length; i++) {
                earContext.addDependency(getDependencyURI(dependencies[i]));
            }
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        }
    }

    public void initContext(EARContext earContext, Module webModule, ClassLoader cl) {
        // application clients do not add anything to the shared context
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        AppClientModule appClientModule = (AppClientModule) module;
        URI appClientModuleLocation;
        if (!appClientModule.getURI().equals(URI.create("/"))) {
            appClientModuleLocation = appClientModule.getURI();
        } else {
            appClientModuleLocation = URI.create("client.jar");
        }

        // add the app client module gbean
        Properties nameProps = new Properties();
        nameProps.put("J2EEServer", earContext.getJ2EEServerName());
        nameProps.put("J2EEApplication", earContext.getJ2EEApplicationName());
        nameProps.put("j2eeType", "AppClientModule");
        nameProps.put("name", appClientModule.getName());
        ObjectName appClientModuleObjectName;
        try {
            appClientModuleObjectName = new ObjectName(earContext.getJ2EEDomainName(), nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }

        GBeanMBean appClientModuleGBean = new GBeanMBean(J2EEAppClientModuleImpl.GBEAN_INFO, cl);
        try {
            appClientModuleGBean.setReferencePatterns("J2EEServer", Collections.singleton(earContext.getServerObjectName()));
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                appClientModuleGBean.setReferencePatterns("J2EEApplication", Collections.singleton(earContext.getApplicationObjectName()));
            }
            appClientModuleGBean.setAttribute("deploymentDescriptor", null);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
        }
        earContext.addGBean(appClientModuleObjectName, appClientModuleGBean);

        // Create a new executable jar within the earcontext
        ApplicationClientType appClient = (ApplicationClientType) appClientModule.getSpecDD();
        GerApplicationClientType geronimoAppClient = (GerApplicationClientType) appClientModule.getVendorDD();

        JarOutputStream earOutputStream = earContext.getJos();
        try {
            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), "org.apache.geronimo.system.main.CommandLine");
            mainAttributes.putValue(CommandLineManifest.MAIN_GBEAN.toString(), appClientModuleObjectName.getCanonicalName());
            mainAttributes.putValue(CommandLineManifest.MAIN_METHOD.toString(), "deploy");
            mainAttributes.putValue(CommandLineManifest.CONFIGURATIONS.toString(), CONFIG_ID.toString());

            earOutputStream.putNextEntry(new ZipEntry(appClientModuleLocation.getPath()));
            JarOutputStream appClientOutputStream = new JarOutputStream(new BufferedOutputStream(earOutputStream), manifest);

            // this is an executable jar add the startup jar finder file
            appClientOutputStream.putNextEntry(new ZipEntry("META-INF/startup-jar"));
            appClientOutputStream.closeEntry();

            DeploymentContext appClientDeploymentContext = null;
            try {
                appClientDeploymentContext = new DeploymentContext(earOutputStream, CONFIG_ID, ConfigurationModuleType.SERVICE, null, kernel);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }

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

            ClassLoader appClientClassLoader = appClientDeploymentContext.getClassLoader(repository);

            if (geronimoAppClient != null) {
                GerGbeanType[] gbeans = geronimoAppClient.getGbeanArray();
                for (int i = 0; i < gbeans.length; i++) {
                    GBeanHelper.addGbean(new AppClientGBeanAdapter(gbeans[i]), appClientClassLoader, appClientDeploymentContext);
                }
            }

            UserTransaction userTransaction = new UserTransactionImpl();
            ReadOnlyContext componentContext = buildComponentContext(earContext, appClientModule, appClient, geronimoAppClient, userTransaction, appClientClassLoader);
            GBeanMBean appClienContainerGBean = new GBeanMBean(AppClientContainer.GBEAN_INFO, cl);
            try {
                appClientModuleGBean.setAttribute("componentContext", componentContext);
            } catch (Exception e) {
                throw new DeploymentException("Unable to initialize AppClientModule GBean", e);
            }
            appClientDeploymentContext.addGBean(appClientModuleObjectName, appClienContainerGBean);

            appClientDeploymentContext.close();
        } catch (IOException e) {
            throw new DeploymentException(e);
        } finally {
            try {
                earOutputStream.closeEntry();
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        }

    }

    public SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }

    private ApplicationClientDocument getAppClientDocument(InputStream ddInputStream) throws XmlException, DeploymentException {
        XmlObject dd;
        try {
            dd = SchemaConversionUtils.parse(ddInputStream);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
        ApplicationClientDocument applicationClientDoc = SchemaConversionUtils.convertToApplicationClientSchema(dd);
        return applicationClientDoc;
    }

    private ReadOnlyContext buildComponentContext(EARContext earContext, AppClientModule appClientModuel, ApplicationClientType appClient, GerApplicationClientType geronimoAppClient, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        Map ejbRefMap = mapRefs(geronimoAppClient.getEjbRefArray());
        Map resourceRefMap = mapRefs(geronimoAppClient.getResourceRefArray());
        Map resourceEnvRefMap = mapRefs(geronimoAppClient.getResourceEnvRefArray());

        URI uri = appClientModuel.getURI();

        return ENCConfigBuilder.buildComponentContext(earContext,
                uri,
                userTransaction,
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
                refMap.put(ref.getRefName(), new AppClientRefAdapter(ref));
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

    private static abstract class InstallCallback {

        protected final Module webModule;

        private InstallCallback(Module webModule) {
            this.webModule = webModule;
        }

        public abstract void installInEARContext(EARContext earContext, URI moduleBase) throws DeploymentException, IOException;

        public InputStream getWebDD() throws DeploymentException, IOException {
            if (null == webModule.getAltSpecDD()) {
                return null;
            }
            return webModule.getAltSpecDD().openStream();
        }

        public InputStream getGeronimoJettyDD() throws DeploymentException, IOException {
            if (null == webModule.getAltVendorDD()) {
                return null;
            }
            return webModule.getAltVendorDD().openStream();
        }

    }

    private static final class UnPackedInstallCallback extends InstallCallback {

        private final File webFolder;

        private UnPackedInstallCallback(Module webModule, File webFolder) {
            super(webModule);
            this.webFolder = webFolder;
        }

        public void installInEARContext(EARContext earContext, URI moduleBase) throws IOException {
            URI warRoot = webFolder.toURI();
            // add the warfile's content to the configuration
            Collection files = new ArrayList();
            FileUtil.listRecursiveFiles(webFolder, files);
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                File file = (File) iter.next();
                URI fileURI = warRoot.relativize(file.toURI());
                URI target = moduleBase.resolve(fileURI);
                // TODO gets rid of these tests when Jetty will use the
                // serialized Geronimo DD.
                if (fileURI.equals("WEB-INF/web.xml") &&
                        null != webModule.getAltSpecDD()) {
                } else {
                    earContext.addFile(target, file);
                }
            }
        }

        public InputStream getWebDD() throws DeploymentException, IOException {
            InputStream in = super.getWebDD();
            if (null != in) {
                return in;
            }
            File webAppFile = new File(webFolder, "WEB-INF/web.xml");
            if (!webAppFile.exists()) {
                throw new DeploymentException("No WEB-INF/web.xml in module [" + webModule.getName() + "]");
            }
            return new FileInputStream(webAppFile);
        }

        public InputStream getGeronimoJettyDD() throws DeploymentException, IOException {
            InputStream in = super.getGeronimoJettyDD();
            if (null != in) {
                return in;
            }
            File jettyWebAppFile = new File(webFolder, "WEB-INF/geronimo-jetty.xml");
            if (jettyWebAppFile.exists()) {
                return new FileInputStream(jettyWebAppFile);
            }
            return null;
        }

    }

    private static final class PackedInstallCallback extends InstallCallback {

        private final JarFile webAppFile;

        private PackedInstallCallback(Module webModule, JarFile webAppFile) {
            super(webModule);
            this.webAppFile = webAppFile;
        }

        public void installInEARContext(EARContext earContext, URI moduleBase) throws IOException {
            JarInputStream jarIS = new JarInputStream(new FileInputStream(webAppFile.getName()));
            for (JarEntry entry; (entry = jarIS.getNextJarEntry()) != null; jarIS.closeEntry()) {
                URI target = moduleBase.resolve(entry.getName());
                // TODO gets rid of these tests when Jetty will use the
                // serialized Geronimo DD.
                if (entry.getName().equals("WEB-INF/web.xml") &&
                        null != webModule.getAltSpecDD()) {
                } else {
                    earContext.addFile(target, jarIS);
                }
            }
        }

        public InputStream getWebDD() throws DeploymentException, IOException {
            InputStream in = super.getWebDD();
            if (null != in) {
                return in;
            }
            JarEntry entry = webAppFile.getJarEntry("WEB-INF/web.xml");
            if (entry == null) {
                throw new DeploymentException("No WEB-INF/web.xml in module [" + webModule.getName() + "]");
            }
            return webAppFile.getInputStream(entry);
        }

        public InputStream getGeronimoJettyDD() throws DeploymentException, IOException {
            InputStream in = super.getGeronimoJettyDD();
            if (null != in) {
                return in;
            }
            JarEntry entry = webAppFile.getJarEntry("WEB-INF/geronimo-jetty.xml");
            if (entry != null) {
                return webAppFile.getInputStream(entry);
            }
            return null;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AppClientModuleBuilder.class);
        infoFactory.addInterface(ModuleBuilder.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
