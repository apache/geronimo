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

package org.apache.geronimo.tomcat.deployment;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;
import javax.transaction.UserTransaction;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.StoredObject;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.deployment.GBeanResourceEnvironmentBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.util.URLPattern;
import org.apache.geronimo.tomcat.RealmGBean;
import org.apache.geronimo.tomcat.TomcatClassLoader;
import org.apache.geronimo.tomcat.TomcatWebAppContext;
import org.apache.geronimo.tomcat.ValveGBean;
import org.apache.geronimo.tomcat.util.SecurityHolder;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.xbeans.geronimo.web.GerConfigParamType;
import org.apache.geronimo.xbeans.geronimo.web.GerContainerConfigType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebContainerType;
import org.apache.geronimo.xbeans.j2ee.FilterMappingType;
import org.apache.geronimo.xbeans.j2ee.HttpMethodType;
import org.apache.geronimo.xbeans.j2ee.RoleNameType;
import org.apache.geronimo.xbeans.j2ee.SecurityConstraintType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleRefType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleType;
import org.apache.geronimo.xbeans.j2ee.ServletMappingType;
import org.apache.geronimo.xbeans.j2ee.ServletType;
import org.apache.geronimo.xbeans.j2ee.UrlPatternType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.j2ee.WebResourceCollectionType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;


/**
 * @version $Rev: 161588 $ $Date: 2005-04-16 12:06:59 -0600 (Sat, 16 Apr 2005) $
 */
public class TomcatModuleBuilder implements ModuleBuilder {

    private final URI defaultParentId;
    private final ObjectName tomcatContainerObjectName;

    private final WebServiceBuilder webServiceBuilder;

    private final Repository repository;

    public TomcatModuleBuilder(URI defaultParentId,
                               ObjectName tomcatContainerObjectName,
                               WebServiceBuilder webServiceBuilder,
                               Repository repository) {
        this.defaultParentId = defaultParentId;
        this.tomcatContainerObjectName = tomcatContainerObjectName;
        this.webServiceBuilder = webServiceBuilder;
        this.repository = repository;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "war", null, true, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, false, (String) moduleContextInfo);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";

