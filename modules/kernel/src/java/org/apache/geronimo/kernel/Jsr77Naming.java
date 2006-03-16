/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.kernel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class Jsr77Naming extends Naming {
    private static final String DEFAULT_DOMAIN_NAME = "geronimo";
    private static final String DEFAULT_SERVER_NAME = "geronimo";
    private static final String J2EE_TYPE = "j2eeType";
    private static final String J2EE_NAME = "name";

    public Jsr77Naming() {
    }

    public AbstractName createRootName(Artifact artifact, String name, String type) {
        Map nameMap = new HashMap();
        nameMap.put(J2EE_TYPE, type);
        nameMap.put(J2EE_NAME, name);

        return new AbstractName(artifact,
                nameMap,
                createObjectName(nameMap));
    }

    public AbstractName createChildName(AbstractName parentAbstractName, String name, String type) {
        return createChildName(parentAbstractName, parentAbstractName.getArtifact(), name, type);
    }

    public AbstractName createChildName(AbstractName parentAbstractName, Artifact artifact, String name, String type) {
        Map nameMap = new HashMap(parentAbstractName.getName());

        String parentType = (String) nameMap.remove(J2EE_TYPE);
        String parentName = (String) nameMap.remove(J2EE_NAME);
        nameMap.put(parentType, parentName);
        nameMap.put(J2EE_TYPE, type);
        nameMap.put(J2EE_NAME, name);

        return new AbstractName(artifact,
                nameMap,
                createObjectName(nameMap));
    }

    /**
     * @deprecated objectnames are being removed
     */
    private static ObjectName createObjectName(Map nameMap) {
        Hashtable objectNameMap = new Hashtable(nameMap);
        objectNameMap.put("J2EEServer", DEFAULT_SERVER_NAME);

        ObjectName moduleObjectName = null;
        try {
            moduleObjectName = ObjectName.getInstance(DEFAULT_DOMAIN_NAME, objectNameMap);
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e);
        }
        return moduleObjectName;
    }

}
