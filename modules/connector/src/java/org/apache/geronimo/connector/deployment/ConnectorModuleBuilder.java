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
package org.apache.geronimo.connector.deployment;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.AttributeNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Reference;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.ActivationSpecInfo;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionLog;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.connector.outbound.security.PasswordCredentialRealm;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanHelper;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.ConnectorModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilder;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.deployment.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.deployment.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.jmx.JMXObjectFactory;
import org.apache.geronimo.naming.jmx.JMXRefAddr;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.geronimo.GerDependencyType;
import org.apache.geronimo.xbeans.geronimo.GerGbeanType;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.j2ee.ActivationspecType;
import org.apache.geronimo.xbeans.j2ee.AdminobjectType;
import org.apache.geronimo.xbeans.j2ee.ConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.ConnectionDefinitionType;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.geronimo.xbeans.j2ee.ConnectorType;
import org.apache.geronimo.xbeans.j2ee.FullyQualifiedClassType;
import org.apache.geronimo.xbeans.j2ee.MessagelistenerType;
import org.apache.geronimo.xbeans.j2ee.ResourceadapterType;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ConfigPropertyType10;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ConnectorDocument10;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ConnectorType10;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ResourceadapterType10;
import org.apache.geronimo.kernel.Kernel;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class ConnectorModuleBuilder implements ModuleBuilder, ResourceReferenceBuilder {
    private static final String BASE_REALM_BRIDGE_NAME = "geronimo.security:service=RealmBridge,name=";
    private static final String BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME = "geronimo.security:service=Realm,type=PasswordCredential,name=";

    private final Kernel kernel;

    public ConnectorModuleBuilder(Kernel kernel) {
        this.kernel = kernel;
    }

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return createModule(plan, moduleFile, "rar", null, true);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId) throws DeploymentException {
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

            // parse it
            try {
                // try 1.0
                ConnectorDocument10 connectorDoc = ConnectorDocument10.Factory.parse(specDD);
                SchemaConversionUtils.validateDD(connectorDoc);
                connector = connectorDoc.getConnector();
            } catch (Exception ignore) {
                // that didn't work try 1.5
                ConnectorDocument connectorDoc = ConnectorDocument.Factory.parse(specDD);
                SchemaConversionUtils.validateDD(connectorDoc);
                connector = connectorDoc.getConnector();
            }
        } catch (Exception e) {
            return null;
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
                throw new DeploymentException("A connector module must contain a geronimo-ra.xml");
            }
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
            GerDependencyType[] dependencies = vendorConnector.getDependencyArray();
            for (int i = 0; i < dependencies.length; i++) {
                earContext.addDependency(getDependencyURI(dependencies[i]));
            }
        } catch (IOException e) {
            throw new DeploymentException("Problem deploying connector", e);
        }
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = new J2eeContextImpl(earJ2eeContext.getJ2eeDomainName(), earJ2eeContext.getJ2eeServerName(), earJ2eeContext.getJ2eeApplicationName(), module.getName(), null, null);
        XmlObject specDD = module.getSpecDD();
        GerConnectorType geronimoConnector = (GerConnectorType) module.getVendorDD();
        GerResourceadapterType[] geronimoResourceAdapters = geronimoConnector.getResourceadapterArray();
        for (int k = 0; k < geronimoResourceAdapters.length; k++) {
            GerResourceadapterType geronimoResourceAdapter = geronimoResourceAdapters[k];
            if (specDD instanceof ConnectorType) {
                ResourceadapterType resourceadapter = ((ConnectorType) specDD).getResourceadapter();

                if (resourceadapter.isSetResourceadapterClass()) {
                    // set the resource adapter class and activationSpec info map
                    try {
                        if (resourceadapter.isSetInboundResourceadapter() && resourceadapter.getInboundResourceadapter().isSetMessageadapter()) {
                            String resourceAdapterName = geronimoResourceAdapter.getResourceadapterInstance().getResourceadapterName();
                            ObjectName resourceAdapterObjectName = NameFactory.getResourceComponentName(null, null, null, null, resourceAdapterName, NameFactory.JCA_RESOURCE_ADAPTER, moduleJ2eeContext);
                            //get the ActivationSpec metadata as GBeanInfos
                            Map activationSpecInfoMap = getActivationSpecInfoMap(resourceadapter.getInboundResourceadapter().getMessageadapter().getMessagelistenerArray(), cl);

                            String containerId = resourceAdapterObjectName.getCanonicalName();
                            earContext.getRefContext().addResourceAdapterId(module.getModuleURI(), resourceAdapterName, containerId);
                            earContext.getRefContext().addActivationSpecInfos(resourceAdapterObjectName, activationSpecInfoMap);
                        }
                    } catch (Exception e) {
                        throw new DeploymentException("Could not set ResourceAdapterClass", e);
                    }
                }
            }
            if (geronimoResourceAdapter.isSetOutboundResourceadapter()) {
                GerConnectionDefinitionType[] connectionDefinitions = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray();
                for (int i = 0; i < connectionDefinitions.length; i++) {
                    GerConnectionDefinitionType connectionDefinition = connectionDefinitions[i];
                    GerConnectiondefinitionInstanceType[] connectionDefinitionInstances = connectionDefinition.getConnectiondefinitionInstanceArray();
                    for (int j = 0; j < connectionDefinitionInstances.length; j++) {
                        GerConnectiondefinitionInstanceType connectionDefinitionInstance = connectionDefinitionInstances[j];
                        String containerId = NameFactory.getResourceComponentNameString(null, null, null, null, connectionDefinitionInstance.getName(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, moduleJ2eeContext);
                        earContext.getRefContext().addConnectionFactoryId(module.getModuleURI(), connectionDefinitionInstance.getName(), containerId);
                    }
                }
            }
        }
        for (int i = 0; i < geronimoConnector.getAdminobjectArray().length; i++) {
            GerAdminobjectType gerAdminObject = geronimoConnector.getAdminobjectArray()[i];
            for (int j = 0; j < gerAdminObject.getAdminobjectInstanceArray().length; j++) {
                GerAdminobjectInstanceType gerAdminObjectInstance = gerAdminObject.getAdminobjectInstanceArray()[j];

                String adminObjectObjectName = NameFactory.getResourceComponentNameString(null, null, null, null, gerAdminObjectInstance.getMessageDestinationName(), NameFactory.JCA_ADMIN_OBJECT, moduleJ2eeContext);
                earContext.getRefContext().addAdminObjectId(module.getModuleURI(), gerAdminObjectInstance.getMessageDestinationName(), adminObjectObjectName);
            }
        }

    }

    public String addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        J2eeContext earJ2eeContext = earContext.getJ2eeContext();
        J2eeContext moduleJ2eeContext = new J2eeContextImpl(earJ2eeContext.getJ2eeDomainName(), earJ2eeContext.getJ2eeServerName(), earJ2eeContext.getJ2eeApplicationName(), module.getName(), null, null);

        addResourceAdapterModuleGBean(earContext, moduleJ2eeContext, module.getOriginalSpecDD(), cl);

        GerConnectorType geronimoConnector = (GerConnectorType) module.getVendorDD();

        GerGbeanType[] gbeans = geronimoConnector.getGbeanArray();
        for (int i = 0; i < gbeans.length; i++) {
            GBeanHelper.addGbean(new RARGBeanAdapter(gbeans[i]), cl, earContext);
        }

        XmlObject specDD = module.getSpecDD();
        if (specDD instanceof ConnectorType10) {
            addConnectorGBeans(earContext, moduleJ2eeContext, (ConnectorType10) specDD, geronimoConnector, cl);
        } else {
            addConnectorGBeans(earContext, moduleJ2eeContext, (ConnectorType) specDD, geronimoConnector, cl);
        }

        return null;
    }

    private ObjectName addResourceAdapterModuleGBean(EARContext earContext, J2eeContext moduleJ2eeContext, String originalSpecDD, ClassLoader cl) throws DeploymentException {
        // build the objectName
        ObjectName resourceAdapterModuleName = NameFactory.getModuleName(null, null, null, null, NameFactory.RESOURCE_ADAPTER_MODULE, moduleJ2eeContext);

        // initalize the GBean
        GBeanMBean resourceAdapterModule = new GBeanMBean(ResourceAdapterModuleImpl.GBEAN_INFO, cl);
        resourceAdapterModule.setReferencePattern(NameFactory.J2EE_SERVER, earContext.getServerObjectName());
        if (!earContext.getJ2EEApplicationName().equals(NameFactory.NULL)) {
            resourceAdapterModule.setReferencePattern(NameFactory.J2EE_APPLICATION, earContext.getApplicationObjectName());
        }

        try {
            resourceAdapterModule.setAttribute("deploymentDescriptor", originalSpecDD);
        } catch (Exception e) {
            throw new DeploymentException("Unable to initialize EJBModule GBean", e);
        }

        // add it
        earContext.addGBean(resourceAdapterModuleName, resourceAdapterModule);

        return resourceAdapterModuleName;
    }

    private void addConnectorGBeans(EARContext earContext, J2eeContext j2eeContext, ConnectorType10 connector, GerConnectorType geronimoConnector, ClassLoader cl) throws DeploymentException {
        ResourceadapterType10 resourceAdapter = connector.getResourceadapter();
        GerResourceadapterType[] geronimoResourceAdapters = geronimoConnector.getResourceadapterArray();
        for (int k = 0; k < geronimoResourceAdapters.length; k++) {
            GerResourceadapterType geronimoResourceAdapter = geronimoResourceAdapters[k];

            for (int i = 0; i < geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; i++) {
                GerConnectionDefinitionType geronimoConnectionDefinition = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray(i);
                assert geronimoConnectionDefinition != null: "Null GeronimoConnectionDefinition";

                for (int j = 0; j < geronimoConnectionDefinition.getConnectiondefinitionInstanceArray().length; j++) {
                    GerConnectiondefinitionInstanceType connectionfactoryInstance = geronimoConnectionDefinition.getConnectiondefinitionInstanceArray()[j];
                    String managedConnectionFactoryClass = resourceAdapter.getManagedconnectionfactoryClass().getStringValue();
                    String connectionFactoryInterface = resourceAdapter.getConnectionfactoryInterface().getStringValue();
                    String connectionFactoryImplClass = resourceAdapter.getConnectionfactoryImplClass().getStringValue();
                    String connectionInterface = resourceAdapter.getConnectionInterface().getStringValue();
                    String connectionImplClass = resourceAdapter.getConnectionImplClass().getStringValue();
                    ConfigProperty[] configProperties = getConfigProperties(resourceAdapter.getConfigPropertyArray(), connectionfactoryInstance.getConfigPropertySettingArray());

                    addOutboundGBeans(earContext, j2eeContext, null, connectionfactoryInstance, configProperties, managedConnectionFactoryClass, connectionFactoryInterface, connectionFactoryImplClass, connectionInterface, connectionImplClass, cl);
                }
            }
        }
    }

    private void addConnectorGBeans(EARContext earContext, J2eeContext moduleJ2eeContext, ConnectorType connector, GerConnectorType geronimoConnector, ClassLoader cl) throws DeploymentException {
        ResourceadapterType resourceadapter = connector.getResourceadapter();
        GerResourceadapterType[] geronimoResourceAdapters = geronimoConnector.getResourceadapterArray();
        for (int k = 0; k < geronimoResourceAdapters.length; k++) {
            GerResourceadapterType geronimoResourceAdapter = geronimoResourceAdapters[k];

            //
            // Resource Adapter
            //

            ObjectName resourceAdapterObjectName = null;
            if (resourceadapter.isSetResourceadapterClass()) {
                String resourceAdapterName = geronimoResourceAdapter.getResourceadapterInstance().getResourceadapterName();

                // Create the resource adapter gbean
                GBeanInfoBuilder resourceAdapterInfoFactory = new GBeanInfoBuilder("org.apache.geronimo.connector.ResourceAdapterWrapper", cl);
                ConfigProperty[] configProperties = getConfigProperties(resourceadapter.getConfigPropertyArray(), geronimoResourceAdapter.getResourceadapterInstance().getConfigPropertySettingArray());
                GBeanMBean resourceAdapterGBean = setUpDynamicGBean(resourceAdapterInfoFactory, configProperties, cl);

                // set the resource adapter class and activationSpec info map
                try {
                    resourceAdapterGBean.setAttribute("resourceAdapterClass", cl.loadClass(resourceadapter.getResourceadapterClass().getStringValue()));
                    if (resourceadapter.isSetInboundResourceadapter() && resourceadapter.getInboundResourceadapter().isSetMessageadapter()) {
                        //get the ActivationSpec metadata as GBeanInfos
                        Map activationSpecInfoMap = getActivationSpecInfoMap(resourceadapter.getInboundResourceadapter().getMessageadapter().getMessagelistenerArray(), cl);
                        resourceAdapterGBean.setAttribute("activationSpecInfoMap", activationSpecInfoMap);
                    }
                } catch (Exception e) {
                    throw new DeploymentException("Could not set ResourceAdapterClass", e);
                }

                // set the work manager name
                ObjectName workManagerName = NameFactory.getComponentName(null, null, geronimoResourceAdapter.getResourceadapterInstance().getWorkmanagerName().trim(), NameFactory.JCA_WORK_MANAGER, moduleJ2eeContext);
                resourceAdapterGBean.setReferencePattern("WorkManager", workManagerName);

                // add it
                resourceAdapterObjectName = NameFactory.getResourceComponentName(null, null, null, null, resourceAdapterName, NameFactory.JCA_RESOURCE_ADAPTER, moduleJ2eeContext);
                earContext.addGBean(resourceAdapterObjectName, resourceAdapterGBean);
            }

            //
            // Outbound Managed Connectopn Factories (think JDBC data source or JMS connection factory)
            //

            // first we need a map of the published outbound adaptors by connection factory interface type
            Map connectionDefinitions = new HashMap();
            if (resourceadapter.isSetOutboundResourceadapter()) {
                for (int j = 0; j < resourceadapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; j++) {
                    ConnectionDefinitionType connectionDefinition = resourceadapter.getOutboundResourceadapter().getConnectionDefinitionArray(j);
                    connectionDefinitions.put(connectionDefinition.getConnectionfactoryInterface().getStringValue(), connectionDefinition);
                }

                // ManagedConnectionFactory setup
                if (geronimoResourceAdapter.isSetOutboundResourceadapter()) {
                    for (int i = 0; i < geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; i++) {
                        GerConnectionDefinitionType geronimoConnectionDefinition = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray(i);
                        assert geronimoConnectionDefinition != null: "Null GeronimoConnectionDefinition";

                        String connectionFactoryInterfaceName = geronimoConnectionDefinition.getConnectionfactoryInterface().getStringValue();
                        ConnectionDefinitionType connectionDefinition = (ConnectionDefinitionType) connectionDefinitions.get(connectionFactoryInterfaceName);
                        if (connectionDefinition == null) {
                            throw new DeploymentException("No connection definition for ConnectionFactory class: " + connectionFactoryInterfaceName);
                        }

                        for (int j = 0; j < geronimoConnectionDefinition.getConnectiondefinitionInstanceArray().length; j++) {
                            GerConnectiondefinitionInstanceType connectionfactoryInstance = geronimoConnectionDefinition.getConnectiondefinitionInstanceArray()[j];
                            String managedConnectionFactoryClass = connectionDefinition.getManagedconnectionfactoryClass().getStringValue();
                            String connectionFactoryInterface = connectionDefinition.getConnectionfactoryInterface().getStringValue();
                            String connectionFactoryImplClass = connectionDefinition.getConnectionfactoryImplClass().getStringValue();
                            String connectionInterface = connectionDefinition.getConnectionInterface().getStringValue();
                            String connectionImplClass = connectionDefinition.getConnectionImplClass().getStringValue();
                            ConfigProperty[] configProperties = getConfigProperties(connectionDefinition.getConfigPropertyArray(), connectionfactoryInstance.getConfigPropertySettingArray());

                            addOutboundGBeans(earContext, moduleJ2eeContext, resourceAdapterObjectName, connectionfactoryInstance, configProperties, managedConnectionFactoryClass, connectionFactoryInterface, connectionFactoryImplClass, connectionInterface, connectionImplClass, cl);
                        }
                    }
                }
            }


         }
        //
        // admin objects (think message queuse and topics)
        //

        // first we need a map of the published admin objects by interface type
        Map adminObjectInterfaceMap = new HashMap();
        for (int i = 0; i < resourceadapter.getAdminobjectArray().length; i++) {
            AdminobjectType adminobject = resourceadapter.getAdminobjectArray()[i];
            adminObjectInterfaceMap.put(adminobject.getAdminobjectInterface().getStringValue(), adminobject);
        }
        // add configured admin objects
        for (int i = 0; i < geronimoConnector.getAdminobjectArray().length; i++) {
             GerAdminobjectType gerAdminObject = geronimoConnector.getAdminobjectArray()[i];

             String adminObjectInterface = gerAdminObject.getAdminobjectInterface().getStringValue();
             AdminobjectType adminObject = (AdminobjectType) adminObjectInterfaceMap.get(adminObjectInterface);
             if (adminObject == null) {
                 throw new DeploymentException("No admin object declared for interface: " + adminObjectInterface);
             }

             for (int j = 0; j < gerAdminObject.getAdminobjectInstanceArray().length; j++) {
                 GerAdminobjectInstanceType gerAdminObjectInstance = gerAdminObject.getAdminobjectInstanceArray()[j];

                 // create the adminObjectGBean
                 GBeanInfoBuilder adminObjectInfoFactory = new GBeanInfoBuilder("org.apache.geronimo.connector.AdminObjectWrapper", cl);
                 ConfigProperty[] configProperties = getConfigProperties(adminObject.getConfigPropertyArray(), gerAdminObjectInstance.getConfigPropertySettingArray());
                 GBeanMBean adminObjectGBean = setUpDynamicGBean(adminObjectInfoFactory, configProperties, cl);

                 // set the standard properties
                 try {
                     adminObjectGBean.setAttribute("adminObjectInterface", cl.loadClass(adminObjectInterface));
                     adminObjectGBean.setAttribute("adminObjectClass", cl.loadClass(adminObject.getAdminobjectClass().getStringValue()));
                 } catch (Exception e) {
                     throw new DeploymentException("Could not initialize AdminObject", e);
                 }

                 // add it
                 ObjectName adminObjectObjectName = NameFactory.getResourceComponentName(null, null, null, null, gerAdminObjectInstance.getMessageDestinationName(), NameFactory.JCA_ADMIN_OBJECT, moduleJ2eeContext);
                 earContext.addGBean(adminObjectObjectName, adminObjectGBean);
             }
         }
    }

    private Map getActivationSpecInfoMap(MessagelistenerType[] messagelistenerArray, ClassLoader cl) throws DeploymentException {
        Map activationSpecInfos = new HashMap();
        for (int i = 0; i < messagelistenerArray.length; i++) {
            MessagelistenerType messagelistenerType = messagelistenerArray[i];
            String messageListenerInterface = messagelistenerType.getMessagelistenerType().getStringValue().trim();
            ActivationspecType activationspec = messagelistenerType.getActivationspec();
            String activationSpecClassName = activationspec.getActivationspecClass().getStringValue();
            GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("org.apache.geronimo.connector.ActivationSpecWrapper", cl);

            //add all javabean properties that have both getter and setter.  Ignore the "required" flag from the dd.
            BeanInfo beanInfo;
            try {
                Class activationSpecClass = cl.loadClass(activationSpecClassName);
                beanInfo = Introspector.getBeanInfo(activationSpecClass);
            } catch (IntrospectionException e) {
                throw new DeploymentException("Can not introspect activation spec class", e);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Can not load activation spec class", e);
            }

            PropertyDescriptor[] attDescriptors = beanInfo.getPropertyDescriptors();
            for (int j = 0; j < attDescriptors.length; j++) {
                PropertyDescriptor desc = attDescriptors[j];
                if (desc.getName().equals("resourceAdapter") || desc.getReadMethod() == null || desc.getWriteMethod() == null) {
                    continue;
                }
                infoFactory.addAttribute(new DynamicGAttributeInfo(desc.getName(), true));
            }

            GBeanInfo gbeanInfo = infoFactory.getBeanInfo();
            Class activationSpecClass = null;
            try {
                activationSpecClass = cl.loadClass(activationSpecClassName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load ActivationSpec class", e);
            }
            ActivationSpecInfo activationSpecInfo = new ActivationSpecInfo(activationSpecClassName, gbeanInfo);
            activationSpecInfos.put(messageListenerInterface, activationSpecInfo);
        }
        return activationSpecInfos;
    }

    private GBeanMBean setUpDynamicGBean(GBeanInfoBuilder infoFactory, ConfigProperty[] configProperties, ClassLoader cl) throws DeploymentException {
        for (int i = 0; i < configProperties.length; i++) {
            infoFactory.addAttribute(new DynamicGAttributeInfo(configProperties[i].getName(), true));
        }

        GBeanInfo gbeanInfo = infoFactory.getBeanInfo();
        GBeanMBean gbean;
        try {
            gbean = new GBeanMBean(gbeanInfo, cl);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to create GMBean", e);
        }

        try {
            for (int i = 0; i < configProperties.length; i++) {
                ConfigProperty configProperty = configProperties[i];

                setAttributeValue(gbean,
                        configProperty.getName(),
                        configProperty.getType(),
                        configProperty.getExplicitValue(),
                        configProperty.getDefaultValue());
            }
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return gbean;
    }

    private ConfigProperty[] getConfigProperties(ConfigPropertyType10[] configPropertyTypes, GerConfigPropertySettingType[] configPropertySettings) {
        Map explicitValues = getExplicitValuesMap(configPropertySettings);

        ConfigProperty[] configProperties = new ConfigProperty[configPropertyTypes.length];
        for (int i = 0; i < configPropertyTypes.length; i++) {
            ConfigPropertyType10 configPropertyType = configPropertyTypes[i];
            String name = configPropertyType.getConfigPropertyName().getStringValue();
            String type = configPropertyType.getConfigPropertyType().getStringValue();
            String explicitValue = (String) explicitValues.get(name);
            String defaultValue;
            if (configPropertyType.getConfigPropertyValue() != null) {
                defaultValue = configPropertyType.getConfigPropertyValue().getStringValue();
            } else {
                defaultValue = null;
            }
            configProperties[i] = new ConfigProperty(name, type, explicitValue, defaultValue);
        }
        return configProperties;
    }

    private ConfigProperty[] getConfigProperties(ConfigPropertyType[] configPropertyTypes, GerConfigPropertySettingType[] configPropertySettings) {
        Map explicitValues = getExplicitValuesMap(configPropertySettings);

        ConfigProperty[] configProperties = new ConfigProperty[configPropertyTypes.length];
        for (int i = 0; i < configPropertyTypes.length; i++) {
            ConfigPropertyType configPropertyType = configPropertyTypes[i];
            String name = configPropertyType.getConfigPropertyName().getStringValue();
            String type = configPropertyType.getConfigPropertyType().getStringValue();
            String explicitValue = (String) explicitValues.get(name);
            String defaultValue = null;
            if (configPropertyType.isSetConfigPropertyValue()) {
                defaultValue = configPropertyType.getConfigPropertyValue().getStringValue();
            }
            configProperties[i] = new ConfigProperty(name, type, explicitValue, defaultValue);
        }
        return configProperties;
    }

    private Map getExplicitValuesMap(GerConfigPropertySettingType[] configPropertySettings) {
        Map explicitValues = new HashMap();
        for (int j = 0; j < configPropertySettings.length; j++) {
            GerConfigPropertySettingType configPropertySetting = configPropertySettings[j];
            String name = configPropertySetting.getName();
            String value = configPropertySetting.getStringValue();
            explicitValues.put(name, value);
        }
        return explicitValues;
    }

    private static void setAttributeValue(GBeanMBean gbean, String name, String type, String explicitValue, String defaultValue) throws DeploymentException, ReflectionException, AttributeNotFoundException {
        if (explicitValue == null && defaultValue == null) {
            return;
        }

        Class clazz;
        try {
            clazz = gbean.getClassLoader().loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Could not load attribute class: attribute: " + name + ", type: " + type, e);
        }

        PropertyEditor editor = PropertyEditors.getEditor(clazz);
        if (explicitValue != null) {
            editor.setAsText(explicitValue);
        } else {
            editor.setAsText(defaultValue);
        }
        Object value = editor.getValue();

        gbean.setAttribute(name, value);
    }

    private ObjectName configureConnectionManager(EARContext earContext, J2eeContext j2eeContext, GerConnectiondefinitionInstanceType connectionfactoryInstance, ClassLoader cl) throws DeploymentException {
        if (connectionfactoryInstance.getConnectionmanagerRef() != null) {
            //we don't configure anything, just use the supplied gbean
            try {
                return ObjectName.getInstance(connectionfactoryInstance.getConnectionmanagerRef());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid ObjectName string supplied for ConnectionManager reference", e);
            }
        }

        //we configure our connection manager
        GerConnectionmanagerType connectionManager = connectionfactoryInstance.getConnectionmanager();
        GBeanMBean connectionManagerGBean;
        try {
            connectionManagerGBean = new GBeanMBean(GBeanInfo.getGBeanInfo("org.apache.geronimo.connector.outbound.GenericConnectionManager", cl), cl);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to create GMBean", e);
        }
        TransactionSupport transactionSupport = null;
        if (connectionManager.getNoTransaction() != null) {
            transactionSupport = NoTransactions.INSTANCE;
        } else if (connectionManager.getLocalTransaction() != null) {
            transactionSupport = LocalTransactions.INSTANCE;
        } else if (connectionManager.getTransactionLog() != null) {
            transactionSupport = TransactionLog.INSTANCE;
        } else if (connectionManager.getXaTransaction() != null) {
            transactionSupport = new XATransactions(connectionManager.getXaTransaction().getTransactionCaching() != null,
                    connectionManager.getXaTransaction().getThreadCaching() != null);
        } else {
            throw new DeploymentException("Unexpected transaction support element");
        }
        PoolingSupport pooling = null;
        if (connectionManager.getSinglePool() != null) {
            pooling = new SinglePool(connectionManager.getSinglePool().getMaxSize(),
                    connectionManager.getSinglePool().getBlockingTimeoutMilliseconds(),
                    connectionManager.getSinglePool().getMatchOne() != null,
                    connectionManager.getSinglePool().getMatchAll() != null,
                    connectionManager.getSinglePool().getSelectOneAssumeMatch() != null);
        } else if (connectionManager.getPartitionedPool() != null) {
            pooling = new PartitionedPool(connectionManager.getPartitionedPool().getPartitionByConnectionrequestinfo() != null,
                    connectionManager.getPartitionedPool().getPartitionBySubject() != null,
                    connectionManager.getPartitionedPool().getMaxSize(),
                    connectionManager.getPartitionedPool().getBlockingTimeoutMilliseconds(),
                    connectionManager.getPartitionedPool().getMatchOne() != null,
                    connectionManager.getPartitionedPool().getMatchAll() != null,
                    connectionManager.getPartitionedPool().getSelectOneAssumeMatch() != null);
        } else if (connectionManager.getNoPool() != null) {
            pooling = new NoPool();
        } else {
            throw new DeploymentException("Unexpected pooling support element");
        }
        try {
            connectionManagerGBean.setAttribute("name", connectionfactoryInstance.getName());
            connectionManagerGBean.setAttribute("transactionSupport", transactionSupport);
            connectionManagerGBean.setAttribute("pooling", pooling);
            connectionManagerGBean.setReferencePattern("ConnectionTracker", earContext.getConnectionTrackerObjectName());
            if (connectionManager.getRealmBridge() != null) {
                connectionManagerGBean.setReferencePattern("RealmBridge", ObjectName.getInstance(BASE_REALM_BRIDGE_NAME + connectionManager.getRealmBridge()));
            }
            connectionManagerGBean.setReferencePattern("TransactionContextManager", earContext.getTransactionContextManagerObjectName());
        } catch (Exception e) {
            throw new DeploymentException("Problem setting up ConnectionManager", e);
        }

        // add it
        ObjectName connectionManagerObjectName = NameFactory.getResourceComponentName(null, null, null, null, connectionfactoryInstance.getName(), NameFactory.JCA_CONNECTION_MANAGER, j2eeContext);
        earContext.addGBean(connectionManagerObjectName, connectionManagerGBean);
        return connectionManagerObjectName;
    }

    private void addOutboundGBeans(EARContext earContext, J2eeContext j2eeContext, ObjectName resourceAdapterObjectName, GerConnectiondefinitionInstanceType connectiondefinitionInstance, ConfigProperty[] configProperties, String managedConnectionFactoryClass, String connectionFactoryInterface, String connectionFactoryImplClass, String connectionInterface, String connectionImplClass, ClassLoader cl) throws DeploymentException {
        // ConnectionManager
        ObjectName connectionManagerObjectName = configureConnectionManager(earContext, j2eeContext, connectiondefinitionInstance, cl);

        // ManagedConnectionFactory
        GBeanInfoBuilder managedConnectionFactoryInfoFactory = new GBeanInfoBuilder("org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper", cl);
        GBeanMBean managedConnectionFactoryGBean = setUpDynamicGBean(managedConnectionFactoryInfoFactory, configProperties, cl);
        try {
            managedConnectionFactoryGBean.setAttribute("managedConnectionFactoryClass", cl.loadClass(managedConnectionFactoryClass));
            managedConnectionFactoryGBean.setAttribute("connectionFactoryInterface", cl.loadClass(connectionFactoryInterface));
            managedConnectionFactoryGBean.setAttribute("connectionFactoryImplClass", cl.loadClass(connectionFactoryImplClass));
            managedConnectionFactoryGBean.setAttribute("connectionInterface", cl.loadClass(connectionInterface));
            managedConnectionFactoryGBean.setAttribute("connectionImplClass", cl.loadClass(connectionImplClass));
            managedConnectionFactoryGBean.setAttribute("globalJNDIName", connectiondefinitionInstance.getGlobalJndiName());
            if (resourceAdapterObjectName != null) {
                managedConnectionFactoryGBean.setReferencePattern("ResourceAdapterWrapper", resourceAdapterObjectName);
            }
            managedConnectionFactoryGBean.setReferencePattern("ConnectionManagerFactory", connectionManagerObjectName);
            if (connectiondefinitionInstance.getCredentialInterface() != null && "javax.resource.spi.security.PasswordCredential".equals(connectiondefinitionInstance.getCredentialInterface().getStringValue())) {
                GBeanMBean realmGBean = new GBeanMBean(PasswordCredentialRealm.getGBeanInfo(), cl);
                realmGBean.setAttribute("realmName", BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + connectiondefinitionInstance.getName());
                ObjectName realmObjectNam = ObjectName.getInstance(BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + connectiondefinitionInstance.getName());
                earContext.addGBean(realmObjectNam, realmGBean);
                managedConnectionFactoryGBean.setReferencePattern("ManagedConnectionFactoryListener", realmObjectNam);
            }
            //additional interfaces implemented by connection factory
            FullyQualifiedClassType[] implementedInterfaceElements = connectiondefinitionInstance.getImplementedInterfaceArray();
            Class[] implementedInterfaces = new Class[implementedInterfaceElements == null ? 0 : implementedInterfaceElements.length];
            for (int i = 0; i < implementedInterfaceElements.length; i++) {
                FullyQualifiedClassType additionalInterfaceType = implementedInterfaceElements[i];
                implementedInterfaces[i] = cl.loadClass(additionalInterfaceType.getStringValue());
            }
            managedConnectionFactoryGBean.setAttribute("implementedInterfaces", implementedInterfaces);

        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        ObjectName managedConnectionFactoryObjectName = NameFactory.getResourceComponentName(null, null, null, null, connectiondefinitionInstance.getName(), NameFactory.JCA_MANAGED_CONNECTION_FACTORY, j2eeContext);
        earContext.addGBean(managedConnectionFactoryObjectName, managedConnectionFactoryGBean);

        // ConnectionFactory
        GBeanMBean connectionFactory = new GBeanMBean(JCAConnectionFactoryImpl.GBEAN_INFO, cl);
        connectionFactory.setReferencePattern("J2EEServer", earContext.getServerObjectName());
        try {
            connectionFactory.setAttribute("managedConnectionFactory", managedConnectionFactoryObjectName.getCanonicalName());
        } catch (Exception e) {
            throw new DeploymentException("Could not initialize JCAConnectionFactory", e);
        }

        ObjectName connectionFactoryObjectName = NameFactory.getResourceComponentName(null, null, null, null, connectiondefinitionInstance.getName(), NameFactory.JCA_CONNECTION_FACTORY, j2eeContext);
        earContext.addGBean(connectionFactoryObjectName, connectionFactory);
    }

    private static URI getDependencyURI(GerDependencyType dependency) throws DeploymentException {
        if (dependency.isSetUri()) {
            try {
                return new URI(dependency.getUri());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid dependency URI " + dependency.getUri(), e);
            }
        } else {
            String id = dependency.getGroupId() + "/jars/" + dependency.getArtifactId() + '-' + dependency.getVersion() + ".jar";
            try {
                return new URI(id);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to construct URI for groupId=" + dependency.getGroupId() + ", artifactId=" + dependency.getArtifactId() + ", version=" + dependency.getVersion(), e);
            }
        }
    }

    //ResourceReferenceBuilder implementation
    public Reference createResourceRef(String containerId, Class iface) throws DeploymentException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        ref.add(new JMXRefAddr(null, containerId, iface));
        return ref;
    }

    public Reference createAdminObjectRef(String containerId, Class iface) throws DeploymentException {
        Reference ref = new Reference(null, JMXObjectFactory.class.getName(), null);
        ref.add(new JMXRefAddr(null, containerId, iface));
        return ref;
    }

    public ObjectName locateResourceName(ObjectName query) throws DeploymentException {
        Set names = kernel.listGBeans(query);
        if (names.size() != 1) {
            throw new DeploymentException("Unknown or ambiguous resource name query: " + query + " match count: " + names.size());
        }
        return (ObjectName) names.iterator().next();
    }

    public Object locateActivationSpecInfo(ObjectName resourceAdapterName, String messageListenerInterface) throws DeploymentException {
        Map activationSpecInfos = null;
        try {
            activationSpecInfos = (Map) kernel.getAttribute(resourceAdapterName, "activationSpecInfoMap");
        } catch (Exception e) {
            throw new DeploymentException("Could not get activation spec infos for resource adapter named: " + resourceAdapterName, e);
        }
        return activationSpecInfos.get(messageListenerInterface);
    }

    private final static class ConfigProperty {
        private final String name;
        private final String type;
        private final String explicitValue;
        private final String defaultValue;

        public ConfigProperty(String name, String type, String explicitValue, String defaultValue) {
            this.name = name;
            this.type = type;
            this.explicitValue = explicitValue;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getExplicitValue() {
            return explicitValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ConnectorModuleBuilder.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addInterface(ModuleBuilder.class);
        infoFactory.addInterface(ResourceReferenceBuilder.class);
        infoFactory.setConstructor(new String[] {"kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
