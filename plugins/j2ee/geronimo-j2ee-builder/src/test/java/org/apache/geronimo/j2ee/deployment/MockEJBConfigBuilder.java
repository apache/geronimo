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
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.osgi.framework.Bundle;

import java.io.File;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.Collection;


/**
 * @version $Rev:385692 $ $Date$
 */
public class MockEJBConfigBuilder extends Assert implements ModuleBuilder {
    private EARContext earContext;
    private Bundle bundle;
    public EJBModule ejbModule;

    public Module createModule(Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        return null;
    }
    
    public Module createModule(File plan, JarFile moduleFile, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        AbstractName earName = naming.createRootName(new Artifact("test", "test-ejb-jar", "", "jar"), NameFactory.NULL, NameFactory.J2EE_APPLICATION) ;
        AbstractName moduleName = naming.createChildName(earName, "ejb-jar", NameFactory.EJB_MODULE);
        return new EJBModule(true, moduleName, null, null, moduleFile, "ejb.jar", null, null, null, null, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, Module parentModule, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
        AbstractName moduleName = naming.createChildName(parentModule.getModuleName(), "ejb-jar", NameFactory.EJB_MODULE);
        return new EJBModule(false, moduleName, null, null, moduleFile, targetPath, null, null, null, Module.share(Module.APP, parentModule.getJndiContext()), parentModule);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module ejbModule, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) {
        assertNotNull(earFile);
        assertNotNull(earContext);
        this.earContext = earContext;
        ejbModule.setEarContext(earContext);
//        assertEquals(this.ejbModule, ejbModule);
//        if ( null != this.ejbModule.getAltSpecDD() ) {
//            assertEquals(this.ejbModule.getAltSpecDD(), ejbModule.getAltSpecDD());
//        }
//        if ( null != this.ejbModule.getAltVendorDD() ) {
//            assertEquals(this.ejbModule.getAltVendorDD(), ejbModule.getAltVendorDD());
//        }
    }

    public void initContext(EARContext earContext, Module ejbModule, Bundle bundle) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.ejbModule, ejbModule);
        assertNotNull(bundle);
        this.bundle = bundle;
    }

    public void addGBeans(EARContext earContext, Module ejbModule, Bundle bundle, Collection repository) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.ejbModule, ejbModule);
        assertEquals(this.bundle, bundle);
    }

    public String getSchemaNamespace() {
        return null;
    }

}
