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

package org.apache.geronimo.deployment.tools.loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.deployment.tools.DDBeanRootImpl;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractDeployable implements DeployableObject {
    private final Bundle bundle;
    private final ModuleType type;
    private final DDBeanRoot root;
    private final List<String> entries;

    protected AbstractDeployable(ModuleType type, Bundle bundle, String rootDD) throws DDBeanCreateException {
        this.type = type;
        this.bundle = bundle;
        URL dd = bundle.getResource(rootDD);
        root = new DDBeanRootImpl(this, dd);

        entries = new ArrayList<String>();
        Enumeration<String> paths = bundle.getEntryPaths("/");
        //TODO WTF?? if statement seems to be required????
        while (paths.hasMoreElements()) {
            String entry = paths.nextElement();
            entries.add(entry);
        }
    }

    public ModuleType getType() {
        return type;
    }

    public DDBeanRoot getDDBeanRoot() {
        return root;
    }

    public DDBeanRoot getDDBeanRoot(String filename) throws FileNotFoundException, DDBeanCreateException {
        URL resource = bundle.getResource(filename);
        if (resource == null) {
            throw new DDBeanCreateException("Unable to construct URL for " + filename);
        }
        return new DDBeanRootImpl(null, resource);
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
        try {
            URL resource = bundle.getResource(name);
            if (resource == null) {
                return null;
            }
            return resource.openStream();
        } catch (IOException e) {
            return null;
//            throw new IllegalStateException("Could not open straem to entry: " + name);
        }
    }

    protected Bundle getModuleBundle() {
        return bundle;
    }

    public Class getClassFromScope(String className) {
        try {
            return bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            // spec does not allow an Exception
            return null;
        }
    }

    public String getModuleDTDVersion() {
        throw new UnsupportedOperationException();
    }
}
