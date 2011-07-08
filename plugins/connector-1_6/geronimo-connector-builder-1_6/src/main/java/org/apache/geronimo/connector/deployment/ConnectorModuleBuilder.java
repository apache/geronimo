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
package org.apache.geronimo.connector.deployment;

import java.beans.Introspector;
import java.beans.PropertyEditor;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.resource.spi.Activation;
import javax.resource.spi.AdministeredObject;
import javax.resource.spi.ConnectionDefinitions;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionLog;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.connector.wrapper.ActivationSpecWrapperGBean;
import org.apache.geronimo.connector.wrapper.AdminObjectWrapperGBean;
import org.apache.geronimo.connector.wrapper.JCAResourceImplGBean;
import org.apache.geronimo.connector.wrapper.ResourceAdapterImplGBean;
import org.apache.geronimo.connector.wrapper.ResourceAdapterModuleImplGBean;
import org.apache.geronimo.connector.wrapper.ResourceAdapterWrapperGBean;
import org.apache.geronimo.connector.wrapper.outbound.GenericConnectionManagerGBean;
import org.apache.geronimo.connector.wrapper.outbound.JCAConnectionFactoryImpl;
import org.apache.geronimo.connector.wrapper.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.connector.wrapper.outbound.ManagedConnectionFactoryWrapperGBean;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.MultiGBeanInfoFactory;
import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.deployment.ActivationSpecInfoLocator;
import org.apache.geronimo.j2ee.deployment.ConnectorModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.apache.geronimo.management.JCAConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.connector.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.connector.GerAdminobjectType;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.connector.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.connector.GerConnectorDocument;
import org.apache.geronimo.xbeans.connector.GerConnectorType;
import org.apache.geronimo.xbeans.connector.GerPartitionedpoolType;
import org.apache.geronimo.xbeans.connector.GerResourceadapterInstanceType;
import org.apache.geronimo.xbeans.connector.GerResourceadapterType;
import org.apache.geronimo.xbeans.connector.GerSinglepoolType;
import org.apache.openejb.jee.ActivationSpec;
import org.apache.openejb.jee.AdminObject;
import org.apache.openejb.jee.ConfigProperty;
import org.apache.openejb.jee.ConnectionDefinition;
import org.apache.openejb.jee.Connector;
import org.apache.openejb.jee.Connector10;
import org.apache.openejb.jee.InboundResourceadapter;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.MessageAdapter;
import org.apache.openejb.jee.MessageListener;
import org.apache.openejb.jee.OutboundResourceAdapter;
import org.apache.openejb.jee.TransactionSupportType;
import org.apache.xbean.finder.BundleAnnotationFinder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @version $Rev:385659 $ $Date$
 */

@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class ConnectorModuleBuilder implements ModuleBuilder, ActivationSpecInfoLocator, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ConnectorModuleBuilder.class);

    private static QName CONNECTOR_QNAME = GerConnectorDocument.type.getDocumentElementName();
    static final String GERCONNECTOR_NAMESPACE = CONNECTOR_QNAME.getNamespaceURI();
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    public static final String OSGI_JNDI_SERVICE_NAME = "osgi.jndi.service.name";

    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/connector", "http://geronimo.apache.org/xml/ns/j2ee/connector-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/connector-1.1", "http://geronimo.apache.org/xml/ns/j2ee/connector-1.2");
