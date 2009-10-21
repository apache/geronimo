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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.wrapper.ActivationSpecWrapperGBean;
import org.apache.geronimo.connector.wrapper.AdminObjectWrapper;
import org.apache.geronimo.connector.wrapper.AdminObjectWrapperGBean;
import org.apache.geronimo.connector.wrapper.JCAResourceImplGBean;
import org.apache.geronimo.connector.wrapper.ResourceAdapterImplGBean;
import org.apache.geronimo.connector.wrapper.ResourceAdapterModuleImplGBean;
import org.apache.geronimo.connector.wrapper.ResourceAdapterWrapperGBean;
import org.apache.geronimo.connector.wrapper.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.connector.wrapper.outbound.JCAConnectionFactoryImplGBean;
import org.apache.geronimo.connector.wrapper.outbound.ManagedConnectionFactoryWrapperGBean;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionLog;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.NamespaceDrivenBuilderCollection;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
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
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.MultiGBeanInfoFactory;
import org.apache.geronimo.j2ee.deployment.ActivationSpecInfoLocator;
import org.apache.geronimo.j2ee.deployment.ConnectorModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.management.JCAConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAAdminObject;
import org.apache.geronimo.management.geronimo.JCAResourceAdapter;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.geronimo.GerPartitionedpoolType;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerSinglepoolType;
import org.apache.geronimo.xbeans.j2ee.ActivationspecType;
import org.apache.geronimo.xbeans.j2ee.AdminobjectType;
import org.apache.geronimo.xbeans.j2ee.ConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.ConnectionDefinitionType;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.geronimo.xbeans.j2ee.ConnectorType;
import org.apache.geronimo.xbeans.j2ee.MessagelistenerType;
import org.apache.geronimo.xbeans.j2ee.ResourceadapterType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:385659 $ $Date$
 */
