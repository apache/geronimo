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
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;
import javax.management.ObjectName;
import javax.naming.Reference;

import junit.framework.Assert;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;


/**
 * @version $Rev$ $Date$
 */
public class MockEJBConfigBuilder extends Assert implements ModuleBuilder, EJBReferenceBuilder {
    public EARContext earContext;
    public EJBModule ejbModule;
    public ClassLoader cl;

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return new EJBModule(true, null, null, moduleFile, "ejb.jar", null, null, null);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
        return new EJBModule(false, null, null, moduleFile, targetPath, null, null, null);
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

    public String getSchemaNamespace() {
        return null;
    }

    public Reference createEJBLocalReference(String objectName, GBeanData gbeanData, boolean isSession, String localHome, String local) throws DeploymentException {
        return null;
    }

    public Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home, String remote) throws DeploymentException {
        return null;
    }

    public Reference createCORBAReference(URI corbaURL, String objectName, ObjectName containerName, String home) throws DeploymentException {
        return null;
    }

    public Object createHandleDelegateReference() {
        return null;
    }

    public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext context) throws DeploymentException {
        return null;
    }

    public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext context) throws DeploymentException {
        return null;
    }
}
