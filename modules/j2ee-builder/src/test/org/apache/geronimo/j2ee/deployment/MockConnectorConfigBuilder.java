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
import java.net.URL;
import java.net.URI;
import java.util.jar.JarFile;

import javax.naming.Reference;
import javax.management.ObjectName;

import junit.framework.Assert;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Rev$ $Date$
 */
public class MockConnectorConfigBuilder extends Assert implements ModuleBuilder, ResourceReferenceBuilder{
    public EARContext earContext;
    public Module connectorModule;
    public ClassLoader cl;

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return new ConnectorModule(true, null, null, moduleFile, "connector", null, null, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId) throws DeploymentException {
        return new ConnectorModule(false, null, null, moduleFile, targetPath, null, null, null);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module connectorModule) {
        assertNotNull(earFile);
        assertNotNull(earContext);
        this.earContext = earContext;
//        assertEquals(this.connectorModule, connectorModule);
//        if ( null != this.connectorModule.getAltSpecDD() ) {
//            assertEquals(this.connectorModule.getAltSpecDD(), connectorModule.getAltSpecDD());
//        }
//        if ( null != this.connectorModule.getAltVendorDD() ) {
//            assertEquals(this.connectorModule.getAltVendorDD(), connectorModule.getAltVendorDD());
//        }
    }

    public void initContext(EARContext earContext, Module connectorModule, ClassLoader cl) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.connectorModule, connectorModule);
        assertNotNull(cl);
        this.cl = cl;
    }

    public String addGBeans(EARContext earContext, Module connectorModule, ClassLoader cl) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.connectorModule, connectorModule);
        assertEquals(this.cl, cl);
        return null;
    }

    public Reference createResourceRef(String containerId, Class iface) throws DeploymentException {
        return null;
    }

    public Reference createAdminObjectRef(String containerId, Class iface) throws DeploymentException {
        return null;
    }

    public ObjectName locateResourceName(ObjectName query) throws DeploymentException {
        return null;
    }

    public GBeanData locateActivationSpecInfo(ObjectName resourceAdapterName, String messageListenerInterface) throws DeploymentException {
        return null;
    }

    public GBeanData locateResourceAdapterGBeanData(ObjectName resourceAdapterModuleName) throws DeploymentException {
        return null;
    }

    public GBeanData locateAdminObjectInfo(ObjectName resourceAdapterModuleName, String adminObjectInterfaceName) throws DeploymentException {
        return null;
    }

    public GBeanData locateConnectionFactoryInfo(ObjectName resourceAdapterModuleName, String connectionFactoryInterfaceName) throws DeploymentException {
        return null;
    }
}