        // parse the spec dd
        String specDD;
        WebAppType webApp;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/web.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);
        } catch (Exception e) {
            //no web.xml, not for us
            return null;
        }
        //we found web.xml, if it won't parse that's an error.
        try {
            // parse it
            XmlObject parsed = SchemaConversionUtils.parse(specDD);
            WebAppDocument webAppDoc = SchemaConversionUtils.convertToServletSchema(parsed);
            webApp = webAppDoc.getWebApp();
        } catch (XmlException xmle) {
            throw new DeploymentException("Error parsing web.xml", xmle);
        }
        check(webApp);


        // parse vendor dd
        GerWebAppType tomcatWebApp = getTomcatWebApp(plan, moduleFile, standAlone, targetPath, webApp);

        // get the ids from either the application plan or for a stand alone module from the specific deployer
        URI configId = null;
        try {
            configId = new URI(tomcatWebApp.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + tomcatWebApp.getConfigId(), e);
        }

        URI parentId = null;
        if (tomcatWebApp.isSetParentId()) {
            try {
                parentId = new URI(tomcatWebApp.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + tomcatWebApp.getParentId(), e);
            }
        } else {
            parentId = defaultParentId;
        }
        if (contextRoot == null) {
            contextRoot = tomcatWebApp.getContextRoot();
        }
        //look for a webservices dd
        Map portMap = Collections.EMPTY_MAP;
        //TODO Jeff, please review
        Map servletNameToPathMap = buildServletNameToPathMap(webApp, contextRoot);
        if (webServiceBuilder != null) {
            try {
                URL wsDDUrl = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/webservices.xml");
                portMap = webServiceBuilder.parseWebServiceDescriptor(wsDDUrl, moduleFile, false, servletNameToPathMap);
            } catch (MalformedURLException e) {
                //no descriptor
            }
        }

        WebModule module = new WebModule(standAlone, configId, parentId, moduleFile, targetPath, webApp, tomcatWebApp, specDD, portMap);
        module.setContextRoot(contextRoot);
        return module;
    }

    /**
     * Some servlets will have multiple url patterns.  However, webservice servlets
     * will only have one, which is what this method is intended for.
     * @param webApp
     * @param contextRoot
     * @return
     */
    private Map buildServletNameToPathMap(WebAppType webApp, String contextRoot) {
        contextRoot = "/" + contextRoot;
        Map map = new HashMap();
        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (int j = 0; j < servletMappings.length; j++) {
            ServletMappingType servletMapping = servletMappings[j];
            String servletName = servletMapping.getServletName().getStringValue().trim();
            map.put(servletName, contextRoot + servletMapping.getUrlPattern().getStringValue());
        }
        return map;
    }

    GerWebAppType getTomcatWebApp(Object plan, JarFile moduleFile, boolean standAlone, String targetPath, WebAppType webApp) throws DeploymentException {
        GerWebAppType tomcatWebApp = null;
        try {
            // load the geronimo-web.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    tomcatWebApp = (GerWebAppType) SchemaConversionUtils.getNestedObjectAsType((XmlObject) plan,
                            "web-app",
                            GerWebAppType.type);
                } else {
                    GerWebAppDocument tomcatWebAppdoc = null;
                    if (plan != null) {
                        tomcatWebAppdoc = GerWebAppDocument.Factory.parse((File) plan);
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "WEB-INF/geronimo-web.xml");
                        tomcatWebAppdoc = GerWebAppDocument.Factory.parse(path);
                    }
                    if (tomcatWebAppdoc != null) {
                        tomcatWebApp = tomcatWebAppdoc.getWebApp();
                    }
                }
            } catch (IOException e) {
            }

            // if we got one extract and validate it otherwise create a default one
            if (tomcatWebApp != null) {
                tomcatWebApp = (GerWebAppType) SchemaConversionUtils.convertToGeronimoNamingSchema(tomcatWebApp);
                tomcatWebApp = (GerWebAppType) SchemaConversionUtils.convertToGeronimoSecuritySchema(tomcatWebApp);
                tomcatWebApp = (GerWebAppType) SchemaConversionUtils.convertToGeronimoServiceSchema(tomcatWebApp);
                SchemaConversionUtils.validateDD(tomcatWebApp);
            } else {
                String path;
                if (standAlone) {
                    // default configId is based on the moduleFile name
                    path = new File(moduleFile.getName()).getName();
                } else {
                    // default configId is based on the module uri from the application.xml
                    path = targetPath;
                }
                tomcatWebApp = createDefaultPlan(path, webApp);
            }
        } catch (XmlException e) {
            throw new DeploymentException("xml problem", e);
        }
        return tomcatWebApp;
    }

    private GerWebAppType createDefaultPlan(String path, WebAppType webApp) {
        String id = webApp.getId();
        if (id == null) {
            id = path;
            if (id.endsWith(".war")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        GerWebAppType tomcatWebApp = GerWebAppType.Factory.newInstance();

        // set the parentId, configId and context root
        tomcatWebApp.setParentId(defaultParentId.toString());
        if (null != webApp.getId()) {
            id = webApp.getId();
        }
        tomcatWebApp.setConfigId(id);
        tomcatWebApp.setContextRoot("/" + id);
        return tomcatWebApp;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        try {
            URI baseDir = URI.create(module.getTargetPath() + "/");

            // add the warfile's content to the configuration
            JarFile warFile = module.getModuleFile();
            Enumeration entries = warFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                URI targetPath = baseDir.resolve(new URI(null, entry.getName(), null));
                if (entry.getName().equals("WEB-INF/web.xml")) {
                    earContext.addFile(targetPath, module.getOriginalSpecDD());
                } else {
                    earContext.addFile(targetPath, warFile, entry);
                }
            }

            // add the manifest classpath entries declared in the war to the class loader
            // we have to explicitly add these since we are unpacking the web module
            // and the url class loader will not pick up a manifiest from an unpacked dir
            earContext.addManifestClassPath(warFile, URI.create(module.getTargetPath()));

            // add the dependencies declared in the geronimo-web.xml file
            GerWebAppType tomcatWebApp = (GerWebAppType) module.getVendorDD();
            DependencyType[] dependencies = tomcatWebApp.getDependencyArray();
            ServiceConfigBuilder.addDependencies(earContext, dependencies, repository);
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Could not construct URI for location of war entry", e);
        }
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) {
        // web application do not add anything to the shared context
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = J2eeContextImpl.newModuleContextFromApplication(earJ2eeContext, NameFactory.WEB_MODULE, module.getName());
        WebModule webModule = (WebModule) module;

        WebAppType webApp = (WebAppType) webModule.getSpecDD();
        GerWebAppType tomcatWebApp = (GerWebAppType) webModule.getVendorDD();

        boolean contextPriorityClassLoader = false;
        if (tomcatWebApp != null) {
            contextPriorityClassLoader = Boolean.valueOf(tomcatWebApp.getContextPriorityClassloader()).booleanValue();
        }
        // construct the webClassLoader
        ClassLoader webClassLoader = getWebClassLoader(earContext, webModule, cl, contextPriorityClassLoader);

        if (tomcatWebApp != null) {
            GbeanType[] gbeans = tomcatWebApp.getGbeanArray();
            ServiceConfigBuilder.addGBeans(gbeans, webClassLoader, moduleJ2eeContext, earContext);
        }

        ObjectName webModuleName = null;
        try {
            webModuleName = NameFactory.getModuleName(null, null, null, null, null, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct module name", e);
        }

        UserTransaction userTransaction = new OnlineUserTransaction();
        //this may add to the web classpath with enhanced classes.
        Map compContext = buildComponentContext(earContext, webModule, webApp, tomcatWebApp, userTransaction, webClassLoader);

        GBeanData webModuleData = new GBeanData(webModuleName, TomcatWebAppContext.GBEAN_INFO);
        try {
            webModuleData.setReferencePattern("J2EEServer", earContext.getServerObjectName());
            if (!earContext.getJ2EEApplicationName().equals("null")) {
                webModuleData.setReferencePattern("J2EEApplication", earContext.getApplicationObjectName());
            }

            webModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());
            Set securityRoles = collectRoleNames(webApp);
            Map rolePermissions = new HashMap();

            URI baseUri = earContext.getTargetFile(URI.create(webModule.getTargetPath() + "/")).toURI();
            webModuleData.setAttribute("webAppRoot", baseUri);
            webModuleData.setAttribute("path", webModule.getContextRoot());

            webModuleData.setAttribute("componentContext", compContext);
            webModuleData.setAttribute("userTransaction", userTransaction);
            //classpath may have been augmented with enhanced classes
            webModuleData.setAttribute("webClassPath", webModule.getWebClasspath());
            // unsharableResources, applicationManagedSecurityResources
            GBeanResourceEnvironmentBuilder rebuilder = new GBeanResourceEnvironmentBuilder(webModuleData);
            ENCConfigBuilder.setResourceEnvironment(earContext, webModule.getModuleURI(), rebuilder, webApp.getResourceRefArray(), tomcatWebApp.getResourceRefArray());

            webModuleData.setAttribute("contextPriorityClassLoader", Boolean.valueOf(contextPriorityClassLoader));

            webModuleData.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
            webModuleData.setReferencePattern("TrackedConnectionAssociator", earContext.getConnectionTrackerObjectName());
            webModuleData.setReferencePattern("Container", tomcatContainerObjectName);

            String virtualServer = null;
            // Process the Tomcat container-config elements
            if (tomcatWebApp != null && tomcatWebApp.sizeOfContainerConfigArray() > 0) {
                Map values = new HashMap();
                GerContainerConfigType[] configs = tomcatWebApp.getContainerConfigArray();
                for (int i = 0; i < configs.length; i++) {
                    GerContainerConfigType config = configs[i];
                    if(config.getContainer().intValue() != GerWebContainerType.INT_TOMCAT) {
                        continue;
                    }
                    GerConfigParamType[] params = config.getConfigParamArray();
                    for (int j = 0; j < params.length; j++) {
                        GerConfigParamType param = params[j];
                        values.put(param.getName(), param.getStringValue());
                    }
                }

                //Is there a Tomcat virtual server declaration?
                virtualServer = (String) values.remove("VirtualServer");

                //Is there a Tomcat crossContext declaration?
                String strCrossContext = (String) values.remove("CrossContext");
                if (strCrossContext != null){
                    if (strCrossContext.trim().toLowerCase().equals("true")){
                        webModuleData.setAttribute("crossContext",new Boolean(true));
                    }
                }
                
                //Is there a Tomcat realm declaration?
                String tomcatRealm = (String) values.remove("TomcatRealm");
                if (tomcatRealm != null) {
                    ObjectName realmName = NameFactory.getComponentName(null, null, null, null, tomcatRealm.trim(), RealmGBean.GBEAN_INFO.getJ2eeType(), moduleJ2eeContext);
                    webModuleData.setReferencePattern("TomcatRealm", realmName);
                }
                //Is there a Tomcat Valve Chain declaration?
                String tomcatValveChain = (String) values.remove("TomcatValveChain");
                if (tomcatValveChain != null) {
                    ObjectName valveName = NameFactory.getComponentName(null, null, null, null, tomcatValveChain.trim(), ValveGBean.J2EE_TYPE, moduleJ2eeContext);
                    //NameFactory.getComponentName(null, null, null, null, tomcatValveChain.trim(), ValveGbean., moduleJ2eeContext);
                    webModuleData.setReferencePattern("TomcatValveChain", valveName);
                }

                // Are there any leftover values?  If so, that's a problem.
                if(values.size() > 0) {
                    StringBuffer msg = new StringBuffer();
                    msg.append("Unexpected container-config/config-params found for Tomcat in web app deployment plan (");
                    boolean first = true;
                    for (Iterator it = values.keySet().iterator(); it.hasNext();) {
                        String value = (String) it.next();
                        if(!first) {
                            msg.append(",");
                        }
                        msg.append(value);
                    }
                    msg.append(")");
                    throw new DeploymentException(msg.toString());
                }
            }


            Map portMap = webModule.getPortMap();

            //Handle the role permissions and webservices on the servlets.
            ServletType[] servletTypes = webApp.getServletArray();
            Map webServices = new HashMap();
            for (int i = 0; i < servletTypes.length; i++) {
                ServletType servletType = servletTypes[i];

                //Handle the Role Ref Permissions
                processRoleRefPermissions(servletType, securityRoles, rolePermissions);

                //Do we have webservices configured?
                if (portMap != null) {
                    //Check if the Servlet is a Webservice
                    String servletName = servletType.getServletName().getStringValue().trim();
                    if (portMap.containsKey(servletName)) {
                        //Yes, this servlet is a web service so let the web service builder
                        // deal with configuring the web service stack
                        String servletClassName = servletType.getServletClass().getStringValue().trim();
                        Object portInfo = portMap.get(servletName);
                        if (portInfo == null) {
                            throw new DeploymentException("No web service deployment info for servlet name " + servletName);
                        }

                        StoredObject wsContainer = configurePOJO(webModule.getModuleFile(), portInfo, servletClassName, webClassLoader);
                        webServices.put(servletName, wsContainer);
                    }
                }
            }

            // JACC v1.0 secion B.19
            addUnmappedJSPPermissions(securityRoles, rolePermissions);

            webModuleData.setAttribute("webServices", webServices);

            if (tomcatWebApp.isSetSecurityRealmName()) {

                SecurityHolder securityHolder = new SecurityHolder();
                securityHolder.setSecurityRealm(tomcatWebApp.getSecurityRealmName().trim());

                /**
                 * TODO - go back to commented version when possible.
                 */
                String policyContextID = webModuleName.getCanonicalName().replaceAll("[, :]", "_");
                securityHolder.setPolicyContextID(policyContextID);

                ComponentPermissions componentPermissions = buildSpecSecurityConfig(webApp, securityRoles, rolePermissions);
                securityHolder.setExcluded(componentPermissions.getExcludedPermissions());
                PermissionCollection checkedPermissions = new Permissions();
                for (Iterator iterator = rolePermissions.values().iterator(); iterator.hasNext();) {
                    PermissionCollection permissionsForRole = (PermissionCollection) iterator.next();
                    for (Enumeration iterator2 = permissionsForRole.elements(); iterator2.hasMoreElements();) {
                        Permission permission = (Permission) iterator2.nextElement();
                        checkedPermissions.add(permission);
                    }
                }
                securityHolder.setChecked(checkedPermissions);
                earContext.addSecurityContext(policyContextID, componentPermissions);
                if (tomcatWebApp.isSetSecurity()) {
                    SecurityConfiguration securityConfiguration = SecurityBuilder.buildSecurityConfiguration(tomcatWebApp.getSecurity());
                    earContext.setSecurityConfiguration(securityConfiguration);
                }
                DefaultPrincipal defaultPrincipal = earContext.getSecurityConfiguration().getDefaultPrincipal();
                securityHolder.setDefaultPrincipal(defaultPrincipal);

                webModuleData.setAttribute("securityHolder", securityHolder);
                webModuleData.setReferencePattern("RoleDesignateSource", earContext.getJaccManagerName());
            }

            if (virtualServer != null) {
                webModuleData.setAttribute("virtualServer", virtualServer);
            }
            earContext.addGBean(webModuleData);

        } catch (DeploymentException de) {
            throw de;
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize webapp GBean", e);
        }
    }

    private ClassLoader getWebClassLoader(EARContext earContext, WebModule webModule, ClassLoader cl, boolean contextPriorityClassLoader) throws DeploymentException {
        getWebClassPath(earContext, webModule);
        URI[] webClassPath = webModule.getWebClasspath();
        URI baseUri = earContext.getBaseDir().toURI();
        URL baseUrl = null;
        try {
            baseUrl = baseUri.resolve(webModule.getTargetPathURI()).toURL();
        } catch (MalformedURLException e) {
            throw new DeploymentException("Invalid module location: " + webModule.getTargetPathURI() + ", baseUri: " + baseUri);
        }
        URL[] webClassPathURLs = new URL[webClassPath.length];
        for (int i = 0; i < webClassPath.length; i++) {
            URI path = baseUri.resolve(webClassPath[i]);
            try {
                webClassPathURLs[i] = path.toURL();
            } catch (MalformedURLException e) {
                throw new DeploymentException("Invalid web class path element: path=" + path + ", baseUri=" + baseUri);
            }
        }

        ClassLoader webClassLoader = new TomcatClassLoader(webClassPathURLs, baseUrl, cl, contextPriorityClassLoader);
        return webClassLoader;
    }

    private void addUnmappedJSPPermissions(Set securityRoles, Map rolePermissions) {
        for (Iterator iter = securityRoles.iterator(); iter.hasNext();) {
            String roleName = (String) iter.next();
            addPermissionToRole(roleName, new WebRoleRefPermission("", roleName), rolePermissions);
        }
    }

    private void processRoleRefPermissions(ServletType servletType,
                                           Set securityRoles,
                                           Map rolePermissions) {
        String servletName = servletType.getServletName().getStringValue().trim();

        //WebRoleRefPermissions
        SecurityRoleRefType[] securityRoleRefTypeArray = servletType.getSecurityRoleRefArray();
        Set unmappedRoles = new HashSet(securityRoles);
        for (int j = 0; j < securityRoleRefTypeArray.length; j++) {
            SecurityRoleRefType securityRoleRefType = securityRoleRefTypeArray[j];
            String roleName = securityRoleRefType.getRoleName().getStringValue().trim();
            String roleLink = securityRoleRefType.getRoleLink().getStringValue().trim();

            //jacc 3.1.3.2
            addPermissionToRole(roleLink, new WebRoleRefPermission(servletName, roleName), rolePermissions);
            unmappedRoles.remove(roleName);
        }
        for (Iterator iterator = unmappedRoles.iterator(); iterator.hasNext();) {
            String roleName = (String) iterator.next();
            addPermissionToRole(roleName, new WebRoleRefPermission(servletName, roleName), rolePermissions);
        }
//       servletData.setAttribute("webRoleRefPermissions", webRoleRefPermissions);

    }

    private ComponentPermissions buildSpecSecurityConfig(WebAppType webApp, Set securityRoles, Map rolePermissions) {
        Map uncheckedPatterns = new HashMap();
        Map uncheckedResourcePatterns = new HashMap();
        Map uncheckedUserPatterns = new HashMap();
        Map excludedPatterns = new HashMap();
        Map rolesPatterns = new HashMap();
        Set allSet = new HashSet();   // == allMap.values()
        Map allMap = new HashMap();   //uncheckedPatterns union excludedPatterns union rolesPatterns.

        SecurityConstraintType[] securityConstraintArray = webApp.getSecurityConstraintArray();
        for (int i = 0; i < securityConstraintArray.length; i++) {
            SecurityConstraintType securityConstraintType = securityConstraintArray[i];
            Map currentPatterns;
            if (securityConstraintType.isSetAuthConstraint()) {
                if (securityConstraintType.getAuthConstraint().getRoleNameArray().length == 0) {
                    currentPatterns = excludedPatterns;
                } else {
                    currentPatterns = rolesPatterns;
                }
            } else {
                currentPatterns = uncheckedPatterns;
            }

            String transport = "";
            if (securityConstraintType.isSetUserDataConstraint()) {
                transport = securityConstraintType.getUserDataConstraint().getTransportGuarantee().getStringValue().trim().toUpperCase();
            }

            WebResourceCollectionType[] webResourceCollectionTypeArray = securityConstraintType.getWebResourceCollectionArray();
            for (int j = 0; j < webResourceCollectionTypeArray.length; j++) {
                WebResourceCollectionType webResourceCollectionType = webResourceCollectionTypeArray[j];
                UrlPatternType[] urlPatternTypeArray = webResourceCollectionType.getUrlPatternArray();
                for (int k = 0; k < urlPatternTypeArray.length; k++) {
                    UrlPatternType urlPatternType = urlPatternTypeArray[k];
                    //presumably, don't trim
                    String url = urlPatternType.getStringValue();
                    URLPattern pattern = (URLPattern) currentPatterns.get(url);
                    if (pattern == null) {
                        pattern = new URLPattern(url);
                        currentPatterns.put(url, pattern);
                    }

                    URLPattern allPattern = (URLPattern) allMap.get(url);
                    if (allPattern == null) {
                        allPattern = new URLPattern(url);
                        allSet.add(allPattern);
                        allMap.put(url, allPattern);
                    }

                    HttpMethodType[] httpMethodTypeArray = webResourceCollectionType.getHttpMethodArray();
                    if (httpMethodTypeArray.length == 0) {
                        pattern.addMethod("");
                        allPattern.addMethod("");
                    } else {
                        for (int l = 0; l < httpMethodTypeArray.length; l++) {
                            HttpMethodType httpMethodType = httpMethodTypeArray[l];
                            //TODO is trim OK?
                            String method = httpMethodType.getStringValue().trim();
                            pattern.addMethod(method);
                            allPattern.addMethod(method);
                        }
                    }
                    if (currentPatterns == rolesPatterns) {
                        RoleNameType[] roleNameTypeArray = securityConstraintType.getAuthConstraint().getRoleNameArray();
                        for (int l = 0; l < roleNameTypeArray.length; l++) {
                            RoleNameType roleNameType = roleNameTypeArray[l];
                            String role = roleNameType.getStringValue().trim();
                            if (role.equals("*")) {
                                pattern.addAllRoles(securityRoles);
                            } else {
                                pattern.addRole(role);
                            }
                        }
                    }

                    pattern.setTransport(transport);
                }
            }
        }

        PermissionCollection excludedPermissions = new Permissions();
        PermissionCollection uncheckedPermissions = new Permissions();

        Iterator iter = excludedPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) excludedPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();

            excludedPermissions.add(new WebResourcePermission(name, actions));
            excludedPermissions.add(new WebUserDataPermission(name, actions));
        }

        iter = rolesPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) rolesPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();
            WebResourcePermission permission = new WebResourcePermission(name, actions);

            for (Iterator names = pattern.getRoles().iterator(); names.hasNext();) {
                String roleName = (String) names.next();
                addPermissionToRole(roleName, permission, rolePermissions);
            }
        }

        iter = uncheckedPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) uncheckedPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethods();

            addOrUpdatePattern(uncheckedResourcePatterns, name, actions);
        }

        iter = rolesPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) rolesPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethodsWithTransport();

            addOrUpdatePattern(uncheckedUserPatterns, name, actions);
        }

        iter = uncheckedPatterns.keySet().iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) uncheckedPatterns.get(iter.next());
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getMethodsWithTransport();

            addOrUpdatePattern(uncheckedUserPatterns, name, actions);
        }

        /**
         * A <code>WebResourcePermission</code> and a <code>WebUserDataPermission</code> must be instantiated for
         * each <tt>url-pattern</tt> in the deployment descriptor and the default pattern "/", that is not combined
         * by the <tt>web-resource-collection</tt> elements of the deployment descriptor with ever HTTP method
         * value.  The permission objects must be contructed using the qualified pattern as their name and with
         * actions defined by the subset of the HTTP methods that do not occur in combination with the pattern.
         * The resulting permissions that must be added to the unchecked policy statements by calling the
         * <code>addToUncheckedPolcy</code> method on the <code>PolicyConfiguration</code> object.
         */
        iter = allSet.iterator();
        while (iter.hasNext()) {
            URLPattern pattern = (URLPattern) iter.next();
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getComplementedMethods();

            if (actions.length() == 0) {
                continue;
            }

            addOrUpdatePattern(uncheckedResourcePatterns, name, actions);
            addOrUpdatePattern(uncheckedUserPatterns, name, actions);
        }

        URLPattern pattern = new URLPattern("/");
        if (!allSet.contains(pattern)) {
            String name = pattern.getQualifiedPattern(allSet);
            String actions = pattern.getComplementedMethods();

            addOrUpdatePattern(uncheckedResourcePatterns, name, actions);
            addOrUpdatePattern(uncheckedUserPatterns, name, actions);
        }

        //Create the uncheckedPermissions for WebResourcePermissions
        iter = uncheckedResourcePatterns.keySet().iterator();
        while (iter.hasNext()) {
            UncheckedItem item = (UncheckedItem) iter.next();
            String actions = (String) uncheckedResourcePatterns.get(item);

            uncheckedPermissions.add(new WebResourcePermission(item.getName(), actions));
        }
        //Create the uncheckedPermissions for WebUserDataPermissions
        iter = uncheckedUserPatterns.keySet().iterator();
        while (iter.hasNext()) {
            UncheckedItem item = (UncheckedItem) iter.next();
            String actions = (String) uncheckedUserPatterns.get(item);

            uncheckedPermissions.add(new WebUserDataPermission(item.getName(), actions));
        }

        ComponentPermissions componentPermissions = new ComponentPermissions(excludedPermissions, uncheckedPermissions, rolePermissions);
        return componentPermissions;

    }

    private void addPermissionToRole(String roleName, Permission permission, Map rolePermissions) {
        PermissionCollection permissionsForRole = (PermissionCollection) rolePermissions.get(roleName);
        if (permissionsForRole == null) {
            permissionsForRole = new Permissions();
            rolePermissions.put(roleName, permissionsForRole);
        }
        permissionsForRole.add(permission);
    }

    private void addOrUpdatePattern(Map patternMap, String name, String actions) {
        UncheckedItem item = new UncheckedItem(name, actions);
        String existingActions = (String) patternMap.get(item);
        if (existingActions != null) {
            patternMap.put(item, actions + "," + existingActions);
            return;
        }

        patternMap.put(item, actions);
    }

    private static Set collectRoleNames(WebAppType webApp) {
        Set roleNames = new HashSet();

        SecurityRoleType[] securityRoles = webApp.getSecurityRoleArray();
        for (int i = 0; i < securityRoles.length; i++) {
            roleNames.add(securityRoles[i].getRoleName().getStringValue().trim());
        }

        return roleNames;
    }

    private static void getWebClassPath(EARContext earContext, WebModule webModule) {
        File baseDir = earContext.getTargetFile(webModule.getTargetPathURI());
        File webInfDir = new File(baseDir, "WEB-INF");

        // check for a classes dir
        File classesDir = new File(webInfDir, "classes");
        if (classesDir.isDirectory()) {
            webModule.addToWebClasspath(webModule.getTargetPathURI().resolve(URI.create("WEB-INF/classes/")));
        }

        // add all of the libs
        File libDir = new File(webInfDir, "lib");
        if (libDir.isDirectory()) {
            File[] libs = libDir.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(".jar");
                }
            });

            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    File lib = libs[i];
                    webModule.addToWebClasspath(webModule.getTargetPathURI().resolve(URI.create("WEB-INF/lib/" + lib.getName())));
                }
            }
        }
    }

    private Map buildComponentContext(EARContext earContext, Module webModule, WebAppType webApp, GerWebAppType tomcatWebApp, UserTransaction userTransaction, ClassLoader cl) throws DeploymentException {
        return ENCConfigBuilder.buildComponentContext(earContext,
                earContext, 
                webModule,
                userTransaction,
                webApp.getEnvEntryArray(),
                webApp.getEjbRefArray(), tomcatWebApp.getEjbRefArray(),
                webApp.getEjbLocalRefArray(), tomcatWebApp.getEjbLocalRefArray(),
                webApp.getResourceRefArray(), tomcatWebApp.getResourceRefArray(),
                webApp.getResourceEnvRefArray(), tomcatWebApp.getResourceEnvRefArray(),
                webApp.getMessageDestinationRefArray(),
                webApp.getServiceRefArray(), tomcatWebApp.getServiceRefArray(),
                cl);
    }

    private static void check(WebAppType webApp) throws DeploymentException {
        checkURLPattern(webApp);
        checkMultiplicities(webApp);
    }

    private static void checkURLPattern(WebAppType webApp) throws DeploymentException {

        FilterMappingType[] filterMappings = webApp.getFilterMappingArray();
        for (int i = 0; i < filterMappings.length; i++) {
            if (filterMappings[i].isSetUrlPattern()) {
                checkString(filterMappings[i].getUrlPattern().getStringValue());
            }
        }

        ServletMappingType[] servletMappings = webApp.getServletMappingArray();
        for (int i = 0; i < servletMappings.length; i++) {
            checkString(servletMappings[i].getUrlPattern().getStringValue());
        }

        SecurityConstraintType[] constraints = webApp.getSecurityConstraintArray();
        for (int i = 0; i < constraints.length; i++) {
            WebResourceCollectionType[] collections = constraints[i].getWebResourceCollectionArray();
            for (int j = 0; j < collections.length; j++) {
                UrlPatternType[] patterns = collections[j].getUrlPatternArray();
                for (int k = 0; k < patterns.length; k++) {
                    checkString(patterns[k].getStringValue());
                }
            }
        }
    }

    private static void checkString(String pattern) throws DeploymentException {
        //j2ee_1_4.xsd explicitly requires preserving all whitespace. Do not trim.
        if (pattern.indexOf(0x0D) >= 0) throw new DeploymentException("<url-pattern> must not contain CR(#xD)");
        if (pattern.indexOf(0x0A) >= 0) throw new DeploymentException("<url-pattern> must not contain LF(#xA)");
    }

    private static void checkMultiplicities(WebAppType webApp) throws DeploymentException {
        if (webApp.getSessionConfigArray().length > 1) throw new DeploymentException("Multiple <session-config> elements found");
        if (webApp.getJspConfigArray().length > 1) throw new DeploymentException("Multiple <jsp-config> elements found");
        if (webApp.getLoginConfigArray().length > 1) throw new DeploymentException("Multiple <login-config> elements found");
    }

    public StoredObject configurePOJO(JarFile moduleFile, Object portInfoObject, String seiClassName, ClassLoader classLoader) throws DeploymentException, IOException {
        //the reason to configure a gbeandata rather than just fetch the WebServiceContainer is that fetching the WSContainer ties us to that
        //ws implementation.  By configuring a servlet gbean, you can provide a different servlet for each combination of
        //web container and ws implementation while assuming almost nothing about their relationship.

        GBeanData fakeData = new GBeanData();
        webServiceBuilder.configurePOJO(fakeData, moduleFile, portInfoObject, seiClassName, classLoader);
        return (StoredObject) fakeData.getAttribute("webServiceContainer");
    }

    class UncheckedItem {
        final static int NA = 0x00;
        final static int INTEGRAL = 0x01;
        final static int CONFIDENTIAL = 0x02;

        private int transportType = NA;
        private String name;

        public UncheckedItem(String name, String actions) {
            setName(name);
            setTransportType(actions);
        }

        public boolean equals(Object o) {
            UncheckedItem item = (UncheckedItem) o;
            return item.getKey().equals(this.getKey());
        }

        public String getKey() {
            return (name + transportType);
        }

        public int hashCode() {
            return getKey().hashCode();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTransportType() {
            return transportType;
        }

        public void setTransportType(String actions) {
            String[] tokens = actions.split(":", 2);
            if (tokens.length == 2) {
                if (tokens[1].equals("INTEGRAL")) {
                    this.transportType = INTEGRAL;
                } else if (tokens[1].equals("CONFIDENTIAL")) {
                    this.transportType = CONFIDENTIAL;
                }
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(TomcatModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultParentId", URI.class, true);
        infoBuilder.addAttribute("tomcatContainerObjectName", ObjectName.class, true);
        infoBuilder.addReference("WebServiceBuilder", WebServiceBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("Repository", Repository.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addInterface(ModuleBuilder.class);

        infoBuilder.setConstructor(new String[]{
            "defaultParentId",
            "tomcatContainerObjectName",
            "WebServiceBuilder",
            "Repository"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
