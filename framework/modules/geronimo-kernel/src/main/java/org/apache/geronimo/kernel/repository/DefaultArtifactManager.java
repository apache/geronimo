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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Rev$ $Date$
 */
@Component
@Service
public class DefaultArtifactManager implements ArtifactManager {
    private final Map<Artifact, Set<Artifact>> artifactsByLoader = new HashMap<Artifact, Set<Artifact>>();
    private final Map<String, List<Artifact>> artifactsByArtifact = new HashMap<String, List<Artifact>>();

    public void loadArtifacts(Artifact loader, Set<Artifact> artifacts) {
        if (!loader.isResolved()) throw new IllegalArgumentException("loader is not a resolved artifact: " + loader);
        for (Artifact artifact : artifacts) {
            if (!artifact.isResolved()) {
                throw new IllegalArgumentException("artifact is not a resolved artifact: " + artifact);
            }
        }

        synchronized (this) {
            if (artifactsByLoader.containsKey(loader)) throw new IllegalArgumentException("loader has already declared artifacts: "+ loader);
            artifactsByLoader.put(loader, artifacts);
            processArtifact(loader);

            for (Artifact artifact : artifacts) {
                processArtifact(artifact);
            }
        }
    }

    private void processArtifact(Artifact artifact) {
        List<Artifact> values = artifactsByArtifact.get(artifact.getArtifactId());
        if (values == null) {
            values = new ArrayList<Artifact>();
            artifactsByArtifact.put(artifact.getArtifactId(), values);
        }
        values.add(artifact);
    }

    public synchronized void unloadAllArtifacts(Artifact loader) {
        removeArtifact(loader);

        Set<Artifact> artifacts = artifactsByLoader.remove(loader);
        if (artifacts == null) {
            return;
        }

        for (Artifact artifact : artifacts) {
            removeArtifact(artifact);
        }
    }

    private void removeArtifact(Artifact artifact) {
        List<Artifact> values = artifactsByArtifact.get(artifact.getArtifactId());
        if (values != null) {
            values.remove(artifact);
            if (values.isEmpty()) {
                artifactsByArtifact.remove(artifact.getArtifactId());
            }
        }
    }

    public SortedSet<Artifact> getLoadedArtifacts(Artifact query) {
        List<Artifact> values = artifactsByArtifact.get(query.getArtifactId());
        SortedSet<Artifact> results = new TreeSet<Artifact>();
        if (values != null) {
            for (Artifact test : values) {
                if (query.matches(test)) {
                    results.add(test);
                }
            }
        }
        return results;
    }

//    public static final GBeanInfo GBEAN_INFO;
//
//    static {
//        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DefaultArtifactManager.class, "ArtifactManager");
//        infoFactory.addInterface(ArtifactManager.class);
//        GBEAN_INFO = infoFactory.getBeanInfo();
//    }
//
//    public static GBeanInfo getGBeanInfo() {
//        return GBEAN_INFO;
//    }
}
