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

package org.apache.geronimo.jetty.deployment;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import javax.management.AttributeNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.common.xml.XmlBeansUtil;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.jetty.JettyWebAppContext;
import org.apache.geronimo.jetty.JettyWebAppJACCContext;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.xbeans.geronimo.jetty.*;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;


/**
 * @version $Revision: 1.21 $ $Date: 2004/08/07 11:22:13 $
 */
public class JettyModuleBuilder implements ModuleBuilder {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[]{
        XmlBeans.typeLoaderForClassLoader(org.apache.geronimo.xbeans.j2ee.String.class.getClassLoader()),
        XmlBeans.typeLoaderForClassLoader(JettyWebAppDocument.class.getClassLoader())
    });

    private static final String PARENT_ID = "org/apache/geronimo/Server";

    public XmlObject getDeploymentPlan(URL module) throws XmlException {
        try {
            URL moduleBase;
            if (module.toString().endsWith("/")) {
                moduleBase = module;
            } else {
                moduleBase = new URL("jar:" + module.toString() + "!/");
            }
            JettyWebAppDocument plan = (JettyWebAppDocument) XmlBeansUtil.getXmlObject(new URL(moduleBase, "WEB-INF/geronimo-jetty.xml"), JettyWebAppDocument.type);
            if (plan == null) {
                return createDefaultPlan(moduleBase);
            }
            return plan;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private JettyWebAppDocument createDefaultPlan(URL moduleBase) throws XmlException {
        // load the web.xml
        URL webXmlUrl = null;
        try {
            webXmlUrl = new URL(moduleBase, "WEB-INF/web.xml");
        } catch (MalformedURLException e) {
            return null;
        }
        WebAppDocument webAppDoc = null;
        try {
            InputStream ddInputStream = webXmlUrl.openStream();
            webAppDoc = getWebAppDocument(ddInputStream);
        } catch (IOException e) {
            return null;
        } catch (DeploymentException e) {
            return null;
        }
        if (webAppDoc == null) {
            return null;
        }

        WebAppType webApp = webAppDoc.getWebApp();
        String id = webApp.getId();
        if (id == null) {
            id = moduleBase.getFile();
            if (id.endsWith("!/")) {
                id = id.substring(0, id.length() - 2);
            }
            if (id.endsWith(".war")) {
                id = id.substring(0, id.length() - 4);
            }
            if ( id.endsWith("/") ) {
                id = id.substring(0, id.length() - 1);
            }
            id = id.substring(id.lastIndexOf('/') + 1);
        }
        return newJettyWebAppDocument(webApp, id);
    }
    
    private JettyWebAppDocument newJettyWebAppDocument(WebAppType webApp, String id) {
        // construct the empty geronimo-jetty.xml
        JettyWebAppDocument jettyWebAppDocument = JettyWebAppDocument.Factory.newInstance();
        JettyWebAppType jettyWebApp = jettyWebAppDocument.addNewWebApp();

        // set the parentId, configId and context root
        jettyWebApp.setParentId(PARENT_ID);
        if ( null != webApp.getId() ) {
            id = webApp.getId();
        }
        jettyWebApp.setConfigId(id);
        jettyWebApp.setContextRoot(id);
        return jettyWebAppDocument;
    }

    public boolean canHandlePlan(XmlObject plan) {
        return plan instanceof JettyWebAppDocument;
    }

    public URI getParentId(XmlObject plan) throws DeploymentException {
        JettyWebAppType jettyWebApp = ((JettyWebAppDocument) plan).getWebApp();
        try {
            return new URI(jettyWebApp.getParentId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid parentId " + jettyWebApp.getParentId(), e);
        }
    }

    public URI getConfigId(XmlObject plan) throws DeploymentException {
        JettyWebAppType jettyWebApp = ((JettyWebAppDocument) plan).getWebApp();
        try {
            return new URI(jettyWebApp.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + jettyWebApp.getConfigId(), e);
        }
    }

    public Module createModule(String name, XmlObject plan) throws DeploymentException {
        JettyWebAppType jettyWebApp = ((JettyWebAppDocument) plan).getWebApp();
        WebModule module = new WebModule(name, URI.create("/"), jettyWebApp.getContextRoot());
        module.setVendorDD(jettyWebApp);
        return module;
    }

    public void installModule(File earFolder, EARContext earContext, Module webModule) throws DeploymentException {
        File webFolder = new File(earFolder, webModule.getURI().toString());
        
        // Unpacked EAR modules can define via application.xml either
        // (standard) packed or unpacked modules
        InstallCallback callback;
        if ( webFolder.isDirectory() ) {
            callback = new UnPackedInstallCallback(webModule, webFolder);
        } else {
            JarFile warFile;
            try {
                warFile = new JarFile(webFolder);
            } catch (IOException e) {
                throw new DeploymentException("Can not create WAR file " + webFolder, e);
            }
            callback = new PackedInstallCallback(webModule, warFile);
        }
        installModule(callback, earContext, webModule);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module webModule) throws DeploymentException {
        JarFile webAppFile;
        try {
            if (!webModule.getURI().equals(URI.create("/"))) {
                ZipEntry warEntry = earFile.getEntry(webModule.getURI().toString());
                if ( null == warEntry ) {
                    throw new DeploymentException("Can not find WAR file " + webModule.getURI());
                }
                // Unpack the nested JAR.
                File tempFile = FileUtil.toTempFile(earFile.getInputStream(warEntry));
                webAppFile = new JarFile(tempFile);
            } else {
                webAppFile = earFile;
            }
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        }
        InstallCallback callback = new PackedInstallCallback(webModule, webAppFile);
        installModule(callback, earContext, webModule);
    }

    private void installModule(InstallCallback callback, EARContext earContext, Module webModule) throws DeploymentException {
        URI moduleBase;
        if (!webModule.getURI().equals(URI.create("/"))) {
            moduleBase = URI.create(webModule.getURI() + "/");
        } else {
            moduleBase = URI.create("war/");
        }
        try {
            // load the web.xml file
            WebAppType webApp;
            try {
                InputStream ddInputStream = callback.getWebDD();
                webApp = getWebAppDocument(ddInputStream).getWebApp();
                webModule.setSpecDD(webApp);
            } catch (XmlException e) {
                throw new DeploymentException("Unable to parse web.xml", e);
            }

            // load the geronimo-jetty.xml file
            JettyWebAppType jettyWebApp = (JettyWebAppType) webModule.getVendorDD();
            if (jettyWebApp == null) {
                try {
                    InputStream jettyDDInputStream = callback.getGeronimoJettyDD();
                    JettyWebAppDocument doc;
                    if (jettyDDInputStream != null) {
                        doc = (JettyWebAppDocument) XmlBeansUtil.parse(jettyDDInputStream, JettyWebAppDocument.type);
                    } else {
                        doc = newJettyWebAppDocument(webApp, moduleBase.toString());
                    }
                    jettyWebApp = doc.getWebApp();
                    webModule.setVendorDD(jettyWebApp);
                } catch (XmlException e) {
                    throw new DeploymentException("Unable to parse openejb-jar.xml");
                }
            }
            
            // add the warfile's content to the configuration
            callback.installInEARContext(earContext, moduleBase);

            // add the dependencies declared in the geronimo-jetty.xml file
            JettyDependencyType[] dependencies = jettyWebApp.getDependencyArray();
            for (int i = 0; i < dependencies.length; i++) {
                earContext.addDependency(getDependencyURI(dependencies[i]));
            }
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        }
    }

    public void initContext(EARContext earContext, Module webModule, ClassLoader cl) {
        // web application do not add anything to the shared context
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        WebModule webModule = (WebModule) module;

        WebAppType webApp = (WebAppType) webModule.getSpecDD();
        JettyWebAppType jettyWebApp = (JettyWebAppType) webModule.getVendorDD();

        if (jettyWebApp != null) {
            JettyGbeanType[] gbeans = jettyWebApp.getGbeanArray();
            for (int i = 0; i < gbeans.length; i++) {
                GBeanHelper.addGbean(new JettyGBeanAdapter(gbeans[i]), cl, earContext);
            }
        }

        URI configID = earContext.getConfigID();

        Properties nameProps = new Properties();
        nameProps.put("J2EEServer", earContext.getJ2EEServerName());
        nameProps.put("J2EEApplication", earContext.getJ2EEApplicationName());
        nameProps.put("j2eeType", "WebModule");
        nameProps.put("name", webModule.getName());
        ObjectName name;
        try {
            name = new ObjectName(earContext.getJ2EEDomainName(), nameProps);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Unable to construct ObjectName", e);
        }

        UserTransaction userTransaction = new UserTransactionImpl();
        ReadOnlyContext compContext = buildComponentContext(earContext, webModule, webApp, jettyWebApp, userTransaction, cl);

        Security security = buildSecurityConfig(jettyWebApp);

        GBeanMBean gbean;
        try {
            if (security == null) {
                gbean = new GBeanMBean(JettyWebAppContext.GBEAN_INFO, cl);
            } else {
                gbean = new GBeanMBean(JettyWebAppJACCContext.GBEAN_INFO, cl);
            }

            URI warRoot = null;
            if (!webModule.getURI().equals(URI.create("/"))) {
                warRoot = URI.create(webModule.getURI() + "/");
            } else {
                warRoot = URI.create("war/");
            }

            String PolicyContextID = (earContext.getApplicationObjectName() == null ? module.getName() : earContext.getApplicationObjectName().toString());

            gbean.setAttribute("uri", warRoot);
            gbean.setAttribute("contextPath", webModule.getContextRoot());
            gbean.setAttribute("contextPriorityClassLoader", Boolean.valueOf(jettyWebApp.getContextPriorityClassloader()));
            if (security != null) {
                gbean.setAttribute("securityConfig", security);
                gbean.setAttribute("policyContextID", PolicyContextID);
            }
            gbean.setAttribute("componentContext", compContext);
            gbean.setAttribute("userTransaction", userTransaction);
            setResourceEnvironment(gbean, webApp.getResourceRefArray(), jettyWebApp.getResourceRefArray());
            gbean.setReferencePattern("Configuration", new ObjectName("geronimo.config:name=" + ObjectName.quote(configID.toString()))); // @todo this is used to resolve relative URIs, we should fix this
            gbean.setReferencePattern("JettyContainer", new ObjectName("*:type=WebContainer,container=Jetty")); // @todo configurable
            gbean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            gbean.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize webapp GBean", e);
        }
        earContext.addGBean(name, gbean);
    }

    public SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }

    private WebAppDocument getWebAppDocument(InputStream ddInputStream) throws XmlException, DeploymentException {
        XmlObject dd;
        try {
            dd = SchemaConversionUtils.parse(ddInputStream);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
        WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(dd);
        return webAppDoc;
    }

    private ReadOnlyContext buildComponentContext(EARContext earContext, WebModule webModule, WebAppType webApp, JettyWebAppType jettyWebApp, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        Map ejbRefMap = mapRefs(jettyWebApp.getEjbRefArray());
        Map ejbLocalRefMap = mapRefs(jettyWebApp.getEjbLocalRefArray());
        Map resourceRefMap = mapRefs(jettyWebApp.getResourceRefArray());
        Map resourceEnvRefMap = mapRefs(jettyWebApp.getResourceEnvRefArray());

        URI uri = webModule.getURI();

        return ENCConfigBuilder.buildComponentContext(earContext,
                uri,
                userTransaction,
                webApp.getEnvEntryArray(),
                webApp.getEjbRefArray(), ejbRefMap,
                webApp.getEjbLocalRefArray(), ejbLocalRefMap,
                webApp.getResourceRefArray(), resourceRefMap,
                webApp.getResourceEnvRefArray(), resourceEnvRefMap,
                webApp.getMessageDestinationRefArray(),
                cl);

    }

    private static Map mapRefs(JettyRemoteRefType[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                JettyRemoteRefType ref = refs[i];
                refMap.put(ref.getRefName(), new JettyRefAdapter(ref));
            }
        }
        return refMap;
    }

    private static Security buildSecurityConfig(JettyWebAppType jettyWebApp) {
        Security security = null;

        JettySecurityType securityType = jettyWebApp.getSecurity();
        if (securityType != null) {
            security = new Security();

            security.setUseContextHandler(securityType.getUseContextHandler());

            JettyDefaultPrincipalType defaultPrincipalType = securityType.getDefaultPrincipal();
            DefaultPrincipal defaultPrincipal = new DefaultPrincipal();

            defaultPrincipal.setRealmName(defaultPrincipalType.getRealmName());
            defaultPrincipal.setPrincipal(buildPrincipal(defaultPrincipalType.getPrincipal()));

            security.setDefaultPrincipal(defaultPrincipal);

            JettyRoleMappingsType roleMappingsType = securityType.getRoleMappings();
            if (roleMappingsType != null) {
                for (int i = 0; i < roleMappingsType.sizeOfRoleArray(); i++) {
                    JettyRoleType roleType = roleMappingsType.getRoleArray(i);
                    Role role = new Role();

                    role.setRoleName(roleType.getRoleName());

                    for (int j = 0; j < roleType.sizeOfRealmArray(); j++) {
                        JettyRealmType realmType = roleType.getRealmArray(j);
                        Realm realm = new Realm();

                        realm.setRealmName(realmType.getRealmName());

                        for (int k = 0; k < realmType.sizeOfPrincipalArray(); k++) {
                            realm.getPrincipals().add(buildPrincipal(realmType.getPrincipalArray(k)));
                        }

                        role.getRealms().add(realm);
                    }

                    security.getRoleMappings().add(role);
                }
            }
        }

        return security;
    }

    private static Principal buildPrincipal(JettyPrincipalType principalType) {
        Principal principal = new Principal();

        principal.setClassName(principalType.getClass1());
        principal.setPrincipalName(principalType.getName());
        principal.setDesignatedRunAs(principalType.isSetDesignatedRunAs());

        return principal;
    }

 
    private void setResourceEnvironment(GBeanMBean bean, ResourceRefType[] resourceRefArray, JettyLocalRefType[] jettyResourceRefArray) throws AttributeNotFoundException, ReflectionException {
        Map openejbNames = new HashMap();
        for (int i = 0; i < jettyResourceRefArray.length; i++) {
            JettyLocalRefType jettyLocalRefType = jettyResourceRefArray[i];
            openejbNames.put(jettyLocalRefType.getRefName(), jettyLocalRefType.getTargetName());
        }
        Set unshareableResources = new HashSet();
        Set applicationManagedSecurityResources = new HashSet();
        for (int i = 0; i < resourceRefArray.length; i++) {
            ResourceRefType resourceRefType = resourceRefArray[i];
            String name = (String) openejbNames.get(resourceRefType.getResRefName().getStringValue());
            if ("Unshareable".equals(getJ2eeStringValue(resourceRefType.getResSharingScope()))) {
                unshareableResources.add(name);
            }
            if ("Application".equals(resourceRefType.getResAuth().getStringValue())) {
                applicationManagedSecurityResources.add(name);
            }
        }
        bean.setAttribute("unshareableResources", unshareableResources);
        bean.setAttribute("applicationManagedSecurityResources", applicationManagedSecurityResources);
    }


    private static String getJ2eeStringValue(org.apache.geronimo.xbeans.j2ee.String string) {
        if (string == null) {
            return null;
        }
        return string.getStringValue();
    }

    private static void assureEJBObjectInterface(String remote, ClassLoader cl) throws DeploymentException {
        assureInterface(remote, "javax.ejb.EJBObject", "Remote", cl);
    }

    private static void assureEJBHomeInterface(String home, ClassLoader cl) throws DeploymentException {
        assureInterface(home, "javax.ejb.EJBHome", "Home", cl);
    }

    private static void assureEJBLocalObjectInterface(String local, ClassLoader cl) throws DeploymentException {
        assureInterface(local, "javax.ejb.EJBLocalObject", "Local", cl);
    }

    private static void assureEJBLocalHomeInterface(String localHome, ClassLoader cl) throws DeploymentException {
        assureInterface(localHome, "javax.ejb.EJBLocalHome", "LocalHome", cl);
    }

    private static void assureInterface(String interfaceName, String superInterfaceName, String interfactType, ClassLoader cl) throws DeploymentException {
        Class clazz = null;
        try {
            clazz = cl.loadClass(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(interfactType + " interface class not found: " + interfaceName);
        }
        if (!clazz.isInterface()) {
            throw new DeploymentException(interfactType + " interface is not an interface: " + interfaceName);
        }
        Class superInterface = null;
        try {
            superInterface = cl.loadClass(superInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Class " + superInterfaceName + " could not be loaded");
        }
        if (clazz.isAssignableFrom(superInterface)) {
            throw new DeploymentException(interfactType + " interface does not extend " + superInterfaceName + ": " + interfaceName);
        }
    }

    private URI getDependencyURI(JettyDependencyType dep) throws DeploymentException {
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


    private byte[] getBytes(InputStream is) throws IOException {
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count;
        while ((count = is.read(buffer)) > 0) {
            baos.write(buffer, 0, count);
        }
        return baos.toByteArray();
    }

    private interface InstallCallback {
        
        public void installInEARContext(EARContext earContext, URI moduleBase) throws DeploymentException, IOException;
        
        public InputStream getWebDD() throws DeploymentException, IOException;
        
        public InputStream getGeronimoJettyDD() throws DeploymentException, IOException;

    }

    private static final class UnPackedInstallCallback implements InstallCallback {
        
        private final File webFolder;
        
        private final Module webModule;
        
        private UnPackedInstallCallback(Module webModule, File webFolder) {
            this.webFolder = webFolder;
            this.webModule = webModule;
        }
        
        public void installInEARContext(EARContext earContext, URI moduleBase) throws DeploymentException, IOException {
            URI warRoot = webFolder.toURI();
            // add the warfile's content to the configuration
            Collection files = new ArrayList();
            FileUtil.listRecursiveFiles(webFolder, files);
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                File file = (File) iter.next();
                URI fileURI = warRoot.relativize(file.toURI());
                URI target = moduleBase.resolve(fileURI);
                earContext.addFile(target, file);
            }
        }
        
        public InputStream getWebDD() throws DeploymentException, IOException {
            File webAppFile = new File(webFolder, "WEB-INF/web.xml");
            if ( !webAppFile.exists() ) {
                throw new DeploymentException("No WEB-INF/web.xml in module [" + webModule.getName() + "]");
            }
            return new FileInputStream(webAppFile);
        }
        
        public InputStream getGeronimoJettyDD() throws DeploymentException, IOException {
            File jettyWebAppFile = new File(webFolder, "WEB-INF/geronimo-jetty.xml");
            if ( jettyWebAppFile.exists() ) {
                return new FileInputStream(jettyWebAppFile);
            }
            return null;
        }
        
    }

    private static final class PackedInstallCallback implements InstallCallback {

        private final Module webModule;
        
        private final JarFile webAppFile;
        
        private PackedInstallCallback(Module webModule, JarFile webAppFile) {
            this.webModule = webModule;
            this.webAppFile = webAppFile;
        }
        
        public void installInEARContext(EARContext earContext, URI moduleBase) throws DeploymentException, IOException {
            JarInputStream jarIS = new JarInputStream(new FileInputStream(webAppFile.getName()));
            for (JarEntry entry; (entry = jarIS.getNextJarEntry()) != null; jarIS.closeEntry()) {
                URI target = moduleBase.resolve(entry.getName());
                earContext.addFile(target, jarIS);
            }
        }
        
        public InputStream getWebDD() throws DeploymentException, IOException {
            JarEntry entry = webAppFile.getJarEntry("WEB-INF/web.xml");
            if (entry == null) {
                throw new DeploymentException("No WEB-INF/web.xml in module [" + webModule.getName() + "]");
            }
            return webAppFile.getInputStream(entry);
        }
        
        public InputStream getGeronimoJettyDD() throws DeploymentException, IOException {
            JarEntry entry = webAppFile.getJarEntry("WEB-INF/geronimo-jetty.xml");
            if (entry != null) {
                return webAppFile.getInputStream(entry);
            }
            return null;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(JettyModuleBuilder.class);
        infoFactory.addInterface(ModuleBuilder.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
