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
import java.util.Map;

import javax.management.ObjectName;

import junit.framework.Assert;
import org.apache.geronimo.common.DeploymentException;

/**
 * @version $Rev$ $Date$
 */
public class MockWARConfigBuilder extends Assert implements ModuleBuilder {
    public EARContext earContext;
    public WebModule webModule;
    public ClassLoader cl;
    public String contextRoot;
    private Map portMap = null;
    private String namespace = "foo";

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
        return new WebModule(true, null, null, moduleFile, "war", null, null, null, contextRoot, portMap, namespace);
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
        return new WebModule(false, null, null, moduleFile, targetPath, null, null, null, contextRoot, portMap, namespace);
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

    public String getSchemaNamespace() {
        return null;
    }

}
