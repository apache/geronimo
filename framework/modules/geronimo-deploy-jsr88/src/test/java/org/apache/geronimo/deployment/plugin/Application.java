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

package org.apache.geronimo.deployment.plugin;

import java.util.Enumeration;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.enterprise.deploy.model.J2eeApplicationObject;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.shared.ModuleType;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class Application implements J2eeApplicationObject {
    private final DDBeanRoot root;

    public Application(DDBeanRoot root) {
        this.root = root;
    }

    public void addXpathListener(ModuleType type, String xpath, XpathListener xpl) {
        throw new UnsupportedOperationException();
    }

    public Enumeration entries() {
        throw new UnsupportedOperationException();
    }

    public DDBean[] getChildBean(ModuleType type, String xpath) {
        throw new UnsupportedOperationException();
    }

    public DDBean[] getChildBean(String xpath) {
        throw new UnsupportedOperationException();
    }

    public Class getClassFromScope(String className) {
        throw new UnsupportedOperationException();
    }

    public DDBeanRoot getDDBeanRoot() {
        return root;
    }

    public DDBeanRoot getDDBeanRoot(String filename) throws FileNotFoundException, DDBeanCreateException {
        throw new UnsupportedOperationException();
    }

    public DeployableObject getDeployableObject(String uri) {
        throw new UnsupportedOperationException();
    }

    public DeployableObject[] getDeployableObjects() {
        throw new UnsupportedOperationException();
    }

    public DeployableObject[] getDeployableObjects(ModuleType type) {
        throw new UnsupportedOperationException();
    }

    public InputStream getEntry(String name) {
        throw new UnsupportedOperationException();
    }

    public String getModuleDTDVersion() {
        throw new UnsupportedOperationException();
    }

    public String[] getModuleUris() {
        throw new UnsupportedOperationException();
    }

    public String[] getModuleUris(ModuleType type) {
        throw new UnsupportedOperationException();
    }

    public String[] getText(ModuleType type, String xpath) {
        throw new UnsupportedOperationException();
    }

    public String[] getText(String xpath) {
        throw new UnsupportedOperationException();
    }

    public ModuleType getType() {
        return ModuleType.EAR;
    }

    public void removeXpathListener(ModuleType type, String xpath, XpathListener xpl) {
        throw new UnsupportedOperationException();
    }
}
