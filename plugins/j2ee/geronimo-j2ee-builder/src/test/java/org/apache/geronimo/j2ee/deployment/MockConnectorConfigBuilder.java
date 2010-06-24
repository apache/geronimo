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
package org.apache.geronimo.j2ee.deployment;

import junit.framework.Assert;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.osgi.framework.Bundle;

import javax.management.ObjectName;
import javax.naming.Reference;
import java.io.File;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.Collection;

/**
 * @version $Rev:385692 $ $Date$
 */
public class MockConnectorConfigBuilder extends Assert implements ModuleBuilder, ActivationSpecInfoLocator {
    private EARContext earContext;
    private Bundle bundle;
    public Module connectorModule;

    public Module createModule(Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return null;
    }
    
    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        AbstractName earName = naming.createRootName(new Artifact("test", "test-war", "", "rar"), NameFactory.NULL, NameFactory.J2EE_APPLICATION) ;
        AbstractName moduleName = naming.createChildName(earName, "rar", NameFactory.RESOURCE_ADAPTER_MODULE);
        return new ConnectorModule(true, moduleName, null, null, moduleFile, "connector", null, null, null, null, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, Module parentModule, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        AbstractName moduleName = naming.createChildName(parentModule.getModuleName(), "rar", NameFactory.RESOURCE_ADAPTER_MODULE);
        return new ConnectorModule(false, moduleName, null, null, moduleFile, targetPath, null, null, null, Module.share(Module.APP, parentModule.getJndiContext()), parentModule);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module connectorModule, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) {
        assertNotNull(earFile);
        assertNotNull(earContext);
        this.earContext = earContext;
        connectorModule.setEarContext(earContext);
//        assertEquals(this.connectorModule, connectorModule);
//        if ( null != this.connectorModule.getAltSpecDD() ) {
//            assertEquals(this.connectorModule.getAltSpecDD(), connectorModule.getAltSpecDD());
//        }
//        if ( null != this.connectorModule.getAltVendorDD() ) {
//            assertEquals(this.connectorModule.getAltVendorDD(), connectorModule.getAltVendorDD());
//        }
    }

    public void initContext(EARContext earContext, Module connectorModule, Bundle bundle) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.connectorModule, connectorModule);
        assertNotNull(bundle);
        this.bundle = bundle;
    }

    public void addGBeans(EARContext earContext, Module connectorModule, Bundle bundle, Collection repository) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.connectorModule, connectorModule);
        assertEquals(this.bundle, bundle);
    }

    public String getSchemaNamespace() {
        return null;
    }

    public Reference createResourceRef(AbstractNameQuery containerId, Class iface, Configuration configuration) throws DeploymentException {
        return null;
    }

    public Reference createAdminObjectRef(AbstractNameQuery containerId, Class iface, Configuration configuration) throws DeploymentException {
        return null;
    }

    public ObjectName locateResourceName(ObjectName query) throws DeploymentException {
        return null;
    }

    public GBeanData locateActivationSpecInfo(AbstractNameQuery nameQuery, String messageListenerInterface, Configuration configuration) throws DeploymentException {
        return null;
    }

    public GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData) throws DeploymentException {
        return null;
    }

    public GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName) throws DeploymentException {
        return null;
    }

    public GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) throws DeploymentException {
        return null;
    }
}
