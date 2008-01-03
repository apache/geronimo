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

package org.apache.geronimo.naming.deployment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanLocatorType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;

/**
 * @version $Rev:385232 $ $Date$
 */
public class ENCConfigBuilder {

    public static AbstractNameQuery getGBeanQuery(String j2eeType, GerGbeanLocatorType gerGbeanLocator) {
        AbstractNameQuery abstractNameQuery;
        if (gerGbeanLocator.isSetGbeanLink()) {
            //exact match
            String linkName = gerGbeanLocator.getGbeanLink().trim();
            abstractNameQuery = buildAbstractNameQuery(null, null, linkName, j2eeType, null);

        } else {
            GerPatternType patternType = gerGbeanLocator.getPattern();
            //construct name from components
            abstractNameQuery = buildAbstractNameQuery(patternType, j2eeType, null, null);
        }
        //TODO check that the query is satisfied.
        return abstractNameQuery;
    }

    public static AbstractNameQuery buildAbstractNameQuery(GerPatternType pattern, String type, String moduleType, Set interfaceTypes) {
        return buildAbstractNameQueryFromPattern(pattern, "car", type, moduleType, interfaceTypes);
    }
    
    public static AbstractNameQuery buildAbstractNameQueryFromPattern(GerPatternType pattern, String artifactType, String type, String moduleType, Set interfaceTypes)  {
        String groupId = pattern.isSetGroupId() ? pattern.getGroupId().trim() : null;
        String artifactid = pattern.isSetArtifactId() ? pattern.getArtifactId().trim() : null;
        String version = pattern.isSetVersion() ? pattern.getVersion().trim() : null;
        String module = pattern.isSetModule() ? pattern.getModule().trim() : null;
        String name = pattern.getName().trim();

        Artifact artifact = artifactid != null ? new Artifact(groupId, artifactid, version, artifactType) : null;
        Map nameMap = new HashMap();
        nameMap.put("name", name);
        if (type != null) {
            nameMap.put("j2eeType", type);
        }
        if (module != null && moduleType != null) {
            nameMap.put(moduleType, module);
        }
        if (interfaceTypes != null) {
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
        if (module != null && moduleType != null) {        
            nameMap.put(moduleType, module);
        }
        return new AbstractNameQuery(configId, nameMap);
    }

}
