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

package org.apache.geronimo.deployment.tools.loader;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.deployment.tools.DDBeanRootImpl;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:39 $
 */
public abstract class AbstractDeployable implements DeployableObject {
    private final URL moduleURL;
    private final ModuleType type;
    private final DDBeanRoot root;
    private final ClassLoader rootCL;
    private final List entries;

    protected AbstractDeployable(ModuleType type, URL moduleURL, String rootDD) throws DDBeanCreateException {
        this.type = type;
        this.moduleURL = moduleURL;
        rootCL = new URLClassLoader(new URL[] {moduleURL}, Thread.currentThread().getContextClassLoader());
        root = new DDBeanRootImpl(this, rootCL.getResource(rootDD));

        // @todo make this work with unpacked
        entries = new ArrayList();
        InputStream is = null;
        try {
            is = moduleURL.openStream();
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        } catch (IOException e) {
            throw (DDBeanCreateException) new DDBeanCreateException("Unable to create list of entries").initCause(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    public ModuleType getType() {
        return type;
    }

    public DDBeanRoot getDDBeanRoot() {
        return root;
    }

    public DDBeanRoot getDDBeanRoot(String filename) throws FileNotFoundException, DDBeanCreateException {
        try {
            return new DDBeanRootImpl(null, new URL(moduleURL, filename));
        } catch (MalformedURLException e) {
            throw (DDBeanCreateException) new DDBeanCreateException("Unable to construct URL for "+filename).initCause(e);
        }
    }

    public DDBean[] getChildBean(String xpath) {
        return root.getChildBean(xpath);
    }

    public String[] getText(String xpath) {
        return root.getText(xpath);
    }

    public Enumeration entries() {
        return Collections.enumeration(entries);
    }

    public InputStream getEntry(String name) {
        return rootCL.getResourceAsStream(name);
    }

    protected ClassLoader getModuleLoader() {
        return rootCL;
    }

    public Class getClassFromScope(String className) {
        try {
            return getModuleLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            // spec does not allow an Exception
            return null;
        }
    }

    public String getModuleDTDVersion() {
        throw new UnsupportedOperationException();
    }
}
