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

import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ModuleType;

import org.dom4j.Document;

/**
 * The DDBeanRootImpl provides an implementation for javax.enterprise.deploy.model.DDBeanRoot.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:43 $
 */
public class DDBeanRootImpl extends DDBeanImpl implements DDBeanRoot {
    
    private String moduleDTDVersion;
    private ModuleType type;
    private Document document;

    /** Creates a new instance of DDBeanRootImpl */
    DDBeanRootImpl(Document document) {
        super(document, "/");
        this.document = document;
        moduleDTDVersion = document.getRootElement().valueOf("@version");
        if ("ejb-jar".equalsIgnoreCase(id)) {
            type = ModuleType.EJB;
        }
    }
    
    public DeployableObject getDeployableObject() {
        return null;
    }
    
    public String getModuleDTDVersion() {
        return moduleDTDVersion;
    }
    
    public ModuleType getType() {
        return type;
    }
    
    /**
     * @return returns null because this is the root object.
     */
    public DDBeanRoot getRoot() {
        return null;
    }

    public String getDDBeanRootVersion() {
        // @todo implement
        return null;
    }

    public String getFilename() {
        // @todo implement
        return null;
    }

    public Document getDocument() {
        return document;
    }
}