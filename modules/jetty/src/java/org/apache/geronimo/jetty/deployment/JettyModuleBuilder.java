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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.AttributeNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.transaction.UserTransaction;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.IOUtil;
import org.apache.geronimo.deployment.util.JarUtil;
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
import org.apache.geronimo.xbeans.geronimo.jetty.JettyDefaultPrincipalType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyDependencyType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyGbeanType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyPrincipalType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyRealmType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyRoleType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettySecurityType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.naming.GerLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


/**
 * @version $Rev$ $Date$
 */
public class JettyModuleBuilder implements ModuleBuilder {
    private static final String PARENT_ID = "org/apache/geronimo/Server";

    public Module createModule(String name, Object plan, JarFile moduleFile, URL webXmlUrl, String targetPath) throws DeploymentException {
        String specDD;
        WebAppType webApp;
        try {
            if (webXmlUrl == null) {
                webXmlUrl = JarUtil.createJarURL(moduleFile, "WEB-INF/web.xml");
            }

            specDD = IOUtil.readAll(webXmlUrl);

            // check if we have an alt spec dd
            WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(SchemaConversionUtils.parse(specDD));
            webApp = webAppDoc.getWebApp();
        } catch (Exception e) {
            return null;
        }


        JettyWebAppType jettyWebApp = null;
        try {
            // load the geronimo-jetty.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    jettyWebApp = (JettyWebAppType) SchemaConversionUtils.getNestedObjectAsType(
                            (XmlObject) plan,
                            "web-app",
                            JettyWebAppType.type);
                } else {
                    JettyWebAppDocument jettyWebAppdoc = null;
                    if (plan != null) {
                        jettyWebAppdoc = JettyWebAppDocument.Factory.parse((File)plan);
                    } else {
                        URL path = JarUtil.createJarURL(moduleFile, "WEB-INF/geronimo-jetty.xml");
                        jettyWebAppdoc = JettyWebAppDocument.Factory.parse(path);
                    }
                    if (jettyWebAppdoc != null) {
                        jettyWebApp = jettyWebAppdoc.getWebApp();
                    }
                }
            } catch (IOException e) {
            }

            // if we got one extract the validate it otherwise create a default one
            if (jettyWebApp != null) {
                jettyWebApp = (JettyWebAppType) SchemaConversionUtils.convertToGeronimoNamingSchema(jettyWebApp);
                SchemaConversionUtils.validateDD(jettyWebApp);
            } else {
                jettyWebApp = createDefaultPlan(name, webApp);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        // get the ids from either the application plan or for a stand alone module from the specific deployer
        URI configId = null;
        try {
            configId = new URI(jettyWebApp.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + jettyWebApp.getConfigId(), e);
        }

        URI parentId = null;
        if (jettyWebApp.isSetParentId()) {
            try {
                parentId = new URI(jettyWebApp.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + jettyWebApp.getParentId(), e);
            }
        }

        URI moduleURI;
        if (targetPath != null) {
            moduleURI = URI.create(targetPath);
            if (targetPath.endsWith("/")) {
                throw new DeploymentException("targetPath must not end with a '/'");
            }
            targetPath += "/";
        } else {
            targetPath = "war/";
            moduleURI = URI.create("");
        }

        WebModule module = new WebModule(name, configId, parentId, moduleURI, moduleFile, targetPath, webApp, jettyWebApp, specDD);
        module.setContextRoot(jettyWebApp.getContextRoot());
        return module;
    }

    private JettyWebAppType createDefaultPlan(String name, WebAppType webApp) {
        String id = webApp.getId();
        if (id == null) {
            id = name;
            if (id.endsWith(".war")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        JettyWebAppType jettyWebApp = JettyWebAppType.Factory.newInstance();

        // set the parentId, configId and context root
        jettyWebApp.setParentId(PARENT_ID);
        if (null != webApp.getId()) {
            id = webApp.getId();
        }
        jettyWebApp.setConfigId(id);
        jettyWebApp.setContextRoot(id);
        return jettyWebApp;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        try {
            URI targetURI = URI.create(module.getTargetPath());

            // add the warfile's content to the configuration
            JarFile warFile = module.getModuleFile();
            Enumeration entries = warFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                URI target = targetURI.resolve(entry.getName());
                if (entry.getName().equals("WEB-INF/web.xml")) {
                    // TODO gets rid of these tests when Jetty will use the serialized Geronimo DD.
                    earContext.addFile(target, new ByteArrayInputStream(module.getOriginalSpecDD().getBytes()));
                } else {
                    InputStream in = warFile.getInputStream(entry);
                    try {
                        earContext.addFile(target, in);
                    } finally {
                        IOUtil.close(in);
                    }
                }
            }

            // add the dependencies declared in the geronimo-jetty.xml file
            JettyWebAppType jettyWebApp = (JettyWebAppType) module.getVendorDD();
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

            gbean.setAttribute("uri", URI.create(module.getTargetPath()));
            gbean.setAttribute("contextPath", webModule.getContextRoot());
            gbean.setAttribute("contextPriorityClassLoader", Boolean.valueOf(jettyWebApp.getContextPriorityClassloader()));
            if (security != null) {
                gbean.setAttribute("securityConfig", security);

                String policyContextID;
                if (earContext.getApplicationObjectName() == null) {
                    policyContextID = module.getName();
                } else {
                    policyContextID = earContext.getApplicationObjectName().toString();
                }
                gbean.setAttribute("policyContextID", policyContextID);
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

    private ReadOnlyContext buildComponentContext(EARContext earContext, WebModule webModule, WebAppType webApp, JettyWebAppType jettyWebApp, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        Map ejbRefMap = mapRefs(jettyWebApp.getEjbRefArray());
        Map ejbLocalRefMap = mapRefs(jettyWebApp.getEjbLocalRefArray());
        Map resourceRefMap = mapRefs(jettyWebApp.getResourceRefArray());
        Map resourceEnvRefMap = mapRefs(jettyWebApp.getResourceEnvRefArray());

        return ENCConfigBuilder.buildComponentContext(earContext,
                webModule.getModuleURI(),
                userTransaction,
                webApp.getEnvEntryArray(),
                webApp.getEjbRefArray(), ejbRefMap,
                webApp.getEjbLocalRefArray(), ejbLocalRefMap,
                webApp.getResourceRefArray(), resourceRefMap,
                webApp.getResourceEnvRefArray(), resourceEnvRefMap,
                webApp.getMessageDestinationRefArray(),
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

    private void setResourceEnvironment(GBeanMBean bean, ResourceRefType[] resourceRefArray, GerLocalRefType[] jettyResourceRefArray) throws AttributeNotFoundException, ReflectionException {
        Map openejbNames = new HashMap();
        for (int i = 0; i < jettyResourceRefArray.length; i++) {
            GerLocalRefType jettyLocalRefType = jettyResourceRefArray[i];
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