//        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/j2ee/connector-1.2", "http://geronimo.apache.org/xml/ns/j2ee/connector-1.3");
    }

    private static final Map<String, Class> TYPE_LOOKUP = new HashMap<String, Class>();

    static {
        TYPE_LOOKUP.put("byte", Byte.class);
        TYPE_LOOKUP.put(Byte.class.getName(), Byte.class);
        TYPE_LOOKUP.put("int", Integer.class);
        TYPE_LOOKUP.put(Integer.class.getName(), Integer.class);
        TYPE_LOOKUP.put("short", Short.class);
        TYPE_LOOKUP.put(Short.class.getName(), Short.class);
        TYPE_LOOKUP.put("long", Long.class);
        TYPE_LOOKUP.put(Long.class.getName(), Long.class);
        TYPE_LOOKUP.put("float", Float.class);
        TYPE_LOOKUP.put(Float.class.getName(), Float.class);
        TYPE_LOOKUP.put("double", Double.class);
        TYPE_LOOKUP.put(Double.class.getName(), Double.class);
        TYPE_LOOKUP.put("boolean", Boolean.class);
        TYPE_LOOKUP.put(Boolean.class.getName(), Boolean.class);
        TYPE_LOOKUP.put("char", Character.class);
        TYPE_LOOKUP.put(Character.class.getName(), Character.class);
        TYPE_LOOKUP.put(String.class.getName(), String.class);
    }

    private final int defaultMaxSize;
    private final int defaultMinSize;
    private final int defaultBlockingTimeoutMilliseconds;
    private final int defaultIdleTimeoutMinutes;
    private final boolean defaultXATransactionCaching;
    private final boolean defaultXAThreadCaching;
    private final Environment defaultEnvironment;
    private final NamespaceDrivenBuilderCollection serviceBuilders;
    private final String defaultWorkManagerName;

    private final PackageAdmin packageAdmin;
    private final Collection<ModuleBuilderExtension> moduleBuilderExtensions;

    public ConnectorModuleBuilder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
                                  @ParamAttribute(name = "defaultMaxSize") int defaultMaxSize,
                                  @ParamAttribute(name = "defaultMinSize") int defaultMinSize,
                                  @ParamAttribute(name = "defaultBlockingTimeoutMilliseconds") int defaultBlockingTimeoutMilliseconds,
                                  @ParamAttribute(name = "defaultIdleTimeoutMinutes") int defaultIdleTimeoutMinutes,
                                  @ParamAttribute(name = "defaultXATransactionCaching") boolean defaultXATransactionCaching,
                                  @ParamAttribute(name = "defaultXAThreadCaching") boolean defaultXAThreadCaching,
                                  @ParamAttribute(name = "defaultWorkManagerName") String defaultWorkManagerName,
                                  @ParamReference(name = "ServiceBuilders", namingType = NameFactory.MODULE_BUILDER) Collection<NamespaceDrivenBuilder> serviceBuilders,
                                  @ParamReference(name = "ModuleBuilderExtensions", namingType = NameFactory.MODULE_BUILDER) Collection<ModuleBuilderExtension> moduleBuilderExtensions,
                                  @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) {
        this.defaultEnvironment = defaultEnvironment;

        this.defaultMaxSize = defaultMaxSize;
        this.defaultMinSize = defaultMinSize;
        this.defaultBlockingTimeoutMilliseconds = defaultBlockingTimeoutMilliseconds;
        this.defaultIdleTimeoutMinutes = defaultIdleTimeoutMinutes;
        this.defaultXATransactionCaching = defaultXATransactionCaching;
        this.defaultXAThreadCaching = defaultXAThreadCaching;
        this.defaultWorkManagerName = defaultWorkManagerName;
        this.moduleBuilderExtensions = moduleBuilderExtensions == null ? new ArrayList<ModuleBuilderExtension>() : moduleBuilderExtensions;
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
        ServiceReference sr = bundleContext.getServiceReference(PackageAdmin.class.getName());
        packageAdmin = (PackageAdmin) bundleContext.getService(sr);
    }   
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

    @Override
    public Module createModule(Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return null;
    }

    @Override
    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "rar", null, null, null, naming, idBuilder);
    }
    
    @Override
    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object o1, Module module, Naming naming, ModuleIDBuilder moduleIDBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, module, naming, moduleIDBuilder);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, Module parentModule, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null : "moduleFile is null";
        assert targetPath != null : "targetPath is null";
        assert !targetPath.endsWith("/") : "targetPath must not end with a '/'";

        String specDD = null;
        Connector connector = null;
        try {
            if (specDDUrl == null) {
                specDDUrl = JarUtils.createJarURL(moduleFile, "META-INF/ra.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = JarUtils.readAll(specDDUrl);
        } catch (Exception e) {
            if (!moduleFile.getName().endsWith(".rar")) {
                //no ra.xml, not a .rar file, not for us.
                return null;
            }
        }
        //we found ra.xml, if it won't parse it's an error.
        // parse it
        if (specDD != null) {
            try {
                InputStream in = specDDUrl.openStream();
                try {
                    connector = (Connector) JaxbJavaee.unmarshalJavaee(Connector.class, in);
                } catch (JAXBException e) {
                    in.close();
                    in = specDDUrl.openStream();
                    Connector10 connector10 = (Connector10) JaxbJavaee.unmarshalJavaee(Connector10.class, in);
                    connector = Connector.newConnector(connector10);
                } finally {
                    in.close();
                }
            } catch (SAXException e) {
                throw new DeploymentException("Cannot parse the ra.xml file: " + specDDUrl.toExternalForm(), e);
            } catch (JAXBException e) {
                throw new DeploymentException("Cannot unmarshall the ra.xml file: " + specDDUrl.toExternalForm(), e);
            } catch (Exception e) {
                throw new DeploymentException("Encountered unknown error parsing the ra.xml file: " + specDDUrl.toExternalForm(), e);
            }
        }
        GerConnectorType gerConnector = null;
        try {
            // load the geronimo connector plan from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    gerConnector = (GerConnectorType) SchemaConversionUtils.getNestedObjectAsType((XmlObject) plan,
                            CONNECTOR_QNAME,
                            GerConnectorType.type);
                } else {
                    GerConnectorDocument gerConnectorDoc;
                    ArrayList errors = new ArrayList();
                    if (plan != null) {
                        gerConnectorDoc = GerConnectorDocument.Factory.parse((File) plan, XmlBeansUtil.createXmlOptions(errors));
                    } else {
                        URL path = JarUtils.createJarURL(moduleFile, "META-INF/geronimo-ra.xml");
                        gerConnectorDoc = GerConnectorDocument.Factory.parse(path, XmlBeansUtil.createXmlOptions(errors));
                    }
                    if (errors.size() > 0) {
                        throw new DeploymentException("Could not parse connector doc: " + errors);
                    }
                    if (gerConnectorDoc != null) {
                        gerConnector = gerConnectorDoc.getConnector();
                    }
                }
            } catch (IOException e) {
                //do nothing
            }

            // if we got one extract the validate it otherwise create a default one
            if (gerConnector == null) {
                throw new DeploymentException("A connector module must be deployed using a Geronimo deployment plan" +
                        " (either META-INF/geronimo-ra.xml in the RAR file or a standalone deployment plan passed to the deployer).");
            }
            ConnectorPlanRectifier.rectifyPlan(gerConnector);
            XmlCursor cursor = gerConnector.newCursor();
            try {
                SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            } finally {
                cursor.dispose();
            }

            XmlBeansUtil.validateDD(gerConnector);
        } catch (XmlException e) {
            throw new DeploymentException("Could not parse module descriptor", e);
        }

        EnvironmentType environmentType = gerConnector.getEnvironment();
        Environment environment = EnvironmentBuilder.buildEnvironment(environmentType, defaultEnvironment);
        if (earEnvironment != null) {
            EnvironmentBuilder.mergeEnvironments(earEnvironment, environment);
            environment = earEnvironment;
            if (!environment.getConfigId().isResolved()) {
                throw new IllegalStateException("Connector module ID should be fully resolved (not " + environment.getConfigId() + ")");
            }
        } else {
            idBuilder.resolve(environment, new File(moduleFile.getName()).getName(), "car");
        }

        AbstractName moduleName;
        if (parentModule == null) {
            AbstractName earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.RESOURCE_ADAPTER_MODULE);
        } else {
            moduleName = naming.createChildName(parentModule.getModuleName(), targetPath, NameFactory.RESOURCE_ADAPTER_MODULE);
        }

        boolean standAlone = earEnvironment == null;

        String name;
        if (connector != null && connector.getModuleName() != null) {
            name = connector.getModuleName();
        } else if (standAlone) {
            name = FileUtils.removeExtension(new File(moduleFile.getName()).getName(), ".rar");
        } else {
            name = FileUtils.removeExtension(targetPath, ".rar");
        }
        
        Module module = new ConnectorModule<Connector, XmlObject>(standAlone, moduleName, name, environment, moduleFile, targetPath, connector, gerConnector, specDD, parentModule == null? null: parentModule.getJndiContext(), parentModule);
        
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.createModule(module, plan, moduleFile, targetPath, specDDUrl, environment, null, moduleName, naming, idBuilder);
        }
        return module; 
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
        module.setEarContext(earContext);
        try {
            JarFile moduleFile = module.getModuleFile();

            // add the manifest classpath entries declared in the connector to the class loader
            // we have to explicitly add these since we are unpacking the connector module
            // and the url class loader will not pick up a manifiest from an unpacked dir
            // N.B. If we ever introduce a separate configuration/module for a rar inside an ear
            // this will need to be modified to use "../" instead of module.getTargetPath().
            // See AbstractWebModuleBuilder.
            earContext.addManifestClassPath(moduleFile, URI.create(module.getTargetPath()), module.getClassPath());

            boolean looseClasses = false;
            Enumeration entries = moduleFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                URI target = module.resolve(entry.getName());
                if (entry.getName().endsWith(".jar")) {
                    earContext.addInclude(target, moduleFile, entry);
                } else {
                    earContext.addFile(target, moduleFile, entry);
                    if (entry.getName().endsWith(".class")) {
                        looseClasses = true;
                    }
                }
            }
            if (looseClasses) {
                earContext.addToClassPath(module.resolve(".").getPath());
            }

        } catch (IOException e) {
            throw new DeploymentException("Problem deploying connector", e);
        }
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.installModule(earFile, earContext, module, configurationStores, targetConfigurationStore, repository);
        }
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        log.info("deploying bundle " + bundle + " at " + bundle.getLocation());
        ConnectorModule<Connector, XmlObject> resourceModule = (ConnectorModule<Connector, XmlObject>) module;
        
        // all connector objects wrappers get injected with the validator reference 
        AbstractName validatorName = module.getEarContext().getNaming().createChildName(module.getModuleName(), "ValidatorFactory", NameFactory.VALIDATOR_FACTORY);

        BundleAnnotationFinder classFinder;
        try {
            classFinder = new BundleAnnotationFinder(packageAdmin, bundle);
        } catch (Exception e) {
            throw new DeploymentException("could not create class finder for rar bundle " + bundle, e);
        }

        Connector connector = resourceModule.getSpecDD();
        connector = mergeMetadata(bundle, classFinder, connector);

        addExportPackages(connector, module.getEnvironment(), bundle);

        /*
        The chain of idiotic jsr-77 meaningless objects is:
        ResourceAdapterModule (1)  >
        ResourceAdapter (n, but there can only be 1 resource adapter in a rar, so we use 1) >
        JCAResource (1) >
        JCAConnectionFactory (n) >
        JCAManagedConnectionFactory (1)
        We also include:
        JCAResourceAdapter (n)  (from JCAResource) (actual instance of ResourceAdapter)
        TODO include admin objects (n) from JCAResource presumably
        */
        AbstractName resourceAdapterModuleName = resourceModule.getModuleName();

        AbstractName resourceAdapterjsr77Name = earContext.getNaming().createChildName(resourceAdapterModuleName, module.getName(), NameFactory.RESOURCE_ADAPTER);
        AbstractName jcaResourcejsr77Name = earContext.getNaming().createChildName(resourceAdapterjsr77Name, module.getName(), NameFactory.JCA_RESOURCE);

        //set up the metadata for the ResourceAdapterModule
        GBeanData resourceAdapterModuleData = new GBeanData(resourceAdapterModuleName, ResourceAdapterModuleImplGBean.GBEAN_INFO);
        // initalize the GBean
        if (earContext.getServerName() != null) {
            //app clients don't have a Server gbean
            resourceAdapterModuleData.setReferencePattern(NameFactory.J2EE_SERVER, earContext.getServerName());
            //app clients don't have an application name either
            if (!earContext.getModuleName().equals(resourceAdapterModuleName)) {
                resourceAdapterModuleData.setReferencePattern(NameFactory.J2EE_APPLICATION, earContext.getModuleName());
            }
        }
        resourceAdapterModuleData.setReferencePattern("ResourceAdapter", resourceAdapterjsr77Name);

        resourceAdapterModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());
        resourceAdapterModuleData.setAttribute("displayName", connector.getDisplayName());
        resourceAdapterModuleData.setAttribute("description", connector.getDescription());
        resourceAdapterModuleData.setAttribute("vendorName", connector.getVendorName());
        resourceAdapterModuleData.setAttribute("EISType", connector.getEisType());
        resourceAdapterModuleData.setAttribute("resourceAdapterVersion", connector.getResourceAdapterVersion());

        org.apache.openejb.jee.ResourceAdapter resourceAdapter = connector.getResourceAdapter();
        // Create the resource adapter gbean
        if (resourceAdapter.getResourceAdapterClass() != null) {
            GBeanInfoBuilder resourceAdapterInfoBuilder = new GBeanInfoBuilder(ResourceAdapterWrapperGBean.class, new MultiGBeanInfoFactory().getGBeanInfo(ResourceAdapterWrapperGBean.class));
            String resourceAdapterClassName = resourceAdapter.getResourceAdapterClass();
            GBeanData resourceAdapterGBeanData = setUpDynamicGBeanWithProperties(resourceAdapterClassName, resourceAdapterInfoBuilder, resourceAdapter.getConfigProperty(), bundle, Collections.<String>emptySet());

            resourceAdapterGBeanData.setAttribute("resourceAdapterClass", resourceAdapterClassName);

            // Add map from messageListenerInterface to activationSpec class
            Map<String, String> messageListenerToActivationSpecMap = new TreeMap<String, String>();
            if (resourceAdapter.getInboundResourceAdapter() != null && resourceAdapter.getInboundResourceAdapter().getMessageAdapter() != null) {
                for (MessageListener messageListener : resourceAdapter.getInboundResourceAdapter().getMessageAdapter().getMessageListener()) {
                    String messageListenerInterface = messageListener.getMessageListenerType();
                    ActivationSpec activationSpec = messageListener.getActivationSpec();
                    String activationSpecClassName = activationSpec.getActivationSpecClass();
                    messageListenerToActivationSpecMap.put(messageListenerInterface, activationSpecClassName);
                }
                resourceAdapterGBeanData.setAttribute("messageListenerToActivationSpecMap", messageListenerToActivationSpecMap);
                resourceAdapterGBeanData.setReferencePattern("XATerminator", earContext.getTransactionManagerName());
                resourceAdapterGBeanData.setReferencePattern("TransactionManager", earContext.getTransactionManagerName());
                resourceAdapterGBeanData.setReferencePattern("TransactionSynchronizationRegistry", earContext.getTransactionManagerName());
                
                resourceAdapterGBeanData.setReferencePattern("ValidatorFactory", validatorName);
                //This was previously in a separate if block, whether or not resourceAdapterClass was set.  I don't think this makes sense
                Map activationSpecInfoMap = getActivationSpecInfoMap(validatorName, resourceAdapter.getInboundResourceAdapter().getMessageAdapter().getMessageListener(), bundle);
                resourceAdapterModuleData.setAttribute("activationSpecInfoMap", activationSpecInfoMap);
            }

            resourceAdapterModuleData.setAttribute("resourceAdapterGBeanData", resourceAdapterGBeanData);
        }

        Map adminObjectInfoMap = getAdminObjectInfoMap(validatorName, resourceAdapter.getAdminObject(), bundle);
        resourceAdapterModuleData.setAttribute("adminObjectInfoMap", adminObjectInfoMap);
        if (resourceAdapter.getOutboundResourceAdapter() != null) {
            Map managedConnectionFactoryInfoMap = getManagedConnectionFactoryInfoMap(validatorName, resourceAdapter.getOutboundResourceAdapter().getConnectionDefinition(), bundle);
            resourceAdapterModuleData.setAttribute("managedConnectionFactoryInfoMap", managedConnectionFactoryInfoMap);
        }

        try {
            earContext.addGBean(resourceAdapterModuleData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add resource adapter module gbean to context", e);
        }

        //construct the bogus resource adapter and jca resource placeholders
        GBeanData resourceAdapterData = new GBeanData(resourceAdapterjsr77Name, ResourceAdapterImplGBean.GBEAN_INFO);
        resourceAdapterData.setReferencePattern("JCAResource", jcaResourcejsr77Name);
        try {
            earContext.addGBean(resourceAdapterData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add resource adapter gbean to context", e);
        }

        GBeanData jcaResourceData = new GBeanData(jcaResourcejsr77Name, JCAResourceImplGBean.GBEAN_INFO);
        Map<String, String> thisModule = new LinkedHashMap<String, String>(2);
        thisModule.put(NameFactory.J2EE_APPLICATION, resourceAdapterModuleName.getNameProperty(NameFactory.J2EE_APPLICATION));
        thisModule.put(NameFactory.RESOURCE_ADAPTER_MODULE, resourceAdapterModuleName.getNameProperty(NameFactory.J2EE_NAME));
        jcaResourceData.setReferencePattern("ConnectionFactories", new AbstractNameQuery(resourceAdapterModuleName.getArtifact(), thisModule, JCAConnectionFactory.class.getName()));
        jcaResourceData.setReferencePattern("ResourceAdapters", new AbstractNameQuery(resourceAdapterModuleName.getArtifact(), thisModule, JCAResourceAdapter.class.getName()));
        jcaResourceData.setReferencePattern("AdminObjects", new AbstractNameQuery(resourceAdapterModuleName.getArtifact(), thisModule, JCAAdminObject.class.getName()));

        try {
            earContext.addGBean(jcaResourceData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add jca resource gbean to context", e);
        }

        GerConnectorType geronimoConnector = (GerConnectorType) module.getVendorDD();

        serviceBuilders.build(geronimoConnector, earContext, earContext);

        addConnectorGBeans(earContext, jcaResourcejsr77Name, resourceAdapterModuleData, connector, geronimoConnector, bundle);
        
        // also give the extensions a crack at this 
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.initContext(earContext, module, bundle);
        }

    }

    private Connector mergeMetadata(Bundle bundle, BundleAnnotationFinder classFinder, Connector connector) throws DeploymentException {
        Class<? extends ResourceAdapter> raClass = null;
        if (connector == null) {
            List<Class<?>> resourceAdapterClasses = classFinder.findAnnotatedClasses(javax.resource.spi.Connector.class);
            if (resourceAdapterClasses.size() != 1) {
                throw new DeploymentException("Not exactly one resource adapter: " + resourceAdapterClasses);
            }
            raClass = resourceAdapterClasses.get(0).asSubclass(ResourceAdapter.class);
            connector = new Connector();
//          connector.setDescriptions(ra.description());
            connector.setMetadataComplete(false);
            connector.setVersion("1.6");
            org.apache.openejb.jee.ResourceAdapter resourceAdapter = new org.apache.openejb.jee.ResourceAdapter();
            connector.setResourceAdapter(resourceAdapter);
            resourceAdapter.setResourceAdapterClass(raClass.getName());
        } else {
            String raClassName = connector.getResourceAdapter().getResourceAdapterClass();
            if (raClassName != null) {
                try {
                    raClass = bundle.loadClass(raClassName).asSubclass(ResourceAdapter.class);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Can not load resource adapter class: " + raClassName, e);
                }
            }
        }
        if (connector.isMetadataComplete() != null && connector.isMetadataComplete()) {
            log.info("Connector is metadata complete");
        } else {
            org.apache.openejb.jee.ResourceAdapter resourceAdapter = connector.getResourceAdapter();
            log.info("Reading connector annotations");
            if (raClass != null/*and not metadata complete */) {
                javax.resource.spi.Connector ra = raClass.getAnnotation(javax.resource.spi.Connector.class);
                if (ra != null) {

                    OutboundResourceAdapter outboundResourceAdapter = resourceAdapter.getOutboundResourceAdapter();
                    if (outboundResourceAdapter == null) {
                        outboundResourceAdapter = new OutboundResourceAdapter();
                        resourceAdapter.setOutboundResourceAdapter(outboundResourceAdapter);
                    }
                    if (outboundResourceAdapter.isReauthenticationSupport() == null) {
                        outboundResourceAdapter.setReauthenticationSupport(ra.reauthenticationSupport());
                    }
                    if (outboundResourceAdapter.getTransactionSupport() == null) {
                        outboundResourceAdapter.setTransactionSupport(TransactionSupportType.fromValue(ra.transactionSupport().toString()));
                    }
                    connector.getRequiredWorkContext().addAll(toString(ra.requiredWorkContexts()));
                    setConfigProperties(resourceAdapter.getConfigProperty(), raClass);
                }
            }

            //inbound
            log.info("connector of type: " + connector);
            InboundResourceadapter inboundResource = resourceAdapter.getInboundResourceAdapter();
            if (inboundResource == null) {
                inboundResource = new InboundResourceadapter();
                inboundResource.setMessageAdapter(new MessageAdapter());
            }
            MessageAdapter messageAdapter = inboundResource.getMessageAdapter();
            List<Class<?>> activationSpecClasses = classFinder.findAnnotatedClasses(Activation.class);

            for (Class<?> asClass : activationSpecClasses) {
                Activation activation = asClass.getAnnotation(Activation.class);
                for (Class messageListenerClass : activation.messageListeners()) {
                    ActivationSpec activationSpec = getActivationSpec(messageAdapter, messageListenerClass);

                    if (activationSpec.getActivationSpecClass() == null) {
                        activationSpec.setActivationSpecClass(asClass.getName());
                    }
                    if (asClass.getName().equals(activationSpec.getActivationSpecClass())) {
                        setConfigProperties(activationSpec.getConfigProperty(), asClass);
                    }
                    //TODO set required config properties from @NotNull annotations
                }
            }
            if (resourceAdapter.getInboundResourceAdapter() == null && inboundResource.getMessageAdapter().getMessageListener().size() > 0) {
                resourceAdapter.setInboundResourceAdapter(inboundResource);
            }

            //admin objects
            for (Class adminObjectClass : classFinder.findAnnotatedClasses(AdministeredObject.class)) {
                AdministeredObject administeredObject = (AdministeredObject) adminObjectClass.getAnnotation(AdministeredObject.class);
                Class[] interfaces = administeredObject.adminObjectInterfaces();
                if (interfaces == null || interfaces.length == 0) {
                    List<Class> allInterfaces = new ArrayList<Class>(Arrays.asList(adminObjectClass.getInterfaces()));
                    allInterfaces.remove(Serializable.class);
                    allInterfaces.remove(Externalizable.class);
                    //TODO check if specified in ra.xml
                    if (allInterfaces.size() != 1) {
                        throw new DeploymentException("Interface for admin object not specified properly: " + allInterfaces);
                    }
                    interfaces = allInterfaces.toArray(new Class[1]);
                }
                for (Class aoInterface : interfaces) {
                    AdminObject adminObject = getAdminObject(resourceAdapter, aoInterface);
                    if (adminObject.getAdminObjectClass() == null) {
                        adminObject.setAdminObjectClass(adminObjectClass.getName());
                    }
                    if (adminObjectClass.getName().equals(adminObject.getAdminObjectClass())) {
                        setConfigProperties(adminObject.getConfigProperty(), adminObjectClass);
                    }
                }
            }

            OutboundResourceAdapter outboundResourceAdapter = resourceAdapter.getOutboundResourceAdapter();
            if (outboundResourceAdapter == null) {
                outboundResourceAdapter = new OutboundResourceAdapter();
            }

            //outbound
            for (Class<?> mcfClass : classFinder.findAnnotatedClasses(javax.resource.spi.ConnectionDefinition.class)) {
                javax.resource.spi.ConnectionDefinition connectionDefinitionAnnotation = mcfClass.getAnnotation(javax.resource.spi.ConnectionDefinition.class);
                buildConnectionDefinition(mcfClass.asSubclass(ManagedConnectionFactory.class), connectionDefinitionAnnotation, outboundResourceAdapter);
            }
            for (Class<?> mcfClass : classFinder.findAnnotatedClasses(ConnectionDefinitions.class)) {
                ConnectionDefinitions connectionDefinitionAnnotations = mcfClass.getAnnotation(ConnectionDefinitions.class);
                for (javax.resource.spi.ConnectionDefinition connectionDefinitionAnnotation : connectionDefinitionAnnotations.value()) {
                    buildConnectionDefinition(mcfClass.asSubclass(ManagedConnectionFactory.class), connectionDefinitionAnnotation, outboundResourceAdapter);
                }
            }
            if (outboundResourceAdapter.getConnectionDefinition().size() > 0) {
                resourceAdapter.setOutboundResourceAdapter(outboundResourceAdapter);
            }
        }
        return connector;
    }

    /**
     * Find or create an ActivationSpec object for the supplied messageListenerClass
     *
     * @param messageAdapter       MessageAdapter container object
     * @param messageListenerClass class for the activation spec
     * @return ActivationSpec data object
     */
    private ActivationSpec getActivationSpec(MessageAdapter messageAdapter, Class messageListenerClass) {
        for (MessageListener messageListener : messageAdapter.getMessageListener()) {
            if (messageListenerClass.getName().equals(messageListener.getMessageListenerType())) {
                return messageListener.getActivationSpec();
            }
        }
        MessageListener messageListener = new MessageListener();
        messageListener.setMessageListenerType(messageListenerClass.getName());
        ActivationSpec activationSpec = new ActivationSpec();
        messageListener.setActivationSpec(activationSpec);
        messageAdapter.getMessageListener().add(messageListener);
        return activationSpec;
    }

    /**
     * find or create an AdminObject for the supplied admin object interface
     *
     * @param resourceAdapter ResourceAdapter container object
     * @param aoInterface     admin object interface
     * @return AdminObject data object
     */
    private AdminObject getAdminObject(org.apache.openejb.jee.ResourceAdapter resourceAdapter, Class aoInterface) {
        for (AdminObject adminObject : resourceAdapter.getAdminObject()) {
            if (aoInterface.getName().equals(adminObject.getAdminObjectInterface())) {
                return adminObject;
            }
        }
        AdminObject adminObject = new AdminObject();
        adminObject.setAdminObjectInterface(aoInterface.getName());
        resourceAdapter.getAdminObject().add(adminObject);
        return adminObject;
    }

    private void setConfigProperties(List<ConfigProperty> configProperty, Class<?> aClass) throws DeploymentException {
        for (Method method : aClass.getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                javax.resource.spi.ConfigProperty cpa = method.getAnnotation(javax.resource.spi.ConfigProperty.class);
                if (cpa != null) {
                    setConfigProperty(configProperty, cpa, method.getName().substring(3), method.getParameterTypes()[0]);
                }
            }
        }
        do {
            for (Field field : aClass.getDeclaredFields()) {
                javax.resource.spi.ConfigProperty cpa = field.getAnnotation(javax.resource.spi.ConfigProperty.class);
                if (cpa != null) {
                    setConfigProperty(configProperty, cpa, field.getName(), field.getType());
                }
            }
            aClass = aClass.getSuperclass();
        } while (aClass != null);
    }

    private void setConfigProperty(List<ConfigProperty> configProperties, javax.resource.spi.ConfigProperty cpa, String name, Class<?> type) throws DeploymentException {
        name = Introspector.decapitalize(name);
        ConfigProperty target = null;
        for (ConfigProperty configProperty : configProperties) {
            if (name.equals(configProperty.getConfigPropertyName())) {
                target = configProperty;
                break;
            }
        }
        if (target == null) {
            target = new ConfigProperty();
            target.setConfigPropertyName(name);
            configProperties.add(target);
        }
        if (cpa.type() != Object.class && cpa.type() != type) {
            throw new DeploymentException("wrong type specified: " + cpa.type().getName() + " expecting " + type.getName());
        }
        if (target.getConfigPropertyType() == null) {
            target.setConfigPropertyType(type.getName());
        }
        if (target.getConfigPropertyValue() == null) {
            target.setConfigPropertyValue(cpa.defaultValue());
        }
        if (target.isConfigPropertyConfidential() == null) {
            target.setConfigPropertyConfidential(cpa.confidential());
        }
        if (target.isConfigPropertyIgnore() == null) {
            target.setConfigPropertyIgnore(cpa.ignore());
        }
        if (target.isConfigPropertySupportsDynamicUpdates() == null) {
            target.setConfigPropertySupportsDynamicUpdates(cpa.supportsDynamicUpdates());
        }
    }

    private List<String> toString(Class<?>[] classes) {
        List<String> list = new ArrayList<String>(classes.length);
        for (Class<?> clazz : classes) {
            list.add(clazz.getName());
        }
        return list;
    }

    private void buildConnectionDefinition(Class<? extends ManagedConnectionFactory> mcfClass, javax.resource.spi.ConnectionDefinition connectionDefinitionAnnotation, OutboundResourceAdapter outboundResourceAdapter) throws DeploymentException {
        ConnectionDefinition connectionDefinition = getConnectionDefinition(connectionDefinitionAnnotation, outboundResourceAdapter);
        if (connectionDefinition.getManagedConnectionFactoryClass() == null) {
            connectionDefinition.setManagedConnectionFactoryClass(mcfClass.getName());
        }
        if (mcfClass.getName().equals(connectionDefinition.getManagedConnectionFactoryClass())) {
            connectionDefinition.setConnectionFactoryImplClass(connectionDefinitionAnnotation.connectionFactoryImpl().getName());
            connectionDefinition.setConnectionInterface(connectionDefinitionAnnotation.connection().getName());
            connectionDefinition.setConnectionImplClass(connectionDefinitionAnnotation.connectionImpl().getName());
            setConfigProperties(connectionDefinition.getConfigProperty(), mcfClass);
        }
    }

    private ConnectionDefinition getConnectionDefinition(javax.resource.spi.ConnectionDefinition connectionDefinitionAnnotation, OutboundResourceAdapter outboundResourceAdapter) {
        for (ConnectionDefinition connectionDefinition : outboundResourceAdapter.getConnectionDefinition()) {
            if (connectionDefinitionAnnotation.connectionFactory().getName().equals(connectionDefinition.getConnectionFactoryInterface())) {
                return connectionDefinition;
            }
        }
        ConnectionDefinition connectionDefinition = new ConnectionDefinition();
        outboundResourceAdapter.getConnectionDefinition().add(connectionDefinition);
        connectionDefinition.setConnectionFactoryInterface(connectionDefinitionAnnotation.connectionFactory().getName());
        return connectionDefinition;
    }

    private void addExportPackages(Connector connector, Environment environment, Bundle bundle) throws DeploymentException {
        if (connector.getResourceAdapter().getOutboundResourceAdapter() != null) {
            for (ConnectionDefinition connectionDefinition : connector.getResourceAdapter().getOutboundResourceAdapter().getConnectionDefinition()) {
                addExportPackage(environment, connectionDefinition.getConnectionFactoryInterface(), bundle);
                addExportPackage(environment, connectionDefinition.getConnectionInterface(), bundle);
            }
        }
        if (connector.getResourceAdapter().getInboundResourceAdapter() != null) {
            for (MessageListener messageListener : connector.getResourceAdapter().getInboundResourceAdapter().getMessageAdapter().getMessageListener()) {
                addExportPackage(environment, messageListener.getMessageListenerType(), bundle);
                addExportPackage(environment, messageListener.getActivationSpec().getActivationSpecClass(), bundle);
            }
        }
        for (AdminObject adminObject : connector.getResourceAdapter().getAdminObject()) {
            addExportPackage(environment, adminObject.getAdminObjectInterface(), bundle);
        }
    }

    private void addExportPackage(Environment environment, String intf, Bundle bundle) throws DeploymentException {
        try {
            Class clazz = bundle.loadClass(intf);
            if (bundle == packageAdmin.getBundle(clazz)) {
                int pos = intf.lastIndexOf(".");
                String aPackage = intf.substring(0, pos);
                //            environment.addImportPackage(aPackage);
                environment.addExportPackage(aPackage);
            }
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load exported class: " + intf);
        }

    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        //all our gbeans are added in  the initContext step
        //in case we decide connectors should be separate bundles
        module.addAsChildConfiguration();
        for (ModuleBuilderExtension mbe : moduleBuilderExtensions) {
            mbe.addGBeans(earContext, module, bundle, repository);
        }
    }

    public String getSchemaNamespace() {
        return GERCONNECTOR_NAMESPACE;
    }

    private void addConnectorGBeans(EARContext earContext, AbstractName jcaResourceName, GBeanData resourceAdapterModuleData, Connector connector, GerConnectorType geronimoConnector, Bundle bundle) throws DeploymentException {
        List<GBeanData> raBeans = new ArrayList<GBeanData>();

        org.apache.openejb.jee.ResourceAdapter resourceAdapter = connector.getResourceAdapter();

        GerResourceadapterType[] geronimoResourceAdapters = geronimoConnector.getResourceadapterArray();
        for (GerResourceadapterType geronimoResourceAdapter : geronimoResourceAdapters) {
            // Resource Adapter
            AbstractName resourceAdapterAbstractName = null;
            if (resourceAdapter.getResourceAdapterClass() != null) {
                GBeanData resourceAdapterGBeanData = locateResourceAdapterGBeanData(resourceAdapterModuleData);
                GBeanData resourceAdapterInstanceGBeanData = new GBeanData(resourceAdapterGBeanData);

                String resourceAdapterName;
                AbstractNameQuery workManagerName;
                if (geronimoResourceAdapter.isSetResourceadapterInstance()) {
                    GerResourceadapterInstanceType resourceAdapterInstance = geronimoResourceAdapter.getResourceadapterInstance();
                    setDynamicGBeanDataAttributes(resourceAdapterInstanceGBeanData, resourceAdapterInstance.getConfigPropertySettingArray(), bundle);
                    workManagerName = ENCConfigBuilder.getGBeanQuery(NameFactory.JCA_WORK_MANAGER, resourceAdapterInstance.getWorkmanager());
                    resourceAdapterName = resourceAdapterInstance.getResourceadapterName();
                } else {
                    workManagerName = ENCConfigBuilder.buildAbstractNameQuery(null, null, defaultWorkManagerName, NameFactory.JCA_WORK_MANAGER, null);
                    resourceAdapterName = "ResourceAdapterInstance-" + System.currentTimeMillis();
                    log.warn("Resource adapter instance information was not specified in Geronimo plan. Using defaults.");
                }

                // set the work manager name
                resourceAdapterInstanceGBeanData.setReferencePattern("WorkManager", workManagerName);

                // set the xa terminator name which is the same as our transaction manager
                resourceAdapterInstanceGBeanData.setReferencePattern("XATerminator", earContext.getTransactionManagerName());

                resourceAdapterAbstractName = earContext.getNaming().createChildName(jcaResourceName, resourceAdapterName, NameFactory.JCA_RESOURCE_ADAPTER);
                resourceAdapterInstanceGBeanData.setAbstractName(resourceAdapterAbstractName);
                raBeans.add(resourceAdapterInstanceGBeanData);
            }

            // Outbound Managed Connection Factories (think JDBC data source or JMS connection factory)

            // ManagedConnectionFactory setup
            if (geronimoResourceAdapter.isSetOutboundResourceadapter()) {
                if (resourceAdapter.getOutboundResourceAdapter() == null) {
                    throw new DeploymentException("Geronimo plan configures an outbound resource adapter but ra.xml does not describe any");
                }
                String transactionSupport = resourceAdapter.getOutboundResourceAdapter().getTransactionSupport().value();
                for (GerConnectionDefinitionType geronimoConnectionDefinition : geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray()) {
                    assert geronimoConnectionDefinition != null : "Null GeronimoConnectionDefinition";

                    String connectionFactoryInterfaceName = geronimoConnectionDefinition.getConnectionfactoryInterface().trim();
                    GBeanData connectionFactoryGBeanData = locateConnectionFactoryInfo(resourceAdapterModuleData, connectionFactoryInterfaceName);

                    if (connectionFactoryGBeanData == null) {
                        throw new DeploymentException("No connection definition for ConnectionFactory class: " + connectionFactoryInterfaceName);
                    }

                    for (int j = 0; j < geronimoConnectionDefinition.getConnectiondefinitionInstanceArray().length; j++) {
                        GerConnectiondefinitionInstanceType connectionfactoryInstance = geronimoConnectionDefinition.getConnectiondefinitionInstanceArray()[j];

                        addOutboundGBeans(raBeans, earContext.getNaming(), jcaResourceName, resourceAdapterAbstractName, connectionFactoryGBeanData, connectionfactoryInstance, transactionSupport, bundle, earContext.getConnectionTrackerName(), earContext.getTransactionManagerName());
                    }
                }
            }
            addAdminObjectGBeans(raBeans, earContext.getNaming(), jcaResourceName, resourceAdapterModuleData, bundle, resourceAdapterAbstractName, geronimoResourceAdapter.getAdminobjectArray());
        }
        // admin objects (think message queues and topics)

        // add configured admin objects
        addAdminObjectGBeans(raBeans, earContext.getNaming(), jcaResourceName, resourceAdapterModuleData, bundle, null, geronimoConnector.getAdminobjectArray());
        List<Exception> problems = new ArrayList<Exception>();
        for (GBeanData data: raBeans) {
            try {
                earContext.addGBean(data);
            } catch (GBeanAlreadyExistsException e) {
                problems.add(e);
            }
        }
        if (!problems.isEmpty()) {
            throw new DeploymentException("Could not add gbeans to configuration: ", problems);
        }
    }

    private void addAdminObjectGBeans(List<GBeanData> raBeans, Naming naming, AbstractName jcaResourceName, GBeanData resourceAdapterModuleData, Bundle bundle, AbstractName resourceAdapterAbstractName, GerAdminobjectType[] adminObjects) throws DeploymentException {
        for (GerAdminobjectType gerAdminObject : adminObjects) {
            String adminObjectInterface = gerAdminObject.getAdminobjectInterface().trim();
            GBeanData adminObjectGBeanData = locateAdminObjectInfo(resourceAdapterModuleData, adminObjectInterface);

            if (adminObjectGBeanData == null) {
                throw new DeploymentException("No admin object declared for interface: " + adminObjectInterface);
            }

            for (GerAdminobjectInstanceType gerAdminObjectInstance : gerAdminObject.getAdminobjectInstanceArray()) {
                GBeanData adminObjectInstanceGBeanData = new GBeanData(adminObjectGBeanData);
                setDynamicGBeanDataAttributes(adminObjectInstanceGBeanData, gerAdminObjectInstance.getConfigPropertySettingArray(), bundle);
                // add it
                AbstractName adminObjectAbstractName = naming.createChildName(jcaResourceName, gerAdminObjectInstance.getMessageDestinationName().trim(), NameFactory.JCA_ADMIN_OBJECT);
                adminObjectInstanceGBeanData.setAbstractName(adminObjectAbstractName);
                if (resourceAdapterAbstractName != null) {
                    adminObjectInstanceGBeanData.setReferencePattern("ResourceAdapterWrapper", resourceAdapterAbstractName);
                }
                Set<String> implementedInterfaces = new HashSet<String>();
                implementedInterfaces.add(checkClass(bundle, (String) adminObjectInstanceGBeanData.getAttribute("adminObjectInterface")));
                implementedInterfaces.add(checkClass(bundle, (String) adminObjectInstanceGBeanData.getAttribute("adminObjectClass")));
                adminObjectInstanceGBeanData.setServiceInterfaces(implementedInterfaces.toArray(new String[implementedInterfaces.size()]));
                String jndiName = naming.toOsgiJndiName(adminObjectAbstractName);
                //TODO allow specifying osig jndi name directly, like for connection factories
                adminObjectInstanceGBeanData.getServiceProperties().put(OSGI_JNDI_SERVICE_NAME, jndiName);
                raBeans.add(adminObjectInstanceGBeanData);
            }
        }
    }

    private Map<String, GBeanData> getActivationSpecInfoMap(AbstractName validatorName, List<MessageListener> messageListeners, Bundle bundle) throws DeploymentException {
        Map<String, GBeanData> activationSpecInfos = new HashMap<String, GBeanData>();
        for (MessageListener messageListener : messageListeners) {
            String messageListenerInterface = messageListener.getMessageListenerType();
            ActivationSpec activationSpec = messageListener.getActivationSpec();
            String activationSpecClassName = activationSpec.getActivationSpecClass();
            GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ActivationSpecWrapperGBean.class, new AnnotationGBeanInfoBuilder(ActivationSpecWrapperGBean.class).buildGBeanInfo());
            Set<String> ignore = Collections.singleton("resourceAdapter");
            setUpDynamicGBean(activationSpecClassName, infoBuilder, ignore, bundle, true);


            GBeanInfo gbeanInfo = infoBuilder.getBeanInfo();

            GBeanData activationSpecInfo = new GBeanData(gbeanInfo);
            activationSpecInfo.setAttribute("activationSpecClass", activationSpecClassName);
            activationSpecInfo.setReferencePattern("ValidatorFactory", validatorName);
            activationSpecInfos.put(messageListenerInterface, activationSpecInfo);
        }
        return activationSpecInfos;
    }

    private void setUpDynamicGBean(String adapterClassName, GBeanInfoBuilder infoBuilder, Set<String> ignore, Bundle bundle, boolean decapitalize) throws DeploymentException {
        //add all javabean properties that have both getter and setter.  Ignore the "required" flag from the dd.
        Map<String, String> getters = new HashMap<String, String>();
        Set<String> setters = new HashSet<String>();
        Method[] methods;
        try {
            Class activationSpecClass = bundle.loadClass(adapterClassName);
            methods = activationSpecClass.getMethods();
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Can not load adapter class in classloader " + bundle, e);
        } catch (NoClassDefFoundError e) {
            throw new DeploymentException("Can not load adapter class in classloader " + bundle, e);
        }
        for (Method method : methods) {
            String methodName = method.getName();
            if ((methodName.startsWith("get") || methodName.startsWith("is")) && method.getParameterTypes().length == 0) {
                String attributeName = (methodName.startsWith("get")) ? methodName.substring(3) : methodName.substring(2);
                getters.put(setCase(attributeName, decapitalize), method.getReturnType().getName());
            } else if (methodName.startsWith("set") && method.getParameterTypes().length == 1) {
                setters.add(setCase(methodName.substring(3), decapitalize));
            }
        }
        getters.keySet().retainAll(setters);
        getters.keySet().removeAll(ignore);

        for (Map.Entry<String, String> entry : getters.entrySet()) {
            infoBuilder.addAttribute(new DynamicGAttributeInfo(entry.getKey(), entry.getValue(), true, true, true, true));
        }
    }

    private String setCase(String attributeName, boolean decapitalize) {
        if (decapitalize) {
            return Introspector.decapitalize(attributeName);
        } else {
            return attributeName;
        }
    }

    private static String switchCase(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (Character.isUpperCase(name.charAt(0))) {
            char chars[] = name.toCharArray();
            chars[0] = Character.toLowerCase(chars[0]);
            return new String(chars);
        } else if (Character.isLowerCase(name.charAt(0))) {
            char chars[] = name.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        } else {
            return name;
        }
    }

    private Map getManagedConnectionFactoryInfoMap(AbstractName validatorName, List<ConnectionDefinition> connectionDefinitions, Bundle bundle) throws DeploymentException {
        Map<String, GBeanData> managedConnectionFactoryInfos = new HashMap<String, GBeanData>();
        for (ConnectionDefinition connectionDefinition : connectionDefinitions) {
            GBeanInfoBuilder managedConnectionFactoryInfoBuilder = new GBeanInfoBuilder(ManagedConnectionFactoryWrapper.class, ManagedConnectionFactoryWrapperGBean.GBEAN_INFO);
            String managedConnectionfactoryClassName = connectionDefinition.getManagedConnectionFactoryClass();
            Set<String> ignore = new HashSet<String>();
            ignore.add("ResourceAdapter");
            ignore.add("LogWriter");
            GBeanData managedConnectionFactoryGBeanData = setUpDynamicGBeanWithProperties(managedConnectionfactoryClassName, managedConnectionFactoryInfoBuilder, connectionDefinition.getConfigProperty(), bundle, ignore);

            // set the standard properties
            String connectionfactoryInterface = connectionDefinition.getConnectionFactoryInterface();
            managedConnectionFactoryGBeanData.setAttribute("managedConnectionFactoryClass", managedConnectionfactoryClassName);
            managedConnectionFactoryGBeanData.setAttribute("connectionFactoryInterface", connectionfactoryInterface);
            managedConnectionFactoryGBeanData.setAttribute("connectionFactoryImplClass", connectionDefinition.getConnectionFactoryImplClass());
            managedConnectionFactoryGBeanData.setAttribute("connectionInterface", connectionDefinition.getConnectionInterface());
            managedConnectionFactoryGBeanData.setAttribute("connectionImplClass", connectionDefinition.getConnectionImplClass());
            managedConnectionFactoryGBeanData.setReferencePattern("ValidatorFactory", validatorName);
            managedConnectionFactoryInfos.put(connectionfactoryInterface, managedConnectionFactoryGBeanData);
        }
        return managedConnectionFactoryInfos;
    }

    private Map getAdminObjectInfoMap(AbstractName validatorName, List<AdminObject> adminObjects, Bundle bundle) throws DeploymentException {
        Map<String, GBeanData> adminObjectInfos = new HashMap<String, GBeanData>();
        for (AdminObject adminObject : adminObjects) {
            GBeanInfoBuilder adminObjectInfoBuilder = new GBeanInfoBuilder(AdminObjectWrapperGBean.class, new AnnotationGBeanInfoBuilder(AdminObjectWrapperGBean.class).buildGBeanInfo());
            String adminObjectClassName = adminObject.getAdminObjectClass();
            GBeanData adminObjectGBeanData = setUpDynamicGBeanWithProperties(adminObjectClassName, adminObjectInfoBuilder, adminObject.getConfigProperty(), bundle, Collections.<String>emptySet());

            // set the standard properties
            String adminObjectInterface = adminObject.getAdminObjectInterface();
            adminObjectGBeanData.setAttribute("adminObjectInterface", adminObjectInterface);
            adminObjectGBeanData.setAttribute("adminObjectClass", adminObjectClassName);
            adminObjectGBeanData.setReferencePattern("ValidatorFactory", validatorName);
            adminObjectInfos.put(adminObjectInterface, adminObjectGBeanData);
        }
        return adminObjectInfos;
    }


    private GBeanData setUpDynamicGBeanWithProperties(String className, GBeanInfoBuilder infoBuilder, List<ConfigProperty> configProperties, Bundle bundle, Set<String> ignore) throws DeploymentException {
        setUpDynamicGBean(className, infoBuilder, ignore, bundle, false);

        GBeanInfo gbeanInfo = infoBuilder.getBeanInfo();
        GBeanData gbeanData = new GBeanData(gbeanInfo);
        for (ConfigProperty configProperty : configProperties) {
            if (configProperty.getConfigPropertyValue() != null) {
                String name = configProperty.getConfigPropertyName();
                if (gbeanInfo.getAttribute(name) == null) {
                    String originalName = name;
                    name = switchCase(name);
                    if (gbeanInfo.getAttribute(name) == null) {
                        log.warn("Unsupported config-property: " + originalName);
                        continue;
                    }
                }
                String type = configProperty.getConfigPropertyType();
                String value = configProperty.getConfigPropertyValue();
                gbeanData.setAttribute(name, getValue(type, value, bundle));
            }
        }
        return gbeanData;
    }

    private void setDynamicGBeanDataAttributes(GBeanData gbeanData, GerConfigPropertySettingType[] configProperties, Bundle bundle) throws DeploymentException {
        List<String> unknownNames = new ArrayList<String>();
        for (GerConfigPropertySettingType configProperty : configProperties) {
            String name = configProperty.getName();
            GAttributeInfo attributeInfo = gbeanData.getGBeanInfo().getAttribute(name);
            if (attributeInfo == null) {
                String originalName = name;
                name = switchCase(name);
                attributeInfo = gbeanData.getGBeanInfo().getAttribute(name);
                if (attributeInfo == null) {
                    unknownNames.add(originalName);
                    continue;
                }
            }

            String type = attributeInfo.getType();
            gbeanData.setAttribute(name, getValue(type, configProperty.getStringValue().trim(), bundle));
        }
        if (unknownNames.size() > 0) {
            StringBuilder buf = new StringBuilder("The plan is trying to set attributes: ").append(unknownNames).append("\n");
            buf.append("Known attributes: \n");
            for (GAttributeInfo attributeInfo : gbeanData.getGBeanInfo().getAttributes()) {
                buf.append(attributeInfo).append("\n");
            }
            throw new DeploymentException(buf.toString());
        }
    }

    private Object getValue(String type, String value, Bundle bundle) throws DeploymentException {
        if (value == null) {
            return null;
        }

        Class clazz = TYPE_LOOKUP.get(type);
        if (clazz == null) {
            try {
                clazz = bundle.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load attribute class:  type: " + type, e);
            }
        }

        // Handle numeric fields with no value set
        if (value.equals("")) {
            if (Number.class.isAssignableFrom(clazz) || Date.class.isAssignableFrom(clazz)) {
                return null;
            }
        }

        PropertyEditor editor = PropertyEditors.getEditor(clazz);
        editor.setAsText(value);
        return editor.getValue();
    }

    private AbstractName configureConnectionManager(List<GBeanData> raBeans, Naming naming, AbstractName managedConnectionFactoryName, String ddTransactionSupport, GerConnectiondefinitionInstanceType connectionfactoryInstance, AbstractNameQuery connectionTrackerName, AbstractNameQuery transactionManagerName) throws DeploymentException {
//        if (connectionfactoryInstance.getConnectionmanagerRef() != null) {
        //we don't configure anything, just use the supplied gbean
//            try {
//                return AbstractName.getInstance(connectionfactoryInstance.getConnectionmanagerRef());
//            } catch (MalformedAbstractNameException e) {
//                throw new DeploymentException("Invalid AbstractName string supplied for ConnectionManager reference", e);
//            }
//        }

        // create the object name for our connection manager
        AbstractName connectionManagerAbstractName = naming.createChildName(managedConnectionFactoryName, connectionfactoryInstance.getName().trim(), NameFactory.JCA_CONNECTION_MANAGER);

        // create the data holder for our connection manager
        GBeanData connectionManagerGBean = new GBeanData(connectionManagerAbstractName, GenericConnectionManagerGBean.class);

        //we configure our connection manager
        GerConnectionmanagerType connectionManager = connectionfactoryInstance.getConnectionmanager();
        TransactionSupport transactionSupport;
        if (connectionManager.isSetNoTransaction()) {
            transactionSupport = NoTransactions.INSTANCE;
        } else if (connectionManager.isSetLocalTransaction()) {
            if ("NoTransaction".equals(ddTransactionSupport)) {
                throw new DeploymentException("You are requesting local transaction support for a connector that does not support transactions: named: " + connectionfactoryInstance.getName().trim());
            }
            transactionSupport = LocalTransactions.INSTANCE;
        } else if (connectionManager.isSetTransactionLog()) {
            if ("NoTransaction".equals(ddTransactionSupport)) {
                throw new DeploymentException("You are requesting local transaction support for a connector that does not support transactions: named: " + connectionfactoryInstance.getName().trim());
            }
            transactionSupport = TransactionLog.INSTANCE;
        } else if (connectionManager.isSetXaTransaction()) {
            if ("NoTransaction".equals(ddTransactionSupport)) {
                throw new DeploymentException("You are requesting xa transaction support for a connector that does not support transactions: named: " + connectionfactoryInstance.getName().trim());
            }
            if ("LocalTransaction".equals(ddTransactionSupport)) {
                throw new DeploymentException("You are requesting xa transaction support for a connector that supports only local transactions: named: " + connectionfactoryInstance.getName().trim());
            }
            transactionSupport = new XATransactions(connectionManager.getXaTransaction().isSetTransactionCaching(),
                    connectionManager.getXaTransaction().isSetThreadCaching());
        } else if ("NoTransaction".equals(ddTransactionSupport)) {
            transactionSupport = NoTransactions.INSTANCE;
        } else if ("LocalTransaction".equals(ddTransactionSupport)) {
            transactionSupport = LocalTransactions.INSTANCE;
        } else if ("XATransaction".equals(ddTransactionSupport)) {
            transactionSupport = new XATransactions(defaultXATransactionCaching, defaultXAThreadCaching);
        } else {
            //this should not happen
            throw new DeploymentException("Unexpected transaction support element in connector named: " + connectionfactoryInstance.getName().trim());
        }
        PoolingSupport pooling;
        if (connectionManager.getSinglePool() != null) {
            GerSinglepoolType pool = connectionManager.getSinglePool();

            pooling = new SinglePool(pool.isSetMaxSize() ? pool.getMaxSize() : defaultMaxSize,
                    pool.isSetMinSize() ? pool.getMinSize() : defaultMinSize,
                    pool.isSetBlockingTimeoutMilliseconds() ? pool.getBlockingTimeoutMilliseconds() : defaultBlockingTimeoutMilliseconds,
                    pool.isSetIdleTimeoutMinutes() ? pool.getIdleTimeoutMinutes() : defaultIdleTimeoutMinutes,
                    pool.getMatchOne() != null,
                    pool.getMatchAll() != null,
                    pool.getSelectOneAssumeMatch() != null);
        } else if (connectionManager.getPartitionedPool() != null) {
            GerPartitionedpoolType pool = connectionManager.getPartitionedPool();
            pooling = new PartitionedPool(pool.isSetMaxSize() ? pool.getMaxSize() : defaultMaxSize,
                    pool.isSetMinSize() ? pool.getMinSize() : defaultMinSize,
                    pool.isSetBlockingTimeoutMilliseconds() ? pool.getBlockingTimeoutMilliseconds() : defaultBlockingTimeoutMilliseconds,
                    pool.isSetIdleTimeoutMinutes() ? pool.getIdleTimeoutMinutes() : defaultIdleTimeoutMinutes,
                    pool.getMatchOne() != null,
                    pool.getMatchAll() != null,
                    pool.getSelectOneAssumeMatch() != null,
                    pool.isSetPartitionByConnectionrequestinfo(),
                    pool.isSetPartitionBySubject());
        } else if (connectionManager.getNoPool() != null) {
            pooling = new NoPool();
        } else {
            throw new DeploymentException("Unexpected pooling support element in connector named " + connectionfactoryInstance.getName().trim());
        }
        try {
            String jndiName = naming.toOsgiJndiName(connectionManagerAbstractName);
            connectionManagerGBean.getServiceProperties().put(OSGI_JNDI_SERVICE_NAME, jndiName);
            connectionManagerGBean.setAttribute("transactionSupport", transactionSupport);
            connectionManagerGBean.setAttribute("pooling", pooling);
            connectionManagerGBean.setReferencePattern("ConnectionTracker", connectionTrackerName);
            connectionManagerGBean.setAttribute("containerManagedSecurity", connectionManager.isSetContainerManagedSecurity());
            connectionManagerGBean.setReferencePattern("TransactionManager", transactionManagerName);
            connectionManagerGBean.setReferencePattern("ManagedConnectionFactory", managedConnectionFactoryName);
        } catch (Exception e) {
            throw new DeploymentException("Problem setting up ConnectionManager named " + connectionfactoryInstance.getName().trim(), e);
        }

        raBeans.add(connectionManagerGBean);
        return connectionManagerAbstractName;
    }

    private void addOutboundGBeans(List<GBeanData> raBeans, Naming naming, AbstractName jcaResourceName, AbstractName resourceAdapterAbstractName, GBeanData managedConnectionFactoryPrototypeGBeanData, GerConnectiondefinitionInstanceType connectiondefinitionInstance, String transactionSupport, Bundle bundle, AbstractNameQuery connectionTrackerName, AbstractNameQuery transactionManagerName) throws DeploymentException {
        GBeanData managedConnectionFactoryInstanceGBeanData = new GBeanData(managedConnectionFactoryPrototypeGBeanData);
        AbstractName connectionFactoryAbstractName = naming.createChildName(jcaResourceName, connectiondefinitionInstance.getName().trim(), NameFactory.JCA_CONNECTION_FACTORY);
        AbstractName managedConnectionFactoryAbstractName = naming.createChildName(connectionFactoryAbstractName, connectiondefinitionInstance.getName().trim(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY);

        // ManagedConnectionFactory
        setDynamicGBeanDataAttributes(managedConnectionFactoryInstanceGBeanData, connectiondefinitionInstance.getConfigPropertySettingArray(), bundle);

        //Check if Driver class is available here. This should be available in cl. If not log a warning as
        //the plan gets deployed and while starting GBean an error is thrown

        Object driver = managedConnectionFactoryInstanceGBeanData.getAttribute("Driver");
        if (driver != null && driver instanceof String) {
            checkClass(bundle, (String)driver);
        }

        Set<String> implementedInterfaces = new HashSet<String>();
        implementedInterfaces.add(checkClass(bundle, (String) managedConnectionFactoryInstanceGBeanData.getAttribute("connectionFactoryInterface")));
        implementedInterfaces.add(checkClass(bundle, (String) managedConnectionFactoryInstanceGBeanData.getAttribute("connectionFactoryImplClass")));
        try {
            if (resourceAdapterAbstractName != null) {
                managedConnectionFactoryInstanceGBeanData.setReferencePattern("ResourceAdapterWrapper", resourceAdapterAbstractName);
            }
            //additional interfaces implemented by connection factory
            String[] additionalInterfaces = connectiondefinitionInstance.getImplementedInterfaceArray();
            if (additionalInterfaces != null) {
                for (String additionalInterface : additionalInterfaces) {
                    implementedInterfaces.add(checkClass(bundle, additionalInterface.trim()));
                }
            }
            //in case some class was not loadable
            implementedInterfaces.remove(null);
            managedConnectionFactoryInstanceGBeanData.setAttribute("implementedInterfaces", implementedInterfaces.toArray(new String[implementedInterfaces.size()]));

        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        managedConnectionFactoryInstanceGBeanData.setAbstractName(managedConnectionFactoryAbstractName);
        raBeans.add(managedConnectionFactoryInstanceGBeanData);
        // ConnectionManager
        AbstractName connectionManagerName = configureConnectionManager(raBeans, naming, managedConnectionFactoryAbstractName, transactionSupport, connectiondefinitionInstance, connectionTrackerName, transactionManagerName);

        // ConnectionFactory
        GBeanData connectionFactoryGBeanData = new GBeanData(connectionFactoryAbstractName, JCAConnectionFactoryImpl.class);
        connectionFactoryGBeanData.setReferencePattern("ConnectionManager", connectionManagerName);
        connectionFactoryGBeanData.setServiceInterfaces(implementedInterfaces.toArray(new String[implementedInterfaces.size()]));
        String jndiName = connectiondefinitionInstance.getJndiName();
        if (jndiName == null) {
            jndiName = naming.toOsgiJndiName(connectionFactoryAbstractName);
        } else {
            jndiName = jndiName.trim();
        }
        connectionFactoryGBeanData.getServiceProperties().put(OSGI_JNDI_SERVICE_NAME, jndiName);

        raBeans.add(connectionFactoryGBeanData);
    }

    /**
     * check class is loadable: return null and log warning if not.
     * @param bundle to use to load the class
     * @param clazz name of class to try to load
     * @return clazz if loadable, null otherwise.
     */
    private String checkClass(Bundle bundle, String clazz) {
        try {
            bundle.loadClass(clazz);
            return clazz;
        } catch (ClassNotFoundException e1) {
            log.warn("Problem loading class: " + clazz, e1);
        }
        return null;
    }

    public GBeanData locateActivationSpecInfo(AbstractNameQuery resourceAdapterInstanceQuery, String messageListenerInterface, Configuration configuration) throws DeploymentException {
        //First, locate the module gbean from the JCAResourceAdapter instance
        AbstractName instanceName;
        try {
            instanceName = configuration.findGBean(resourceAdapterInstanceQuery);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("No resource adapter instance gbean found matching " + resourceAdapterInstanceQuery + " from configuration " + configuration.getId(), e);
        }
        String moduleName = (String) instanceName.getName().get(NameFactory.RESOURCE_ADAPTER_MODULE);
        Map<String, String> moduleNameMap = new HashMap<String, String>(instanceName.getName());
        moduleNameMap.remove(NameFactory.JCA_RESOURCE);
        moduleNameMap.remove(NameFactory.RESOURCE_ADAPTER);
        moduleNameMap.remove(NameFactory.RESOURCE_ADAPTER_MODULE);
        moduleNameMap.put(NameFactory.J2EE_TYPE, NameFactory.RESOURCE_ADAPTER_MODULE);
        moduleNameMap.put(NameFactory.J2EE_NAME, moduleName);
        AbstractNameQuery nameQuery = new AbstractNameQuery(instanceName.getArtifact(), moduleNameMap, ResourceAdapterModule.class.getName());
        //now find the gbeandata and extract the activation spec info.
        GBeanData resourceModuleData;
        try {
            resourceModuleData = configuration.findGBeanData(nameQuery);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("No resource module gbean found matching " + nameQuery + " from configuration " + configuration.getId(), e);
        }
        Map activationSpecInfos = (Map) resourceModuleData.getAttribute("activationSpecInfoMap");
        if (activationSpecInfos == null) {
            throw new DeploymentException("No activation spec info map found in resource adapter module: " + resourceModuleData.getAbstractName());
        }
        return (GBeanData) activationSpecInfos.get(messageListenerInterface);
    }

    private GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData) throws DeploymentException {
        GBeanData data = (GBeanData) resourceAdapterModuleData.getAttribute("resourceAdapterGBeanData");
        if (data == null) {
            throw new DeploymentException("No resource adapter info found for resource adapter module: " + resourceAdapterModuleData.getAbstractName());
        }
        return data;
    }

    private GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName) throws DeploymentException {
        Map adminObjectInfos = (Map) resourceAdapterModuleData.getAttribute("adminObjectInfoMap");
        if (adminObjectInfos == null) {
            throw new DeploymentException("No admin object infos found for resource adapter module: " + resourceAdapterModuleData.getAbstractName());
        }
        return (GBeanData) adminObjectInfos.get(adminObjectInterfaceName);
    }

    private GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) throws DeploymentException {
        Map managedConnectionFactoryInfos = (Map) resourceAdapterModuleData.getAttribute("managedConnectionFactoryInfoMap");
        if (managedConnectionFactoryInfos == null) {
            throw new DeploymentException("No managed connection factory infos found for resource adapter module: " + resourceAdapterModuleData.getAbstractName());
        }
        return (GBeanData) managedConnectionFactoryInfos.get(connectionFactoryInterfaceName);
    }

}
