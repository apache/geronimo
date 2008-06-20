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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.geronimo.console.configcreator.AbstractHandler;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

/**
 * @version $Rev$ $Date$
 */
@RemoteProxy
public class EARHelper {
    //private EARConfigData earConfig;

    /**
     * 
     * Dojo tree expects a JSON in the below format:
     * { label: 'name',
     *   identifier: 'name',
     *   items: [
     *     { name:'Web Modules', type:'webModules',
     *       children: [
     *         { name:'module-name-1', type:'webModule' },
     *         ...
     *         { name:'module-name-n', type:'webModule' }
     *       ]
     *     },
     *     { name:'EJB Modules', type: 'ejbModules',
     *       children: [
     *         { name:'module-name-1', type:'ejbModule' },
     *         ...
     *         { name:'module-name-n', type:'ejbModule' }
     *       ]
     *     }
     *   ]
     * }
     * 
     */
    public static class TreeJson implements Serializable {
        String identifier = "name";
        String label = "name";
        List<TreeNode> items = new ArrayList<TreeNode>();

        public TreeJson() {
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<TreeNode> getItems() {
            return items;
        }

        public void setItems(List<TreeNode> items) {
            this.items = items;
        }
    }

    @DataTransferObject
    public static class EarJsonTree extends TreeJson implements Serializable {

        public EarJsonTree(EARConfigData earConfig) {
            if (earConfig.getWebModules().size() > 0) {
                TreeFolder webModules = new TreeFolder("Web Modules", "folder");
                items.add(webModules);
                for (Enumeration<String> e = earConfig.getWebModules().keys(); e.hasMoreElements();) {
                    String moduleName = e.nextElement();
                    webModules.getChildren().add(new TreeNode(moduleName, "webModule"));
                }
            }
            if (earConfig.getEjbModules().size() > 0) {
                TreeFolder ejbModules = new TreeFolder("EJB Modules", "folder");
                items.add(ejbModules);
                for (Enumeration<String> e = earConfig.getEjbModules().keys(); e.hasMoreElements();) {
                    String moduleName = e.nextElement();
                    ejbModules.getChildren().add(new TreeNode(moduleName, "ejbModule"));
                }
            }
        }
    }

    @DataTransferObject
    public static class TreeNode implements Serializable {
        String name;
        String type;

        public TreeNode(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    @DataTransferObject
    public static class TreeFolder extends TreeNode implements Serializable {
        List<TreeNode> children = new ArrayList<TreeNode>();

        public TreeFolder(String name, String type) {
            super(name, type);
        }

        public List<TreeNode> getChildren() {
            return children;
        }

        public void setChildren(List<TreeNode> children) {
            this.children = children;
        }
    }

    public EARHelper() {
        //earConfig = (EARConfigData) WebContextFactory.get().getHttpServletRequest().getSession().getAttribute(
        //        AbstractHandler.EAR_CONFIG_DATA_ID);
    }

    private EARConfigData getEarConfigData(HttpServletRequest request) {
        return (EARConfigData) request.getSession().getAttribute(AbstractHandler.EAR_CONFIG_DATA_ID);
    }

    @RemoteMethod
    public EarJsonTree getEarTree(HttpServletRequest request) {
        return new EarJsonTree(getEarConfigData(request));
    }

    @DataTransferObject
    public static class EnvironmentJson implements Serializable {
        String groupId;
        String artifactId;
        String version;
        String type;
        String hiddenClasses;
        String nonOverridableClasses;
        List<String> inverseClassLoading = new ArrayList<String>();

        public EnvironmentJson() {
        }

        public EnvironmentJson(EnvironmentType environment) {
            ArtifactType moduleId = environment.getModuleId();
            groupId = moduleId.getGroupId();
            artifactId = moduleId.getArtifactId();
            version = moduleId.getVersion();
            type = moduleId.getType();
            if (environment.isSetHiddenClasses()) {
                hiddenClasses = mergeStrings(environment.getHiddenClasses().getFilterArray());
            }
            if (environment.isSetNonOverridableClasses()) {
                nonOverridableClasses = mergeStrings(environment.getNonOverridableClasses().getFilterArray());
            }
            if (environment.isSetInverseClassloading()) {
                inverseClassLoading.add("true");
            }
        }

        private String mergeStrings(String[] strArray) {
            StringBuffer str = new StringBuffer("");
            for (int i = 0; i < strArray.length; i++) {
                str.append(strArray[i] + ";");
            }
            return str.toString();
        }

        public void save(EnvironmentType environment) {
            ArtifactType moduleId = environment.getModuleId();
            moduleId.setArtifactId(artifactId);

            if (moduleId.isSetGroupId()) {
                moduleId.unsetGroupId();
            }
            if (!isEmpty(groupId)) {
                moduleId.setGroupId(groupId);
            }

            if (moduleId.isSetVersion()) {
                moduleId.unsetVersion();
            }
            if (!isEmpty(version)) {
                moduleId.setVersion(version);
            }

            if (moduleId.isSetType()) {
                moduleId.unsetType();
            }
            if (!isEmpty(type)) {
                moduleId.setType(type);
            }

            if (environment.isSetHiddenClasses()) {
                environment.unsetHiddenClasses();
            }
            if (!isEmpty(hiddenClasses)) {
                String[] splitStrings = getNonEmptyStrings(hiddenClasses.split(";"));
                if (splitStrings.length > 0) {
                    environment.addNewHiddenClasses().setFilterArray(splitStrings);
                }
            }
            if (environment.isSetNonOverridableClasses()) {
                environment.unsetNonOverridableClasses();
            }
            if (!isEmpty(nonOverridableClasses)) {
                String[] splitStrings = getNonEmptyStrings(nonOverridableClasses.split(";"));
                if (splitStrings.length > 0) {
                    environment.addNewNonOverridableClasses().setFilterArray(splitStrings);
                }
            }
            if (environment.isSetInverseClassloading()) {
                environment.unsetInverseClassloading();
            }
            if (inverseClassLoading.size() > 0 && "true".equalsIgnoreCase(inverseClassLoading.get(0))) {
                environment.addNewInverseClassloading();
            }
        }

        private String[] getNonEmptyStrings(String[] strings) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < strings.length; i++) {
                if (strings[i].trim().length() > 0)
                    list.add(strings[i].trim());
            }
            return list.toArray(new String[list.size()]);
        }

