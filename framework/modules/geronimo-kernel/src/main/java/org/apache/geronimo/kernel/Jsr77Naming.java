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
package org.apache.geronimo.kernel;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Arrays;

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
    public static final String J2EE_TYPE = "j2eeType";
    public static final String J2EE_NAME = "name";
    private static final String INVALID_GENERIC_PARENT_TYPE = "GBean";

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

    public AbstractName createSiblingName(AbstractName parentAbstractName, String name, String type) {
        Map nameMap = new HashMap(parentAbstractName.getName());

        nameMap.put(J2EE_TYPE, type);
        nameMap.put(J2EE_NAME, name);

        return new AbstractName(parentAbstractName.getArtifact(),
                nameMap,
                createObjectName(nameMap));
    }

    public AbstractName createChildName(AbstractName parentAbstractName, Artifact artifact, String name, String type) {
        if (name == null) {
            throw new NullPointerException("No name supplied");
        }
        if (type == null) {
            throw new NullPointerException("No type supplied");
        }
        Map nameMap = new HashMap(parentAbstractName.getName());

        String parentType = (String) nameMap.remove(J2EE_TYPE);
        String parentName = (String) nameMap.remove(J2EE_NAME);
        if (parentType == null) {
            throw new IllegalArgumentException("parent name must have a j2eeType name component");
        }
        if (INVALID_GENERIC_PARENT_TYPE.equals(parentType)) {
            throw new IllegalArgumentException("You can't create a child of a generic typed gbean");
        }
        nameMap.put(parentType, parentName);
        nameMap.put(J2EE_TYPE, type);
        nameMap.put(J2EE_NAME, name);

        return new AbstractName(artifact,
                nameMap,
                createObjectName(nameMap));
    }

    @Override
    public String toOsgiJndiName(AbstractName abstractName) {
        return abstractName.getArtifact().getGroupId() + "/" +
                            abstractName.getArtifact().getArtifactId() + "/" +
                            abstractName.getNameProperty("j2eeType") + "/" +
                            abstractName.getNameProperty("name");
    }

    /**
     * @deprecated objectnames are being removed
     */
    public static ObjectName createObjectName(Map nameMap) {
        Hashtable objectNameMap = new Hashtable(nameMap);
        String type = (String) nameMap.get(J2EE_TYPE);
        if ("JVM".equals(type)) {
            objectNameMap.keySet().retainAll(Arrays.asList(new String[] {J2EE_TYPE, J2EE_NAME, "J2EEServer"}));
            objectNameMap.put("J2EEServer", DEFAULT_SERVER_NAME);
        } else if ("J2EEDomain".equals(type)) {
            //special case J2EEDomain gbean
            objectNameMap.clear();
            objectNameMap.put(J2EE_TYPE, "J2EEDomain");
            objectNameMap.put(J2EE_NAME, DEFAULT_DOMAIN_NAME);
        } else if ("J2EEServer".equals(type)) {
            //special case J2EEServer gbean
            objectNameMap.clear();
            objectNameMap.put(J2EE_TYPE, "J2EEServer");
            objectNameMap.put(J2EE_NAME, DEFAULT_SERVER_NAME);
        } else {
            objectNameMap.put("J2EEServer", DEFAULT_SERVER_NAME);
        }

        ObjectName moduleObjectName;
        try {
            moduleObjectName = ObjectName.getInstance(DEFAULT_DOMAIN_NAME, objectNameMap);
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e);
        }
        return moduleObjectName;
    }

}
