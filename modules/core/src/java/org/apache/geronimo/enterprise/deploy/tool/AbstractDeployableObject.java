/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.enterprise.deploy.tool;

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.shared.ModuleType;

/**
 * The base class for all DeployableObject implementations.  Each subclass
 * defines how to get specific deployment descriptors.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractDeployableObject implements DeployableObject {
    private JarFile jar;
    private ModuleType type;
    private DDBeanRoot defaultRoot;
    private ClassLoader loader;

    public AbstractDeployableObject(JarFile jar, ModuleType type, DDBeanRoot defaultRoot, ClassLoader loader) {
        this.jar = jar;
        this.type = type;
        this.defaultRoot = defaultRoot;
        this.loader = loader;
    }

    public ModuleType getType() {
        return type;
    }

    public DDBeanRoot getDDBeanRoot() {
        return defaultRoot;
    }

    public DDBean[] getChildBean(String xpath) {
        return defaultRoot.getChildBean(xpath);
    }

    public String[] getText(String xpath) {
        return defaultRoot.getText(xpath);
    }

    public Class getClassFromScope(String className) {
        try {
            return loader.loadClass(className);
        } catch(ClassNotFoundException e) {
            return null;
        }
    }

    public String getModuleDTDVersion() {
        return defaultRoot.getModuleDTDVersion();
    }

    public Enumeration entries() {
        return new JarEnumerator(jar.entries());
    }

    public InputStream getEntry(String name) {
        try {
            return jar.getInputStream(jar.getEntry(name));
        } catch(IOException e) {
            return null;
        }
    }

    private static class JarEnumerator implements Enumeration {
        private Enumeration entries;

        public JarEnumerator(Enumeration entries) {
            this.entries = entries;
        }

        public boolean hasMoreElements() {
            return entries.hasMoreElements();
        }

        public Object nextElement() {
            return ((JarEntry)entries.nextElement()).getName();
        }
    }
}
