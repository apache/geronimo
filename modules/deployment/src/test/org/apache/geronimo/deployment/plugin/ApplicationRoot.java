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

package org.apache.geronimo.deployment.plugin;

import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ModuleType;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:39 $
 */
public class ApplicationRoot implements DDBeanRoot {
    public void addXpathListener(String xpath, XpathListener xpl) {
        throw new UnsupportedOperationException();
    }

    public String[] getAttributeNames() {
        throw new UnsupportedOperationException();
    }

    public String getAttributeValue(String attrName) {
        throw new UnsupportedOperationException();
    }

    public DDBean[] getChildBean(String xpath) {
        throw new UnsupportedOperationException();
    }

    public String getDDBeanRootVersion() {
        throw new UnsupportedOperationException();
    }

    public DeployableObject getDeployableObject() {
        throw new UnsupportedOperationException();
    }

    public String getFilename() {
        throw new UnsupportedOperationException();
    }

    public String getId() {
        throw new UnsupportedOperationException();
    }

    public String getModuleDTDVersion() {
        throw new UnsupportedOperationException();
    }

    public DDBeanRoot getRoot() {
        throw new UnsupportedOperationException();
    }

    public String getText() {
        throw new UnsupportedOperationException();
    }

    public String[] getText(String xpath) {
        throw new UnsupportedOperationException();
    }

    public ModuleType getType() {
        throw new UnsupportedOperationException();
    }

    public String getXpath() {
        throw new UnsupportedOperationException();
    }

    public void removeXpathListener(String xpath, XpathListener xpl) {
        throw new UnsupportedOperationException();
    }
}
