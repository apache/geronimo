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

import java.io.ByteArrayInputStream;
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
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import javax.management.AttributeNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.NamingException;
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
import org.apache.geronimo.j2ee.deployment.ModuleBuilderWithUnpack;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.jetty.JettyWebAppContext;
import org.apache.geronimo.jetty.JettyWebAppJACCContext;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.transaction.UserTransactionImpl;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyDefaultPrincipalType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyDependencyType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyGbeanType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyLocalRefType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyPrincipalType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyRealmType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyRoleType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettySecurityType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;


/**
 * @version $Revision: 1.17 $ $Date: 2004/07/30 00:25:13 $
 */
public class JettyModuleBuilder implements ModuleBuilderWithUnpack {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderUnion(new SchemaTypeLoader[] {
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
        XmlObject dd = null;
        try {
            dd = SchemaConversionUtils.parse(webXmlUrl.openStream());
        } catch (IOException e) {
            return null;
        }
        WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(dd);
        if (webAppDoc == null) {
            return null;
        }

        // construct the empty geronimo-jetty.xml
        JettyWebAppDocument jettyWebAppDocument = JettyWebAppDocument.Factory.newInstance();
        JettyWebAppType jettyWebApp = jettyWebAppDocument.addNewWebApp();

        // set the parentId, configId and context root
        jettyWebApp.setParentId(PARENT_ID);
        String id = webAppDoc.getWebApp().getId();
        if (id == null) {
            id = moduleBase.getFile();
            if (id.endsWith("!/")) {
                id = id.substring(0, id.length()-2);
            }
            if (id.endsWith(".war")) {
                id = id.substring(0, id.length()-4);
            }
            id = id.substring(id.lastIndexOf('/') + 1);
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

    public void installModule(File earFolder, EARContext earContext,
        Module webModule) throws DeploymentException {
        try {
            File webFolder = new File(earFolder, webModule.getURI().toString());
            URI warRoot = webFolder.toURI();
            URI moduleBase;
            if ( !webModule.getURI().equals(URI.create("/")) ) {
                moduleBase = URI.create(webModule.getURI().toString() + "/");
            } else {
                moduleBase = URI.create("war/");
            }

            WebAppType webApp = null;
            JettyWebAppType jettyWebApp = (JettyWebAppType) webModule.getVendorDD();

            // add the warfile's content to the configuration
            Collection files = new ArrayList();
            FileUtil.listRecursiveFiles(webFolder, files);
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                File file = (File) iter.next();
                URI fileURI = warRoot.relativize(file.toURI());
                URI target = moduleBase.resolve(fileURI);
                if (fileURI.toString().equals("WEB-INF/web.xml")) {
                    earContext.addFile(target, file);
                    try {
                        XmlObject dd = SchemaConversionUtils.parse(new FileInputStream(file));
                        WebAppDocument doc = SchemaConversionUtils.convertToServletSchema(dd);
                        webApp = doc.getWebApp();
                    } catch (XmlException e) {
                        throw new DeploymentException("Unable to parse web.xml", e);
                    }
                } else if (jettyWebApp == null && fileURI.toString().equals("WEB-INF/geronimo-jetty.xml")) {
                    earContext.addFile(target, file);
                    try {
                        JettyWebAppDocument doc = (JettyWebAppDocument) XmlBeansUtil.parse(new FileInputStream(file), JettyWebAppDocument.type);
                        jettyWebApp = doc.getWebApp();
                    } catch (XmlException e) {
                        throw new DeploymentException("Unable to parse geronimo-jetty.xml", e);
                    }
                } else {
                    earContext.addFile(target, file);
                }
            }

            if (webApp == null) {
                throw new DeploymentException("Did not find WEB-INF/web.xml in module");
            }
            webModule.setSpecDD(webApp);

            if (jettyWebApp == null) {
                throw new DeploymentException("No plan or WEB-INF/jetty-web.xml found");
            }
            webModule.setVendorDD(jettyWebApp);

            // add the dependencies declared in the geronimo-jetty.xml file
            JettyDependencyType[] dependencies = jettyWebApp.getDependencyArray();
            for (int i = 0; i < dependencies.length; i++) {
                earContext.addDependency(getDependencyURI(dependencies[i]));
            }
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        }
    }
    
    public void installModule(JarFile earFile, EARContext earContext, Module webModule) throws DeploymentException {
        try {
            URI warRoot = null;
            JarInputStream jarIS = null;
            if (!webModule.getURI().equals(URI.create("/"))) {
                ZipEntry warEntry = earFile.getEntry(webModule.getURI().toString());
                jarIS = new JarInputStream(earFile.getInputStream(warEntry));
                warRoot = URI.create(webModule.getURI() + "/");
            } else {
                jarIS = new JarInputStream(new FileInputStream(earFile.getName()));
                warRoot = URI.create("war/");
            }

            // add the warfile's content to the configuration
            ZipEntry src;
            WebAppType webApp = null;
            JettyWebAppType jettyWebApp = (JettyWebAppType) webModule.getVendorDD();
            while ((src = jarIS.getNextEntry()) != null) {
                URI target = warRoot.resolve(src.getName());
                if ("WEB-INF/web.xml".equals(src.getName())) {
                    byte[] buffer = getBytes(jarIS);
                    earContext.addFile(target, new ByteArrayInputStream(buffer));
                    try {
                        XmlObject dd = SchemaConversionUtils.parse(new ByteArrayInputStream(buffer));
                        WebAppDocument doc = SchemaConversionUtils.convertToServletSchema(dd);
                        webApp = doc.getWebApp();
                    } catch (XmlException e) {
                        throw new DeploymentException("Unable to parse web.xml", e);
                    }
                } else if (jettyWebApp == null && "WEB-INF/geronimo-jetty.xml".equals(src.getName())) {
                    byte[] buffer = getBytes(jarIS);
                    earContext.addFile(target, new ByteArrayInputStream(buffer));
                    try {
                        JettyWebAppDocument doc = (JettyWebAppDocument) XmlBeansUtil.parse(new ByteArrayInputStream(buffer), JettyWebAppDocument.type);
                        jettyWebApp = doc.getWebApp();
                    } catch (XmlException e) {
                        throw new DeploymentException("Unable to parse geronimo-jetty.xml", e);
                    }
                } else {
                    earContext.addFile(target, jarIS);
                }
            }

            if (webApp == null) {
                throw new DeploymentException("Did not find WEB-INF/web.xml in module");
            }
            webModule.setSpecDD(webApp);

            if (jettyWebApp == null) {
                throw new DeploymentException("No plan or WEB-INF/jetty-web.xml found");
            }
            webModule.setVendorDD(jettyWebApp);

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

            String PolicyContextID = (earContext.getApplicationObjectName()==null? module.getName():earContext.getApplicationObjectName().toString());

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

    private ReadOnlyContext buildComponentContext(EARContext earContext, WebModule webModule, WebAppType webApp, JettyWebAppType jettyWebApp, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        ComponentContextBuilder builder = new ComponentContextBuilder(new JMXReferenceFactory());
        if (userTransaction != null) {
            try {
                builder.addUserTransaction(userTransaction);
            } catch (NamingException e) {
                throw new DeploymentException("Unable to bind UserTransaction into ENC", e);
            }
        }

        EnvEntryType[] envEntries = webApp.getEnvEntryArray();
        ENCConfigBuilder.addEnvEntries(envEntries, builder);

        // ejb-ref
        EjbRefType[] ejbRefs = webApp.getEjbRefArray();
        addEJBRefs(earContext, webModule, ejbRefs, cl, builder);

        // ejb-local-ref
        EjbLocalRefType[] ejbLocalRefs = webApp.getEjbLocalRefArray();
        addEJBLocalRefs(earContext, webModule, ejbLocalRefs, cl, builder);


        // resource-ref
        Map resourceRefMap = new HashMap();
        JettyLocalRefType[] jettyResourceRefs = jettyWebApp.getResourceRefArray();
        for (int i = 0; i < jettyResourceRefs.length; i++) {
            JettyLocalRefType jettyResourceRef = jettyResourceRefs[i];
            resourceRefMap.put(jettyResourceRef.getRefName(), new JettyRefAdapter(jettyResourceRef));
        }
        ENCConfigBuilder.addResourceRefs(webApp.getResourceRefArray(), cl, resourceRefMap, builder);

        // resource-env-ref
        Map resourceEnvRefMap = new HashMap();
        JettyLocalRefType[] jettyResourceEnvRefs = jettyWebApp.getResourceEnvRefArray();
        for (int i = 0; i < jettyResourceEnvRefs.length; i++) {
            JettyLocalRefType jettyResourceEnvRef = jettyResourceEnvRefs[i];
            resourceEnvRefMap.put(jettyResourceEnvRef.getRefName(), new JettyRefAdapter(jettyResourceEnvRef));
        }
        ENCConfigBuilder.addResourceEnvRefs(webApp.getResourceEnvRefArray(), cl, resourceEnvRefMap, builder);

        ENCConfigBuilder.addMessageDestinationRefs(webApp.getMessageDestinationRefArray(), cl, builder);

        return builder.getContext();
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

    private static void addEJBRefs(EARContext earContext, WebModule webModule, EjbRefType[] ejbRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbRefs.length; i++) {
            EjbRefType ejbRef = ejbRefs[i];

            String ejbRefName = ejbRef.getEjbRefName().getStringValue();

            String remote = ejbRef.getRemote().getStringValue();
            assureEJBObjectInterface(remote, cl);

            String home = ejbRef.getHome().getStringValue();
            assureEJBHomeInterface(home, cl);

            String ejbLink = getJ2eeStringValue(ejbRef.getEjbLink());
            Object ejbRefObject;
            if (ejbLink != null) {
                ejbRefObject = earContext.getEJBRef(webModule.getURI(), ejbLink);
            } else {
                // todo get the id from the geronimo-jetty.xml file
                throw new IllegalArgumentException("non ejb-link refs not supported");
            }

            try {
                builder.bind(ejbRefName, ejbRefObject);
            } catch (NamingException e) {
                throw new DeploymentException("Unable to to bind ejb-ref: ejb-ref-name=" + ejbRefName);
            }
        }
    }

    private static void addEJBLocalRefs(EARContext earContext, WebModule webModule, EjbLocalRefType[] ejbLocalRefs, ClassLoader cl, ComponentContextBuilder builder) throws DeploymentException {
        for (int i = 0; i < ejbLocalRefs.length; i++) {
            EjbLocalRefType ejbLocalRef = ejbLocalRefs[i];

            String ejbRefName = ejbLocalRef.getEjbRefName().getStringValue();

            String local = ejbLocalRef.getLocal().getStringValue();
            assureEJBLocalObjectInterface(local, cl);

            String localHome = ejbLocalRef.getLocalHome().getStringValue();
            assureEJBLocalHomeInterface(localHome, cl);

            String ejbLink = getJ2eeStringValue(ejbLocalRef.getEjbLink());
            Object ejbLocalRefObject;
            if (ejbLink != null) {
                ejbLocalRefObject = earContext.getEJBLocalRef(webModule.getURI(), ejbLink);
            } else {
                // todo get the id from the geronimo-jetty.xml file
                throw new IllegalArgumentException("non ejb-link refs not supported");
            }

            try {
                builder.bind(ejbRefName, ejbLocalRefObject);
            } catch (NamingException e) {
                throw new DeploymentException("Unable to to bind ejb-local-ref: ejb-ref-name=" + ejbRefName);
            }
        }
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(JettyModuleBuilder.class);
        infoFactory.addInterface(ModuleBuilder.class);
        infoFactory.addInterface(ModuleBuilderWithUnpack.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
