/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.connector.deployment;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.jar.JarInputStream;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.AdminObjectWrapper;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.outbound.ConnectionManagerDeployment;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.UnclosableInputStream;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.j2ee.AdminobjectType;
import org.apache.geronimo.xbeans.j2ee.ConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.ConnectionDefinitionType;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.geronimo.xbeans.j2ee.ResourceadapterType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.6 $ $Date: 2004/02/09 23:13:27 $
 *
 * */
public class Connector_1_5Module extends AbstractConnectorModule {

    private ConnectorDocument connectorDocument;

    public Connector_1_5Module(URI configID, InputStream moduleArchive, GerConnectorDocument geronimoConnectorDocument, ObjectName connectionTrackerNamePattern) {
        super(configID, moduleArchive, geronimoConnectorDocument, connectionTrackerNamePattern);
    }


    protected void getConnectorDocument(JarInputStream jarInputStream) throws XmlException, IOException, DeploymentException {
        connectorDocument = ConnectorDocument.Factory.parse(new UnclosableInputStream(jarInputStream));
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        Collection errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
        if (!connectorDocument.validate(xmlOptions)) {
            throw new DeploymentException("Invalid deployment descriptor: errors: " + errors);
        }
    }

    public void defineGBeans(ConfigurationCallback callback, ClassLoader cl) throws DeploymentException {
        ResourceadapterType resourceadapter = connectorDocument.getConnector().getResourceadapter();
        GerResourceadapterType geronimoResourceAdapter = geronimoConnectorDocument.getConnector().getResourceadapter();
        //ResourceAdapter setup
        String resourceAdapterClassName = resourceadapter.getResourceadapterClass().getStringValue();
        if (resourceAdapterClassName == null) {
            throw new DeploymentException("No resource adapter class provided for J2ee Connector Architecture 1.5 adapter");
        }
        ObjectName resourceAdapterObjectName = null;
        GBeanInfoFactory resourceAdapterInfoFactory = new GBeanInfoFactory(ResourceAdapterWrapper.class.getName(), ResourceAdapterWrapper.getGBeanInfo());
        GBeanMBean resourceAdapterGBean = setUpDynamicGBean(resourceAdapterInfoFactory, resourceadapter.getConfigPropertyArray(), geronimoResourceAdapter.getConfigPropertySettingArray());
        try {
            resourceAdapterGBean.setAttribute("ResourceAdapterClass", cl.loadClass(resourceAdapterClassName));
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        try {
            resourceAdapterObjectName = ObjectName.getInstance(BASE_RESOURCE_ADAPTER_NAME + configID);
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct resource adapter object name", e);
        }
        callback.addGBean(resourceAdapterObjectName, resourceAdapterGBean);

        Map connectionDefinitions = new HashMap();
        for (int j = 0; j < resourceadapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; j++) {
            ConnectionDefinitionType connectionDefinition = resourceadapter.getOutboundResourceadapter().getConnectionDefinitionArray(j);
            connectionDefinitions.put(connectionDefinition.getConnectionfactoryInterface().getStringValue(), connectionDefinition);
        }
        //ManagedConnectionFactory setup
        for (int i = 0; i < geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; i++) {
            GerConnectionDefinitionType geronimoConnectionDefinition = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray(i);
            assert geronimoConnectionDefinition != null: "Null GeronimoConnectionDefinition";
            String connectionFactoryInterfaceName = geronimoConnectionDefinition.getConnectionfactoryInterface().getStringValue();
            ConnectionDefinitionType connectionDefinition = (ConnectionDefinitionType) connectionDefinitions.get(connectionFactoryInterfaceName);
            assert connectionDefinition != null: "No connection definition for ConnectionFactory class: " + connectionFactoryInterfaceName;
            for (int j = 0; j < geronimoConnectionDefinition.getConnectiondefinitionInstanceArray().length; j++) {
                GerConnectiondefinitionInstanceType connectionfactoryInstance = geronimoConnectionDefinition.getConnectiondefinitionInstanceArray()[j];

                //ConnectionManagerFactory
                GerConnectionmanagerType connectionManagerFactory = connectionfactoryInstance.getConnectionmanager();
                GBeanInfo connectionManagerFactoryGBeanInfo;
                try {
                    connectionManagerFactoryGBeanInfo = GBeanInfo.getGBeanInfo(ConnectionManagerDeployment.class.getName(), cl);
                } catch (InvalidConfigurationException e) {
                    throw new DeploymentException("Unable to get GBeanInfo from ConnectionManagerDeployment", e);
                }

                GBeanMBean connectionManagerFactoryGBean;
                try {
                    connectionManagerFactoryGBean = new GBeanMBean(connectionManagerFactoryGBeanInfo, cl);
                } catch (InvalidConfigurationException e) {
                    throw new DeploymentException("Unable to create GMBean", e);
                }
                try {
                    connectionManagerFactoryGBean.setAttribute("Name", connectionManagerFactory.getName());
                    connectionManagerFactoryGBean.setAttribute("BlockingTimeout", new Integer(connectionManagerFactory.getBlockingTimeout().intValue()));
                    connectionManagerFactoryGBean.setAttribute("MaxSize", new Integer(connectionManagerFactory.getMaxSize().intValue()));
                    connectionManagerFactoryGBean.setAttribute("UseTransactions", Boolean.valueOf(connectionManagerFactory.getUseTransactions()));
                    connectionManagerFactoryGBean.setAttribute("UseLocalTransactions", Boolean.valueOf(connectionManagerFactory.getUseLocalTransactions()));
                    connectionManagerFactoryGBean.setAttribute("UseTransactionCaching", Boolean.valueOf(connectionManagerFactory.getUseTransactionCaching()));
                    connectionManagerFactoryGBean.setAttribute("UseConnectionRequestInfo", Boolean.valueOf(connectionManagerFactory.getUseConnectionRequestInfo()));
                    connectionManagerFactoryGBean.setAttribute("UseSubject", Boolean.valueOf(connectionManagerFactory.getUseSubject()));
                    connectionManagerFactoryGBean.setReferencePatterns("Kernel", Collections.singleton(Kernel.KERNEL));
                    connectionManagerFactoryGBean.setReferencePatterns("ConnectionTracker", Collections.singleton(connectionTrackerNamePattern));
                    if (connectionManagerFactory.getRealmBridge() != null) {
                        connectionManagerFactoryGBean.setReferencePatterns("RealmBridge", Collections.singleton(ObjectName.getInstance(BASE_REALM_BRIDGE_NAME + connectionManagerFactory.getRealmBridge())));
                    }
                } catch (Exception e) {
                    throw new DeploymentException("Problem setting up ConnectionManagerFactory", e);
                }
                ObjectName connectionManagerFactoryObjectName = null;
                try {
                    connectionManagerFactoryObjectName = ObjectName.getInstance(BASE_CONNECTION_MANAGER_FACTORY_NAME + connectionfactoryInstance.getName());
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not name ConnectionManagerFactory", e);
                }
                callback.addGBean(connectionManagerFactoryObjectName, connectionManagerFactoryGBean);
                //ManagedConnectionFactory

                GBeanInfoFactory managedConnectionFactoryInfoFactory = new GBeanInfoFactory(ManagedConnectionFactoryWrapper.class.getName(), ManagedConnectionFactoryWrapper.getGBeanInfo());
                GBeanMBean managedConnectionFactoryGBean = setUpDynamicGBean(managedConnectionFactoryInfoFactory, connectionDefinition.getConfigPropertyArray(), connectionfactoryInstance.getConfigPropertySettingArray());
                try {
                    managedConnectionFactoryGBean.setAttribute("ManagedConnectionFactoryClass", cl.loadClass(connectionDefinition.getManagedconnectionfactoryClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionFactoryInterface", cl.loadClass(connectionDefinition.getConnectionfactoryInterface().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionFactoryImplClass", cl.loadClass(connectionDefinition.getConnectionfactoryImplClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionInterface", cl.loadClass(connectionDefinition.getConnectionInterface().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionImplClass", cl.loadClass(connectionDefinition.getConnectionImplClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("GlobalJNDIName", connectionfactoryInstance.getGlobalJndiName());
                    if (resourceAdapterClassName != null) {
                        managedConnectionFactoryGBean.setReferencePatterns("ResourceAdapterWrapper", Collections.singleton(resourceAdapterObjectName));
                    }
                    managedConnectionFactoryGBean.setReferencePatterns("ConnectionManagerFactory", Collections.singleton(connectionManagerFactoryObjectName));
                    /*
                    //TODO also set up the login module
                    if (geronimoConnectionDefinition.getAuthentication().equals("BasicUserPassword")) {
                        managedConnectionFactoryGBean.setReferencePatterns("ManagedConnectionFactoryListener", Collections.singleton(ObjectName.getInstance(BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + geronimoConnectionDefinition.getName())));
                    }
                    */
                } catch (Exception e) {
                    throw new DeploymentException(e);
                }
                ObjectName managedConnectionFactoryObjectName = null;
                try {
                    managedConnectionFactoryObjectName = ObjectName.getInstance(BASE_MANAGED_CONNECTION_FACTORY_NAME + connectionfactoryInstance.getName());
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct ManagedConnectionFactory object name", e);
                }
                callback.addGBean(managedConnectionFactoryObjectName, managedConnectionFactoryGBean);
            }

        }
        //admin objects
        Map adminObjectInterfaceMap = new HashMap();
        for (int i = 0; i < resourceadapter.getAdminobjectArray().length; i++) {
            AdminobjectType adminobject = resourceadapter.getAdminobjectArray()[i];
            adminObjectInterfaceMap.put(adminobject.getAdminobjectInterface().getStringValue(), adminobject);
        }
        for (int i = 0; i < geronimoResourceAdapter.getAdminobjectArray().length; i++) {
            GerAdminobjectType gerAdminObject = geronimoResourceAdapter.getAdminobjectArray()[i];
            AdminobjectType adminobject = (AdminobjectType) adminObjectInterfaceMap.get(gerAdminObject.getAdminobjectInterface().getStringValue());
            assert adminobject != null;
            for (int j = 0; j < gerAdminObject.getAdminobjectInstanceArray().length; j++) {
                GerAdminobjectInstanceType gerAdminobjectInstance = gerAdminObject.getAdminobjectInstanceArray()[j];
                GBeanInfoFactory adminObjectInfoFactory = new GBeanInfoFactory(AdminObjectWrapper.class.getName(), AdminObjectWrapper.getGBeanInfo());
                GBeanMBean adminObjectGBean = setUpDynamicGBean(adminObjectInfoFactory, adminobject.getConfigPropertyArray(), gerAdminobjectInstance.getConfigPropertySettingArray());
                ObjectName adminObjectObjectName = null;
                try {
                    adminObjectObjectName = ObjectName.getInstance(BASE_ADMIN_OBJECT_NAME + gerAdminobjectInstance.getAdminobjectName());
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct ManagedConnectionFactory object name", e);
                }
                callback.addGBean(adminObjectObjectName, adminObjectGBean);

            }
        }

    }


    private GBeanMBean setUpDynamicGBean(GBeanInfoFactory infoFactory, ConfigPropertyType[] configProperties, GerConfigPropertySettingType[] configPropertySettings) throws DeploymentException {
        addDynamicAttributes(infoFactory, configProperties);
        GBeanInfo gbeanInfo = infoFactory.getBeanInfo();
        GBeanMBean gbean;
        try {
            gbean = new GBeanMBean(gbeanInfo);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to create GMBean", e);
        }
        try {
            setDynamicAttributes(gbean, configProperties, configPropertySettings);
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return gbean;
    }

//ManagedConnectionFactories are extremely restricted as to the attribute types.
    private void setDynamicAttributes(GBeanMBean gBean, ConfigPropertyType[] configProperties, GerConfigPropertySettingType[] configPropertySettings) throws DeploymentException, ReflectionException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException {
        for (int i = 0; i < configProperties.length; i++) {
            ConfigPropertyType configProperty = configProperties[i];
            if (configProperty.getConfigPropertyValue() == null) {
                continue;
            }
            Object value;
            try {
                PropertyEditor editor = PropertyEditors.findEditor(configProperty.getConfigPropertyType().getStringValue());
                String valueString = null;
                if (editor != null) {
//look for explicit value setting
                    for (int j = 0; j < configPropertySettings.length; j++) {
                        GerConfigPropertySettingType configPropertySetting = configPropertySettings[j];
                        if (configPropertySetting.getName().equals(configProperty.getConfigPropertyName().getStringValue())) {
                            valueString = configPropertySetting.getStringValue();
                            break;
                        }
                    }
//look for default value
                    if (valueString == null) {
                        if (configProperty.getConfigPropertyValue() != null) {
                            valueString = configProperty.getConfigPropertyValue().getStringValue();
                        }
                    }
                    if (valueString != null) {
                        editor.setAsText(valueString);
                        value = editor.getValue();
                        gBean.setAttribute(configProperty.getConfigPropertyName().getStringValue(), value);
                    }
                } else {
                    throw new DeploymentException("No property editor for type: " + configProperty.getConfigPropertyType());
                }
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load attribute class: attribute: " + configProperty.getConfigPropertyName() + ", type: " + configProperty.getConfigPropertyType(), e);
            }

        }
    }

    void addDynamicAttributes(GBeanInfoFactory infoFactory, ConfigPropertyType[] configProperties) {
        for (int i = 0; i < configProperties.length; i++) {
            ConfigPropertyType configProperty = configProperties[i];
            infoFactory.addAttribute(new DynamicGAttributeInfo(configProperty.getConfigPropertyName().getStringValue(), true));
        }
    }

}
