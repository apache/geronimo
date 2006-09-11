/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.naming.deployment;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSet;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;

/**
 * @version $Rev:$ $Date:$
 */
public abstract class AbstractNamingBuilder implements NamingBuilder {

    private final Environment defaultEnvironment;

    protected AbstractNamingBuilder() {
        defaultEnvironment = null;
    }

    protected AbstractNamingBuilder(Environment defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) {
        if (willMergeEnvironment(specDD, plan)) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        }
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return false;
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
    }

    protected static String getStringValue(org.apache.geronimo.xbeans.j2ee.String string) {
        if (string == null) {
            return null;
        }
        String s = string.getStringValue();
        return s == null ? null : s.trim();
    }

    public static AbstractNameQuery buildAbstractNameQuery(GerPatternType pattern, String type, String moduleType, Set interfaceTypes) {
        String groupId = pattern.isSetGroupId() ? pattern.getGroupId().trim() : null;
        String artifactid = pattern.isSetArtifactId() ? pattern.getArtifactId().trim() : null;
        String version = pattern.isSetVersion() ? pattern.getVersion().trim() : null;
        String module = pattern.isSetModule() ? pattern.getModule().trim() : null;
        String name = pattern.getName().trim();

        Artifact artifact = artifactid != null ? new Artifact(groupId, artifactid, version, null) : null;
        Map nameMap = new HashMap();
        nameMap.put("name", name);
        if (type != null) {
            nameMap.put("j2eeType", type);
        }
        if (module != null && moduleType != null) {
            nameMap.put(moduleType, module);
        }
        if(interfaceTypes != null) {
            Set trimmed = new HashSet();
            for (Iterator it = interfaceTypes.iterator(); it.hasNext();) {
                String intf = (String) it.next();
                trimmed.add(intf == null ? null : intf.trim());
            }
            interfaceTypes = trimmed;
        }
        return new AbstractNameQuery(artifact, nameMap, interfaceTypes);
    }

    public static AbstractNameQuery buildAbstractNameQuery(Artifact configId, String module, String name, String type, String moduleType) {
        Map nameMap = new HashMap();
        nameMap.put("name", name);
        if (type != null) {
            nameMap.put("j2eeType", type);
        }
        if (module != null) {
            nameMap.put(moduleType, module);
        }
        return new AbstractNameQuery(configId, nameMap);
    }

    public static Class assureInterface(String interfaceName, String superInterfaceName, String interfaceType, ClassLoader cl) throws DeploymentException {
        if (interfaceName == null || interfaceName.equals("")) {
            throw new DeploymentException("interface name cannot be blank");
        }
        Class clazz;
        try {
            clazz = cl.loadClass(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(interfaceType + " interface class not found: " + interfaceName);
        }
        if (!clazz.isInterface()) {
            throw new DeploymentException(interfaceType + " interface is not an interface: " + interfaceName);
        }
        Class superInterface;
        try {
            superInterface = cl.loadClass(superInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Class " + superInterfaceName + " could not be loaded");
        }
        if (!superInterface.isAssignableFrom(clazz)) {
            throw new DeploymentException(interfaceType + " interface does not extend " + superInterfaceName + ": " + interfaceName);
        }
        return clazz;
    }


}
