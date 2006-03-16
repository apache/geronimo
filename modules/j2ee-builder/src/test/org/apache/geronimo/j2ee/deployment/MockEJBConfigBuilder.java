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

import junit.framework.Assert;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.Naming;

import javax.naming.Reference;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;


/**
 * @version $Rev:385692 $ $Date$
 */
public class MockEJBConfigBuilder extends Assert implements ModuleBuilder, EJBReferenceBuilder {
    private EARContext earContext;
    private ClassLoader cl;
    public EJBModule ejbModule;

    public Module createModule(File plan, JarFile moduleFile, Naming naming) throws DeploymentException {
        AbstractName earName = naming.createRootName(new Artifact("test", "test-ejb-jar", "", "jar"), NameFactory.NULL, NameFactory.J2EE_APPLICATION) ;
        AbstractName moduleName = naming.createChildName(earName, "ejb-jar", NameFactory.EJB_MODULE);
        return new EJBModule(true, moduleName, null, moduleFile, "ejb.jar", null, null, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming) throws DeploymentException {
        AbstractName moduleName = naming.createChildName(earName, "ejb-jar", NameFactory.EJB_MODULE);
        return new EJBModule(false, moduleName, null, moduleFile, targetPath, null, null, null);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module ejbModule, ConfigurationStore configurationStore, Repository repository) {
        assertNotNull(earFile);
        assertNotNull(earContext);
        this.earContext = earContext;
//        assertEquals(this.ejbModule, ejbModule);
//        if ( null != this.ejbModule.getAltSpecDD() ) {
//            assertEquals(this.ejbModule.getAltSpecDD(), ejbModule.getAltSpecDD());
//        }
//        if ( null != this.ejbModule.getAltVendorDD() ) {
//            assertEquals(this.ejbModule.getAltVendorDD(), ejbModule.getAltVendorDD());
//        }
    }

    public void initContext(EARContext earContext, Module ejbModule, ClassLoader cl) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.ejbModule, ejbModule);
        assertNotNull(cl);
        this.cl = cl;
    }

    public void addGBeans(EARContext earContext, Module ejbModule, ClassLoader cl, Repository repository) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.ejbModule, ejbModule);
        assertEquals(this.cl, cl);
    }

    public String getSchemaNamespace() {
        return null;
    }

    public Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) throws DeploymentException {
        return null;
    }

    public Object createHandleDelegateReference() {
        return null;
    }

    public Reference createEJBRemoteRef(String requiredModule, String optionalModule, String name, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote, Configuration configuration) throws DeploymentException {
        return null;
    }

    public Reference createEJBLocalRef(String requiredModule, String optionalModule, String name, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local, Configuration configuration) throws DeploymentException {
        return null;
    }

}
