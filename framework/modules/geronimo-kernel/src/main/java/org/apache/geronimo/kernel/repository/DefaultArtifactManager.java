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
    private final Map artifactsByArtifact = new HashMap();

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
            processArtifact(loader);

            for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                processArtifact(artifact);
            }
        }
    }

    private void processArtifact(Artifact artifact) {
        List values = (List) artifactsByArtifact.get(artifact.getArtifactId());
        if (values == null) {
            values = new ArrayList();
            artifactsByArtifact.put(artifact.getArtifactId(), values);
        }
        values.add(artifact);
    }

    public synchronized void unloadAllArtifacts(Artifact loader) {
        removeArtifact(loader);

        Collection artifacts = (Collection) artifactsByLoader.remove(loader);
        if (artifacts == null) {
            return;
        }

        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            Artifact artifact = (Artifact) iterator.next();
            removeArtifact(artifact);
        }
    }

    private void removeArtifact(Artifact artifact) {
        List values = (List) artifactsByArtifact.get(artifact.getArtifactId());
        if (values != null) {
            values.remove(artifact);
            if (values.isEmpty()) {
                artifactsByArtifact.remove(artifact.getArtifactId());
            }
        }
    }

    public SortedSet getLoadedArtifacts(Artifact query) {
        List values = (List) artifactsByArtifact.get(query.getArtifactId());
        SortedSet results = new TreeSet();
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                Artifact test = (Artifact) values.get(i);
                if(query.matches(test)) {
                    results.add(test);
                }
            }
        }
        return results;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DefaultArtifactManager.class, "ArtifactManager");
        infoFactory.addInterface(ArtifactManager.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
