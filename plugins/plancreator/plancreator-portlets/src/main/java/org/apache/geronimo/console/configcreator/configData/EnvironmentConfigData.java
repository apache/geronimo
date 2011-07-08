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
package org.apache.geronimo.console.configcreator.configData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.DependenciesType;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * 
 * @version $Rev$ $Date$
 */
public class EnvironmentConfigData {
    private EnvironmentType environment;

    private HashSet<String> dependenciesSet = new HashSet<String>();

    public EnvironmentConfigData(EnvironmentType environment) {
        this.environment = environment;
        DependenciesType dependencies = environment.getDependencies();
        if(dependencies != null) {
            DependencyType[] depArray = dependencies.getDependencyArray();
            for(int i = 0; i < depArray.length; i++) {
                DependencyType d = depArray[i];
                Artifact artifact = new Artifact(d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getType());
                dependenciesSet.add(artifact.toString());
            }
        }
    }

    public void parseEnvironment(Environment env) {
        ArtifactType moduleId = environment.addNewModuleId();
        Artifact configId = env.getConfigId();
        moduleId.setGroupId(configId.getGroupId());
        moduleId.setArtifactId(configId.getArtifactId());
        moduleId.setVersion(configId.getVersion().toString());
        moduleId.setType(configId.getType());
        //List<Dependency> deps = env.getDependencies();
        //for (int i = 0; i < deps.size(); i++) {
        //    String depString = deps.get(i).toString();
        //    dependenciesSet.add(depString.substring(6, depString.length() - 1));
        //}
    }

    public void readEnvironmentData(PortletRequest request) {
        ArtifactType moduleId = environment.getModuleId();
        moduleId.setArtifactId(request.getParameter("artifactId"));
        moduleId.setGroupId(request.getParameter("groupId"));
        moduleId.setVersion(request.getParameter("version"));
        moduleId.setType(request.getParameter("type"));

        String hiddenClassesString = request.getParameter("hiddenClasses");
        if (!isEmpty(hiddenClassesString)) {
            String[] hiddenClasses = getNonEmptyStrings(hiddenClassesString.split(";"));
            if (hiddenClasses.length > 0) {
                environment.addNewHiddenClasses().setFilterArray(hiddenClasses);
            }
        }
        String nonOverridableClassesString = request.getParameter("nonOverridableClasses");
        if (!isEmpty(nonOverridableClassesString)) {
            String[] nonOverridableClasses = getNonEmptyStrings(nonOverridableClassesString.split(";"));
            if (nonOverridableClasses.length > 0) {
                environment.addNewNonOverridableClasses().setFilterArray(nonOverridableClasses);
            }
        }
        if ("true".equalsIgnoreCase(request.getParameter("inverseClassLoading"))) {
            environment.addNewInverseClassloading();
        }
    }

    public void storeDependencies() {
        if (environment.isSetDependencies()) {
            environment.unsetDependencies();
        }
        DependenciesType dependencies = environment.addNewDependencies();
        Iterator<String> iter = dependenciesSet.iterator();
        while (iter.hasNext()) {
            populateDependency(dependencies.addNewDependency(), iter.next());
        }
    }

    private void populateDependency(DependencyType dep, String dependencyString) {
        Artifact artifact = Artifact.create(dependencyString.trim());
        dep.setArtifactId(artifact.getArtifactId());
        if (artifact.getGroupId() != null) {
            dep.setGroupId(artifact.getGroupId());
        }
        if (artifact.getType() != null) {
            dep.setType(artifact.getType());
        }
        if (artifact.getVersion() != null) {
            dep.setVersion(artifact.getVersion().toString());
        }
    }

    public HashSet<String> getDependenciesSet() {
        return dependenciesSet;
    }

    public List<String> getDependencies() {
        List<String> dependencies = new ArrayList<String>();
        Iterator<String> iter = getDependenciesSet().iterator();
        while (iter.hasNext()) {
            dependencies.add(iter.next());
        }
        return dependencies;
    }

    public String getHiddenClassesString() {
        StringBuilder str = new StringBuilder("");
        if (environment.isSetHiddenClasses()) {
            String[] hiddenClasses = environment.getHiddenClasses().getFilterArray();
            for (int i = 0; i < hiddenClasses.length; i++) {
                str.append(hiddenClasses[i]);
            }
        }
        return str.toString();
    }

    public String getNonOverridableClassesString() {
        StringBuilder str = new StringBuilder("");
        if (environment.isSetNonOverridableClasses()) {
            String[] nonOverridableClasses = environment.getNonOverridableClasses().getFilterArray();
            for (int i = 0; i < nonOverridableClasses.length; i++) {
                str.append(nonOverridableClasses[i]);
            }
        }
        return str.toString();
    }

    public boolean getInverseClassLoading() {
        return environment.isSetInverseClassloading();
    }

    private String[] getNonEmptyStrings(String[] strings) {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].trim().length() > 0)
                list.add(strings[i].trim());
        }
        return list.toArray(new String[list.size()]);
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().equals("");
    }

    public EnvironmentType getEnvironment() {
        return environment;
    }
}
