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

package org.apache.geronimo.web25.deployment;

import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.security.auth.message.module.ServerAuthModule;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.components.jaspi.model.AuthModuleType;
import org.apache.geronimo.components.jaspi.model.ConfigProviderType;
import org.apache.geronimo.components.jaspi.model.JaspiXmlUtil;
import org.apache.geronimo.components.jaspi.model.ServerAuthConfigType;
import org.apache.geronimo.components.jaspi.model.ServerAuthContextType;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.FragmentContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.deployment.WebServiceBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.SecurityAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.j2ee.jndi.WebContextSource;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.security.jaspi.AuthConfigProviderGBean;
import org.apache.geronimo.security.jaspi.ServerAuthConfigGBean;
import org.apache.geronimo.security.jaspi.ServerAuthContextGBean;
import org.apache.geronimo.security.jaspi.ServerAuthModuleGBean;
import org.apache.geronimo.web.info.WebAppInfo;
import org.apache.geronimo.web.security.SpecSecurityBuilder;
import org.apache.geronimo.web25.deployment.merge.MergeHelper;
import org.apache.geronimo.web25.deployment.security.AuthenticationWrapper;
import org.apache.geronimo.web25.deployment.utils.WebAppXmlAttributeBuilder;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerSecurityDocument;
import org.apache.openejb.jee.Filter;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.Listener;
import org.apache.openejb.jee.LoginConfig;
import org.apache.openejb.jee.Servlet;
import org.apache.openejb.jee.ServletMapping;
import org.apache.openejb.jee.SessionConfig;
import org.apache.openejb.jee.WebApp;
import org.apache.xbean.finder.AbstractFinder;
import org.apache.xbean.finder.ClassFinder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractWebModuleBuilder implements ModuleBuilder {
    private static final Logger log = LoggerFactory.getLogger(AbstractWebModuleBuilder.class);

    static {
        PropertyEditorManager.registerEditor(WebAppInfo.class, WebAppXmlAttributeBuilder.class);
    }

    //are we combining all web apps into one bundle in an ear?
    //TODO eliminate this
    protected static final boolean COMBINED_BUNDLE = true;

    public final static EARContext.Key<GBeanData> DEFAULT_JSP_SERVLET_KEY = new EARContext.Key<GBeanData>() {
        public GBeanData get(Map<EARContext.Key, Object> context) {
            return (GBeanData) context.get(this);
        }
    };

    public static final EARContext.Key<Boolean> WEB_MODULE_HAS_SECURITY_REALM = new EARContext.Key<Boolean>() {

        @Override
        public Boolean get(Map<EARContext.Key, Object> context) {
            return (Boolean) context.get(this);
        }
    };

    public static final EARContext.Key<Set<String>> EXCLUDED_JAR_URLS = new EARContext.Key<Set<String>>() {

        @Override
        public Set<String> get(Map<EARContext.Key, Object> context) {
            Set<String> excludedJarUrls = (Set<String>) context.get(this);
            if (excludedJarUrls == null) {
                excludedJarUrls = new HashSet<String>();
                context.put(this, excludedJarUrls);
            }
            return excludedJarUrls;
        }
    };

    public static final EARContext.Key<Map<String, Set<String>>> SERVLET_CONTAINER_INITIALIZERS = new EARContext.Key<Map<String, Set<String>>>() {

        @Override
        public Map<String, Set<String>> get(Map<EARContext.Key, Object> context) {
            return (Map<String, Set<String>>) context.get(this);
        }
    };

    public static final EARContext.Key<Float> INITIAL_WEB_XML_SCHEMA_VERSION = new EARContext.Key<Float>() {

        @Override
        public Float get(Map<EARContext.Key, Object> context) {
            return (Float) context.get(this);
        }
    };

    public static final EARContext.Key<List<String>> ORDERED_LIBS = new EARContext.Key<List<String>>() {
        @Override
        public List<String> get(Map<EARContext.Key, Object> context) {
            return (List<String>) context.get(this);
        }
    };

    private static final String LINE_SEP = System.getProperty("line.separator");

    protected static final AbstractNameQuery MANAGED_CONNECTION_FACTORY_PATTERN;

    private static final AbstractNameQuery ADMIN_OBJECT_PATTERN;

    protected static final AbstractNameQuery STATELESS_SESSION_BEAN_PATTERN;

    protected static final AbstractNameQuery STATEFUL_SESSION_BEAN_PATTERN;
    protected static final AbstractNameQuery SINGLETON_BEAN_PATTERN;
    protected static final AbstractNameQuery MANAGED_BEAN_PATTERN;

    protected static final AbstractNameQuery ENTITY_BEAN_PATTERN;

    protected static final AbstractNameQuery EJB_MODULE_PATTERN;

    static {
        MANAGED_CONNECTION_FACTORY_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JCA_MANAGED_CONNECTION_FACTORY));
        ADMIN_OBJECT_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JCA_ADMIN_OBJECT));
        STATELESS_SESSION_BEAN_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATELESS_SESSION_BEAN));
        STATEFUL_SESSION_BEAN_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.STATEFUL_SESSION_BEAN));
        SINGLETON_BEAN_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.SINGLETON_BEAN));
        MANAGED_BEAN_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.MANAGED_BEAN));
        ENTITY_BEAN_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.ENTITY_BEAN));
        EJB_MODULE_PATTERN = new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.EJB_MODULE));
    }

    protected final Kernel kernel;

    protected final NamespaceDrivenBuilderCollection serviceBuilders;

    protected final ResourceEnvironmentSetter resourceEnvironmentSetter;

    protected final Collection<WebServiceBuilder> webServiceBuilder;

    protected final NamingBuilder namingBuilders;

    protected final Collection<ModuleBuilderExtension> moduleBuilderExtensions;

    private static final QName SECURITY_QNAME = GerSecurityDocument.type.getDocumentElementName();

    private final PackageAdmin packageAdmin;


    /**
     * Manifest classpath entries in a war configuration must be resolved relative to the war configuration, not the
     * enclosing ear configuration.  Resolving relative to he war configuration using this offset produces the same
     * effect as URI.create(module.targetPath()).resolve(mcpEntry) executed in the ear configuration.
     */
    private static final URI RELATIVE_MODULE_BASE_URI = URI.create("../");
    public static final String MESSAGE_LAYER = "HttpServlet";

    protected AbstractWebModuleBuilder(Kernel kernel, Collection<NamespaceDrivenBuilder> serviceBuilders, NamingBuilder namingBuilders, ResourceEnvironmentSetter resourceEnvironmentSetter,
                                       Collection<WebServiceBuilder> webServiceBuilder, Collection<ModuleBuilderExtension> moduleBuilderExtensions, BundleContext bundleContext) {
        this.kernel = kernel;
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
        this.namingBuilders = namingBuilders;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;
        this.webServiceBuilder = webServiceBuilder;
        this.moduleBuilderExtensions = moduleBuilderExtensions == null ? new ArrayList<ModuleBuilderExtension>() : moduleBuilderExtensions;
        ServiceReference sr = bundleContext.getServiceReference(PackageAdmin.class.getName());
        packageAdmin = (PackageAdmin) bundleContext.getService(sr);
    }

    public NamingBuilder getNamingBuilders() {
        return namingBuilders;
    }

    protected void addGBeanDependencies(EARContext earContext, GBeanData webModuleData) {
        //log.debug("Adding dependencies to web module: " + webModuleData.getAbstractName());
//        Configuration earConfiguration = earContext.getConfiguration();
//        addDependencies(earContext.findGBeanDatas(earConfiguration, MANAGED_CONNECTION_FACTORY_PATTERN), webModuleData);
//        addDependencies(earContext.findGBeanDatas(earConfiguration, ADMIN_OBJECT_PATTERN), webModuleData);
//        addDependencies(earContext.findGBeanDatas(earConfiguration, STATELESS_SESSION_BEAN_PATTERN), webModuleData);
//        addDependencies(earContext.findGBeanDatas(earConfiguration, STATEFUL_SESSION_BEAN_PATTERN), webModuleData);
//        addDependencies(earContext.findGBeanDatas(earConfiguration, SINGLETON_BEAN_PATTERN), webModuleData);
//        addDependencies(earContext.findGBeanDatas(earConfiguration, MANAGED_BEAN_PATTERN), webModuleData);
//        addDependencies(earContext.findGBeanDatas(earConfiguration, ENTITY_BEAN_PATTERN), webModuleData);
//        addDependencies(earContext.findGBeanDatas(earConfiguration, EJB_MODULE_PATTERN), webModuleData);
    }

    private void addDependencies(LinkedHashSet<GBeanData> dependencies, GBeanData webModuleData) {
        for (GBeanData dependency : dependencies) {
            AbstractName dependencyName = dependency.getAbstractName();
            webModuleData.addDependency(dependencyName);
            log.debug("Dependency on " + dependencyName);
        }
    }

    @Override
    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, ".", null, null, null, null, naming, idBuilder);
    }

    @Override
    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, Module parentModule, Naming naming,
                               ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, (String) moduleContextInfo, parentModule, naming, idBuilder);
    }

    protected abstract Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, String contextRoot, Module parentModule, Naming naming,
                                           ModuleIDBuilder idBuilder) throws DeploymentException;

    protected static Map<JndiKey, Map<String, Object>> shareJndi(Module parent) {
        return Module.share(Module.APP, parent == null ? null : parent.getJndiContext());
    }

    /**
     * Some servlets will have multiple url patterns.  However, webservice servlets
     * will only have one, which is what this method is intended for.
     *
     * @param webApp      spec deployment descriptor
     * @param contextRoot context root for web app from application.xml or geronimo plan
     * @return map of servlet names to path mapped to them.  Possibly inaccurate except for web services.
     */
    protected Map<String, String> buildServletNameToPathMap(WebApp webApp, String contextRoot) {
        if (contextRoot == null) {
            contextRoot = "";
        } else if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        Map<String, String> map = new HashMap<String, String>();
        List<ServletMapping> servletMappings = webApp.getServletMapping();
        for (ServletMapping servletMapping : servletMappings) {
            String servletName = servletMapping.getServletName();
            List<String> urlPatterns = servletMapping.getUrlPattern();
            for (String urlPattern : urlPatterns) {
                map.put(servletName, contextRoot + urlPattern);
            }
        }
        return map;
    }

    protected String determineDefaultContextRoot(WebApp webApp, boolean isStandAlone, JarFile moduleFile, String targetPath) {
        if (webApp != null && webApp.getId() != null) {
            return webApp.getId();
        }
        if (isStandAlone) {
            // default configId is based on the moduleFile name
            return "/" + trimPath(new File(moduleFile.getName()).getName());
        }
        // default configId is based on the module uri from the application.xml
        return trimPath(targetPath);
    }

    private String trimPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.endsWith(".war")) {
            path = path.substring(0, path.length() - 4);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repositories)
            throws DeploymentException {
        EARContext moduleContext;
        //TODO GERONIMO-4972 find a way to create working nested bundles.
        if (module.isStandAlone()) {
            moduleContext = earContext;
        } else {
            /*Environment environment = module.getEnvironment();
            Artifact earConfigId = earContext.getConfigID();
            Artifact configId = new Artifact(earConfigId.getGroupId(), earConfigId.getArtifactId() + "_" + module.getTargetPath(), earConfigId.getVersion(), "car");
            environment.setConfigId(configId);
            environment.addDependency(earConfigId, ImportType.ALL);
            File configurationDir = new File(earContext.getBaseDir(), module.getTargetPath());
            configurationDir.mkdirs();
            // construct the web app deployment context... this is the same class used by the ear context
            try {
                File inPlaceConfigurationDir = null;
                if (null != earContext.getInPlaceConfigurationDir()) {
                    inPlaceConfigurationDir = new File(earContext.getInPlaceConfigurationDir(), module.getTargetPath());
                }
                moduleContext = new EARContext(configurationDir, inPlaceConfigurationDir, environment, ConfigurationModuleType.WAR, module.getModuleName(), earContext);
            } catch (DeploymentException e) {
                cleanupConfigurationDir(configurationDir);
                throw e;
            }*/
            //Merge the environment configurations to the parent EAR package, as now the same bundle is shared for all the modules.
            EnvironmentBuilder.mergeEnvironments(earContext.getEnvironment(), module.getEnvironment());
            moduleContext = new FragmentContext(earContext, ConfigurationModuleType.WAR);
        }
        module.setEarContext(moduleContext);
        module.setRootEarContext(earContext);
        try {
            Collection<String> manifestcp = module.getClassPath();
            // add the warfile's content to the configuration
            JarFile warFile = module.getModuleFile();
            Enumeration<JarEntry> entries = warFile.entries();
            List<ZipEntry> libs = new ArrayList<ZipEntry>();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                URI targetPath = module.resolve(entry.getName());
                if (entry.getName().equals("WEB-INF/web.xml")) {
                    moduleContext.addFile(targetPath, module.getOriginalSpecDD());
                } else if (entry.getName().startsWith("WEB-INF/lib") && entry.getName().endsWith(".jar")) {
                    // keep a collection of all libs in the war
                    // libs must be installed after WEB-INF/classes which must be installed after this copy phase
                    libs.add(entry);
                } else {
                    moduleContext.addFile(targetPath, warFile, entry);
                }
            }
            // always add WEB-INF/classes to the classpath regardless of whether
            // any classes exist.  This must be searched BEFORE the WEB-INF/lib jar files,
            // per the servlet specifications.
            moduleContext.addToClassPath(module.resolve("WEB-INF/classes").getPath());
            manifestcp.add("WEB-INF/classes");      // NOTE:  Spec requires there be no trailing "/" on this.
            // install the libs
            for (ZipEntry entry : libs) {
                URI targetPath = module.resolve(entry.getName());
                moduleContext.addInclude(targetPath, warFile, entry);
                manifestcp.add(entry.getName());
            }
            // add the manifest classpath entries declared in the war to the class loader
            // we have to explicitly add these since we are unpacking the web module
            // and the url class loader will not pick up a manifest from an unpacked dir
            //GERONIMO-4972 this can't be correct for one-bundle deployments.
            moduleContext.addManifestClassPath(warFile, RELATIVE_MODULE_BASE_URI, manifestcp);

            for (String classpath : manifestcp) {
                earContext.addToClassPath(module.resolve(classpath).toString());
            }

        } catch (IOException e) {
            throw new DeploymentException("Problem deploying war", e);
        } finally {
            if (!module.isStandAlone()) {
                try {
                    moduleContext.flush();
                } catch (IOException e) {
                    throw new DeploymentException("Problem closing war context", e);
                }
            }
        }
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.installModule(earFile, earContext, module, configurationStores, targetConfigurationStore, repositories);
        }
    }

    protected abstract void preInitContext(EARContext earContext, WebModule module, Bundle bundle) throws DeploymentException;

    protected abstract void postInitContext(EARContext earContext, WebModule module, Bundle bundle) throws DeploymentException;

    @Override
    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        WebModule webModule = (WebModule)module;
        preInitContext(earContext, webModule, bundle);
        basicInitContext(earContext, webModule, bundle, (XmlObject) module.getVendorDD());
        postInitContext(earContext, webModule, bundle);
    }

    protected void basicInitContext(EARContext earContext, WebModule webModule, Bundle bundle, XmlObject gerWebApp) throws DeploymentException {
        //complete manifest classpath
        EARContext moduleContext = webModule.getEarContext();
        Collection<String> manifestcp = webModule.getClassPath();
        Collection<String> moduleLocations = EARContext.MODULE_LIST_KEY.get(webModule.getRootEarContext().getGeneralData());
        URI baseUri = URI.create(webModule.getTargetPath());
        URI resolutionUri = invertURI(baseUri);
        earContext.getCompleteManifestClassPath(webModule.getDeployable(), baseUri, resolutionUri, manifestcp, moduleLocations);
        //Security Configuration Validation
        WebApp webApp = webModule.getSpecDD();
        boolean hasSecurityRealmName = (Boolean) webModule.getEarContext().getGeneralData().get(WEB_MODULE_HAS_SECURITY_REALM);
        if ((!webApp.getSecurityConstraint().isEmpty() || !webApp.getSecurityRole().isEmpty())) {
            if (!hasSecurityRealmName) {
                throw new DeploymentException("web.xml for web app " + webModule.getName()
                        + " includes security elements but Geronimo deployment plan is not provided or does not contain <security-realm-name> element necessary to configure security accordingly.");
            }
        }
        if (hasSecurityRealmName) {
            earContext.setHasSecurity(true);
        }

      //Inform errors if login-config element contains more than one
        List<LoginConfig> loginConfigs = webApp.getLoginConfig();
        if (loginConfigs.size() > 1) {
            throw new DeploymentException("Web app " + webApp.getDisplayName() + " cannot have more than one login-config element.  Currently has " + loginConfigs.size() + " login-config elements.");
        }

       //Inform errors if session-config element contains more than one
        List<SessionConfig> sessionConfigs = webApp.getSessionConfig();
        if (sessionConfigs.size() > 1) {
            throw new DeploymentException("Web app " + webApp.getDisplayName() + " cannot have more than one sesion-config element.  Currently has " + sessionConfigs.size() + " session-config elements.");
        }

        //TODO think about how to provide a default security realm name
        XmlObject[] securityElements = XmlBeansUtil.selectSubstitutionGroupElements(SECURITY_QNAME, gerWebApp);
        if (securityElements.length > 0 && !hasSecurityRealmName) {
            throw new DeploymentException("You have supplied a security configuration for web app " + webModule.getName() + " but no security-realm-name to allow login");
        }

        //Process Naming
        getNamingBuilders().buildEnvironment(webApp, webModule.getVendorDD(), webModule.getEnvironment());
        getNamingBuilders().initContext(webApp, gerWebApp, webModule);

        float originalSpecDDVersion;
        String originalSpecDD = webModule.getOriginalSpecDD();
        if (originalSpecDD == null) {
            originalSpecDDVersion = 3.0f;
        } else {
            originalSpecDDVersion = identifySpecDDSchemaVersion(originalSpecDD);
        }
        webModule.getEarContext().getGeneralData().put(INITIAL_WEB_XML_SCHEMA_VERSION, originalSpecDDVersion);
        //Process web fragments and annotations
        if (INITIAL_WEB_XML_SCHEMA_VERSION.get(webModule.getEarContext().getGeneralData()) >= 2.5f && !webApp.isMetadataComplete()) {
            MergeHelper.processWebFragmentsAndAnnotations(earContext, webModule, bundle, webApp);
        }
        MergeHelper.processServletContainerInitializer(earContext, webModule, bundle);
        serviceBuilders.build(gerWebApp, earContext, webModule.getEarContext());
    }

    static URI invertURI(URI baseUri) {
        URI resolutionUri = URI.create(".");
        for (URI test = baseUri; !test.equals(RELATIVE_MODULE_BASE_URI); test = test.resolve(RELATIVE_MODULE_BASE_URI)) {
            resolutionUri = resolutionUri.resolve(RELATIVE_MODULE_BASE_URI);
        }
        return resolutionUri;
    }

    protected String getSpecDDAsString(WebModule module) throws JAXBException {
        return JaxbJavaee.marshal(WebApp.class, module.getSpecDD());
    }

    /**
     * Identify the spec DD schema version, and save it in the EARContext
     *
     * @param originalSpecDD text of original spec dd
     * @return spec dd version
     */
    protected float identifySpecDDSchemaVersion(String originalSpecDD) {
        float schemaVersion = 0f;
        XmlCursor cursor = null;
        try {
            cursor = XmlBeansUtil.parse(originalSpecDD).newCursor();
            cursor.toStartDoc();
            cursor.toFirstChild();
            String nameSpaceURI = cursor.getName().getNamespaceURI();
            if (nameSpaceURI != null && nameSpaceURI.length() > 0) {
                String version = cursor.getAttributeText(new QName("", "version"));
                if (version != null) {
                    schemaVersion = Float.parseFloat(version);
                }
            } else {
                XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
                String publicId = xmlDocumentProperties.getDoctypePublicId();
                if ("-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".equals(publicId)) {
                    schemaVersion = 2.2f;
                } else if ("-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN".equals(publicId)) {
                    schemaVersion = 2.3f;
                }
            }
        } catch (Exception e) {
            log.error("Fail to identify web.xml schema version", e);
            //Should never happen, as we have checked the deployment plan  in the previous code
        } finally {
            if (cursor != null) {
                try {
                    cursor.dispose();
                } catch (Exception e) {
                    //ignore
                }
            }
        }
        return schemaVersion;
    }

    protected ComponentPermissions buildSpecSecurityConfig(EARContext earContext, WebApp webApp, Bundle bundle) {
        SpecSecurityBuilder builder = new SpecSecurityBuilder(new WebAppInfo());
        return builder.buildSpecSecurityConfig();
    }

    protected void configureLocalJaspicProvider(AuthenticationWrapper authType, String contextPath, Module module, GBeanData securityFactoryData) throws DeploymentException,
            GBeanAlreadyExistsException {
        EARContext moduleContext = module.getEarContext();
        GBeanData authConfigProviderData = null;
        AbstractName providerName = moduleContext.getNaming().createChildName(module.getModuleName(), "authConfigProvider", GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        try {
            if (authType.isSetConfigProvider()) {
                authConfigProviderData = new GBeanData(providerName, AuthConfigProviderGBean.class);
                final XmlCursor xmlCursor = authType.getConfigProvider().newCursor();
                try {
                    XMLStreamReader reader = new InternWrapper(xmlCursor.newXMLStreamReader());
                    ConfigProviderType configProviderType = JaspiXmlUtil.loadConfigProvider(reader);
                    StringWriter out = new StringWriter();
                    JaspiXmlUtil.writeConfigProvider(configProviderType, out);
                    authConfigProviderData.setAttribute("config", out.toString());
                } finally {
                    xmlCursor.dispose();
                }
            } else if (authType.isSetServerAuthConfig()) {
                authConfigProviderData = new GBeanData(providerName, ServerAuthConfigGBean.class);
                final XmlCursor xmlCursor = authType.getServerAuthConfig().newCursor();
                try {
                    XMLStreamReader reader = new InternWrapper(xmlCursor.newXMLStreamReader());
                    ServerAuthConfigType serverAuthConfigType = JaspiXmlUtil.loadServerAuthConfig(reader);
                    StringWriter out = new StringWriter();
                    JaspiXmlUtil.writeServerAuthConfig(serverAuthConfigType, out);
                    authConfigProviderData.setAttribute("config", out.toString());
                } finally {
                    xmlCursor.dispose();
                }
            } else if (authType.isSetServerAuthContext()) {
                authConfigProviderData = new GBeanData(providerName, ServerAuthContextGBean.class);
                final XmlCursor xmlCursor = authType.getServerAuthContext().newCursor();
                try {
                    XMLStreamReader reader = new InternWrapper(xmlCursor.newXMLStreamReader());
                    ServerAuthContextType serverAuthContextType = JaspiXmlUtil.loadServerAuthContext(reader);
                    StringWriter out = new StringWriter();
                    JaspiXmlUtil.writeServerAuthContext(serverAuthContextType, out);
                    authConfigProviderData.setAttribute("config", out.toString());
                } finally {
                    xmlCursor.dispose();
                }
            } else if (authType.isSetServerAuthModule()) {
                authConfigProviderData = new GBeanData(providerName, ServerAuthModuleGBean.class);
                final XmlCursor xmlCursor = authType.getServerAuthModule().newCursor();
                try {
                    XMLStreamReader reader = new InternWrapper(xmlCursor.newXMLStreamReader());
                    AuthModuleType<ServerAuthModule> authModuleType = JaspiXmlUtil.loadServerAuthModule(reader);
                    StringWriter out = new StringWriter();
                    JaspiXmlUtil.writeServerAuthModule(authModuleType, out);
                    authConfigProviderData.setAttribute("config", out.toString());
                    authConfigProviderData.setAttribute("messageLayer", MESSAGE_LAYER);
                    authConfigProviderData.setAttribute("appContext", "server " + contextPath);
                    //TODO ??
                    authConfigProviderData.setAttribute("authenticationID", contextPath);
                } finally {
                    xmlCursor.dispose();
                }
            }
        } catch (ParserConfigurationException e) {
            throw new DeploymentException("Could not read auth config", e);
        } catch (IOException e) {
            throw new DeploymentException("Could not read auth config", e);
        } catch (SAXException e) {
            throw new DeploymentException("Could not read auth config", e);
        } catch (JAXBException e) {
            throw new DeploymentException("Could not read auth config", e);
        } catch (XMLStreamException e) {
            throw new DeploymentException("Could not read auth config", e);
        }
        if (authConfigProviderData != null) {
            moduleContext.addGBean(authConfigProviderData);
            securityFactoryData.addDependency(authConfigProviderData.getAbstractName());
        }
    }

    private boolean cleanupConfigurationDir(File configurationDir) {
        LinkedList<String> cannotBeDeletedList = new LinkedList<String>();
        if (!FileUtils.recursiveDelete(configurationDir, cannotBeDeletedList)) {
            // Output a message to help user track down file problem
            log.warn("Unable to delete " + cannotBeDeletedList.size() + " files while recursively deleting directory " + configurationDir.getAbsolutePath() + LINE_SEP
                    + "The first file that could not be deleted was:" + LINE_SEP + "  " + (!cannotBeDeletedList.isEmpty() ? cannotBeDeletedList.getFirst() : ""));
            return false;
        }
        return true;
    }

    protected AbstractFinder createWebAppClassFinder(WebApp webApp, WebModule webModule) throws DeploymentException {
        // Get the classloader from the module's EARContext
        Bundle bundle = webModule.getEarContext().getDeploymentBundle();
//        return createWebAppClassFinder(webApp, bundle);
        try {
            LinkedHashSet<Class<?>> classes = new LinkedHashSet<Class<?>>();
            for (Servlet servlet : webApp.getServlet()) {
                if (servlet.getServletClass() != null) {
                    addClass(bundle, classes, servlet.getServletClass());
                }
            }
            for (Filter filter : webApp.getFilter()) {
                addClass(bundle, classes, filter.getFilterClass());
            }
            for (Listener listener : webApp.getListener()) {
                addClass(bundle, classes, listener.getListenerClass());
            }
            return new ClassFinder(new ArrayList<Class<?>>(classes));
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private void addClass(Bundle bundle, LinkedHashSet<Class<?>> classes, String className) throws ClassNotFoundException {
        Class<?> cl = bundle.loadClass(className);
        while (cl != Object.class) {
            classes.add(cl);
            cl = cl.getSuperclass();
        }
    }

    protected void configureBasicWebModuleAttributes(WebApp webApp, XmlObject vendorPlan, EARContext moduleContext, EARContext earContext, WebModule webModule, GBeanData webModuleData)
            throws DeploymentException {
        Map<EARContext.Key, Object> buildingContext = new HashMap<EARContext.Key, Object>();
        buildingContext.put(NamingBuilder.GBEAN_NAME_KEY, moduleContext.getModuleName());

        String moduleName =  webModule.getName();

        if (earContext.getSubModuleNames().contains(moduleName)){
            log.warn("Duplicated moduleName: '"+moduleName +"' is found ! deployer will rename it to: '"+moduleName +
                    "_duplicated' , please check your modules in application to make sure they don't share the same name");
            moduleName = moduleName +"_duplicated";
            earContext.getSubModuleNames().add(moduleName);
        }

        earContext.getSubModuleNames().add(moduleName);
        webModule.getJndiContext().get(JndiScope.module).put("module/ModuleName", moduleName);


        if (!webApp.isMetadataComplete()) {
            // Create a classfinder and populate it for the naming builder(s). The absence of a
            // classFinder in the module will convey whether metadata-complete is set (or not)
            webModule.setClassFinder(createWebAppClassFinder(webApp, webModule));
            SecurityAnnotationHelper.processAnnotations(webApp, webModule.getClassFinder());
        }
        //N.B. we use the ear context which has all the gbeans we could possibly be looking up from this ear.
        //nope, persistence units can be in the war.
        //This means that you cannot use the default environment of the web builder to add configs that will be searched.
        getNamingBuilders().buildNaming(webApp, vendorPlan, webModule, buildingContext);
        //Combine contexts.  Note this may not work right for jaxws which has a comp/env/WebServiceContext binding
        AbstractName contextSourceName = moduleContext.getNaming().createChildName(webModuleData.getAbstractName(), "ContextSource", "ContextSource");
        GBeanData contextSourceData = new GBeanData(contextSourceName, WebContextSource.class);

        contextSourceData.setAttribute("componentContext", webModule.getJndiContext().get(JndiScope.comp));
        contextSourceData.setAttribute("moduleContext", webModule.getJndiContext().get(JndiScope.module));
        contextSourceData.setReferencePattern("ApplicationJndi", EARContext.APPLICATION_JNDI_NAME_KEY.get(earContext.getGeneralData()));
        contextSourceData.setReferencePattern("TransactionManager", moduleContext.getTransactionManagerName());
        try {
            moduleContext.addGBean(contextSourceData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("ContextSource for this webapp already present:" + webModuleData.getAbstractName(), e);
        }
        webModuleData.setReferencePattern("ContextSource", contextSourceName);
        Holder holder = NamingBuilder.INJECTION_KEY.get(buildingContext);
        webModule.getSharedContext().put(WebModule.WEB_APP_DATA, webModuleData);
        webModule.getSharedContext().put(NamingBuilder.INJECTION_KEY, holder);
        if (moduleContext.getServerName() != null) {
            webModuleData.setReferencePattern("J2EEServer", moduleContext.getServerName());
        }
//        if (!webModule.isStandAlone()) {
//            webModuleData.setReferencePattern("J2EEApplication", earContext.getModuleName());
//        }
        webModuleData.setAttribute("holder", holder);
        //Add dependencies on managed connection factories and ejbs in this app
        //This is overkill, but allows for people not using java:comp context (even though we don't support it)
        //and sidesteps the problem of circular references between ejbs.
        addGBeanDependencies(earContext, webModuleData);
        webModuleData.setReferencePattern("TransactionManager", moduleContext.getTransactionManagerName());
        webModuleData.setReferencePattern("TrackedConnectionAssociator", moduleContext.getConnectionTrackerName());
        webModuleData.setAttribute("modulePath", webModule.isStandAlone() ? null : webModule.getTargetPath());
    }

    private static class InternWrapper implements XMLStreamReader {

        private final XMLStreamReader delegate;

        private InternWrapper(XMLStreamReader delegate) {
            this.delegate = delegate;
        }

        public void close() throws XMLStreamException {
            delegate.close();
        }

        public int getAttributeCount() {
            return delegate.getAttributeCount();
        }

        public String getAttributeLocalName(int i) {
            return delegate.getAttributeLocalName(i);
        }

        public QName getAttributeName(int i) {
            return delegate.getAttributeName(i);
        }

        public String getAttributeNamespace(int i) {
            return delegate.getAttributeNamespace(i);
        }

        public String getAttributePrefix(int i) {
            return delegate.getAttributePrefix(i);
        }

        public String getAttributeType(int i) {
            return delegate.getAttributeType(i);
        }

        public String getAttributeValue(int i) {
            return delegate.getAttributeValue(i);
        }

        public String getAttributeValue(String s, String s1) {
            return delegate.getAttributeValue(s, s1);
        }

        public String getCharacterEncodingScheme() {
            return delegate.getCharacterEncodingScheme();
        }

        public String getElementText() throws XMLStreamException {
            return delegate.getElementText();
        }

        public String getEncoding() {
            return delegate.getEncoding();
        }

        public int getEventType() {
            return delegate.getEventType();
        }

        public String getLocalName() {
            return delegate.getLocalName().intern();
        }

        public Location getLocation() {
            return delegate.getLocation();
        }

        public QName getName() {
            return delegate.getName();
        }

        public NamespaceContext getNamespaceContext() {
            return delegate.getNamespaceContext();
        }

        public int getNamespaceCount() {
            return delegate.getNamespaceCount();
        }

        public String getNamespacePrefix(int i) {
            return delegate.getNamespacePrefix(i);
        }

        public String getNamespaceURI() {
            return delegate.getNamespaceURI().intern();
        }

        public String getNamespaceURI(int i) {
            return delegate.getNamespaceURI(i);
        }

        public String getNamespaceURI(String s) {
            return delegate.getNamespaceURI(s);
        }

        public String getPIData() {
            return delegate.getPIData();
        }

        public String getPITarget() {
            return delegate.getPITarget();
        }

        public String getPrefix() {
            return delegate.getPrefix();
        }

        public Object getProperty(String s) throws IllegalArgumentException {
            return delegate.getProperty(s);
        }

        public String getText() {
            return delegate.getText();
        }

        public char[] getTextCharacters() {
            return delegate.getTextCharacters();
        }

        public int getTextCharacters(int i, char[] chars, int i1, int i2) throws XMLStreamException {
            return delegate.getTextCharacters(i, chars, i1, i2);
        }

        public int getTextLength() {
            return delegate.getTextLength();
        }

        public int getTextStart() {
            return delegate.getTextStart();
        }

        public String getVersion() {
            return delegate.getVersion();
        }

        public boolean hasName() {
            return delegate.hasName();
        }

        public boolean hasNext() throws XMLStreamException {
            return delegate.hasNext();
        }

        public boolean hasText() {
            return delegate.hasText();
        }

        public boolean isAttributeSpecified(int i) {
            return delegate.isAttributeSpecified(i);
        }

        public boolean isCharacters() {
            return delegate.isCharacters();
        }

        public boolean isEndElement() {
            return delegate.isEndElement();
        }

        public boolean isStandalone() {
            return delegate.isStandalone();
        }

        public boolean isStartElement() {
            return delegate.isStartElement();
        }

        public boolean isWhiteSpace() {
            return delegate.isWhiteSpace();
        }

        public int next() throws XMLStreamException {
            return delegate.next();
        }

        public int nextTag() throws XMLStreamException {
            return delegate.nextTag();
        }

        public void require(int i, String s, String s1) throws XMLStreamException {
            delegate.require(i, s, s1);
        }

        public boolean standaloneSet() {
            return delegate.standaloneSet();
        }
    }
}
