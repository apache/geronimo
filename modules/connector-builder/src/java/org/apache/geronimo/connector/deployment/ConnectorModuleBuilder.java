/**
 *
 * Copyright 2004-2005 The Apache Software Foundation
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
package org.apache.geronimo.connector.deployment;

import java.beans.Introspector;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import javax.naming.Reference;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.connector.ActivationSpecWrapperGBean;
import org.apache.geronimo.connector.AdminObjectWrapper;
import org.apache.geronimo.connector.AdminObjectWrapperGBean;
import org.apache.geronimo.connector.JCAResourceImplGBean;
import org.apache.geronimo.connector.ResourceAdapterImplGBean;
import org.apache.geronimo.connector.ResourceAdapterModuleImplGBean;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.ResourceAdapterWrapperGBean;
import org.apache.geronimo.connector.outbound.JCAConnectionFactoryImplGBean;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapperGBean;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionLog;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.j2ee.deployment.ConnectorModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.reference.ResourceReference;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.geronimo.GerCredentialInterfaceType;
import org.apache.geronimo.xbeans.geronimo.GerPartitionedpoolType;
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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorModuleBuilder implements ModuleBuilder, ResourceReferenceBuilder {

    private final int defaultMaxSize;
    private final int defaultMinSize;
    private final int defaultBlockingTimeoutMilliseconds;
    private final int defaultIdleTimeoutMinutes;
    private final boolean defaultXATransactionCaching;
    private final boolean defaultXAThreadCaching;
    private final URI defaultParentId;
    private final Repository repository;
    private final Kernel kernel;

    public ConnectorModuleBuilder(URI defaultParentId,
                                  int defaultMaxSize,
                                  int defaultMinSize,
                                  int defaultBlockingTimeoutMilliseconds,
                                  int defaultIdleTimeoutMinutes,
                                  boolean defaultXATransactionCaching,
                                  boolean defaultXAThreadCaching,
                                  Repository repository,
                                  Kernel kernel) {
        assert repository != null;
        this.defaultParentId = defaultParentId;
        this.defaultMaxSize = defaultMaxSize;
        this.defaultMinSize = defaultMinSize;
        this.defaultBlockingTimeoutMilliseconds = defaultBlockingTimeoutMilliseconds;
        this.defaultIdleTimeoutMinutes = defaultIdleTimeoutMinutes;
        this.defaultXATransactionCaching = defaultXATransactionCaching;
        this.defaultXAThreadCaching = defaultXAThreadCaching;
        this.repository = repository;
        this.kernel = kernel;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "rar", null, true);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
        return createModule(plan, moduleFile, targetPath, specDDUrl, false);
    }

    private Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone) throws DeploymentException {
        assert moduleFile != null: "moduleFile is null";
        assert targetPath != null: "targetPath is null";
        assert !targetPath.endsWith("/"): "targetPath must not end with a '/'";

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
            XmlObject xmlObject = SchemaConversionUtils.parse(specDD);
            ConnectorDocument connectorDoc = SchemaConversionUtils.convertToConnectorSchema(xmlObject);
            connector = connectorDoc.getConnector();
        } catch (XmlException e) {
            throw new DeploymentException("Could not parse ra.xml descriptor", e);
        }
        GerConnectorType gerConnector = null;
        try {
            // load the geronimo-application-client.xml from either the supplied plan or from the earFile
            try {
                if (plan instanceof XmlObject) {
                    gerConnector = (GerConnectorType) SchemaConversionUtils.getNestedObjectAsType((XmlObject) plan,
                            "connector",
                            GerConnectorType.type);
                } else {
                    GerConnectorDocument gerConnectorDoc = null;
                    if (plan != null) {
                        gerConnectorDoc = GerConnectorDocument.Factory.parse((File) plan);
                    } else {
                        URL path = DeploymentUtil.createJarURL(moduleFile, "META-INF/geronimo-ra.xml");
                        gerConnectorDoc = GerConnectorDocument.Factory.parse(path);
                    }
                    if (gerConnectorDoc != null) {
                        gerConnector = gerConnectorDoc.getConnector();
                    }
                }
            } catch (IOException e) {
            }

            // if we got one extract the validate it otherwise create a default one
            if (gerConnector == null) {
                throw new DeploymentException("A connector module must be deployed using a Geronimo deployment plan" +
                        " (either META-INF/geronimo-ra.xml in the RAR file or a standalone deployment plan passed to the deployer).");
            }
            gerConnector = (GerConnectorType) SchemaConversionUtils.convertToGeronimoServiceSchema(gerConnector);
            //for workmanager
            gerConnector = (GerConnectorType) SchemaConversionUtils.convertToGeronimoNamingSchema(gerConnector);
            SchemaConversionUtils.validateDD(gerConnector);
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        // get the ids from either the application plan or for a stand alone module from the specific deployer
        URI configId = null;
        try {
            configId = new URI(gerConnector.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + gerConnector.getConfigId(), e);
        }

        URI parentId = null;
        if (gerConnector.isSetParentId()) {
            try {
                parentId = new URI(gerConnector.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + gerConnector.getParentId(), e);
            }
        } else {
            parentId = defaultParentId;
        }

        return new ConnectorModule(standAlone, configId, parentId, moduleFile, targetPath, connector, gerConnector, specDD);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        try {
            JarFile moduleFile = module.getModuleFile();

            // add the manifest classpath entries declared in the connector to the class loader
            // we have to explicitly add these since we are unpacking the connector module
            // and the url class loader will not pick up a manifiest from an unpacked dir
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

            GerConnectorType vendorConnector = (GerConnectorType) module.getVendorDD();
            DependencyType[] dependencies = vendorConnector.getDependencyArray();
            ServiceConfigBuilder.addDependencies(earContext, dependencies, repository);
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying connector", e);
        }
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = J2eeContextImpl.newModuleContextFromApplication(earJ2eeContext, NameFactory.RESOURCE_ADAPTER_MODULE, module.getName());
        J2eeContext resourceJ2eeContext = J2eeContextImpl.newModuleContextFromApplication(earJ2eeContext, NameFactory.JCA_RESOURCE, module.getName());
        XmlObject specDD = module.getSpecDD();

        //set up the metadata for the ResourceAdapterModule
        ObjectName resourceAdapterModuleName = null;
        try {
            resourceAdapterModuleName = NameFactory.getModuleName(null, null, null, null, null, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct module name", e);
        }
        GBeanData resourceAdapterModuleData = new GBeanData(resourceAdapterModuleName, ResourceAdapterModuleImplGBean.GBEAN_INFO);

        // initalize the GBean
        resourceAdapterModuleData.setReferencePattern(NameFactory.J2EE_SERVER, earContext.getServerObjectName());
        if (!earContext.getJ2EEApplicationName().equals(NameFactory.NULL)) {
            resourceAdapterModuleData.setReferencePattern(NameFactory.J2EE_APPLICATION, earContext.getApplicationObjectName());
        }

        resourceAdapterModuleData.setAttribute("deploymentDescriptor", module.getOriginalSpecDD());

        ResourceadapterType resourceadapter = ((ConnectorType) specDD).getResourceadapter();
        // Create the resource adapter gbean
        if (resourceadapter.isSetResourceadapterClass()) {
            GBeanInfoBuilder resourceAdapterInfoBuilder = new GBeanInfoBuilder(ResourceAdapterWrapperGBean.class, ResourceAdapterWrapperGBean.GBEAN_INFO);
            GBeanData resourceAdapterGBeanData = setUpDynamicGBean(resourceAdapterInfoBuilder, resourceadapter.getConfigPropertyArray(), cl);

            resourceAdapterGBeanData.setAttribute("resourceAdapterClass", resourceadapter.getResourceadapterClass().getStringValue().trim());
            resourceAdapterModuleData.setAttribute("resourceAdapterGBeanData", resourceAdapterGBeanData);
        }

        if (resourceadapter.isSetInboundResourceadapter() && resourceadapter.getInboundResourceadapter().isSetMessageadapter()) {
            Map activationSpecInfoMap = getActivationSpecInfoMap(resourceadapter.getInboundResourceadapter().getMessageadapter().getMessagelistenerArray(), cl);
            resourceAdapterModuleData.setAttribute("activationSpecInfoMap", activationSpecInfoMap);
        }
        Map adminObjectInfoMap = getAdminObjectInfoMap(resourceadapter.getAdminobjectArray(), cl);
        resourceAdapterModuleData.setAttribute("adminObjectInfoMap", adminObjectInfoMap);
        if (resourceadapter.isSetOutboundResourceadapter()) {
            Map managedConnectionFactoryInfoMap = getManagedConnectionFactoryInfoMap(resourceadapter.getOutboundResourceadapter().getConnectionDefinitionArray(), cl);
            resourceAdapterModuleData.setAttribute("managedConnectionFactoryInfoMap", managedConnectionFactoryInfoMap);
        }

        earContext.addGBean(resourceAdapterModuleData);

        //register the instances we will create later
        GerConnectorType geronimoConnector = (GerConnectorType) module.getVendorDD();
        GerResourceadapterType[] geronimoResourceAdapters = geronimoConnector.getResourceadapterArray();
        for (int k = 0; k < geronimoResourceAdapters.length; k++) {
            GerResourceadapterType geronimoResourceAdapter = geronimoResourceAdapters[k];

            if (resourceadapter.isSetResourceadapterClass()) {
                // set the resource adapter class and activationSpec info map
                try {
                    if (resourceadapter.isSetInboundResourceadapter() && resourceadapter.getInboundResourceadapter().isSetMessageadapter()) {
                        String resourceAdapterName = geronimoResourceAdapter.getResourceadapterInstance().getResourceadapterName();
                        ObjectName resourceAdapterObjectName = NameFactory.getComponentName(null, null, null, null, null, resourceAdapterName, NameFactory.JCA_RESOURCE_ADAPTER, resourceJ2eeContext);
                        GBeanData resourceAdapterData = new GBeanData(resourceAdapterObjectName, null);
                        earContext.addGBean(resourceAdapterData);
                    }
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct resource adapter instance", e);
                }
            }
            if (geronimoResourceAdapter.isSetOutboundResourceadapter()) {
                GerConnectionDefinitionType[] connectionDefinitions = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray();
                for (int i = 0; i < connectionDefinitions.length; i++) {
                    GerConnectionDefinitionType connectionDefinition = connectionDefinitions[i];
                    GerConnectiondefinitionInstanceType[] connectionDefinitionInstances = connectionDefinition.getConnectiondefinitionInstanceArray();
                    for (int j = 0; j < connectionDefinitionInstances.length; j++) {
                        GerConnectiondefinitionInstanceType connectionDefinitionInstance = connectionDefinitionInstances[j];
                        ObjectName connectionFactoryObjectName = null;
                        try {
                            connectionFactoryObjectName = NameFactory.getComponentName(null, null, null, null, null, connectionDefinitionInstance.getName(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, resourceJ2eeContext);
                        } catch (MalformedObjectNameException e) {
                            throw new DeploymentException("Could not construct resource object name", e);
                        }
                        GBeanData connectionFactoryData = new GBeanData(connectionFactoryObjectName, null);
                        earContext.addGBean(connectionFactoryData);
                    }
                }
            }
        }
        for (int i = 0; i < geronimoConnector.getAdminobjectArray().length; i++) {
            GerAdminobjectType gerAdminObject = geronimoConnector.getAdminobjectArray()[i];
            for (int j = 0; j < gerAdminObject.getAdminobjectInstanceArray().length; j++) {
                GerAdminobjectInstanceType gerAdminObjectInstance = gerAdminObject.getAdminobjectInstanceArray()[j];

                ObjectName adminObjectObjectName = null;
                try {
                    adminObjectObjectName = NameFactory.getComponentName(null, null, null, null, null, gerAdminObjectInstance.getMessageDestinationName(), NameFactory.JCA_ADMIN_OBJECT, resourceJ2eeContext);
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct resource object name", e);
                }
                GBeanData adminObjectData = new GBeanData(adminObjectObjectName, null);
                earContext.addGBean(adminObjectData);
            }
        }

    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = J2eeContextImpl.newModuleContextFromApplication(earJ2eeContext, NameFactory.RESOURCE_ADAPTER_MODULE, module.getName());
        J2eeContext resourceJ2eeContext = J2eeContextImpl.newModuleContextFromApplication(earJ2eeContext, NameFactory.JCA_RESOURCE, module.getName());

        XmlObject specDD = module.getSpecDD();

        // build the objectName
        ObjectName resourceAdapterModuleName = null;
        try {
            resourceAdapterModuleName = NameFactory.getModuleName(null, null, null, null, null, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct module name", e);
        }
        GBeanData resourceAdapterModuleData = null;
        try {
            resourceAdapterModuleData = earContext.getGBeanInstance(resourceAdapterModuleName);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Internal consistency bug: Could not retrieve gbean data for module: " + resourceAdapterModuleName);
        }
        if (resourceAdapterModuleData == null) {
            throw new DeploymentException("Internal consistency bug: gbean data for module is missing: " + resourceAdapterModuleName);
        }
        ObjectName resourceAdapterjsr77Name = null;
        try {
            resourceAdapterjsr77Name = NameFactory.getComponentName(null, null, null, null, null, resourceJ2eeContext.getJ2eeModuleName(), NameFactory.RESOURCE_ADAPTER, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct resource adapter placeholder name", e);
        }
        resourceAdapterModuleData.setAttribute("resourceAdapter", resourceAdapterjsr77Name.getCanonicalName());

        // add it
        earContext.addGBean(resourceAdapterModuleData);

        //construct the bogus resource adapter and jca resource placeholders
        GBeanData resourceAdapterData = new GBeanData(resourceAdapterjsr77Name, ResourceAdapterImplGBean.GBEAN_INFO);
        ObjectName jcaResourcejsr77Name = null;
        try {
            //TODO double check the module type is correct.
            jcaResourcejsr77Name = NameFactory.getComponentName(null, null, null, NameFactory.RESOURCE_ADAPTER, null, resourceJ2eeContext.getJ2eeModuleName(), NameFactory.JCA_RESOURCE, moduleJ2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct jca resource placeholder name", e);
        }
        resourceAdapterData.setAttribute("JCAResource", jcaResourcejsr77Name.getCanonicalName());
        earContext.addGBean(resourceAdapterData);

        GBeanData jcaResourceData = new GBeanData(jcaResourcejsr77Name, JCAResourceImplGBean.GBEAN_INFO);
        earContext.addGBean(jcaResourceData);

        GerConnectorType geronimoConnector = (GerConnectorType) module.getVendorDD();

        GbeanType[] gbeans = geronimoConnector.getGbeanArray();
        ServiceConfigBuilder.addGBeans(gbeans, cl, moduleJ2eeContext, earContext);

        addConnectorGBeans(earContext, resourceJ2eeContext, resourceAdapterModuleData, (ConnectorType) specDD, geronimoConnector, cl);
    }

    private void addConnectorGBeans(EARContext earContext, J2eeContext moduleJ2eeContext, GBeanData resourceAdapterModuleData, ConnectorType connector, GerConnectorType geronimoConnector, ClassLoader cl) throws DeploymentException {
        ResourceadapterType resourceadapter = connector.getResourceadapter();

        GerResourceadapterType[] geronimoResourceAdapters = geronimoConnector.getResourceadapterArray();
        for (int k = 0; k < geronimoResourceAdapters.length; k++) {
            GerResourceadapterType geronimoResourceAdapter = geronimoResourceAdapters[k];

            // Resource Adapter
            ObjectName resourceAdapterObjectName = null;
            if (resourceadapter.isSetResourceadapterClass()) {
                GBeanData resourceAdapterGBeanData = locateResourceAdapterGBeanData(resourceAdapterModuleData);
                GBeanData resourceAdapterInstanceGBeanData = new GBeanData(resourceAdapterGBeanData);

                setDynamicGBeanDataAttributes(resourceAdapterInstanceGBeanData, geronimoResourceAdapter.getResourceadapterInstance().getConfigPropertySettingArray(), cl);

                // set the work manager name
                ObjectName workManagerName = ENCConfigBuilder.getGBeanId(NameFactory.JCA_WORK_MANAGER, geronimoResourceAdapter.getResourceadapterInstance().getWorkmanager(), moduleJ2eeContext, earContext, kernel);
                resourceAdapterInstanceGBeanData.setReferencePattern("WorkManager", workManagerName);

                String resourceAdapterName = geronimoResourceAdapter.getResourceadapterInstance().getResourceadapterName();
                try {
                    resourceAdapterObjectName = NameFactory.getComponentName(null, null, null, null, null, resourceAdapterName, NameFactory.JCA_RESOURCE_ADAPTER, moduleJ2eeContext);
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct resource adapter object name", e);
                }
                resourceAdapterInstanceGBeanData.setName(resourceAdapterObjectName);
                earContext.addGBean(resourceAdapterInstanceGBeanData);
            }

            // Outbound Managed Connection Factories (think JDBC data source or JMS connection factory)

            // ManagedConnectionFactory setup
            if (geronimoResourceAdapter.isSetOutboundResourceadapter()) {
                if (!resourceadapter.isSetOutboundResourceadapter()) {
                    throw new DeploymentException("Geronimo plan configures an outbound resource adapter but ra.xml does not describe any");
                }
                String transactionSupport = resourceadapter.getOutboundResourceadapter().getTransactionSupport().getStringValue().trim();
                for (int i = 0; i < geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; i++) {
                    GerConnectionDefinitionType geronimoConnectionDefinition = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray(i);
                    assert geronimoConnectionDefinition != null: "Null GeronimoConnectionDefinition";

                    String connectionFactoryInterfaceName = geronimoConnectionDefinition.getConnectionfactoryInterface().trim();
                    GBeanData connectionFactoryGBeanData = locateConnectionFactoryInfo(resourceAdapterModuleData, connectionFactoryInterfaceName);

                    if (connectionFactoryGBeanData == null) {
                        throw new DeploymentException("No connection definition for ConnectionFactory class: " + connectionFactoryInterfaceName);
                    }

                    for (int j = 0; j < geronimoConnectionDefinition.getConnectiondefinitionInstanceArray().length; j++) {
                        GerConnectiondefinitionInstanceType connectionfactoryInstance = geronimoConnectionDefinition.getConnectiondefinitionInstanceArray()[j];

                        addOutboundGBeans(earContext, moduleJ2eeContext, resourceAdapterObjectName, connectionFactoryGBeanData, connectionfactoryInstance, transactionSupport, cl);
                    }
                }
            }
        }
        // admin objects (think message queues and topics)

        // add configured admin objects
        for (int i = 0; i < geronimoConnector.getAdminobjectArray().length; i++) {
            GerAdminobjectType gerAdminObject = geronimoConnector.getAdminobjectArray()[i];

            String adminObjectInterface = gerAdminObject.getAdminobjectInterface().trim();
            GBeanData adminObjectGBeanData = locateAdminObjectInfo(resourceAdapterModuleData, adminObjectInterface);

            if (adminObjectGBeanData == null) {
                throw new DeploymentException("No admin object declared for interface: " + adminObjectInterface);
            }

            for (int j = 0; j < gerAdminObject.getAdminobjectInstanceArray().length; j++) {
                GBeanData adminObjectInstanceGBeanData = new GBeanData(adminObjectGBeanData);
                GerAdminobjectInstanceType gerAdminObjectInstance = gerAdminObject.getAdminobjectInstanceArray()[j];
                setDynamicGBeanDataAttributes(adminObjectInstanceGBeanData, gerAdminObjectInstance.getConfigPropertySettingArray(), cl);
                // add it
                ObjectName adminObjectObjectName = null;
                try {
                    adminObjectObjectName = NameFactory.getComponentName(null, null, null, null, null, gerAdminObjectInstance.getMessageDestinationName(), NameFactory.JCA_ADMIN_OBJECT, moduleJ2eeContext);
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct admin object object name", e);
                }
                adminObjectInstanceGBeanData.setName(adminObjectObjectName);
                earContext.addGBean(adminObjectInstanceGBeanData);
            }
        }
    }

    private Map getActivationSpecInfoMap(MessagelistenerType[] messagelistenerArray, ClassLoader cl) throws DeploymentException {
        Map activationSpecInfos = new HashMap();
        for (int i = 0; i < messagelistenerArray.length; i++) {
            MessagelistenerType messagelistenerType = messagelistenerArray[i];
            String messageListenerInterface = messagelistenerType.getMessagelistenerType().getStringValue().trim();
            ActivationspecType activationspec = messagelistenerType.getActivationspec();
            String activationSpecClassName = activationspec.getActivationspecClass().getStringValue().trim();
            GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ActivationSpecWrapperGBean.class, ActivationSpecWrapperGBean.GBEAN_INFO);

            //add all javabean properties that have both getter and setter.  Ignore the "required" flag from the dd.
            Map getters = new HashMap();
            Set setters = new HashSet();
            Method[] methods = null;
            try {
                Class activationSpecClass = cl.loadClass(activationSpecClassName);
                methods = activationSpecClass.getMethods();
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Can not load activation spec class", e);
            }
            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                String methodName = method.getName();
                if ((methodName.startsWith("get") || methodName.startsWith("is")) && method.getParameterTypes().length == 0) {
                    String attributeName = (methodName.startsWith("get")) ? methodName.substring(3) : methodName.substring(2);
                    getters.put(Introspector.decapitalize(attributeName), method.getReturnType().getName());
                } else if (methodName.startsWith("set") && method.getParameterTypes().length == 1) {
                    setters.add(Introspector.decapitalize(methodName.substring(3)));
                }
            }
            getters.keySet().retainAll(setters);
            getters.remove("resourceAdapter");

            for (Iterator iterator = getters.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                infoBuilder.addAttribute(new DynamicGAttributeInfo((String) entry.getKey(), (String) entry.getValue(), true, true, true));
            }

            GBeanInfo gbeanInfo = infoBuilder.getBeanInfo();
            try {
                //make sure the class is available, but we don't use it.
                cl.loadClass(activationSpecClassName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load ActivationSpec class", e);
            }
            GBeanData activationSpecInfo = new GBeanData(gbeanInfo);
            activationSpecInfo.setAttribute("activationSpecClass", activationSpecClassName);
            activationSpecInfos.put(messageListenerInterface, activationSpecInfo);
        }
        return activationSpecInfos;
    }

    private Map getManagedConnectionFactoryInfoMap(ConnectionDefinitionType[] connectionDefinitionArray, ClassLoader cl) throws DeploymentException {
        Map managedConnectionFactoryInfos = new HashMap();
        for (int i = 0; i < connectionDefinitionArray.length; i++) {
            ConnectionDefinitionType connectionDefinition = connectionDefinitionArray[i];

            GBeanInfoBuilder managedConnectionFactoryInfoBuilder = new GBeanInfoBuilder(ManagedConnectionFactoryWrapper.class, ManagedConnectionFactoryWrapperGBean.GBEAN_INFO);
            GBeanData managedConnectionFactoryGBeanData = setUpDynamicGBean(managedConnectionFactoryInfoBuilder, connectionDefinition.getConfigPropertyArray(), cl);

            // set the standard properties
            String connectionfactoryInterface = connectionDefinition.getConnectionfactoryInterface().getStringValue().trim();
            managedConnectionFactoryGBeanData.setAttribute("managedConnectionFactoryClass", connectionDefinition.getManagedconnectionfactoryClass().getStringValue().trim());
            managedConnectionFactoryGBeanData.setAttribute("connectionFactoryInterface", connectionfactoryInterface);
            managedConnectionFactoryGBeanData.setAttribute("connectionFactoryImplClass", connectionDefinition.getConnectionfactoryImplClass().getStringValue().trim());
            managedConnectionFactoryGBeanData.setAttribute("connectionInterface", connectionDefinition.getConnectionInterface().getStringValue().trim());
            managedConnectionFactoryGBeanData.setAttribute("connectionImplClass", connectionDefinition.getConnectionImplClass().getStringValue().trim());
            managedConnectionFactoryInfos.put(connectionfactoryInterface, managedConnectionFactoryGBeanData);
        }
        return managedConnectionFactoryInfos;
    }

    private Map getAdminObjectInfoMap(AdminobjectType[] adminobjectArray, ClassLoader cl) throws DeploymentException {
        Map adminObjectInfos = new HashMap();
        for (int i = 0; i < adminobjectArray.length; i++) {
            AdminobjectType adminObject = adminobjectArray[i];

            GBeanInfoBuilder adminObjectInfoBuilder = new GBeanInfoBuilder(AdminObjectWrapper.class, AdminObjectWrapperGBean.GBEAN_INFO);
            GBeanData adminObjectGBeanData = setUpDynamicGBean(adminObjectInfoBuilder, adminObject.getConfigPropertyArray(), cl);

            // set the standard properties
            String adminObjectInterface = adminObject.getAdminobjectInterface().getStringValue().trim();
            adminObjectGBeanData.setAttribute("adminObjectInterface", adminObjectInterface);
            adminObjectGBeanData.setAttribute("adminObjectClass", adminObject.getAdminobjectClass().getStringValue().trim());
            adminObjectInfos.put(adminObjectInterface, adminObjectGBeanData);
        }
        return adminObjectInfos;
    }


    private GBeanData setUpDynamicGBean(GBeanInfoBuilder infoBuilder, ConfigPropertyType[] configProperties, ClassLoader cl) throws DeploymentException {
        for (int i = 0; i < configProperties.length; i++) {
            infoBuilder.addAttribute(new DynamicGAttributeInfo(configProperties[i].getConfigPropertyName().getStringValue().trim(), configProperties[i].getConfigPropertyType().getStringValue().trim(), true, true, true));
        }

        GBeanInfo gbeanInfo = infoBuilder.getBeanInfo();
        GBeanData gbeanData = new GBeanData(gbeanInfo);
        for (int i = 0; i < configProperties.length; i++) {
            if (configProperties[i].isSetConfigPropertyValue()) {
                gbeanData.setAttribute(configProperties[i].getConfigPropertyName().getStringValue(),
                        getValue(configProperties[i].getConfigPropertyType().getStringValue(),
                                configProperties[i].getConfigPropertyValue().getStringValue(),
                                cl));
            }
        }
        return gbeanData;
    }

    private void setDynamicGBeanDataAttributes(GBeanData gbeanData, GerConfigPropertySettingType[] configProperties, ClassLoader cl) throws DeploymentException {

        try {
            for (int i = 0; i < configProperties.length; i++) {
                String name = configProperties[i].getName();
                GAttributeInfo attributeInfo = gbeanData.getGBeanInfo().getAttribute(name);
                if (attributeInfo == null) {
                    throw new DeploymentException("The plan is trying to set attribute: " + name + " which does not exist.  Known attributs are: " + gbeanData.getGBeanInfo().getAttributes());
                }
                String type = attributeInfo.getType();
                gbeanData.setAttribute(name,
                        getValue(type, configProperties[i].getStringValue().trim(), cl));
            }
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private Object getValue(String type, String value, ClassLoader cl) throws DeploymentException {
        if (value == null) {
            return null;
        }

        Class clazz;
        try {
            clazz = cl.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load attribute class:  type: " + type, e);
        }

        PropertyEditor editor = PropertyEditors.getEditor(clazz);
        editor.setAsText(value);
        return editor.getValue();
    }

    private ObjectName configureConnectionManager(EARContext earContext, J2eeContext j2eeContext, String ddTransactionSupport, GerConnectiondefinitionInstanceType connectionfactoryInstance, ClassLoader cl) throws DeploymentException {
        if (connectionfactoryInstance.getConnectionmanagerRef() != null) {
            //we don't configure anything, just use the supplied gbean
            try {
                return ObjectName.getInstance(connectionfactoryInstance.getConnectionmanagerRef());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid ObjectName string supplied for ConnectionManager reference", e);
            }
        }

        // create the object name for our connection manager
        ObjectName connectionManagerObjectName = null;
        try {
            connectionManagerObjectName = NameFactory.getComponentName(null, null, null, null, null, connectionfactoryInstance.getName(), NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct connection manager object name", e);
        }

        // create the data holder for our connection manager
        GBeanInfo gbeanInfo;
        try {
            gbeanInfo = GBeanInfo.getGBeanInfo("org.apache.geronimo.connector.outbound.GenericConnectionManagerGBean", cl);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to create GMBean", e);
        }
        GBeanData connectionManagerGBean = new GBeanData(connectionManagerObjectName, gbeanInfo);

        //we configure our connection manager
        GerConnectionmanagerType connectionManager = connectionfactoryInstance.getConnectionmanager();
        TransactionSupport transactionSupport = null;
        if (connectionManager.isSetNoTransaction()) {
            transactionSupport = NoTransactions.INSTANCE;
        } else if (connectionManager.isSetLocalTransaction()) {
            transactionSupport = LocalTransactions.INSTANCE;
        } else if (connectionManager.isSetTransactionLog()) {
            transactionSupport = TransactionLog.INSTANCE;
        } else if (connectionManager.isSetXaTransaction()) {
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
            throw new DeploymentException("Unexpected transaction support element");
        }
        PoolingSupport pooling = null;
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
            throw new DeploymentException("Unexpected pooling support element");
        }
        try {
            connectionManagerGBean.setAttribute("transactionSupport", transactionSupport);
            connectionManagerGBean.setAttribute("pooling", pooling);
            connectionManagerGBean.setReferencePattern("ConnectionTracker", earContext.getConnectionTrackerObjectName());
            connectionManagerGBean.setAttribute("containerManagedSecurity", new Boolean(connectionManager.isSetContainerManagedSecurity()));
            connectionManagerGBean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
        } catch (Exception e) {
            throw new DeploymentException("Problem setting up ConnectionManager", e);
        }

        earContext.addGBean(connectionManagerGBean);
        return connectionManagerObjectName;
    }

    private void addOutboundGBeans(EARContext earContext,
                                   J2eeContext j2eeContext,
                                   ObjectName resourceAdapterObjectName,
                                   GBeanData managedConnectionFactoryPrototypeGBeanData,
                                   GerConnectiondefinitionInstanceType connectiondefinitionInstance,
                                   String transactionSupport,
                                   ClassLoader cl) throws DeploymentException {
        GBeanData managedConnectionFactoryInstanceGBeanData = new GBeanData(managedConnectionFactoryPrototypeGBeanData);
        // ConnectionManager
        ObjectName connectionManagerObjectName = configureConnectionManager(earContext, j2eeContext, transactionSupport, connectiondefinitionInstance, cl);

        // ManagedConnectionFactory
        setDynamicGBeanDataAttributes(managedConnectionFactoryInstanceGBeanData, connectiondefinitionInstance.getConfigPropertySettingArray(), cl);
        try {
            if (connectiondefinitionInstance.isSetGlobalJndiName()) {
                managedConnectionFactoryInstanceGBeanData.setAttribute("globalJNDIName", connectiondefinitionInstance.getGlobalJndiName().trim());
            }
            if (resourceAdapterObjectName != null) {
                managedConnectionFactoryInstanceGBeanData.setReferencePattern("ResourceAdapterWrapper", resourceAdapterObjectName);
            }
            managedConnectionFactoryInstanceGBeanData.setReferencePattern("ConnectionManagerContainer", connectionManagerObjectName);
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

        ObjectName managedConnectionFactoryObjectName = null;
        try {
            managedConnectionFactoryObjectName = NameFactory.getComponentName(null, null, null, null, null, connectiondefinitionInstance.getName(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct managed connection factory object name", e);
        }
        managedConnectionFactoryInstanceGBeanData.setName(managedConnectionFactoryObjectName);
        earContext.addGBean(managedConnectionFactoryInstanceGBeanData);

        // ConnectionFactory
        ObjectName connectionFactoryObjectName = null;
        try {
            connectionFactoryObjectName = NameFactory.getComponentName(null, null, null, null, null, connectiondefinitionInstance.getName(), NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct connection factory object name", e);
        }
        GBeanData connectionFactoryGBeanData = new GBeanData(connectionFactoryObjectName, JCAConnectionFactoryImplGBean.GBEAN_INFO);
        connectionFactoryGBeanData.setReferencePattern("J2EEServer", earContext.getServerObjectName());
        connectionFactoryGBeanData.setAttribute("managedConnectionFactory", managedConnectionFactoryObjectName.getCanonicalName());

        earContext.addGBean(connectionFactoryGBeanData);
    }

    //ResourceReferenceBuilder implementation
    public Reference createResourceRef(String containerId, Class iface) throws DeploymentException {
        return new ResourceReference(containerId, iface);
    }

    public Reference createAdminObjectRef(String containerId, Class iface) throws DeploymentException {
        return new ResourceReference(containerId, iface);
    }

    public GBeanData locateActivationSpecInfo(GBeanData resourceAdapterModuleData, String messageListenerInterface) throws DeploymentException {
        Map activationSpecInfos = (Map) resourceAdapterModuleData.getAttribute("activationSpecInfoMap");
        if (activationSpecInfos == null) {
            throw new DeploymentException("No activation spec info map found in resource adapter module: " + resourceAdapterModuleData.getName());
        }
        return (GBeanData) activationSpecInfos.get(messageListenerInterface);
    }

    public GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData) throws DeploymentException {
        GBeanData data = (GBeanData) resourceAdapterModuleData.getAttribute("resourceAdapterGBeanData");
        if (data == null) {
            throw new DeploymentException("No resource adapter info found for resource adapter module: " + resourceAdapterModuleData.getName());
        }
        return data;
    }

    public GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName) throws DeploymentException {
        Map adminObjectInfos = (Map) resourceAdapterModuleData.getAttribute("adminObjectInfoMap");
        if (adminObjectInfos == null) {
            throw new DeploymentException("No admin object infos found for resource adapter module: " + resourceAdapterModuleData.getName());
        }
        return (GBeanData) adminObjectInfos.get(adminObjectInterfaceName);
    }

    public GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) throws DeploymentException {
        Map managedConnectionFactoryInfos = (Map) resourceAdapterModuleData.getAttribute("managedConnectionFactoryInfoMap");
        if (managedConnectionFactoryInfos == null) {
            throw new DeploymentException("No managed connection factory infos found for resource adapter module: " + resourceAdapterModuleData.getName());
        }
        return (GBeanData) managedConnectionFactoryInfos.get(connectionFactoryInterfaceName);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(ConnectorModuleBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.addAttribute("defaultParentId", URI.class, true);
        infoBuilder.addAttribute("defaultMaxSize", int.class, true);
        infoBuilder.addAttribute("defaultMinSize", int.class, true);
        infoBuilder.addAttribute("defaultBlockingTimeoutMilliseconds", int.class, true);
        infoBuilder.addAttribute("defaultIdleTimeoutMinutes", int.class, true);
        infoBuilder.addAttribute("defaultXATransactionCaching", boolean.class, true);
        infoBuilder.addAttribute("defaultXAThreadCaching", boolean.class, true);

        infoBuilder.addReference("Repository", Repository.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.addInterface(ModuleBuilder.class);
        infoBuilder.addInterface(ResourceReferenceBuilder.class);

        infoBuilder.setConstructor(new String[]{"defaultParentId",
                                                "defaultMaxSize",
                                                "defaultMinSize",
                                                "defaultBlockingTimeoutMilliseconds",
                                                "defaultIdleTimeoutMinutes",
                                                "defaultXATransactionCaching",
                                                "defaultXAThreadCaching",
                                                "Repository",
                                                "kernel"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo
            () {
        return GBEAN_INFO;
    }
}
