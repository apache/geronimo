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

import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;

import junit.framework.Assert;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * @version $Revision: 1.2 $ $Date: 2004/07/18 22:04:27 $
 */
public class MockEJBConfigBuilder extends Assert implements ModuleBuilder {
    public EARContext earContext;
    public EJBModule ejbModule;
    public ClassLoader cl;

    public XmlObject getDeploymentPlan(URL module) {
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

    public Module createModule(String name, XmlObject plan) {
        return null;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module ejbModule) {
        assertNotNull(earFile);
        assertNotNull(earContext);
        this.earContext = earContext;
        assertEquals(this.ejbModule, ejbModule);
    }

    public void initContext(EARContext earContext, Module ejbModule, ClassLoader cl) {
        assertEquals(this.earContext, earContext);
        assertEquals(this.ejbModule, ejbModule);
        assertNotNull(cl);
        this.cl = cl;
    }

    public void addGBeans(EARContext earContext, Module ejbModule, ClassLoader cl) {
        assertEquals(this.earContext, earContext);
        assertEquals(this.ejbModule, ejbModule);
        assertEquals(this.cl, cl);
    }

    public SchemaTypeLoader getSchemaTypeLoader() {
        return null;
    }
}
