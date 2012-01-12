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
package org.apache.geronimo.openejb.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.ejb.EntityContext;
import javax.ejb.TimerService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceContext;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.wrapper.ResourceAdapterWrapperGBean;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.deployment.DeployableJarFile;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.deployment.AppClientModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.FragmentContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentSetter;
import org.apache.geronimo.openejb.AppInfoGBean;
import org.apache.geronimo.openejb.EjbContainer;
import org.apache.geronimo.openejb.EjbDeployment;
import org.apache.geronimo.openejb.EjbModuleImpl;
import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.Vendor;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.CmpJarBuilder;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.FacilitiesInfo;
import org.apache.openejb.assembler.classic.MdbContainerInfo;
import org.apache.openejb.assembler.classic.MessageDrivenBeanInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.StatefulBeanInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ApplyOpenejbJar;
import org.apache.openejb.config.AutoConfig;
import org.apache.openejb.config.ClearEmptyMappedName;
import org.apache.openejb.config.CmpJpaConversion;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DynamicDeployer;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.config.GeneratedClientModules;
import org.apache.openejb.config.GeronimoMappedName;
import org.apache.openejb.config.InitEjbDeployments;
import org.apache.openejb.config.LegacyProcessor;
import org.apache.openejb.config.MappedNameBuilder;
import org.apache.openejb.config.OpenEjb2Conversion;
import org.apache.openejb.config.OutputGeneratedDescriptors;
import org.apache.openejb.config.ReadDescriptors;
import org.apache.openejb.config.SunConversion;
import org.apache.openejb.config.ValidateModules;
import org.apache.openejb.config.ValidationError;
import org.apache.openejb.config.ValidationFailedException;
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.config.WlsConversion;
import org.apache.openejb.config.WsDeployer;
import org.apache.openejb.jee.DataSource;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.MessageDestination;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.jee.oejb2.EjbRefType;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.MessageDrivenBeanType;
import org.apache.openejb.jee.oejb2.OpenejbJarType;
import org.apache.openejb.jee.oejb2.PatternType;
import org.apache.openejb.jee.oejb2.ResourceLocatorType;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.osgi.core.BundleFinderFactory;
import org.apache.openejb.util.AnnotationFinder;
import org.apache.openejb.util.LinkResolver;
import org.apache.openejb.util.UniqueDefaultLinkResolver;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master builder for processing EJB JAR deployments and creating the
 * corresponding runtime objects (GBeans, etc.).
 *
 * Acts as either a ModuleBuilder (for standalone ejb jars or ejb jars in an ear) or as a ModuleBuilderExtension
 * for ejbs embedded in a web app.
 *
 * @version $Revision$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class EjbModuleBuilder implements ModuleBuilder, GBeanLifecycle, ModuleBuilderExtension {
    private static final Logger log = LoggerFactory.getLogger(EjbModuleBuilder.class);
    
    private static final String DEFAULT_BUNDLE_CLASSPATH = ".";

    private static final String OPENEJBJAR_NAMESPACE = XmlUtil.OPENEJBJAR_QNAME.getNamespaceURI();
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    static {
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/openejb-jar", "http://openejb.apache.org/xml/ns/openejb-jar-2.3");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/openejb-jar-2.1", "http://openejb.apache.org/xml/ns/openejb-jar-2.3");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/openejb-jar-2.2", "http://openejb.apache.org/xml/ns/openejb-jar-2.3");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/openejb-jar-2.3", "http://openejb.apache.org/xml/ns/openejb-jar-2.3");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/pkgen", "http://openejb.apache.org/xml/ns/pkgen-2.1");
        NAMESPACE_UPDATES.put("http://www.openejb.org/xml/ns/pkgen-2.0", "http://openejb.apache.org/xml/ns/pkgen-2.1");
    }

    private final Environment defaultEnvironment;
    private final String defaultCmpJTADataSource;
    private final String defaultCmpNonJTADataSource;
    private final NamespaceDrivenBuilderCollection serviceBuilders;
    private final NamingBuilder namingBuilder;
    private final ResourceEnvironmentSetter resourceEnvironmentSetter;
    private final Collection<ModuleBuilderExtension> moduleBuilderExtensions;

    public EjbModuleBuilder(@ParamAttribute(name = "defaultEnvironment")Environment defaultEnvironment,
                            @ParamAttribute(name = "defaultCmpJTADataSource")String defaultCmpJTADataSource,
                            @ParamAttribute(name = "defaultCmpNonJTADataSource")String defaultCmpNonJTADataSource,
                            @ParamReference(name = "ModuleBuilderExtensions", namingType = NameFactory.MODULE_BUILDER)Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                            @ParamReference(name = "ServiceBuilders", namingType = NameFactory.MODULE_BUILDER)Collection<NamespaceDrivenBuilder> serviceBuilders,
                            @ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER)NamingBuilder namingBuilders,
                            @ParamReference(name = "ResourceEnvironmentSetter", namingType = NameFactory.MODULE_BUILDER)ResourceEnvironmentSetter resourceEnvironmentSetter) {
        this.defaultEnvironment = defaultEnvironment;
        this.defaultCmpJTADataSource = defaultCmpJTADataSource;
        this.defaultCmpNonJTADataSource = defaultCmpNonJTADataSource;
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
        this.namingBuilder = namingBuilders;
        this.resourceEnvironmentSetter = resourceEnvironmentSetter;

        if (moduleBuilderExtensions == null) {
            moduleBuilderExtensions = Collections.emptyList();
        }
        this.moduleBuilderExtensions = moduleBuilderExtensions;

        SystemInstance.get().setComponent(FinderFactory.class, new BundleFinderFactory());

        //duplicate of stuff in OpenEjbSystemGBean, may not be essential
        System.setProperty("openejb.geronimo", "true");
        System.setProperty("admin.disabled", "true");
        System.setProperty("openejb.logger.external", "true");
        System.setProperty("openejb.log.factory", "org.apache.openejb.util.PaxLogStreamFactory");

        setDefaultProperty("openejb.deploymentId.format", "{moduleUri}/{ejbName}");
        setDefaultProperty("openejb.jndiname.strategy.class", "org.apache.openejb.assembler.classic.JndiBuilder$TemplatedStrategy");
        setDefaultProperty("openejb.jndiname.format", "{ejbName}{interfaceType.annotationName}");
        setDefaultProperty("openejb.jndiname.failoncollision", "false");

        System.setProperty("openejb.naming", "xbean");
    }

    @Override
    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    @Override
    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    @Override
    public void doFail() {
        doStop();
    }

    private void setDefaultProperty(String key, String value) {
        SystemInstance systemInstance = SystemInstance.get();
        String format = systemInstance.getProperty(key);
        if (format == null) {
            systemInstance.setProperty(key, value);
        }
    }


    public String getSchemaNamespace() {
        return EjbModuleBuilder.OPENEJBJAR_NAMESPACE;
    }

    @Override
    public Module createModule(Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return null;
    }

    @Override
    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "ejb.jar", null, null, null, naming, idBuilder, "META-INF/", false);
    }

    @Override
    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, Module parentModule, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, parentModule, naming, idBuilder, "META-INF/", false);
    }

    //ModuleBuilderExtension entry points

    @Override
    public void createModule(Module module, Bundle bundle, Naming naming, ModuleIDBuilder moduleIDBuilder) throws DeploymentException {
        //May be implemented for EBA support ?
    }

    @Override
    public void createModule(Module module,
                             Object plan,
                             JarFile moduleFile,
                             String targetPath,
                             URL specDDUrl,
                             Environment environment,
                             Object moduleContextInfo,
                             AbstractName earName,
                             Naming naming,
                             ModuleIDBuilder idBuilder) throws DeploymentException {
        //check for web module
        if (module instanceof WebModule || module instanceof AppClientModule) {
            //check for WEB-INF/ejb-jar.xml
            Module ejbModule = createModule(plan, moduleFile, targetPath, null, environment, module, naming, idBuilder, "WEB-INF/", true);
            if (ejbModule != null) {
                module.getModules().add(ejbModule);
                //???
                module.getModuleLocations().add(targetPath);
                ejbModule.getSharedContext().putAll(module.getSharedContext());
            }
        }

    }


    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, Module parentModule, Naming naming, ModuleIDBuilder idBuilder,
            String ddDir, boolean subModule) throws DeploymentException {
        if (moduleFile == null)
            throw new NullPointerException("moduleFile is null");
        if (targetPath == null)
            throw new NullPointerException("targetPath is null");
        if (targetPath.endsWith("/"))
            throw new IllegalArgumentException("targetPath must not end with a '/'");

        ClassLoader detectTempClassLoader = null;

        ClassLoader ejbModuleTempClassLoader = null;

        List<File> tempFileList = new ArrayList<File>();

        try {
            // verify we have a valid file
            String jarPath = moduleFile.getName();

            URL baseUrl = null;

            Map<String, URL> descriptors = null;

            File jarFile = new File(moduleFile.getName());

            baseUrl = jarFile.toURI().toURL();

            List<URL> libURLs = new ArrayList<URL>();

            libURLs.add(baseUrl);

            boolean standAlone = earEnvironment == null;

           if (parentModule instanceof WebModule)  {

               Deployable deployable = parentModule.getDeployable();
               if (!(deployable instanceof DeployableJarFile)) {
                   throw new IllegalArgumentException("Expected DeployableJarFile");
               }

                JarFile war = ((DeployableJarFile) deployable).getJarFile();

                Enumeration<JarEntry> entries = war.entries();

                while (entries.hasMoreElements()) {
                    String jarEntryName = entries.nextElement().getName();
                   if (jarEntryName.startsWith("WEB-INF/lib/") && jarEntryName.endsWith(".jar")) {
                       File libJar = JarUtils.toTempFile(war, jarEntryName);
                       tempFileList.add(libJar);
                       libURLs.add(libJar.toURI().toURL());
                       libJar.deleteOnExit();
                    }
                }

                // The war it self is a ejbmodule. If the war is standalone,
                // the corresponding ejbmodule is standalone too.
                standAlone = parentModule.isStandAlone();
            }



            detectTempClassLoader = ClassLoaderUtil.createTempClassLoader(ClassLoaderUtil.createClassLoader(jarPath, libURLs.toArray(new URL[0]), OpenEJB.class.getClassLoader()));

            ResourceFinder finder = new ResourceFinder("", detectTempClassLoader, baseUrl);

            descriptors = finder.getResourcesMap(ddDir);

            // The ResourceFinder.getResourcesMap() method does not work if the jar in question
            // does not properly have "directories" and only has file entries.  So we as a backup
            // measure will explicitly look for specific known descriptor files and add them if
            // the getResourcesMap() method was unable to find them.  In this "bad jar" scenario
            // some extra features such as the openejb.altdd.prefix functionality will not work.

            String[] doubleCheck = {"ejb-jar.xml", "geronimo-openejb.xml", "openejb-jar.xml", "beans.xml", "env-entries.properties", "web.xml"};

            for (String entry : doubleCheck) {
                try {
                    final URL url = finder.find(ddDir + entry);
                    if (url == null) continue;
                    if (descriptors.containsKey(entry)) continue;
                    descriptors.put(entry, url);
                } catch (IOException descriptorNotFound) {
                    // ignore
                }
            }


            if (!isEjbModule(baseUrl,libURLs, detectTempClassLoader, descriptors)) {

                return null;
            }


            libURLs.clear();
            libURLs.add(baseUrl);

            ejbModuleTempClassLoader = ClassLoaderUtil.createTempClassLoader(ClassLoaderUtil.createClassLoader(jarPath, libURLs.toArray(new URL[0]), OpenEJB.class.getClassLoader()));


            // create the EJB Module
            org.apache.openejb.config.EjbModule ejbModule = new org.apache.openejb.config.EjbModule(ejbModuleTempClassLoader, null, jarPath, null, null);
            ejbModule.getAltDDs().putAll(descriptors);

            if (specDDUrl != null) {
                ejbModule.getAltDDs().put("ejb-jar.xml", specDDUrl);
            }

            // convert the vendor plan object to the ejbModule altDD map
            XmlObject unknownXmlObject = null;
            if (plan instanceof XmlObject) {
                unknownXmlObject = (XmlObject) plan;
            } else if (plan != null) {
                try {
                    unknownXmlObject = XmlBeansUtil.parse(((File) plan).toURI().toURL(), XmlUtil.class.getClassLoader());
                } catch (Exception e) {
                    throw new DeploymentException(e);
                }
            }

            if (unknownXmlObject != null) {
                XmlCursor xmlCursor = unknownXmlObject.newCursor();
                //
                QName qname = xmlCursor.getName();
                if (qname == null) {
                    xmlCursor.toFirstChild();
                    qname = xmlCursor.getName();
                }

             while (xmlCursor.hasNextToken()){

                 qname = xmlCursor.getName();
                    if (qname == null || qname.getLocalPart() == null) {
                        xmlCursor.toNextToken();
                        continue;
                    }

                if (qname.getLocalPart().equals("openejb-jar")) {
                    ejbModule.getAltDDs().put("openejb-jar.xml", xmlCursor.xmlText());
                    break;
                } else if (qname.getLocalPart().equals("ejb-jar") && qname.getNamespaceURI().equals("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0")) {
                    ejbModule.getAltDDs().put("geronimo-openejb.xml", xmlCursor.xmlText());
                    break;
                }

                xmlCursor.toNextToken();

                }
            }

            // Read in the deploument desiptor files
            ReadDescriptors readDescriptors = new ReadDescriptors();
            try {
                readDescriptors.deploy(new AppModule(ejbModule));
            } catch (OpenEJBException e) {
                throw new DeploymentException("Failed parsing descriptors for module: " + moduleFile.getName(), e);
            }

            // Get the geronimo-openejb.xml tree

            GeronimoEjbJarType geronimoEjbJarType = (GeronimoEjbJarType) ejbModule.getAltDDs().get("geronimo-openejb.xml");
            if (geronimoEjbJarType == null) {
                // create default plan
                String path = (standAlone) ? new File(moduleFile.getName()).getName() : targetPath;
                geronimoEjbJarType = XmlUtil.createDefaultPlan(path, ejbModule.getEjbJar());
                ejbModule.getAltDDs().put("geronimo-openejb.xml", geronimoEjbJarType);
            }
            OpenejbGeronimoEjbJarType geronimoOpenejb = XmlUtil.convertToXmlbeans(geronimoEjbJarType);

            // create the geronimo environment object
            Environment environment = XmlUtil.buildEnvironment(geronimoEjbJarType.getEnvironment(), defaultEnvironment);
            if (earEnvironment != null) {
                EnvironmentBuilder.mergeEnvironments(earEnvironment, environment);
                environment = earEnvironment;
                if (!environment.getConfigId().isResolved()) {
                    throw new IllegalStateException("EJB module ID should be fully resolved (not " + environment.getConfigId() + ")");
                }
            } else {
                idBuilder.resolve(environment, new File(moduleFile.getName()).getName(), "car");
            }

            AbstractName moduleName;
            if (parentModule == null) {
                AbstractName earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
                moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.EJB_MODULE);
            } else {

                AbstractName parentName = parentModule instanceof AppClientModule ? ((AppClientModule) parentModule)
                        .getAppClientName() : parentModule.getModuleName();

                moduleName = naming.createChildName(parentName, targetPath, NameFactory.EJB_MODULE);
            }

            // Create XMLBeans version of EjbJarType for the AnnotatedApp interface
            EjbJar ejbJar = ejbModule.getEjbJar();

            File file = new File(moduleFile.getName());
            String packageName = file.getName();

            String name;

            if (subModule) {
                name = parentModule.getName();
            } else if (ejbJar.getModuleName() != null) {
                name = ejbJar.getModuleName().trim();
            } else if (standAlone) {
                name = FileUtils.removeExtension(packageName, ".jar");
            } else {
                name = FileUtils.removeExtension(targetPath, ".jar");
            }

            ejbModule.setModuleId(name);

            if (standAlone) {
                ejbModule.setModuleUri(URI.create(packageName));
            } else {
                ejbModule.setModuleUri(URI.create(targetPath));
            }

            Map<JndiKey, Map<String, Object>> context = null;
            if (subModule) {
                context = parentModule.getJndiContext();
            } else if (parentModule != null) {
                context = Module.share(Module.APP, parentModule.getJndiContext());
            }
            EjbModule module = new EjbModule(ejbModule, standAlone, moduleName, name, environment, moduleFile, targetPath, "", ejbJar, geronimoOpenejb, context, parentModule, subModule);

            for (ModuleBuilderExtension builder : moduleBuilderExtensions) {
                try {
                    builder.createModule(module, plan, moduleFile, targetPath, specDDUrl, environment, null, parentModule == null ? null : parentModule.getModuleName(), naming, idBuilder);

                } catch (Throwable t) {
                    String builderName = builder.getClass().getSimpleName();
                    log.error(builderName + ".createModule() failed: " + t.getMessage(), t);

                }
            }
            return module;
        } catch (DeploymentException e) {

            throw e;

        } catch (Exception e) {

            throw new DeploymentException(e);

        }  finally {

            releaseTempClassLoader(detectTempClassLoader);
            releaseTempClassLoader(ejbModuleTempClassLoader);

            for (File file:tempFileList){
                FileUtils.recursiveDelete(file);
            }
        }
    }

    protected static void unmapReferences(EjbJar ejbJar, GeronimoEjbJarType geronimoEjbJarType) {
        Set<String> corbaEjbRefs = new TreeSet<String>();
        for (EjbRefType ejbRef : geronimoEjbJarType.getEjbRef()) {
            if (ejbRef.getNsCorbaloc() != null) {
                corbaEjbRefs.add(ejbRef.getRefName());
            }
        }

        for (EnterpriseBean enterpriseBean : ejbJar.getEnterpriseBeans()) {
            enterpriseBean.getEnvEntry().clear();
            enterpriseBean.getEjbLocalRef().clear();

            for (Iterator<EjbRef> iterator = enterpriseBean.getEjbRef().iterator(); iterator.hasNext();) {
                EjbRef ref = iterator.next();
                if (!corbaEjbRefs.contains(ref.getEjbRefName())) {
                    // remove all non corba refs to avoid overwriting normal ejb refs
                    iterator.remove();
                } else {
                    // clear mapped named data from corba refs
                    ref.setMappedName(null);
                    ref.getInjectionTarget().clear();
                }
            }

            for (MessageDestinationRef ref : enterpriseBean.getMessageDestinationRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
            for (PersistenceContextRef ref : enterpriseBean.getPersistenceContextRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
            for (PersistenceUnitRef ref : enterpriseBean.getPersistenceUnitRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
            for (ResourceRef ref : enterpriseBean.getResourceRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
            for (Iterator<ResourceEnvRef> iterator = enterpriseBean.getResourceEnvRef().iterator(); iterator.hasNext();) {
                ResourceEnvRef ref = iterator.next();
                if (ref.getType().equals(EntityContext.class.getName())) {
                    iterator.remove();
                } else if (ref.getType().equals(TimerService.class.getName())) {
                    iterator.remove();
                } else if (ref.getType().equals(WebServiceContext.class.getName())) {
                    iterator.remove();
                } else {
                    ref.setMappedName(null);
                }
                ref.getInjectionTarget().clear();

            }
            for (ServiceRef ref : enterpriseBean.getServiceRef()) {
                ref.setMappedName(null);
                ref.getInjectionTarget().clear();
            }
        }
    }


    @Override
    public void installModule(JarFile earFile,
                              EARContext earContext,
                              Module module,
                              Collection configurationStores,
                              ConfigurationStore targetConfigurationStore,
                              Collection repository) throws DeploymentException {
        if (module instanceof EjbModule) {
            installModule(module, earContext);
            EARContext moduleContext;
            if (module.isStandAlone()) {
                moduleContext = earContext;
                earContext.addToClassPath(DEFAULT_BUNDLE_CLASSPATH);
            } else {
                moduleContext = new FragmentContext(earContext, ConfigurationModuleType.EJB);
                //            Environment environment = earContext.getConfiguration().getEnvironment();
                //            File configurationDir = new File(earContext.getBaseDir(), module.getTargetPath());
                ////            configurationDir.mkdirs();
                //
                //            // construct the ejb app deployment context... this is the same class used by the ear context
                //            try {
                //                File inPlaceConfigurationDir = null;
                //                if (null != earContext.getInPlaceConfigurationDir()) {
                //                    inPlaceConfigurationDir = new File(earContext.getInPlaceConfigurationDir(), module.getTargetPath());
                //                }
                //                moduleContext = new EARContext(configurationDir,
                //                        inPlaceConfigurationDir,
                //                        environment,
                //                        ConfigurationModuleType.EJB,
                //                        module.getModuleName(),
                //                        earContext);
                //            } catch (DeploymentException e) {
                //                cleanupConfigurationDir(configurationDir);
                //                throw e;
                //            }
            }
            module.setEarContext(moduleContext);
            module.setRootEarContext(earContext);
            if (((EjbModule) module).getEjbJar().getAssemblyDescriptor() != null) {
                namingBuilder.buildEnvironment(null, null, module.getEnvironment());
            }
            for (ModuleBuilderExtension builder : moduleBuilderExtensions) {
                try {
                    builder.installModule(earFile, earContext, module, configurationStores, targetConfigurationStore, repository);
                } catch (Throwable t) {
                    String builderName = builder.getClass().getSimpleName();
                    log.error(builderName + ".installModule() failed: " + t.getMessage(), t);
                }
            }
        } else {
            LinkedHashSet<Module<?,?>> modules = module.getModules();
            for (Module<?,?> subModule: modules) {
                if (subModule instanceof EjbModule)  {
                    subModule.setEarContext(module.getEarContext());
                    subModule.setRootEarContext(module.getRootEarContext());
                    //don't copy, module is already in classloader
                    if(module instanceof AppClientModule){
                        earContext = module.getEarContext();
                    }
                    registerModule(subModule, earContext);
                }
            }
        }
    }

    private void installModule(Module module, EARContext earContext) throws DeploymentException {
        registerModule(module, earContext);
        JarFile moduleFile = module.getModuleFile();
        try {
            if (module.isStandAlone()) {
                JarUtils.unzipToDirectory(new JarFile(moduleFile.getName()), earContext.getBaseDir());
            } else {
                // extract the ejbJar file into a standalone packed jar file and add the contents to the output
                earContext.addIncludeAsPackedJar(URI.create(module.getTargetPath()), moduleFile);
               // add manifest class path entries to the ejb module classpath
                Collection<String> EjbModuleClasspaths = module.getClassPath();
                EjbModuleClasspaths.add(module.getTargetPath());
                Collection<String> moduleLocations = module.isStandAlone() ? null : module.getParentModule()
                        .getModuleLocations();
                URI baseUri = URI.create(module.getTargetPath());
                earContext.getCompleteManifestClassPath(module.getDeployable(), baseUri, URI.create("."), EjbModuleClasspaths, moduleLocations);

                for (String classpath:EjbModuleClasspaths){
                    earContext.addToClassPath(classpath);
                }

            }
            //earContext.addInclude(".", moduleFile);
        } catch (IOException e) {
            throw new DeploymentException("Unable to copy ejb module jar into configuration: " + moduleFile.getName(), e);
        }
    }

    private void registerModule(Module module, EARContext earContext) {
        EarData earData = EarData.KEY.get(earContext.getGeneralData());
        if (earData == null) {
            earData = new EarData();
            earContext.getGeneralData().put(EarData.KEY, earData);
        }
        earData.addEjbModule((EjbModule) module);
    }

    private static final String LINE_SEP = System.getProperty("line.separator");

    private boolean cleanupConfigurationDir(File configurationDir) {
        LinkedList<String> cannotBeDeletedList = new LinkedList<String>();

        if (!FileUtils.recursiveDelete(configurationDir, cannotBeDeletedList)) {
            // Output a message to help user track down file problem
            log.warn("Unable to delete " + cannotBeDeletedList.size() +
                    " files while recursively deleting directory "
                    + configurationDir.getAbsolutePath() + LINE_SEP +
                    "The first file that could not be deleted was:" + LINE_SEP + "  " +
                    (!cannotBeDeletedList.isEmpty() ? cannotBeDeletedList.getFirst() : ""));
            return false;
        }
        return true;
    }

    @Override
    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        if (module instanceof EjbModule) {
            doInitContext(earContext, module, bundle);
        } else {
            LinkedHashSet<Module<?,?>> modules = module.getModules();
            for (Module<?,?> subModule: modules) {
                if (subModule instanceof EjbModule)  {

                    if(module instanceof AppClientModule){
                        earContext = module.getEarContext();
                    }
                    doInitContext(earContext, subModule, bundle);
                }
            }
        }
    }


    private void doInitContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        EjbModule ejbModule = (EjbModule) module;



        AppInfo appInfo = getAppInfo(earContext, ejbModule, bundle);

        ejbModule.setEjbJarInfo(AppInfoGBean.getEjbJarInfo(appInfo, ejbModule.getModuleURI()));

        // update the original spec dd with the metadata complete dd
        EjbJar ejbJar = ejbModule.getEjbJar();
        ejbModule.setOriginalSpecDD(XmlUtil.marshal(ejbJar));

        // Get the geronimo-openejb plan
           GeronimoEjbJarType geronimoEjbJarType = (GeronimoEjbJarType) ejbModule.getEjbModule().getAltDDs().get("geronimo-openejb.xml");

        // We must set all mapped name references back to null or Geronimo will blow up
        // unmapReferences(ejbJar, geronimoEjbJarType);

        // create a xmlbeans version of the ejb-jar.xml file, because the jndi code is coupled based on xmlbeans objects
       /* EjbJarType ejbJarType = XmlUtil.convertToXmlbeans(ejbJar);
        ejbModule.setSpecDD(ejbJarType);*/

        // convert the plan to xmlbeans since geronimo naming is coupled on xmlbeans objects
            OpenejbGeronimoEjbJarType geronimoOpenejb = XmlUtil.convertToXmlbeans(geronimoEjbJarType);

           ejbModule.getVendorDD().set(geronimoOpenejb);

        // todo move namingBuilders.buildEnvironment() here when geronimo naming supports it

        // initialize the naming builders
        if (ejbJar.getAssemblyDescriptor() != null) {
            final List<MessageDestination> specMessageDestinations = ejbJar.getAssemblyDescriptor().getMessageDestination();
            final JndiConsumer  jndiConsumer = new MessageDestinationInfo(specMessageDestinations);
                namingBuilder.initContext(jndiConsumer,
                        (XmlObject)module.getVendorDD(),
                        ejbModule);
        }

        EjbDeploymentBuilder ejbDeploymentBuilder = new EjbDeploymentBuilder(earContext, ejbModule, namingBuilder, resourceEnvironmentSetter);
        ejbModule.setEjbBuilder(ejbDeploymentBuilder);
        ejbDeploymentBuilder.initContext();

        // Add extra gbean declared in the geronimo-openejb.xml file
        serviceBuilders.build(ejbModule.getVendorDD(), earContext, ejbModule.getEarContext());


        GBeanData ejbModuleGBeanData = new GBeanData(ejbModule.getModuleName(), EjbModuleImpl.class);
        try {

            //starts the ejbs; happens in module order (which may be before or after web modules)
            ejbModule.addGBean(ejbModuleGBeanData);

        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add ejb module gbean", e);
        }
        //commented out code is for sharing owb context of an ejb module in a war with the war
//        module.getSharedContext().put(SharedOwbContext.class, ejbModuleGBeanData);

        for (ModuleBuilderExtension builder : moduleBuilderExtensions) {
            try {
                builder.initContext(earContext, module, bundle);
            } catch (Throwable t) {
                String builderName = builder.getClass().getSimpleName();
                log.error(builderName + ".initContext() failed: " + t.getMessage(), t);
            }
        }
    }

    private AppInfo getAppInfo(EARContext earContext, EjbModule ejbModule, Bundle bundle) throws DeploymentException {
        EarData earData = EarData.KEY.get(earContext.getGeneralData());
        if (earData.getAppInfo() == null) {

            ClassLoader bundleLoader = new BundleClassLoader(bundle);

            // temporary classloader is used for processing ejb annotations and byte code manipulation during ejb load
//            TemporaryClassLoader temporaryClassLoader = new TemporaryClassLoader(new URL[0], bundleLoader);

            // create an openejb app module for the ear containing all ejb modules
            AppModule appModule = new AppModule(bundleLoader, earContext.getConfigID().toString(), null, ejbModule.isStandAlone());
            for (EjbModule module : earData.getEjbModules()) {
                module.setClassLoader(bundleLoader);
                appModule.getEjbModules().add(module.getEjbModule());
            }

            if(!ejbModule.isStandAlone())
            {
               appModule.setModuleId(ejbModule.getParentModule().getName());
            }

            // build the config info tree
            // this method fills in the ejbJar jaxb tree based on the annotations
            // (metadata complete) and it run the openejb verifier
            AppInfo appInfo;
            try {
                appInfo = configureApplication(appModule, ejbModule, earContext.getConfiguration());
            } catch (ValidationFailedException set) {
                StringBuilder sb = new StringBuilder();
                sb.append("Jar failed validation: ").append(appModule.getModuleId());

                for (ValidationError e : set.getErrors()) {
                    sb.append(e.getPrefix()).append(" ... ").append(e.getComponentName()).append(":\t").append(e.getMessage(2));
                }

                for (ValidationFailure e : set.getFailures()) {
                    sb.append(e.getPrefix()).append(" ... ").append(e.getComponentName()).append(":\t").append(e.getMessage(2));
                }

                throw new DeploymentException(sb.toString());
            } catch (OpenEJBException e) {
                throw new DeploymentException(e);
            }

            earData.setAppInfo(appInfo);
            // add the cmp jar
            CmpJarBuilder cmp2Builder = new CmpJarBuilder(appInfo, bundleLoader);
            try {
                File generatedJar = cmp2Builder.getJarFile();
                if (generatedJar != null) {
                    String generatedPath = ejbModule.getTargetPath();
                    if (generatedPath.endsWith(".jar")) {
                        generatedPath = generatedPath.substring(0, generatedPath.length() - 4);
                    }
                    generatedPath += "-cmp2.jar";
                    earContext.addInclude(URI.create(generatedPath), generatedJar);
                }
            } catch (IOException e) {
                throw new DeploymentException(e);
            }

            // add the cmp persistence unit if needed
            if (appInfo.cmpMappingsXml != null) {
                addGeronimmoOpenEJBPersistenceUnit(ejbModule);
            }
            Module m = ejbModule;
            while (m.getParentModule() != null) {
                m = m.getParentModule();
            }
            AbstractName appInfoName = earContext.getNaming().createChildName(m.getModuleName(), "appInfoGBean", "appInfoGBean");
            earContext.getGeneralData().put(EARContext.APPINFO_GBEAN_NAME_KEY, appInfoName);
            GBeanData appInfoData = new GBeanData(appInfoName, AppInfoGBean.class);
            appInfoData.setAttribute("appInfo", appInfo);
            appInfoData.setReferencePattern("OpenEjbSystem", new AbstractNameQuery(null, Collections.EMPTY_MAP, OpenEjbSystem.class.getName()));
            try {
                earContext.addGBean(appInfoData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("Duplicate app info gbean", e);
            }
        }

        return earData.getAppInfo();
    }

    private AppInfo configureApplication(AppModule appModule, EjbModule ejbModule, Configuration configuration)
            throws OpenEJBException {
        OpenEjbConfiguration openEjbConfiguration = new OpenEjbConfiguration();
        openEjbConfiguration.containerSystem = new ContainerSystemInfo();
        openEjbConfiguration.facilities = new FacilitiesInfo();
        boolean offline = true;

        ConfigurationFactory.Chain chain = new ConfigurationFactory.Chain();
        ConfigurationFactory configurationFactory = new ConfigurationFactory(offline, chain, openEjbConfiguration);
        buildChain(offline,
                   ejbModule.getPreAutoConfigDeployer(),
                   SystemInstance.get().getOptions(),
                   configurationFactory,
                   chain);

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appModule.getClassLoader());
        try {
            addContainerInfos(configuration, openEjbConfiguration.containerSystem, configurationFactory);
            addResourceAdapterMDBInfos(configuration, openEjbConfiguration.containerSystem, configurationFactory);
            //process resource adapters

            return configurationFactory.configureApplication(appModule);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    private static final String DEBUGGABLE_VM_HACKERY_PROPERTY = "openejb.debuggable-vm-hackery";
    private static final String VALIDATION_SKIP_PROPERTY = "openejb.validation.skip";
    private static final String WEBSERVICES_ENABLED = "openejb.webservices.enabled";

    private static ConfigurationFactory.Chain buildChain(boolean offline, DynamicDeployer preAutoConfigDeployer, Options options, ConfigurationFactory configurationFactory, ConfigurationFactory.Chain chain) {
        chain.add(new GeneratedClientModules.Add());

        chain.add(new ReadDescriptors());

        chain.add(new LegacyProcessor());

        chain.add(new GeronimoAnnotationDeployer());

        chain.add(new GeneratedClientModules.Prune());

        chain.add(new ClearEmptyMappedName());

        if (!options.get(VALIDATION_SKIP_PROPERTY, false)) {
            chain.add(new ValidateModules());
        } else {
            log.info("validationDisabled", VALIDATION_SKIP_PROPERTY);
        }

        chain.add(new InitEjbDeployments());

//        if (options.get(DEBUGGABLE_VM_HACKERY_PROPERTY, false)){
//            chain.add(new DebuggableVmHackery());
//        }

        if (options.get(WEBSERVICES_ENABLED, true)){
            chain.add(new WsDeployer());
        } else {
//            chain.add(new RemoveWebServices());
        }

        chain.add(new CmpJpaConversion());

        // By default all vendor support is enabled
        Set<Vendor> support = SystemInstance.get().getOptions().getAll("openejb.vendor.config", Vendor.values());

        if (support.contains(Vendor.GERONIMO) || SystemInstance.get().hasProperty("openejb.geronimo")) {
            chain.add(new OpenEjb2Conversion());
        }

        if (support.contains(Vendor.GLASSFISH)) {
            chain.add(new SunConversion());
        }

        if (support.contains(Vendor.WEBLOGIC)) {
            chain.add(new WlsConversion());
        }

        if (SystemInstance.get().hasProperty("openejb.geronimo")){
            // must be after CmpJpaConversion since it adds new persistence-context-refs
            chain.add(new GeronimoMappedName());
        }

        if (null != preAutoConfigDeployer) {
            chain.add(preAutoConfigDeployer);
        }

        if (offline) {
            AutoConfig autoConfig = new AutoConfig(configurationFactory);
            autoConfig.autoCreateResources(false);
            autoConfig.autoCreateContainers(true);
            chain.add(autoConfig);
        } else {
            chain.add(new AutoConfig(configurationFactory));
        }

        chain.add(new ApplyOpenejbJar());
        chain.add(new MappedNameBuilder());

        // TODO: How do we want this plugged in?
        chain.add(new OutputGeneratedDescriptors());
        return chain;
    }

    private void addContainerInfos(Configuration configuration, ContainerSystemInfo containerSystem, ConfigurationFactory configurationFactory) throws OpenEJBException {
        LinkedHashSet<GBeanData> containerDatas = configuration.findGBeanDatas(Collections.singleton(new AbstractNameQuery(EjbContainer.class.getName())));
        for (GBeanData containerData : containerDatas) {

            String id = EjbContainer.getId(containerData.getAbstractName());

            Class<? extends ContainerInfo> infoClass = EjbContainer.getInfoType(containerData.getGBeanInfo().getClassName());

            Properties declaredProperties = new Properties();

            String providerId = null;

            ContainerInfo containerInfo = configurationFactory.configureService(infoClass, id, declaredProperties, providerId, "Container");
            containerSystem.containers.add(containerInfo);
        }
    }

    private void addResourceAdapterMDBInfos(Configuration configuration, ContainerSystemInfo containerSystem, ConfigurationFactory configurationFactory) throws OpenEJBException {
        LinkedHashSet<GBeanData> resourceAdapterWrappers = configuration.findGBeanDatas(Collections.singleton(new AbstractNameQuery(ResourceAdapterWrapperGBean.class.getName())));
        for (GBeanData resourceAdapterWrapperData : resourceAdapterWrappers) {
            String resourceAdapterId = getResourceAdapterId(resourceAdapterWrapperData.getAbstractName());
            Map<String, String> messageListenerToActivationSpecMap = (Map<String, String>) resourceAdapterWrapperData.getAttribute("messageListenerToActivationSpecMap");
            if (messageListenerToActivationSpecMap == null) {
                continue;
            }
            for (Map.Entry<String, String> entry : messageListenerToActivationSpecMap.entrySet()) {
                String messageListenerInterface = entry.getKey();
                String activationSpecClass = entry.getValue();

                // only process RA if not previously processed
                String containerName = resourceAdapterId + "-" + messageListenerInterface;
                // get default mdb config
                ContainerInfo containerInfo = configurationFactory.configureService(MdbContainerInfo.class);
                containerInfo.id = containerName;
                containerInfo.displayName = containerName;

                // set ra specific properties

                try {
                    containerInfo.properties.put("MessageListenerInterface",
                        configuration.getBundle().loadClass(messageListenerInterface));
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Could not load MessageListenerInterface " + messageListenerInterface + " in bundle: " + configuration.getBundle(), e);
                }
                try {
                    containerInfo.properties.put("ActivationSpecClass",
                        configuration.getBundle().loadClass(activationSpecClass));
                } catch (ClassNotFoundException e) {
                    throw new OpenEJBException("Could not load ActivationSpecClass " + activationSpecClass + " in bundle: " + configuration.getBundle(), e);
                }
                containerInfo.properties.put("TxRecovery", true);
                //TODO is this necessary????
//                containerInfo.properties.put("ResourceAdapter", resourceAdapter);

                containerSystem.containers.add(containerInfo);
            }
        }
    }


    private void addGeronimmoOpenEJBPersistenceUnit(EjbModule ejbModule) {
        GeronimoEjbJarType geronimoEjbJarType = (GeronimoEjbJarType) ejbModule.getEjbModule().getAltDDs().get("geronimo-openejb.xml");

        // search for the cmp persistence unit
        PersistenceUnit persistenceUnit = null;
        for (Persistence persistence : geronimoEjbJarType.getPersistence()) {
            for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
                if ("cmp".equals(unit.getName())) {
                    persistenceUnit = unit;
                    break;
                }
            }
        }

        // if not found create one
        if (persistenceUnit == null) {
            String jtaDataSource = null;
            // todo Persistence Unit Data Sources need to be global JNDI names
            Object altDD = ejbModule.getEjbModule().getAltDDs().get("openejb-jar.xml");
            if (altDD instanceof OpenejbJarType) {
                ResourceLocatorType cmpConnectionFactory = ((OpenejbJarType) altDD).getCmpConnectionFactory();
                if (cmpConnectionFactory != null) {
                    String datasourceName = cmpConnectionFactory.getResourceLink();
                    if (datasourceName != null) {
                        jtaDataSource = datasourceName.trim();
                    }
                }
            }

            persistenceUnit = new PersistenceUnit();
            persistenceUnit.setName("cmp");
            persistenceUnit.setTransactionType(TransactionType.JTA);
            if (jtaDataSource != null) {
                persistenceUnit.setJtaDataSource(jtaDataSource);
            } else {
                persistenceUnit.setJtaDataSource(defaultCmpJTADataSource);
            }
            persistenceUnit.setNonJtaDataSource(defaultCmpNonJTADataSource);
            persistenceUnit.setExcludeUnlistedClasses(true);

            Persistence persistence = new Persistence();
            persistence.setVersion("2.0");
            persistence.getPersistenceUnit().add(persistenceUnit);

            geronimoEjbJarType.getPersistence().add(persistence);
        }
        persistenceUnit.getMappingFile().add("META-INF/openejb-cmp-generated-orm.xml");
    }

    /**
     * Does the meaty work of processing the deployment information and
     * creating GBeans for all the EJBs in the JAR, etc.
     */
    @Override
    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repositories) throws DeploymentException {
        if (module instanceof EjbModule) {
            doAddGBeans(earContext, module, bundle, repositories);
        } else {
            LinkedHashSet<Module<?,?>> modules = module.getModules();
            for (Module<?,?> subModule: modules) {
                if (subModule instanceof EjbModule)  {
                    doAddGBeans(earContext, subModule, bundle, repositories);
                }
            }
        }
    }

    public void doAddGBeans(EARContext earContext, Module module, Bundle bundle, Collection repositories) throws DeploymentException {
        EjbModule ejbModule = (EjbModule) module;
        EjbDeploymentBuilder ejbDeploymentBuilder = ejbModule.getEjbBuilder();

        //push the gbeans into the configuration
        try {
            ejbModule.flushGBeansToContext();
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Name conflict between ejb modules", e);
        }
        // add enc
        String moduleName = module.getName();

        if (earContext.getSubModuleNames().size() > 1 && earContext.getSubModuleNames().contains(moduleName)){
            log.warn("Duplicated moduleName: '"+moduleName +"' is found ! deployer will rename it to: '"+moduleName +
                    "_duplicated' , please check your modules in application to make sure they don't share the same name");
            moduleName = moduleName +"_duplicated";
            earContext.getSubModuleNames().add(moduleName);
        }

        if (!(module.getParentModule() instanceof WebModule) && !(module.getParentModule() instanceof AppClientModule)) {
            earContext.getSubModuleNames().add(moduleName);
            module.getJndiScope(JndiScope.module).put("module/ModuleName", moduleName);
        }

        ejbDeploymentBuilder.buildEnc();

        Set<GBeanData> gBeanDatas = earContext.getConfiguration().findGBeanDatas(Collections.singleton(new AbstractNameQuery(PersistenceUnitGBean.class.getName())));
        LinkResolver<String> linkResolver = new UniqueDefaultLinkResolver<String>();
        for (GBeanData gBeanData : gBeanDatas) {
            String name = (String) gBeanData.getAttribute("persistenceUnitName");
            String rootUrl = (String) gBeanData.getAttribute("persistenceUnitRoot");
            if (name.equals("cmp")) continue;
            String id = name + " " + rootUrl.hashCode();
            linkResolver.add(rootUrl, name, id);
        }

        EjbJarInfo ejbJarInfo = ejbModule.getEjbJarInfo();
        for (EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
            if (beanInfo instanceof StatefulBeanInfo) {
                StatefulBeanInfo statefulBeanInfo = (StatefulBeanInfo) beanInfo;
                for (PersistenceContextReferenceInfo refInfo : statefulBeanInfo.jndiEnc.persistenceContextRefs) {
                    if (refInfo.extended) {
                        refInfo.unitId = linkResolver.resolveLink(refInfo.persistenceUnitName, ejbJarInfo.moduleUri);
                    }
                }
            }
        }
        // Add JSR77 EJBModule GBean
        GBeanData ejbModuleGBeanData;
        try {
            ejbModuleGBeanData = module.getEarContext().findGBeanDatas(module.getEarContext().getConfiguration(), new AbstractNameQuery(ejbModule.getModuleName())).iterator().next();
            ejbModuleGBeanData.setReferencePattern("J2EEServer", module.getRootEarContext().getServerName());
            //TODO nested modules figure out when this makes sense
//            if (!ejbModule.isShared()) {
//                ejbModuleGBeanData.setReferencePattern("J2EEApplication", module.getRootEarContext().getModuleName());
//            }

            ejbModuleGBeanData.setAttribute("deploymentDescriptor", ejbModule.getOriginalSpecDD());
            ejbModuleGBeanData.setAttribute("isStandalone", ejbModule.isStandAlone());

            ejbModuleGBeanData.setReferencePatterns("EJBCollection",
                    new ReferencePatterns(new AbstractNameQuery(null,
                            Collections.singletonMap(NameFactory.EJB_MODULE, ejbModule.getModuleName().getNameProperty(NameFactory.J2EE_NAME)),
                            EjbDeployment.class.getName())));

            ejbModuleGBeanData.setReferencePattern("OpenEjbSystem", new AbstractNameQuery(null, Collections.EMPTY_MAP, OpenEjbSystem.class.getName()));
            ejbModuleGBeanData.setReferencePattern("AppInfo", EARContext.APPINFO_GBEAN_NAME_KEY.get(earContext.getGeneralData()));
            ejbModuleGBeanData.setAttribute("moduleURI", ejbModule.getEjbModule().getModuleUri());
            ejbModuleGBeanData.setAttribute("moduleContext", module.getJndiScope(JndiScope.module));
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean " + ejbModule.getModuleName(), e);
        }

        // add a depdendency on the ejb module object
        ejbDeploymentBuilder.addEjbModuleDependency(ejbModuleGBeanData);

        // add the Jacc permissions to the ear
        String policyContextId = ejbModule.getEjbJarInfo().moduleUri.toString();
        ComponentPermissions componentPermissions = (ComponentPermissions) earContext.getContextIDToPermissionsMap().get(policyContextId);
        if (componentPermissions == null) {
            componentPermissions = new ComponentPermissions(new Permissions(), new Permissions(), new HashMap<String, PermissionCollection>());
            earContext.addSecurityContext(policyContextId, componentPermissions);
        }
        ejbDeploymentBuilder.buildComponentPermissions(componentPermissions);

        setMdbContainerIds(earContext, ejbModule, ejbModuleGBeanData);

        for (ModuleBuilderExtension builder : moduleBuilderExtensions) {
            try {
                builder.addGBeans(earContext, module, bundle, repositories);
            } catch (Throwable t) {
                String builderName = builder.getClass().getSimpleName();
                log.error(builderName + ".addGBeans() failed: " + t.getMessage(), t);
            }
        }
    }

    private void setMdbContainerIds(EARContext earContext, EjbModule ejbModule, GBeanData ejbModuleGBeanData) throws DeploymentException {
        Object altDD = ejbModule.getEjbModule().getAltDDs().get("openejb-jar.xml");
        if (!(altDD instanceof OpenejbJarType)) {
            return;
        }
        OpenejbJarType openejbJarType = (OpenejbJarType) altDD;
        EjbJarInfo ejbJarInfo = ejbModule.getEjbJarInfo();

        Map<String, MessageDrivenBeanInfo> mdbs = new TreeMap<String, MessageDrivenBeanInfo>();
        for (EnterpriseBeanInfo enterpriseBean : ejbJarInfo.enterpriseBeans) {
            if (enterpriseBean instanceof MessageDrivenBeanInfo) {
                mdbs.put(enterpriseBean.ejbName, (MessageDrivenBeanInfo) enterpriseBean);
            }
        }
        for (org.apache.openejb.jee.oejb2.EnterpriseBean enterpriseBean : openejbJarType.getEnterpriseBeans()) {
            if (!(enterpriseBean instanceof MessageDrivenBeanType)) {
                continue;
            }
            MessageDrivenBeanType bean = (MessageDrivenBeanType) enterpriseBean;
            MessageDrivenBeanInfo messageDrivenBeanInfo = mdbs.get(bean.getEjbName());
            if (messageDrivenBeanInfo == null) {
                continue;
            }
            if (messageDrivenBeanInfo.containerId != null) {
                // containerId already set
                continue;
            }

            if (bean.getResourceAdapter() == null) {
                throw new DeploymentException("No Resource Adapter defined for MDB '" + bean.getEjbName() + "'");
            }

            AbstractNameQuery resourceAdapterNameQuery = getResourceAdapterNameQuery(bean.getResourceAdapter());
            AbstractName resourceAdapterAbstractName;
            try {
                resourceAdapterAbstractName = earContext.findGBean(resourceAdapterNameQuery);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Resource Adapter for MDB '" + bean.getEjbName() + "'not found: " + resourceAdapterNameQuery, e);
            }

            String resourceAdapterId = getResourceAdapterId(resourceAdapterAbstractName);
            messageDrivenBeanInfo.containerId = resourceAdapterId + "-" + messageDrivenBeanInfo.mdbInterface;

            // add a dependency from the module to the ra so we can be assured the mdb
            // container exists when this app is started
            //TODO we are now useing a sledgehammer in EjbDeploymentBuilder and adding any possibly relevant
            // dependency to every ejb gbean.
            ejbModuleGBeanData.addDependency(resourceAdapterAbstractName);
        }
        //check that all the mdbs have resource adapters identified.
        for(MessageDrivenBeanInfo mdbInfo:mdbs.values()){
            if(mdbInfo != null && mdbInfo.containerId == null){
                throw new DeploymentException("No Resource Adapter defined for MDB '" + mdbInfo.ejbName + "'");
            }
        }
    }

    private String getResourceAdapterId(AbstractName resourceAdapterAbstractName) {
        Map properties = resourceAdapterAbstractName.getName();
        String shortName = (String) properties.get("name");
        String moduleName = (String) properties.get("ResourceAdapterModule");
        if (shortName != null && moduleName != null) {
            return moduleName + "." + shortName;
        } else {
            return resourceAdapterAbstractName.getObjectName().toString();
        }
    }

    private static AbstractNameQuery getResourceAdapterNameQuery(ResourceLocatorType resourceLocator) {
        if (resourceLocator.getResourceLink() != null) {
            Map<String, String> nameMap = new HashMap<String, String>();
            nameMap.put("name", resourceLocator.getResourceLink());
            nameMap.put("j2eeType", NameFactory.JCA_RESOURCE_ADAPTER);
            return new AbstractNameQuery(null, nameMap);
        }

        //construct name from components
        PatternType pattern = resourceLocator.getPattern();
        Artifact artifact = null;
        if (pattern.getArtifactId() != null) {
            artifact = new Artifact(pattern.getGroupId(), pattern.getArtifactId(), pattern.getVersion(), "car");
        }

        Map<String, String> nameMap = new HashMap<String, String>();
        nameMap.put("name", pattern.getName());
        nameMap.put("j2eeType", NameFactory.JCA_RESOURCE_ADAPTER);
        if (pattern.getModule() != null) {
            nameMap.put(NameFactory.RESOURCE_ADAPTER_MODULE, pattern.getModule());
        }
        return new AbstractNameQuery(artifact, nameMap, (Set) null);
    }

    private boolean isEjbModule(URL baseUrl,List<URL> urls, ClassLoader classLoader, Map<String, URL> descriptors) {
        try {

            String path = baseUrl.getPath();

            if (path.endsWith("/")) path = path.substring(0, path.length() - 1);

            if (descriptors.containsKey("application.xml") || path.endsWith(".ear")) {
                return false;
            }

            if (descriptors.containsKey("application-client.xml")) {
                return false;
            }

            if (descriptors.containsKey("ra.xml") || path.endsWith(".rar")) {
                return false;
            }

            if (descriptors.containsKey("ejb-jar.xml") || descriptors.containsKey("beans.xml")) {
                return true;
            }

            URL manifestUrl = descriptors.get("MANIFEST.MF");
            if (manifestUrl != null) {
                // In this case scanPotentialClientModules really means "require application-client.xml"
                InputStream is = manifestUrl.openStream();
                Manifest manifest = new Manifest(is);
                String mainClass = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                if (mainClass != null) {
                    return false;
                }
            }

            AnnotationFinder classFinder = new AnnotationFinder(classLoader, urls);

            AnnotationFinder.Filter filter = new AnnotationFinder.Filter() {
                public boolean accept(String annotationName) {
                    if (annotationName.startsWith("javax.ejb.")) {
                        if ("javax.ejb.Stateful".equals(annotationName)) return true;
                        if ("javax.ejb.Stateless".equals(annotationName)) return true;
                        if ("javax.ejb.Singleton".equals(annotationName)) return true;
                        if ("javax.ejb.MessageDriven".equals(annotationName)) return true;
                    } else if ("javax.annotation.ManagedBean".equals(annotationName)) {
                        return true;
                    }
                    return false;
                }
            };

            return classFinder.find(filter);

        } catch (Exception e) {
            log.warn("Unable to determine module type for jar: " + baseUrl.toExternalForm(), e);
            return false;
        }
    }

    private void releaseTempClassLoader(ClassLoader classLoader) {
        if (classLoader != null)
            try {
                    ClassLoaderUtil.destroyClassLoader(classLoader.getParent());
                    ClassLoaderUtil.destroyClassLoader(classLoader);
            } catch (Exception e) {
            }
    }

    public static class EarData {
        public static final EARContext.Key<EarData> KEY = new EARContext.Key<EarData>() {

            @Override
            public EarData get(Map<EARContext.Key, Object> context) {
                return (EarData) context.get(this);
            }
        };

        private final Map<String, EjbModule> ejbModules = new TreeMap<String, EjbModule>();
        private AppInfo appInfo;

        public void addEjbModule(EjbModule ejbModule) {
            ejbModules.put(ejbModule.getEjbModule().getModuleUri().toString(), ejbModule);
        }

        public EjbModule getEjbModule(String moduleURI) throws DeploymentException {
            EjbModule ejbModule = ejbModules.get(moduleURI);
            if (ejbModule == null) {
                throw new DeploymentException("Ejb  module " + moduleURI + " was not found in configured module list " + ejbModules.keySet());
            }
            return ejbModule;
        }

        public Collection<EjbModule> getEjbModules() {
            return ejbModules.values();
        }

        public void setAppInfo(AppInfo appInfo) {
            this.appInfo = appInfo;
        }

        public AppInfo getAppInfo() throws DeploymentException {
            return appInfo;
        }
    }

    public static class MessageDestinationInfo implements JndiConsumer {

        private final IllegalStateException notAllowedException = new IllegalStateException(
                "this instance is only used for adminObjectRefBuilder to get MessageDestination defined.");
        private final List<MessageDestination> specMessageDestinations;

        public MessageDestinationInfo(List<MessageDestination> specMessageDestinations) {
            this.specMessageDestinations = specMessageDestinations == null ? new ArrayList<MessageDestination>()
                    : specMessageDestinations;
        }

        @Override
        public Collection<DataSource> getDataSource() {
            throw notAllowedException;
        }

        @Override
        public Map<String, DataSource> getDataSourceMap() {
            throw notAllowedException;
        }

        @Override
        public Collection<EjbLocalRef> getEjbLocalRef() {
            throw notAllowedException;
        }

        @Override
        public Map<String, EjbLocalRef> getEjbLocalRefMap() {
            throw notAllowedException;
        }

        @Override
        public Collection<EjbRef> getEjbRef() {
            throw notAllowedException;
        }

        @Override
        public Map<String, EjbRef> getEjbRefMap() {
            throw notAllowedException;
        }

        @Override
        public Collection<EnvEntry> getEnvEntry() {
            throw notAllowedException;
        }

        @Override
        public Map<String, EnvEntry> getEnvEntryMap() {
            throw notAllowedException;
        }

        @Override
        public String getJndiConsumerName() {
            throw notAllowedException;
        }

        @Override
        public Collection<MessageDestinationRef> getMessageDestinationRef() {
            throw notAllowedException;
        }

        @Override
        public Map<String, MessageDestinationRef> getMessageDestinationRefMap() {
            throw notAllowedException;
        }

        @Override
        public Collection<PersistenceContextRef> getPersistenceContextRef() {
            throw notAllowedException;
        }

        @Override
        public Map<String, PersistenceContextRef> getPersistenceContextRefMap() {
            throw notAllowedException;
        }

        @Override
        public Collection<PersistenceUnitRef> getPersistenceUnitRef() {
            throw notAllowedException;
        }

        @Override
        public Map<String, PersistenceUnitRef> getPersistenceUnitRefMap() {
            throw notAllowedException;
        }

        @Override
        public Collection<ResourceEnvRef> getResourceEnvRef() {
            throw notAllowedException;
        }

        @Override
        public Map<String, ResourceEnvRef> getResourceEnvRefMap() {
            throw notAllowedException;
        }

        @Override
        public Collection<ResourceRef> getResourceRef() {
            throw notAllowedException;
        }

        @Override
        public Map<String, ResourceRef> getResourceRefMap() {
            throw notAllowedException;
        }

        @Override
        public Collection<ServiceRef> getServiceRef() {
            throw notAllowedException;
        }

        @Override
        public Map<String, ServiceRef> getServiceRefMap() {
            throw notAllowedException;
        }

        /*
         * allow AdminObjectRefBuilder.initContext(JndiConsumer, XmlObject, Module) to call this method with reflection
         * to get MessageDestinations in spec dd.
         */
        public List<MessageDestination> getMessageDestination() {
            return specMessageDestinations;
        }

    }

}
