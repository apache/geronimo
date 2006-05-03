/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.j2ee.deployment;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.j2ee.ApplicationInfo;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.J2EEApplicationImpl;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationAlreadyExistsException;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.security.deployment.SecurityBuilder;
import org.apache.geronimo.security.deployment.SecurityConfiguration;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationDocument;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerExtModuleType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerModuleType;
import org.apache.geronimo.xbeans.j2ee.ApplicationType;
import org.apache.geronimo.xbeans.j2ee.ModuleType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev:385232 $ $Date$
 */
public class EARConfigBuilder implements ConfigurationBuilder {

    private final static QName APPLICATION_QNAME = GerApplicationDocument.type.getDocumentElementName();

    private final ConfigurationManager configurationManager;
    private final Collection repositories;
    private final SingleElementCollection ejbConfigBuilder;
    private final SingleElementCollection webConfigBuilder;
    private final SingleElementCollection connectorConfigBuilder;
    private final SingleElementCollection appClientConfigBuilder;
    private final SingleElementCollection ejbReferenceBuilder;
    private final SingleElementCollection resourceReferenceBuilder;
    private final SingleElementCollection serviceReferenceBuilder;

    private final Environment defaultEnvironment;
    private final AbstractNameQuery serverName;
    private final AbstractNameQuery transactionContextManagerObjectName;
    private final AbstractNameQuery connectionTrackerObjectName;
    private final AbstractNameQuery transactionalTimerObjectName;
    private final AbstractNameQuery nonTransactionalTimerObjectName;
    private final AbstractNameQuery corbaGBeanObjectName;
    private final Naming naming;


    public EARConfigBuilder(Environment defaultEnvironment,
            AbstractNameQuery transactionContextManagerAbstractName,
            AbstractNameQuery connectionTrackerAbstractName,
            AbstractNameQuery transactionalTimerAbstractName,
            AbstractNameQuery nonTransactionalTimerAbstractName,
            AbstractNameQuery corbaGBeanAbstractName,
            AbstractNameQuery serverName,
            Collection repositories,
            Collection ejbConfigBuilder,
            Collection ejbReferenceBuilder,
            Collection webConfigBuilder,
            Collection connectorConfigBuilder,
            Collection resourceReferenceBuilder,
            Collection appClientConfigBuilder,
            Collection serviceReferenceBuilder,
            Kernel kernel) {
        this(defaultEnvironment,
                transactionContextManagerAbstractName,
                connectionTrackerAbstractName,
                transactionalTimerAbstractName,
                nonTransactionalTimerAbstractName,
                corbaGBeanAbstractName,
                serverName,
                ConfigurationUtil.getConfigurationManager(kernel),
                repositories,
                new SingleElementCollection(ejbConfigBuilder),
                new SingleElementCollection(ejbReferenceBuilder),
                new SingleElementCollection(webConfigBuilder),
                new SingleElementCollection(connectorConfigBuilder),
                new SingleElementCollection(resourceReferenceBuilder),
                new SingleElementCollection(appClientConfigBuilder),
                new SingleElementCollection(serviceReferenceBuilder),
                kernel.getNaming());
    }
    public EARConfigBuilder(Environment defaultEnvironment,
            AbstractNameQuery transactionContextManagerAbstractName,
            AbstractNameQuery connectionTrackerAbstractName,
            AbstractNameQuery transactionalTimerAbstractName,
            AbstractNameQuery nonTransactionalTimerAbstractName,
            AbstractNameQuery corbaGBeanAbstractName,
            AbstractNameQuery serverName,
            Collection repositories,
            ModuleBuilder ejbConfigBuilder,
            EJBReferenceBuilder ejbReferenceBuilder,
            ModuleBuilder webConfigBuilder,
            ModuleBuilder connectorConfigBuilder,
            ResourceReferenceBuilder resourceReferenceBuilder,
            ModuleBuilder appClientConfigBuilder,
            ServiceReferenceBuilder serviceReferenceBuilder,
            Naming naming) {
        this(defaultEnvironment,
                transactionContextManagerAbstractName,
                connectionTrackerAbstractName,
                transactionalTimerAbstractName,
                nonTransactionalTimerAbstractName,
                corbaGBeanAbstractName,
                serverName,
                null,
                repositories,
                new SingleElementCollection(ejbConfigBuilder),
                new SingleElementCollection(ejbReferenceBuilder),
                new SingleElementCollection(webConfigBuilder),
                new SingleElementCollection(connectorConfigBuilder),
                new SingleElementCollection(resourceReferenceBuilder),
                new SingleElementCollection(appClientConfigBuilder),
                new SingleElementCollection(serviceReferenceBuilder),
                naming);
    }