public class ConnectorModuleBuilder implements ModuleBuilder, ActivationSpecInfoLocator, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ConnectorModuleBuilder.class);

    private static final QName RESOURCE_ADAPTER_VERSION = new QName(SchemaConversionUtils.J2EE_NAMESPACE, "resourceadapter-version");
    private static QName CONNECTOR_QNAME = GerConnectorDocument.type.getDocumentElementName();
    static final String GERCONNECTOR_NAMESPACE = CONNECTOR_QNAME.getNamespaceURI();
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
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
    
    public ConnectorModuleBuilder(Environment defaultEnvironment,
            int defaultMaxSize,
            int defaultMinSize,
            int defaultBlockingTimeoutMilliseconds,
            int defaultIdleTimeoutMinutes,
            boolean defaultXATransactionCaching,
            boolean defaultXAThreadCaching,
            String defaultWorkManagerName,
            Collection<NamespaceDrivenBuilder> serviceBuilders) {
        this.defaultEnvironment = defaultEnvironment;

        this.defaultMaxSize = defaultMaxSize;
        this.defaultMinSize = defaultMinSize;
        this.defaultBlockingTimeoutMilliseconds = defaultBlockingTimeoutMilliseconds;
        this.defaultIdleTimeoutMinutes = defaultIdleTimeoutMinutes;
        this.defaultXATransactionCaching = defaultXATransactionCaching;
        this.defaultXAThreadCaching = defaultXAThreadCaching;
        this.defaultWorkManagerName = defaultWorkManagerName;
        this.serviceBuilders = new NamespaceDrivenBuilderCollection(serviceBuilders);
    }

    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doFail() {
        doStop();
    }

    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, "rar", null, null, null, naming, idBuilder);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, environment, earName, naming, idBuilder);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment earEnvironment, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        assert moduleFile != null : "moduleFile is null";
        assert targetPath != null : "targetPath is null";
        assert !targetPath.endsWith("/") : "targetPath must not end with a '/'";

        String specDD;
        XmlObject connector;
        try {
            if (specDDUrl == null) {
                specDDUrl = DeploymentUtil.createJarURL(moduleFile, "META-INF/ra.xml");
            }

            // read in the entire specDD as a string, we need this for getDeploymentDescriptor
            // on the J2ee management object
            specDD = DeploymentUtil.readAll(specDDUrl);
        } catch (Exception e) {
            //no ra.xml, not for us.
            return null;
        }
        //we found ra.xml, if it won't parse it's an error.
        try {
            // parse it
            XmlObject xmlObject = XmlBeansUtil.parse(specDD);
            ConnectorDocument connectorDoc = convertToConnectorSchema(xmlObject);
            connector = connectorDoc.getConnector();
        } catch (XmlException e) {
            throw new DeploymentException("Could not parse ra.xml descriptor", e);
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
                        URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/geronimo-ra.xml");
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
        if (earName == null) {
            earName = naming.createRootName(environment.getConfigId(), NameFactory.NULL, NameFactory.J2EE_APPLICATION);
            moduleName = naming.createChildName(earName, environment.getConfigId().toString(), NameFactory.RESOURCE_ADAPTER_MODULE);
        } else {
            moduleName = naming.createChildName(earName, targetPath, NameFactory.RESOURCE_ADAPTER_MODULE);
        }

        boolean standAlone = earEnvironment == null;
        AnnotatedApp annotatedApp = null;
        return new ConnectorModule(standAlone, moduleName, environment, moduleFile, targetPath, connector, gerConnector, specDD, annotatedApp);

    }

    static ConnectorDocument convertToConnectorSchema(XmlObject xmlObject) throws XmlException {
        if (ConnectorDocument.type.equals(xmlObject.schemaType())) {
            XmlBeansUtil.validateDD(xmlObject);
            return (ConnectorDocument) xmlObject;
        }
        XmlCursor cursor = xmlObject.newCursor();
        XmlDocumentProperties xmlDocumentProperties = cursor.documentProperties();
        String publicId = xmlDocumentProperties.getDoctypePublicId();
        try {
            if ("-//Sun Microsystems, Inc.//DTD Connector 1.0//EN".equals(publicId)) {
                XmlCursor moveable = xmlObject.newCursor();
                try {
                    String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/connector_1_5.xsd";
                    String version = "1.5";
                    SchemaConversionUtils.convertToSchema(cursor, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version);
                    cursor.toStartDoc();
                    cursor.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "connector");
                    cursor.toFirstChild();
                    SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.J2EE_NAMESPACE, cursor, moveable);
                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "spec-version");
                    cursor.removeXml();
                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "version");
                    cursor.setName(RESOURCE_ADAPTER_VERSION);
                    cursor.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "resourceadapter");
                    moveable.toCursor(cursor);
                    cursor.toFirstChild();
                    cursor.beginElement("outbound-resourceadapter", SchemaConversionUtils.J2EE_NAMESPACE);
                    cursor.beginElement("connection-definition", SchemaConversionUtils.J2EE_NAMESPACE);
                    moveable.toChild(SchemaConversionUtils.J2EE_NAMESPACE, "managedconnectionfactory-class");
                    moveable.push();
                    //from moveable to cursor
                    moveable.moveXml(cursor);
                    while (moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "config-property")) {
                        moveable.moveXml(cursor);
                    }
                    moveable.pop();
                    moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "connectionfactory-interface");
                    moveable.moveXml(cursor);
                    moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "connectionfactory-impl-class");
                    moveable.moveXml(cursor);
                    moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "connection-interface");
                    moveable.moveXml(cursor);
                    moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "connection-impl-class");
                    moveable.moveXml(cursor);
                    //get out of connection-definition element
                    cursor.toNextToken();
                    moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "transaction-support");
                    moveable.moveXml(cursor);
                    while (moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "authentication-mechanism")) {
                        moveable.moveXml(cursor);
                    }
                    moveable.toNextSibling(SchemaConversionUtils.J2EE_NAMESPACE, "reauthentication-support");
                    moveable.moveXml(cursor);
                } finally {
                    moveable.dispose();
                }

            }
        } finally {
            cursor.dispose();
        }
        XmlObject result = xmlObject.changeType(ConnectorDocument.type);
        if (result != null) {
            XmlBeansUtil.validateDD(result);
            return (ConnectorDocument) result;
        }
        XmlBeansUtil.validateDD(xmlObject);
        return (ConnectorDocument) xmlObject;

    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
        try {
            JarFile moduleFile = module.getModuleFile();

            // add the manifest classpath entries declared in the connector to the class loader
            // we have to explicitly add these since we are unpacking the connector module
            // and the url class loader will not pick up a manifiest from an unpacked dir
            // N.B. If we ever introduce a separate configuration/module for a rar inside an ear
            // this will need to be modified to use "../" instead of module.getTargetPath().
            // See AbstractWebModuleBuilder.
            earContext.addManifestClassPath(moduleFile, URI.create(module.getTargetPath()));

            URI targetURI = URI.create(module.getTargetPath() + "/");
            Enumeration entries = moduleFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                URI target = targetURI.resolve(entry.getName());
                if (entry.getName().endsWith(".jar")) {
                    earContext.addInclude(target, moduleFile, entry);
                } else {
                    earContext.addFile(target, moduleFile, entry);
                }
            }

        } catch (IOException e) {
            throw new DeploymentException("Problem deploying connector", e);
        }
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        ConnectorModule resourceModule = (ConnectorModule) module;

        final ConnectorType connector = (ConnectorType) module.getSpecDD();

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
        resourceAdapterModuleData.setAttribute("displayName", connector.getDisplayNameArray().length == 0 ? null : connector.getDisplayNameArray(0).getStringValue());
        resourceAdapterModuleData.setAttribute("description", connector.getDescriptionArray().length == 0 ? null : connector.getDescriptionArray(0).getStringValue());
        resourceAdapterModuleData.setAttribute("vendorName", connector.getVendorName().getStringValue());
        resourceAdapterModuleData.setAttribute("EISType", connector.getEisType().getStringValue());
        resourceAdapterModuleData.setAttribute("resourceAdapterVersion", connector.getResourceadapterVersion().getStringValue());

        ResourceadapterType resourceadapter = connector.getResourceadapter();
        // Create the resource adapter gbean
        if (resourceadapter.isSetResourceadapterClass()) {
            GBeanInfoBuilder resourceAdapterInfoBuilder = new GBeanInfoBuilder(ResourceAdapterWrapperGBean.class, new MultiGBeanInfoFactory().getGBeanInfo(ResourceAdapterWrapperGBean.class));
            String resourceAdapterClassName = resourceadapter.getResourceadapterClass().getStringValue().trim();
            GBeanData resourceAdapterGBeanData = setUpDynamicGBeanWithProperties(resourceAdapterClassName, resourceAdapterInfoBuilder, resourceadapter.getConfigPropertyArray(), bundle, Collections.<String>emptySet());

            resourceAdapterGBeanData.setAttribute("resourceAdapterClass", resourceAdapterClassName);

            // Add map from messageListenerInterface to activationSpec class
            Map<String, String> messageListenerToActivationSpecMap = new TreeMap<String, String>();
            if (resourceadapter.isSetInboundResourceadapter() && resourceadapter.getInboundResourceadapter().isSetMessageadapter()) {
                for (MessagelistenerType messagelistenerType : resourceadapter.getInboundResourceadapter().getMessageadapter().getMessagelistenerArray()) {
                    String messageListenerInterface = messagelistenerType.getMessagelistenerType().getStringValue().trim();
                    ActivationspecType activationspec = messagelistenerType.getActivationspec();
                    String activationSpecClassName = activationspec.getActivationspecClass().getStringValue().trim();
                    messageListenerToActivationSpecMap.put(messageListenerInterface, activationSpecClassName);
                    resourceAdapterGBeanData.setAttribute("messageListenerToActivationSpecMap", messageListenerToActivationSpecMap);
                    resourceAdapterGBeanData.setReferencePattern("TransactionManager", earContext.getTransactionManagerName());
                }
            }

            resourceAdapterModuleData.setAttribute("resourceAdapterGBeanData", resourceAdapterGBeanData);
        }

        if (resourceadapter.isSetInboundResourceadapter() && resourceadapter.getInboundResourceadapter().isSetMessageadapter()) {
            Map activationSpecInfoMap = getActivationSpecInfoMap(resourceadapter.getInboundResourceadapter().getMessageadapter().getMessagelistenerArray(), bundle);
            resourceAdapterModuleData.setAttribute("activationSpecInfoMap", activationSpecInfoMap);
        }
        Map adminObjectInfoMap = getAdminObjectInfoMap(resourceadapter.getAdminobjectArray(), bundle);
        resourceAdapterModuleData.setAttribute("adminObjectInfoMap", adminObjectInfoMap);
        if (resourceadapter.isSetOutboundResourceadapter()) {
            Map managedConnectionFactoryInfoMap = getManagedConnectionFactoryInfoMap(resourceadapter.getOutboundResourceadapter().getConnectionDefinitionArray(), bundle);
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

    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
        //all our gbeans are added in  the initContext step
    }

    public String getSchemaNamespace() {
        return GERCONNECTOR_NAMESPACE;
    }

    private void addConnectorGBeans(EARContext earContext, AbstractName jcaResourceName, GBeanData resourceAdapterModuleData, ConnectorType connector, GerConnectorType geronimoConnector, Bundle bundle) throws DeploymentException {
        ResourceadapterType resourceadapter = connector.getResourceadapter();

        GerResourceadapterType[] geronimoResourceAdapters = geronimoConnector.getResourceadapterArray();
        for (GerResourceadapterType geronimoResourceAdapter : geronimoResourceAdapters) {
            // Resource Adapter
            AbstractName resourceAdapterAbstractName = null;
            if (resourceadapter.isSetResourceadapterClass()) {
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
                try {
                    earContext.addGBean(resourceAdapterInstanceGBeanData);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException("Could not add resource adapter instance gbean to context", e);
                }
            }

            // Outbound Managed Connection Factories (think JDBC data source or JMS connection factory)

            // ManagedConnectionFactory setup
            if (geronimoResourceAdapter.isSetOutboundResourceadapter()) {
                if (!resourceadapter.isSetOutboundResourceadapter()) {
                    throw new DeploymentException("Geronimo plan configures an outbound resource adapter but ra.xml does not describe any");
                }
                String transactionSupport = resourceadapter.getOutboundResourceadapter().getTransactionSupport().getStringValue().trim();
                for (GerConnectionDefinitionType geronimoConnectionDefinition : geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray()) {
                    assert geronimoConnectionDefinition != null : "Null GeronimoConnectionDefinition";

                    String connectionFactoryInterfaceName = geronimoConnectionDefinition.getConnectionfactoryInterface().trim();
                    GBeanData connectionFactoryGBeanData = locateConnectionFactoryInfo(resourceAdapterModuleData, connectionFactoryInterfaceName);

                    if (connectionFactoryGBeanData == null) {
                        throw new DeploymentException("No connection definition for ConnectionFactory class: " + connectionFactoryInterfaceName);
                    }

                    for (int j = 0; j < geronimoConnectionDefinition.getConnectiondefinitionInstanceArray().length; j++) {
                        GerConnectiondefinitionInstanceType connectionfactoryInstance = geronimoConnectionDefinition.getConnectiondefinitionInstanceArray()[j];

                        addOutboundGBeans(earContext, jcaResourceName, resourceAdapterAbstractName, connectionFactoryGBeanData, connectionfactoryInstance, transactionSupport, bundle);
                    }
                }
            }
            addAdminObjectGBeans(earContext, jcaResourceName, resourceAdapterModuleData, bundle, resourceAdapterAbstractName, geronimoResourceAdapter.getAdminobjectArray());
        }
        // admin objects (think message queues and topics)

        // add configured admin objects
        addAdminObjectGBeans(earContext, jcaResourceName, resourceAdapterModuleData, bundle, null, geronimoConnector.getAdminobjectArray());
    }

    private void addAdminObjectGBeans(EARContext earContext, AbstractName jcaResourceName, GBeanData resourceAdapterModuleData, Bundle bundle, AbstractName resourceAdapterAbstractName, GerAdminobjectType[] adminObjects) throws DeploymentException {
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
                AbstractName adminObjectAbstractName = earContext.getNaming().createChildName(jcaResourceName, gerAdminObjectInstance.getMessageDestinationName().trim(), NameFactory.JCA_ADMIN_OBJECT);
                adminObjectInstanceGBeanData.setAbstractName(adminObjectAbstractName);
                if (resourceAdapterAbstractName != null) {
                    adminObjectInstanceGBeanData.setReferencePattern("ResourceAdapterWrapper", resourceAdapterAbstractName);
                }
                try {
                    earContext.addGBean(adminObjectInstanceGBeanData);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException("Could not add admin object gbean to context", e);
                }
            }
        }
    }

    private Map getActivationSpecInfoMap(MessagelistenerType[] messagelistenerArray, Bundle bundle) throws DeploymentException {
        Map<String, GBeanData> activationSpecInfos = new HashMap<String, GBeanData>();
        for (MessagelistenerType messagelistenerType : messagelistenerArray) {
            String messageListenerInterface = messagelistenerType.getMessagelistenerType().getStringValue().trim();
            ActivationspecType activationspec = messagelistenerType.getActivationspec();
            String activationSpecClassName = activationspec.getActivationspecClass().getStringValue().trim();
            GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ActivationSpecWrapperGBean.class, ActivationSpecWrapperGBean.GBEAN_INFO);
            Set<String> ignore = Collections.singleton("resourceAdapter");
            setUpDynamicGBean(activationSpecClassName, infoBuilder, ignore, bundle, true);


            GBeanInfo gbeanInfo = infoBuilder.getBeanInfo();

            GBeanData activationSpecInfo = new GBeanData(gbeanInfo);
            activationSpecInfo.setAttribute("activationSpecClass", activationSpecClassName);
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
        } else{ 
            return name;
        }
    }

    private Map getManagedConnectionFactoryInfoMap(ConnectionDefinitionType[] connectionDefinitionArray, Bundle bundle) throws DeploymentException {
        Map<String, GBeanData> managedConnectionFactoryInfos = new HashMap<String, GBeanData>();
        for (ConnectionDefinitionType connectionDefinition : connectionDefinitionArray) {
            GBeanInfoBuilder managedConnectionFactoryInfoBuilder = new GBeanInfoBuilder(ManagedConnectionFactoryWrapper.class, ManagedConnectionFactoryWrapperGBean.GBEAN_INFO);
            String managedConnectionfactoryClassName = connectionDefinition.getManagedconnectionfactoryClass().getStringValue().trim();
            Set<String> ignore = new HashSet<String>();
            ignore.add("ResourceAdapter");
            ignore.add("LogWriter");
            GBeanData managedConnectionFactoryGBeanData = setUpDynamicGBeanWithProperties(managedConnectionfactoryClassName, managedConnectionFactoryInfoBuilder, connectionDefinition.getConfigPropertyArray(), bundle, ignore);

            // set the standard properties
            String connectionfactoryInterface = connectionDefinition.getConnectionfactoryInterface().getStringValue().trim();
            managedConnectionFactoryGBeanData.setAttribute("managedConnectionFactoryClass", managedConnectionfactoryClassName);
            managedConnectionFactoryGBeanData.setAttribute("connectionFactoryInterface", connectionfactoryInterface);
            managedConnectionFactoryGBeanData.setAttribute("connectionFactoryImplClass", connectionDefinition.getConnectionfactoryImplClass().getStringValue().trim());
            managedConnectionFactoryGBeanData.setAttribute("connectionInterface", connectionDefinition.getConnectionInterface().getStringValue().trim());
            managedConnectionFactoryGBeanData.setAttribute("connectionImplClass", connectionDefinition.getConnectionImplClass().getStringValue().trim());
            managedConnectionFactoryInfos.put(connectionfactoryInterface, managedConnectionFactoryGBeanData);
        }
        return managedConnectionFactoryInfos;
    }

    private Map getAdminObjectInfoMap(AdminobjectType[] adminobjectArray, Bundle bundle) throws DeploymentException {
        Map<String, GBeanData> adminObjectInfos = new HashMap<String, GBeanData>();
        for (AdminobjectType adminObject : adminobjectArray) {
            GBeanInfoBuilder adminObjectInfoBuilder = new GBeanInfoBuilder(AdminObjectWrapper.class, AdminObjectWrapperGBean.GBEAN_INFO);
            String adminObjectClassName = adminObject.getAdminobjectClass().getStringValue().trim();
            GBeanData adminObjectGBeanData = setUpDynamicGBeanWithProperties(adminObjectClassName, adminObjectInfoBuilder, adminObject.getConfigPropertyArray(), bundle, Collections.<String>emptySet());

            // set the standard properties
            String adminObjectInterface = adminObject.getAdminobjectInterface().getStringValue().trim();
            adminObjectGBeanData.setAttribute("adminObjectInterface", adminObjectInterface);
            adminObjectGBeanData.setAttribute("adminObjectClass", adminObjectClassName);
            adminObjectInfos.put(adminObjectInterface, adminObjectGBeanData);
        }
        return adminObjectInfos;
    }


    private GBeanData setUpDynamicGBeanWithProperties(String className, GBeanInfoBuilder infoBuilder, ConfigPropertyType[] configProperties, Bundle bundle, Set<String> ignore) throws DeploymentException {
        setUpDynamicGBean(className, infoBuilder, ignore, bundle, false);

        GBeanInfo gbeanInfo = infoBuilder.getBeanInfo();
        GBeanData gbeanData = new GBeanData(gbeanInfo);
        for (ConfigPropertyType configProperty : configProperties) {
            if (configProperty.isSetConfigPropertyValue()) {
                String name = configProperty.getConfigPropertyName().getStringValue();
                if (gbeanInfo.getAttribute(name) == null) {
                    String originalName = name;
                    name = switchCase(name);
                    if (gbeanInfo.getAttribute(name) == null) {
                        log.warn("Unsupported config-property: " + originalName);
                        continue;
                    }
                }
                String type = configProperty.getConfigPropertyType().getStringValue();
                String value = configProperty.getConfigPropertyValue().getStringValue();
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
            StringBuffer buf = new StringBuffer("The plan is trying to set attributes: ").append(unknownNames).append("\n");
            buf.append("Known attributes: \n");
            for (GAttributeInfo attributeInfo: gbeanData.getGBeanInfo().getAttributes()) {
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

    private AbstractName configureConnectionManager(EARContext earContext, AbstractName jcaResourceName, String ddTransactionSupport, GerConnectiondefinitionInstanceType connectionfactoryInstance, Bundle bundle) throws DeploymentException {
//        if (connectionfactoryInstance.getConnectionmanagerRef() != null) {
        //we don't configure anything, just use the supplied gbean
//            try {
//                return AbstractName.getInstance(connectionfactoryInstance.getConnectionmanagerRef());
//            } catch (MalformedAbstractNameException e) {
//                throw new DeploymentException("Invalid AbstractName string supplied for ConnectionManager reference", e);
//            }
//        }

        // create the object name for our connection manager
        AbstractName connectionManagerAbstractName = earContext.getNaming().createChildName(jcaResourceName, connectionfactoryInstance.getName().trim(), NameFactory.JCA_CONNECTION_MANAGER);

        // create the data holder for our connection manager
        GBeanInfo gbeanInfo;
        try {
            gbeanInfo = GBeanInfo.getGBeanInfo("org.apache.geronimo.connector.wrapper.outbound.GenericConnectionManagerGBean", bundle);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to create GMBean", e);
        }
        GBeanData connectionManagerGBean = new GBeanData(connectionManagerAbstractName, gbeanInfo);

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
            connectionManagerGBean.setAttribute("transactionSupport", transactionSupport);
            connectionManagerGBean.setAttribute("pooling", pooling);
            connectionManagerGBean.setReferencePattern("ConnectionTracker", earContext.getConnectionTrackerName());
            connectionManagerGBean.setAttribute("containerManagedSecurity", connectionManager.isSetContainerManagedSecurity());
            connectionManagerGBean.setReferencePattern("TransactionManager", earContext.getTransactionManagerName());
        } catch (Exception e) {
            throw new DeploymentException("Problem setting up ConnectionManager named " + connectionfactoryInstance.getName().trim(), e);
        }

        try {
            earContext.addGBean(connectionManagerGBean);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add connection manager gbean to context: name: " + connectionfactoryInstance.getName().trim(), e);
        }
        return connectionManagerAbstractName;
    }

    private void addOutboundGBeans(EARContext earContext, AbstractName jcaResourceName, AbstractName resourceAdapterAbstractName, GBeanData managedConnectionFactoryPrototypeGBeanData, GerConnectiondefinitionInstanceType connectiondefinitionInstance, String transactionSupport, Bundle bundle) throws DeploymentException {
        GBeanData managedConnectionFactoryInstanceGBeanData = new GBeanData(managedConnectionFactoryPrototypeGBeanData);
        AbstractName connectionFactoryAbstractName = earContext.getNaming().createChildName(jcaResourceName, connectiondefinitionInstance.getName().trim(), NameFactory.JCA_CONNECTION_FACTORY);
        AbstractName managedConnectionFactoryAbstractName = earContext.getNaming().createChildName(connectionFactoryAbstractName, connectiondefinitionInstance.getName().trim(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        // ConnectionManager
        AbstractName connectionManagerAbstractName = configureConnectionManager(earContext, managedConnectionFactoryAbstractName, transactionSupport, connectiondefinitionInstance, bundle);

        // ManagedConnectionFactory
        setDynamicGBeanDataAttributes(managedConnectionFactoryInstanceGBeanData, connectiondefinitionInstance.getConfigPropertySettingArray(), bundle);

        //Check if Driver class is available here. This should be available in cl. If not log a warning as
        //the plan gets deployed and while starting GBean an error is thrown

        Object driver = managedConnectionFactoryInstanceGBeanData.getAttribute("Driver");
        if (driver != null && driver instanceof String) {
            try {
                bundle.loadClass((String) driver);
            } catch (ClassNotFoundException e1) {
                log.warn("Problem loading driver class '" + driver + "', possibly due to a missing dependency on the driver jar!!", e1);
            }
        }

        try {
            if (resourceAdapterAbstractName != null) {
                managedConnectionFactoryInstanceGBeanData.setReferencePattern("ResourceAdapterWrapper", resourceAdapterAbstractName);
            }
            managedConnectionFactoryInstanceGBeanData.setReferencePattern("ConnectionManagerContainer", connectionManagerAbstractName);
            //additional interfaces implemented by connection factory
            String[] implementedInterfaces = connectiondefinitionInstance.getImplementedInterfaceArray();
            if (implementedInterfaces != null) {
                for (int i = 0; i < implementedInterfaces.length; i++) {
                    implementedInterfaces[i] = implementedInterfaces[i].trim();
                }
            } else {
                implementedInterfaces = new String[0];
            }
            managedConnectionFactoryInstanceGBeanData.setAttribute("implementedInterfaces", implementedInterfaces);

        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        managedConnectionFactoryInstanceGBeanData.setAbstractName(managedConnectionFactoryAbstractName);
        try {
            earContext.addGBean(managedConnectionFactoryInstanceGBeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add managed connection factory gbean to context", e);
        }

        // ConnectionFactory
        GBeanData connectionFactoryGBeanData = new GBeanData(connectionFactoryAbstractName, JCAConnectionFactoryImplGBean.GBEAN_INFO);
        connectionFactoryGBeanData.setReferencePattern("JCAManagedConnectionFactory", managedConnectionFactoryAbstractName);

        try {
            earContext.addGBean(connectionFactoryGBeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Could not add connection factory gbean to context", e);
        }
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ConnectorModuleBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("defaultMaxSize", int.class, true, true);
        infoBuilder.addAttribute("defaultMinSize", int.class, true, true);
        infoBuilder.addAttribute("defaultBlockingTimeoutMilliseconds", int.class, true, true);
        infoBuilder.addAttribute("defaultIdleTimeoutMinutes", int.class, true, true);
        infoBuilder.addAttribute("defaultXATransactionCaching", boolean.class, true, true);
        infoBuilder.addAttribute("defaultXAThreadCaching", boolean.class, true, true);
        infoBuilder.addAttribute("defaultWorkManagerName", String.class, true, true);

        infoBuilder.addReference("ServiceBuilders", NamespaceDrivenBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.addInterface(ModuleBuilder.class);
        infoBuilder.addInterface(ActivationSpecInfoLocator.class);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment",
                "defaultMaxSize",
                "defaultMinSize",
                "defaultBlockingTimeoutMilliseconds",
                "defaultIdleTimeoutMinutes",
                "defaultXATransactionCaching",
                "defaultXAThreadCaching",
                "defaultWorkManagerName", 
                "ServiceBuilders"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
