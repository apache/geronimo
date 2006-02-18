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
package org.apache.geronimo.kernel.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Rev$ $Date$
 */
public class DefaultArtifactManager implements ArtifactManager {
    private final Map artifactsByLoader = new HashMap();
    private final Map artifactsByTemplate = new HashMap();

    public void loadArtifacts(Artifact loader, Set artifacts) {
        if (!loader.isResolved()) throw new IllegalArgumentException("loader is not a resolved artifact: " + loader);
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            if (!artifact.isResolved()) {
                throw new IllegalArgumentException("artifact is not a resolved artifact: " + artifact);
            }
        }

        synchronized (this) {
            if (artifactsByLoader.containsKey(loader)) throw new IllegalArgumentException("loader has already declared artifacts: "+ loader);
            artifactsByLoader.put(loader, artifacts);
            addArtifactByTempate(loader);

            for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                addArtifactByTempate(artifact);
            }
        }
    }

    private void addArtifactByTempate(Artifact artifact) {
        Artifact template = new Artifact(artifact.getGroupId(),artifact.getArtifactId(), (Version) null, artifact.getType());
        List versions = (List) artifactsByTemplate.get(template);
        if (versions == null) {
            versions = new ArrayList();
            artifactsByTemplate.put(template, versions);
        }
        versions.add(artifact);
    }

    public synchronized void unloadAllArtifacts(Artifact loader) {
        removeArtifactByTempate(loader);

        Collection artifacts = (Collection) artifactsByLoader.remove(loader);
        if (artifacts == null) {
            return;
        }

        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            removeArtifactByTempate(artifact);
        }
    }

    private void removeArtifactByTempate(Artifact artifact) {
        Artifact template = new Artifact(artifact.getGroupId(),artifact.getArtifactId(), (Version) null, artifact.getType());
        List versions = (List) artifactsByTemplate.get(template);
        if (versions != null) {
            versions.remove(artifact);
            if (versions.isEmpty()) {
                artifactsByTemplate.remove(template);
            }
        }
    }

    public SortedSet getLoadedArtifacts(String groupId, String artifactId, String type) {
        Artifact template = new Artifact(groupId, artifactId, (Version) null, type);
        List versions = (List) artifactsByTemplate.get(template);
        if (versions != null) {
            return new TreeSet(versions);
        }
        return new TreeSet();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DefaultArtifactManager.class);
        infoFactory.addInterface(ArtifactManager.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
