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

import java.net.URL;
import java.net.URI;
import java.util.jar.JarFile;

import junit.framework.Assert;
import org.apache.geronimo.deployment.DeploymentException;

/**
 * @version $Rev$ $Date$
 */
public class MockEJBConfigBuilder extends Assert implements ModuleBuilder {
    public EARContext earContext;
    public EJBModule ejbModule;
    public ClassLoader cl;

    public Module createModule(String name, Object planFile, JarFile moduleFile, URL specDDUrl, String targetPath) throws DeploymentException {
        return new EJBModule(name, null, null, URI.create(targetPath), moduleFile, targetPath, null, null, null);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module ejbModule) {
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

    public void addGBeans(EARContext earContext, Module ejbModule, ClassLoader cl) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.ejbModule, ejbModule);
        assertEquals(this.cl, cl);
    }

}