    private EARConfigBuilder(Environment defaultEnvironment,
            AbstractNameQuery transactionContextManagerAbstractName,
            AbstractNameQuery connectionTrackerAbstractName,
            AbstractNameQuery transactionalTimerAbstractName,
            AbstractNameQuery nonTransactionalTimerAbstractName,
            AbstractNameQuery corbaGBeanAbstractName,
            AbstractNameQuery serverName,
            ConfigurationManager configurationManager,
            Collection repositories,
            SingleElementCollection ejbConfigBuilder,
            SingleElementCollection ejbReferenceBuilder,
            SingleElementCollection webConfigBuilder,
            SingleElementCollection connectorConfigBuilder,
            SingleElementCollection resourceReferenceBuilder,
            SingleElementCollection appClientConfigBuilder,
            SingleElementCollection serviceReferenceBuilder,
            Naming naming) {
        this.configurationManager = configurationManager;
        this.repositories = repositories;
        this.defaultEnvironment = defaultEnvironment;

        this.ejbConfigBuilder = ejbConfigBuilder;
        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.webConfigBuilder = webConfigBuilder;
        this.connectorConfigBuilder = connectorConfigBuilder;
        this.appClientConfigBuilder = appClientConfigBuilder;
        this.serviceReferenceBuilder = serviceReferenceBuilder;
        this.transactionContextManagerObjectName = transactionContextManagerAbstractName;
        this.connectionTrackerObjectName = connectionTrackerAbstractName;
        this.transactionalTimerObjectName = transactionalTimerAbstractName;
        this.nonTransactionalTimerObjectName = nonTransactionalTimerAbstractName;
        this.corbaGBeanObjectName = corbaGBeanAbstractName;
        this.serverName = serverName;
        this.naming = naming;
    }

    private ModuleBuilder getEjbConfigBuilder() {
        return (ModuleBuilder) ejbConfigBuilder.getElement();
    }

    private ModuleBuilder getWebConfigBuilder() {
        return (ModuleBuilder) webConfigBuilder.getElement();
    }

    private ModuleBuilder getConnectorConfigBuilder() {
        return (ModuleBuilder) connectorConfigBuilder.getElement();
    }

    private ModuleBuilder getAppClientConfigBuilder() {
        return (ModuleBuilder) appClientConfigBuilder.getElement();
    }

    private EJBReferenceBuilder getEjbReferenceBuilder() {
        return (EJBReferenceBuilder) ejbReferenceBuilder.getElement();
    }

    private ResourceReferenceBuilder getResourceReferenceBuilder() {
        return (ResourceReferenceBuilder) resourceReferenceBuilder.getElement();
    }

    private ServiceReferenceBuilder getServiceReferenceBuilder() {
        return (ServiceReferenceBuilder) serviceReferenceBuilder.getElement();
    }

    public Object getDeploymentPlan(File planFile, JarFile jarFile, ModuleIDBuilder idBuilder) throws DeploymentException {
        if (planFile == null && jarFile == null) {
            return null;
        }
        ApplicationInfo plan = getEarPlan(planFile, jarFile, idBuilder);
        if (plan != null) {
            return plan;
        }
        //Only "synthetic" ears with only external modules can have no jar file.
        if (jarFile == null) {
            return null;
        }

        // get the modules either the application plan or for a stand alone module from the specific deployer
        Module module = null;
        if (getWebConfigBuilder() != null) {
            module = getWebConfigBuilder().createModule(planFile, jarFile, naming, idBuilder);
        }
        if (module == null && getEjbConfigBuilder() != null) {
            module = getEjbConfigBuilder().createModule(planFile, jarFile, naming, idBuilder);
        }
        if (module == null && getConnectorConfigBuilder() != null) {
            module = getConnectorConfigBuilder().createModule(planFile, jarFile, naming, idBuilder);
        }
        if (module == null && getAppClientConfigBuilder() != null) {
            module = getAppClientConfigBuilder().createModule(planFile, jarFile, naming, idBuilder);
        }
        if (module == null) {
            return null;
        }

        return new ApplicationInfo(module.getType(),
                module.getEnvironment(),
                module.getModuleName(),
                null,
                null,
                new LinkedHashSet(Collections.singleton(module)),
                Collections.EMPTY_SET,
                null);
    }

