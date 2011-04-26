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
package org.apache.geronimo.deployment.service.jsr88;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.deployment.xbeans.DependenciesType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 * Represents an environmentType (e.g. an environment element) in a Geronimo
 * deployment plan.
 *
 * @version $Rev$ $Date$
 */
public class EnvironmentData extends XmlBeanSupport {
    static final SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.typeLoaderForClassLoader(EnvironmentType.class.getClassLoader());

    private Artifact configId;
    private Artifact[] dependencies = new Artifact[0];

    public EnvironmentData() {
        super(null);
    }

    public EnvironmentData(EnvironmentType dependency) {
        super(null);
        configure(dependency);
    }

    protected EnvironmentType getEnvironmentType() {
        return (EnvironmentType) getXmlObject();
    }

    public void configure(EnvironmentType env) {
        setXmlObject(env);
        if(env.isSetModuleId()) {
            configId = new Artifact(env.getModuleId());
        }
        if(env.isSetDependencies()) {
            DependenciesType deps = env.getDependencies();
            dependencies = new Artifact[deps.getDependencyArray().length];
            for (int i = 0; i < dependencies.length; i++) {
                dependencies[i] = new Artifact(deps.getDependencyArray(i));
            }
        }
    }

    // ----------------------- JavaBean Properties for environmentType ----------------------

    public Artifact getConfigId() {
        return configId;
    }

    public void setConfigId(Artifact configId) {
        Artifact old = this.configId;
        this.configId = configId;
        if((old == null && configId == null) || (old != null&& old == configId)) {
            return;
        }
        if(old != null) {
            getEnvironmentType().unsetModuleId();
        }
        if(configId != null) {
            configId.configure(getEnvironmentType().addNewModuleId());
        }
        pcs.firePropertyChange("moduleId", old, configId);
    }

    public Artifact[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(Artifact[] dependencies) {
        Artifact[] old = this.dependencies;
        Set before = new HashSet();
        for (int i = 0; i < old.length; i++) {
            before.add(old[i]);
        }
        this.dependencies = dependencies;
        // Handle current or new dependencies
        for (int i = 0; i < dependencies.length; i++) {
            Artifact dep = dependencies[i];
            if(dep.getArtifactType() == null) {
                if(!getEnvironmentType().isSetDependencies()) {
                    getEnvironmentType().addNewDependencies();
                }
                dep.configure(getEnvironmentType().getDependencies().addNewDependency());
            } else {
                before.remove(dep);
            }
        }
        // Handle removed or new dependencies
        for (Iterator it = before.iterator(); it.hasNext();) {
            Artifact adapter = (Artifact) it.next();
            ArtifactType all[] = getEnvironmentType().getDependencies().getDependencyArray();
            for (int i = 0; i < all.length; i++) {
                if(all[i] == adapter) {
                    getEnvironmentType().getDependencies().removeDependency(i);
                    break;
                }
            }
        }
        if(getEnvironmentType().isSetDependencies() && getEnvironmentType().getDependencies().getDependencyArray().length == 0) {
            getEnvironmentType().unsetDependencies();
        }
        pcs.firePropertyChange("dependencies", old, dependencies);
    }

    public String[] getHiddenClasses() {
        return getEnvironmentType().getHiddenClasses().getFilterArray();
    }

    public void setHiddenClasses(String[] hidden) {
        String[] old = getEnvironmentType().isSetHiddenClasses() ? getEnvironmentType().getHiddenClasses().getFilterArray() : null;
        if(!getEnvironmentType().isSetHiddenClasses()) {
            getEnvironmentType().addNewHiddenClasses();
        }
        getEnvironmentType().getHiddenClasses().setFilterArray(hidden);
        pcs.firePropertyChange("hiddenClasses", old, hidden);
    }

    public String[] getNonOverridableClasses() {
        return getEnvironmentType().getNonOverridableClasses().getFilterArray();
    }

    public void setNonOverridableClasses(String[] fixed) {
        String[] old = getEnvironmentType().isSetNonOverridableClasses() ? getEnvironmentType().getNonOverridableClasses().getFilterArray() : null;
        if(!getEnvironmentType().isSetNonOverridableClasses()) {
            getEnvironmentType().addNewNonOverridableClasses();
        }
        getEnvironmentType().getNonOverridableClasses().setFilterArray(fixed);
        pcs.firePropertyChange("nonOverridableClasses", old, fixed);
    }

    public boolean isInverseClassLoading() {
        return getEnvironmentType().isSetInverseClassloading();
    }

    public void setInverseClassLoading(boolean inverse) {
        boolean old = isInverseClassLoading();
        if(!inverse) {
            if(old) {
                getEnvironmentType().unsetInverseClassloading();
            }
        } else {
            if(!old) {
                getEnvironmentType().addNewInverseClassloading();
            }
        }
        pcs.firePropertyChange("inverseClassLoading", old, inverse);
    }

    public boolean isSuppressDefaultEnvironment() {
        return getEnvironmentType().isSetSuppressDefaultEnvironment();
    }

    public void setSuppressDefaultEnvironment(boolean suppress) {
        boolean old = isSuppressDefaultEnvironment();
        if(!suppress) {
            if(old) {
                getEnvironmentType().unsetSuppressDefaultEnvironment();
            }
        } else {
            if(!old) {
                getEnvironmentType().addNewSuppressDefaultEnvironment();
            }
        }
        pcs.firePropertyChange("suppressDefaultEnvironment", old, suppress);
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return SCHEMA_TYPE_LOADER;
    }
}