        private boolean isEmpty(String s) {
            return s == null || s.trim().equals("");
        }

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getHiddenClasses() {
            return hiddenClasses;
        }

        public void setHiddenClasses(String hiddenClasses) {
            this.hiddenClasses = hiddenClasses;
        }

        public String getNonOverridableClasses() {
            return nonOverridableClasses;
        }

        public void setNonOverridableClasses(String nonOverridableClasses) {
            this.nonOverridableClasses = nonOverridableClasses;
        }

        public List<String> getInverseClassLoading() {
            return inverseClassLoading;
        }

        public void setInverseClassLoading(List<String> inverseClassLoading) {
            this.inverseClassLoading = inverseClassLoading;
        }
    }

    @RemoteMethod 
    public EnvironmentJson getEnvironmentJson(HttpServletRequest request) {
        return new EnvironmentJson(getEarConfigData(request).getEnvironmentConfig().getEnvironment());
    }

    @RemoteMethod
    public void saveEnvironmentJson(HttpServletRequest request, EnvironmentJson envJson){
        envJson.save(getEarConfigData(request).getEnvironmentConfig().getEnvironment());
    }

    @DataTransferObject
    public static class DependencyItem implements Serializable {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @DataTransferObject
    public static class DependenciesJsonTree implements Serializable {
        String identifier = "name";
        String label = "name";
        List<DependencyItem> items = new ArrayList<DependencyItem>();

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<DependencyItem> getItems() {
            return items;
        }

        public void setItems(List<DependencyItem> items) {
            this.items = items;
        }

        public DependenciesJsonTree() {
        }

        public DependenciesJsonTree(EnvironmentConfigData environmentConfig) {
            Iterator<String> iter = environmentConfig.getDependenciesSet().iterator();
            while (iter.hasNext()) {
                String depString = iter.next();
                DependencyItem item = new DependencyItem();
                item.setName(depString);
                items.add(item);
            }
        }

        public void save(HashSet<String> dependenciesSet) {
            dependenciesSet.clear();
            for (int i = 0; i < items.size(); i++) {
                String depString = items.get(i).getName();
                dependenciesSet.add(depString);
            }
        }
    }

    @RemoteMethod
    public DependenciesJsonTree getDependenciesJsonTree(HttpServletRequest request) {
        return new DependenciesJsonTree(getEarConfigData(request).getEnvironmentConfig());
    }

    @RemoteMethod
    public void saveDependenciesJsonTree(HttpServletRequest request, DependenciesJsonTree dependenciesJsonTree){
        dependenciesJsonTree.save(getEarConfigData(request).getEnvironmentConfig().getDependenciesSet());
    }

    @RemoteMethod
    public String getGeneratedPlan(HttpServletRequest request) {
        return getEarConfigData(request).getDeploymentPlan();
    }

    /*@RemoteMethod
    public String[] getWebModules() {
        return null;
    }

    @RemoteMethod
    public String[] getEjbModules() {
        return null;
    }

    @RemoteMethod
    public String[] getSessionBeans(String ejbModuleName) {
        return null;
    }

    @RemoteMethod
    public String[] getMDBs(String ejbModuleName) {
        return null;
    }*/
}