    private ApplicationInfo getEarPlan(File planFile, JarFile earFile, ModuleIDBuilder idBuilder) throws DeploymentException {
        String specDD;
        ApplicationType application = null;
        if (earFile != null) {
            try {
                URL applicationXmlUrl = DeploymentUtil.createJarURL(earFile, "META-INF/application.xml");
                specDD = DeploymentUtil.readAll(applicationXmlUrl);
            } catch (Exception e) {
                //no application.xml, not for us
                return null;
            }
            //we found something called application.xml in the right place, if we can't parse it it's an error
            try {
                XmlObject xmlObject = XmlBeansUtil.parse(specDD);
                application = SchemaConversionUtils.convertToApplicationSchema(xmlObject).getApplication();
            } catch (XmlException e) {
                throw new DeploymentException("Could not parse application.xml", e);
            }
        }

        GerApplicationType gerApplication = null;
        try {
            // load the geronimo-application.xml from either the supplied plan or from the earFile
            XmlObject rawPlan;
            try {
                if (planFile != null) {
                    rawPlan = XmlBeansUtil.parse(planFile.toURL());
                    gerApplication = (GerApplicationType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, APPLICATION_QNAME, GerApplicationType.type);
                    if (gerApplication == null) {
                        return null;
                    }
                } else {
                    URL path = DeploymentUtil.createJarURL(earFile, "META-INF/geronimo-application.xml");
                    rawPlan = XmlBeansUtil.parse(path);
                    gerApplication = (GerApplicationType) SchemaConversionUtils.fixGeronimoSchema(rawPlan, APPLICATION_QNAME, GerApplicationType.type);
                }
            } catch (IOException e) {
                //TODO isn't this an error?
            }

            // if we got one extract the validate it otherwise create a default one
            if (gerApplication == null) {
                gerApplication = createDefaultPlan(application, earFile);
            }
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        EnvironmentType environmentType = gerApplication.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        idBuilder.resolve(environment, earFile == null ? planFile.getName() : new File(earFile.getName()).getName(), "ear");
        // Make this EAR's settings the default for child modules
        idBuilder.setDefaultGroup(environment.getConfigId().getGroupId());
        idBuilder.setDefaultVersion(environment.getConfigId().getVersion());

        Artifact artifact = environment.getConfigId();
        AbstractName earName = naming.createRootName(artifact, artifact.toString(), NameFactory.J2EE_APPLICATION);

        // get the modules either the application plan or for a stand alone module from the specific deployer
        // todo change module so you can extract the real module path back out.. then we can eliminate
        // the moduleLocations and have addModules return the modules
        Set moduleLocations = new HashSet();
        LinkedHashSet modules = new LinkedHashSet();
        try {
            addModules(earFile, application, gerApplication, moduleLocations, modules, environment, earName, idBuilder);
        } catch (Throwable e) {
            // close all the modules
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                module.close();
            }

            if (e instanceof DeploymentException) {
                throw (DeploymentException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            }
            throw new DeploymentException(e);
        }

        return new ApplicationInfo(ConfigurationModuleType.EAR,
                environment,
                earName,
                application,
                gerApplication,
                modules,
                moduleLocations,
                application == null ? null : application.toString());
    }


    private GerApplicationType createDefaultPlan(ApplicationType application, JarFile module) {
        // construct the empty geronimo-application.xml
        GerApplicationType gerApplication = GerApplicationType.Factory.newInstance();
        EnvironmentType environmentType = gerApplication.addNewEnvironment();
        ArtifactType artifactType = environmentType.addNewConfigId();

        artifactType.setGroupId(Artifact.DEFAULT_GROUP_ID);

        // set the configId
        String id = application.getId();
        if (id == null) {
            File fileName = new File(module.getName());
            id = fileName.getName();
            if (id.endsWith(".ear")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }

        artifactType.setArtifactId(id);
        artifactType.setVersion("" + System.currentTimeMillis());
        artifactType.setType("car");
        return gerApplication;
    }

    public Artifact getConfigurationID(Object plan, JarFile module, ModuleIDBuilder idBuilder) throws IOException, DeploymentException {
        ApplicationInfo applicationInfo = (ApplicationInfo) plan;
        Artifact test = applicationInfo.getEnvironment().getConfigId();
        if(!test.isResolved()) {
            throw new IllegalStateException("Module ID should be fully resolved by now (not "+test+")");
        }
        return test;
    }

    public DeploymentContext buildConfiguration(boolean inPlaceDeployment, Artifact configId, Object plan, JarFile earFile, Collection configurationStores, ArtifactResolver artifactResolver, ConfigurationStore targetConfigurationStore) throws IOException, DeploymentException {
        assert plan != null;
        ApplicationInfo applicationInfo = (ApplicationInfo) plan;

        EARContext earContext = null;
        ConfigurationModuleType applicationType = applicationInfo.getType();
        applicationInfo.getEnvironment().setConfigId(configId);
        File configurationDir;
        try {
            configurationDir = targetConfigurationStore.createNewConfigurationDir(configId);
        } catch (ConfigurationAlreadyExistsException e) {
            throw new DeploymentException(e);
        }

        ConfigurationManager configurationManager = this.configurationManager;
        if (configurationManager == null) {
            configurationManager = new SimpleConfigurationManager(configurationStores, artifactResolver, repositories);
        }
        try {
            // Create the output ear context
            earContext = new EARContext(configurationDir,
                    inPlaceDeployment ? DeploymentUtil.toFile(earFile) : null,
                    applicationInfo.getEnvironment(),
                    applicationType,
                    naming,
                    configurationManager,
                    repositories,
                    serverName,
                    applicationInfo.getBaseName(),
                    transactionContextManagerObjectName,
                    connectionTrackerObjectName,
                    transactionalTimerObjectName,
                    nonTransactionalTimerObjectName,
                    corbaGBeanObjectName,
                    new RefContext(getEjbReferenceBuilder(), getResourceReferenceBuilder(), getServiceReferenceBuilder()));

            // Copy over all files that are _NOT_ modules
            Set moduleLocations = applicationInfo.getModuleLocations();
            if (ConfigurationModuleType.EAR == applicationType && earFile != null) {
                for (Enumeration e = earFile.entries(); e.hasMoreElements();) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    String entryName = entry.getName();
                    boolean addEntry = true;
                    for (Iterator iter = moduleLocations.iterator(); iter.hasNext();) {
                        String location = (String) iter.next();
                        if (entryName.startsWith(location)) {
                            addEntry = false;
                            break;
                        }
                    }
                    if (addEntry) {
                        earContext.addFile(URI.create(entry.getName()), earFile, entry);
                    }
                }
            }

            GerApplicationType geronimoApplication = (GerApplicationType) applicationInfo.getVendorDD();

            // each module installs it's files into the output context.. this is different for each module type
            LinkedHashSet modules = applicationInfo.getModules();
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                getBuilder(module).installModule(earFile, earContext, module, configurationStores, targetConfigurationStore, repositories);
            }

            earContext.flush();

            // give each module a chance to populate the earContext now that a classloader is available
            ClassLoader cl = earContext.getClassLoader();
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                getBuilder(module).initContext(earContext, module, cl);
            }

            // add gbeans declared in the geronimo-application.xml
            if (geronimoApplication != null) {
                GbeanType[] gbeans = geronimoApplication.getGbeanArray();
                ServiceConfigBuilder.addGBeans(gbeans, cl, earContext.getModuleName(), earContext);
            }

            // Create the J2EEApplication managed object
            if (ConfigurationModuleType.EAR == applicationType) {
                GBeanData gbeanData = new GBeanData(earContext.getModuleName(), J2EEApplicationImpl.GBEAN_INFO);
                try {
                    String originalSpecDD = applicationInfo.getOriginalSpecDD();
                    if (originalSpecDD == null) {
                        originalSpecDD = "Synthetic EAR";
                    }
                    gbeanData.setAttribute("deploymentDescriptor", originalSpecDD);
                } catch (Exception e) {
                    throw new DeploymentException("Error initializing J2EEApplication managed object");
                }
                gbeanData.setReferencePatterns("Server", new ReferencePatterns(new AbstractNameQuery(J2EEServer.class.getName())));

                Map thisApp = Collections.singletonMap(NameFactory.J2EE_APPLICATION, earContext.getModuleName().getNameProperty(NameFactory.J2EE_NAME));
                LinkedHashSet resourcePatterns = new LinkedHashSet();
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JAVA_MAIL_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JCA_CONNECTION_FACTORY), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JDBC_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JDBC_DRIVER), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JMS_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JNDI_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.JTA_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.RMI_IIOP_RESOURCE), J2EEResource.class.getName()));
                resourcePatterns.add(new AbstractNameQuery(null, filter(thisApp, NameFactory.J2EE_TYPE, NameFactory.URL_RESOURCE), J2EEResource.class.getName()));
                gbeanData.setReferencePatterns("Resources", resourcePatterns);

                gbeanData.setReferencePatterns("AppClientModules", new ReferencePatterns(new AbstractNameQuery(null, thisApp, org.apache.geronimo.management.AppClientModule.class.getName())));
                gbeanData.setReferencePatterns("EJBModules", new ReferencePatterns(new AbstractNameQuery(null, thisApp, org.apache.geronimo.management.EJBModule.class.getName())));
                gbeanData.setReferencePatterns("ResourceAdapterModules", new ReferencePatterns(new AbstractNameQuery(null, thisApp, org.apache.geronimo.management.geronimo.ResourceAdapterModule.class.getName())));
                gbeanData.setReferencePatterns("WebModules", new ReferencePatterns(new AbstractNameQuery(null, thisApp, org.apache.geronimo.management.geronimo.WebModule.class.getName())));
                earContext.addGBean(gbeanData);
            }

            //look for application plan security config
            if (geronimoApplication != null && geronimoApplication.isSetSecurity()) {
                SecurityConfiguration securityConfiguration = SecurityBuilder.buildSecurityConfiguration(geronimoApplication.getSecurity(), cl);
                earContext.setSecurityConfiguration(securityConfiguration);
            }

            //add the JACC gbean if there is a principal-role mapping
            if (earContext.getSecurityConfiguration() != null) {
                GBeanData jaccBeanData = SecurityBuilder.configureApplicationPolicyManager(naming, earContext.getModuleName(), earContext.getContextIDToPermissionsMap(), earContext.getSecurityConfiguration());
                earContext.addGBean(jaccBeanData);
                earContext.setJaccManagerName(jaccBeanData.getAbstractName());
            }

            // each module can now add it's GBeans
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                getBuilder(module).addGBeans(earContext, module, cl, repositories);
            }

            // it's the caller's responsibility to close the context...
            return earContext;
        } catch (GBeanAlreadyExistsException e) {
            // todo delete owned configuraitons like appclients
            if (earContext != null) {
                earContext.close();
            }
            DeploymentUtil.recursiveDelete(configurationDir);
            throw new DeploymentException(e);
        } catch (IOException e) {
            if (earContext != null) {
                earContext.close();
            }
            DeploymentUtil.recursiveDelete(configurationDir);
            throw e;
        } catch (DeploymentException e) {
            if (earContext != null) {
                earContext.close();
            }
            DeploymentUtil.recursiveDelete(configurationDir);
            throw e;
        } catch(RuntimeException e) {
            if (earContext != null) {
                earContext.close();
            }
            DeploymentUtil.recursiveDelete(configurationDir);
            throw e;
        } catch(Error e) {
            if (earContext != null) {
                earContext.close();
            }
            DeploymentUtil.recursiveDelete(configurationDir);
            throw e;
        } finally {
            Set modules = applicationInfo.getModules();
            for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
                Module module = (Module) iterator.next();
                module.close();
            }
        }
    }

    private static Map filter(Map original, String key, String value) {
        LinkedHashMap filter = new LinkedHashMap(original);
        filter.put(key, value);
        return filter;
    }

    private void addModules(JarFile earFile, ApplicationType application, GerApplicationType gerApplication, Set moduleLocations, LinkedHashSet modules, Environment environment, AbstractName earName, ModuleIDBuilder idBuilder) throws DeploymentException {
        Map altVendorDDs = new HashMap();
        try {
            if (earFile != null) {
                ModuleType[] moduleTypes = application.getModuleArray();
                //paths is used to check that all modules in the geronimo plan are in the application.xml.
                Set paths = new HashSet();
                for (int i = 0; i < moduleTypes.length; i++) {
                    ModuleType type = moduleTypes[i];
                    if (type.isSetEjb()) {
                        paths.add(type.getEjb().getStringValue());
                    } else if (type.isSetWeb()) {
                        paths.add(type.getWeb().getWebUri().getStringValue());
                    } else if (type.isSetConnector()) {
                        paths.add(type.getConnector().getStringValue());
                    } else if (type.isSetJava()) {
                        paths.add(type.getJava().getStringValue());
                    }
                }

                // build map from module path to alt vendor dd
                GerModuleType gerModuleTypes[] = gerApplication.getModuleArray();
                for (int i = 0; i < gerModuleTypes.length; i++) {
                    GerModuleType gerModule = gerModuleTypes[i];
                    String path = null;
                    if (gerModule.isSetEjb()) {
                        path = gerModule.getEjb().getStringValue();
                    } else if (gerModule.isSetWeb()) {
                        path = gerModule.getWeb().getStringValue();
                    } else if (gerModule.isSetConnector()) {
                        path = gerModule.getConnector().getStringValue();
                    } else if (gerModule.isSetJava()) {
                        path = gerModule.getJava().getStringValue();
                    }
                    if (!paths.contains(path)) {
                        throw new DeploymentException("Geronimo deployment plan refers to module '" + path + "' but that was not defined in the META-INF/application.xml");
                    }

                    if (gerModule.isSetAltDd()) {
                        // the the url of the alt dd
                        try {
                            altVendorDDs.put(path, DeploymentUtil.toTempFile(earFile, gerModule.getAltDd().getStringValue()));
                        } catch (IOException e) {
                            throw new DeploymentException("Invalid alt vendor dd url: " + gerModule.getAltDd().getStringValue(), e);
                        }
                    } else {
                        //dd is included explicitly
                        XmlObject[] anys = gerModule.selectChildren(GerModuleType.type.qnameSetForWildcardElements());
                        if (anys.length != 1) {
                            throw new DeploymentException("Unexpected count of xs:any elements in embedded vendor plan " + anys.length + " qnameset: " + GerModuleType.type.qnameSetForWildcardElements());
                        }
                        altVendorDDs.put(path, anys[0]);
                    }
                }

                // get a set containing all of the files in the ear that are actually modules
                for (int i = 0; i < moduleTypes.length; i++) {
                    ModuleType moduleXml = moduleTypes[i];

                    String modulePath;
                    ModuleBuilder builder;

                    Object moduleContextInfo = null;
                    String moduleTypeName;
                    if (moduleXml.isSetEjb()) {
                        modulePath = moduleXml.getEjb().getStringValue();
                        builder = getEjbConfigBuilder();
                        if (builder == null) {
                            throw new DeploymentException("Cannot deploy ejb application; No ejb deployer defined: " + modulePath);
                        }
                        moduleTypeName = "an EJB";
                    } else if (moduleXml.isSetWeb()) {
                        modulePath = moduleXml.getWeb().getWebUri().getStringValue();
                        if (getWebConfigBuilder() == null) {
                            throw new DeploymentException("Cannot deploy web application; No war deployer defined: " + modulePath);
                        }
                        builder = getWebConfigBuilder();
                        moduleTypeName = "a war";
                        moduleContextInfo = moduleXml.getWeb().getContextRoot().getStringValue().trim();
                    } else if (moduleXml.isSetConnector()) {
                        modulePath = moduleXml.getConnector().getStringValue();
                        if (getConnectorConfigBuilder() == null) {
                            throw new DeploymentException("Cannot deploy resource adapter; No rar deployer defined: " + modulePath);
                        }
                        builder = getConnectorConfigBuilder();
                        moduleTypeName = "a connector";
                    } else if (moduleXml.isSetJava()) {
                        modulePath = moduleXml.getJava().getStringValue();
                        if (getAppClientConfigBuilder() == null) {
                            throw new DeploymentException("Cannot deploy app client; No app client deployer defined: " + modulePath);
                        }
                        builder = getAppClientConfigBuilder();
                        moduleTypeName = "an application client";
                    } else {
                        throw new DeploymentException("Could not find a module builder for module: " + moduleXml);
                    }

                    moduleLocations.add(modulePath);

                    URL altSpecDD = null;
                    if (moduleXml.isSetAltDd()) {
                        try {
                            altSpecDD = DeploymentUtil.createJarURL(earFile, moduleXml.getAltDd().getStringValue());
                        } catch (MalformedURLException e) {
                            throw new DeploymentException("Invalid alt sped dd url: " + moduleXml.getAltDd().getStringValue(), e);
                        }
                    }

                    NestedJarFile moduleFile;
                    try {
                        moduleFile = new NestedJarFile(earFile, modulePath);
                    } catch (IOException e) {
                        throw new DeploymentException("Invalid moduleFile: " + modulePath, e);
                    }

                    Module module = builder.createModule(altVendorDDs.get(modulePath),
                            moduleFile,
                            modulePath,
                            altSpecDD,
                            environment,
                            moduleContextInfo,
                            earName,
                            naming, idBuilder);

                    if (module == null) {
                        throw new DeploymentException("Module was not " + moduleTypeName + ": " + modulePath);
                    }

                    modules.add(module);
                }
            }

            //deploy the extension modules
            GerExtModuleType gerExtModuleTypes[] = gerApplication.getExtModuleArray();
            for (int i = 0; i < gerExtModuleTypes.length; i++) {
                GerExtModuleType gerExtModule = gerExtModuleTypes[i];
                String moduleName;
                ModuleBuilder builder;
                Object moduleContextInfo = null;
                String moduleTypeName;

                if (gerExtModule.isSetEjb()) {
                    moduleName = gerExtModule.getEjb().getStringValue();
                    builder = getEjbConfigBuilder();
                    if (builder == null) {
                        throw new DeploymentException("Cannot deploy ejb application; No ejb deployer defined: " + moduleName);
                    }
                    moduleTypeName = "an EJB";
                } else if (gerExtModule.isSetWeb()) {
                    moduleName = gerExtModule.getWeb().getStringValue();
                    if (getWebConfigBuilder() == null) {
                        throw new DeploymentException("Cannot deploy web application; No war deployer defined: " + moduleName);
                    }
                    builder = getWebConfigBuilder();
                    moduleTypeName = "a war";
                    //ext modules must use vendor plan to set context-root
//                    moduleContextInfo = gerExtModule.getWeb().getContextRoot().getStringValue().trim();
                } else if (gerExtModule.isSetConnector()) {
                    moduleName = gerExtModule.getConnector().getStringValue();
                    if (getConnectorConfigBuilder() == null) {
                        throw new DeploymentException("Cannot deploy resource adapter; No rar deployer defined: " + moduleName);
                    }
                    builder = getConnectorConfigBuilder();
                    moduleTypeName = "a connector";
                } else if (gerExtModule.isSetJava()) {
                    moduleName = gerExtModule.getJava().getStringValue();
                    if (getAppClientConfigBuilder() == null) {
                        throw new DeploymentException("Cannot deploy app client; No app client deployer defined: " + moduleName);
                    }
                    builder = getAppClientConfigBuilder();
                    moduleTypeName = "an application client";
                } else {
                    throw new DeploymentException("Could not find a module builder for module: " + gerExtModule);
                }
                //dd is included explicitly
                XmlObject[] anys = gerExtModule.selectChildren(GerExtModuleType.type.qnameSetForWildcardElements());
                if (anys.length != 1) {
                    throw new DeploymentException("Unexpected count of xs:any elements in embedded vendor plan " + anys.length + " qnameset: " + GerExtModuleType.type.qnameSetForWildcardElements());
                }
                Object vendorDD = anys[0];

                JarFile moduleFile;
                if (gerExtModule.isSetInternalPath()) {
                    String modulePath = gerExtModule.getInternalPath().trim();
                    moduleLocations.add(modulePath);
                    try {
                        moduleFile = new NestedJarFile(earFile, modulePath);
                    } catch (IOException e) {
                        throw new DeploymentException("Invalid moduleFile: " + modulePath, e);
                    }
                } else {
                    String path = gerExtModule.getExternalPath().trim();
                    Artifact artifact = Artifact.create(path);
                    File location = null;
                    for (Iterator iterator = repositories.iterator(); iterator.hasNext();) {
                        Repository repository = (Repository) iterator.next();
                        if (repository.contains(artifact)) {
                             location = repository.getLocation(artifact);
                            break;
                        }
                    }
                    if (location == null) {
                        throw new DeploymentException(moduleTypeName + " is missing in repositories: " + path);
                    }
                    try {
                        moduleFile = new JarFile(location);
                    } catch (IOException e) {
                        throw new DeploymentException("Could not access contents of " + moduleTypeName, e);
                    }

                }


                URL altSpecDD = null;
                //todo implement an alt-spec-dd element.
//                if (moduleXml.isSetAltDd()) {
//                    try {
//                        altSpecDD = DeploymentUtil.createJarURL(earFile, moduleXml.getAltDd().getStringValue());
//                    } catch (MalformedURLException e) {
//                        throw new DeploymentException("Invalid alt spec dd url: " + moduleXml.getAltDd().getStringValue(), e);
//                    }
//                }


                Module module = builder.createModule(vendorDD,
                        moduleFile,
                        moduleName,
                        altSpecDD,
                        environment,
                        moduleContextInfo,
                        earName,
                        naming, idBuilder);

                if (module == null) {
                    throw new DeploymentException("Module was not " + moduleTypeName + ": " + moduleName);
                }

                modules.add(module);
            }


        } finally {
            // delete all the temp files created for alt vendor dds
            for (Iterator iterator = altVendorDDs.values().iterator(); iterator.hasNext();) {
                Object altVendorDD = iterator.next();
                if (altVendorDD instanceof File) {
                    ((File) altVendorDD).delete();
                }
            }
        }
    }

    private ModuleBuilder getBuilder(Module module) throws DeploymentException {
        if (module instanceof EJBModule) {
            if (getEjbConfigBuilder() == null) {
                throw new DeploymentException("Cannot deploy ejb application; No ejb deployer defined: " + module.getModuleURI());
            }
            return getEjbConfigBuilder();
        } else if (module instanceof WebModule) {
            if (getWebConfigBuilder() == null) {
                throw new DeploymentException("Cannot deploy web application; No war deployer defined: " + module.getModuleURI());
            }
            return getWebConfigBuilder();
        } else if (module instanceof ConnectorModule) {
            if (getConnectorConfigBuilder() == null) {
                throw new DeploymentException("Cannot deploy resource adapter; No rar deployer defined: " + module.getModuleURI());
            }
            return getConnectorConfigBuilder();
        } else if (module instanceof AppClientModule) {
            if (getAppClientConfigBuilder() == null) {
                throw new DeploymentException("Cannot deploy app client; No app client deployer defined: " + module.getModuleURI());
            }
            return getAppClientConfigBuilder();
        }
        throw new IllegalArgumentException("Unknown module type: " + module.getClass().getName());
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(EARConfigBuilder.class, NameFactory.CONFIG_BUILDER);
        infoFactory.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoFactory.addAttribute("transactionContextManagerAbstractName", AbstractNameQuery.class, true);
        infoFactory.addAttribute("connectionTrackerAbstractName", AbstractNameQuery.class, true);
        infoFactory.addAttribute("transactionalTimerAbstractName", AbstractNameQuery.class, true);
        infoFactory.addAttribute("nonTransactionalTimerAbstractName", AbstractNameQuery.class, true);
        infoFactory.addAttribute("corbaGBeanAbstractName", AbstractNameQuery.class, true);
        infoFactory.addAttribute("serverName", AbstractNameQuery.class, true);

        infoFactory.addReference("Repositories", Repository.class, "Repository");
        infoFactory.addReference("EJBConfigBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoFactory.addReference("EJBReferenceBuilder", EJBReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoFactory.addReference("WebConfigBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoFactory.addReference("ConnectorConfigBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoFactory.addReference("ResourceReferenceBuilder", ResourceReferenceBuilder.class, NameFactory.MODULE_BUILDER);
        infoFactory.addReference("AppClientConfigBuilder", ModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoFactory.addReference("ServiceReferenceBuilder", ServiceReferenceBuilder.class, NameFactory.MODULE_BUILDER);

        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addInterface(ConfigurationBuilder.class);

        infoFactory.setConstructor(new String[]{
                "defaultEnvironment",
                "transactionContextManagerAbstractName",
                "connectionTrackerAbstractName",
                "transactionalTimerAbstractName",
                "nonTransactionalTimerAbstractName",
                "corbaGBeanAbstractName",
                "serverName",
                "Repositories",
                "EJBConfigBuilder",
                "EJBReferenceBuilder",
                "WebConfigBuilder",
                "ConnectorConfigBuilder",
                "ResourceReferenceBuilder",
                "AppClientConfigBuilder",
                "ServiceReferenceBuilder",
                "kernel"
        });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
