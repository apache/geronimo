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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;

import junit.framework.Assert;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class MockWARConfigBuilder extends Assert implements ModuleBuilder {
    public EARContext earContext;
    public WebModule webModule;
    public ClassLoader cl;
    public String contextRoot;

    public XmlObject getDeploymentPlan(JarFile module) {
        return null;
    }

    public boolean canHandlePlan(XmlObject plan) {
        return false;
    }

    public URI getParentId(XmlObject plan) {
        return null;
    }

    public URI getConfigId(XmlObject plan) {
        return null;
    }

    public Module createModule(String name, JarFile moduleFile, XmlObject vendorDD) throws DeploymentException {
        return createModule(name, moduleFile, vendorDD, "connector", null);
    }

    public Module createModule(String name, JarFile moduleFile, XmlObject vendorDD, String targetPath, URL specDD) throws DeploymentException {
        return new WebModule(name, URI.create(targetPath), moduleFile, targetPath, null, vendorDD, null);
    }

    public void installModule(JarFile earFile, EARContext earContext, Module webModule) throws DeploymentException {
        assertNotNull(earFile);
        assertNotNull(earContext);
        this.earContext = earContext;
//        assertEquals(this.webModule, webModule);
//        if ( null != this.webModule.getAltSpecDD() ) {
//            assertEquals(this.webModule.getAltSpecDD(), webModule.getAltSpecDD());
//        }
//        if ( null != this.webModule.getAltVendorDD() ) {
//            assertEquals(this.webModule.getAltVendorDD(), webModule.getAltVendorDD());
//        }
    }

    public XmlObject parseSpecDD(URL path) throws DeploymentException {
        InputStream in = null;
        try {
            in = path.openStream();
            return XmlObject.Factory.newInstance();
        } catch (IOException e) {
            throw new DeploymentException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public XmlObject validateVendorDD(XmlObject vendorDD) throws DeploymentException {
        return XmlObject.Factory.newInstance();
    }

    public void initContext(EARContext earContext, Module webModule, ClassLoader cl) {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.webModule, webModule);
        assertNotNull(cl);
        this.cl = cl;
    }

    public void addGBeans(EARContext earContext, Module webModule, ClassLoader cl) throws DeploymentException {
        assertEquals(this.earContext, earContext);
//        assertEquals(this.webModule, webModule);
        assertEquals(this.cl, cl);
        assertNotNull(contextRoot);
        this.contextRoot = ((WebModule) webModule).getContextRoot();
    }

    public SchemaTypeLoader getSchemaTypeLoader() {
        return null;
    }
}
