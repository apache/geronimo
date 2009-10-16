/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.kernel.mock;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Rev$ $Date$
 */
public class MockRepository implements ListableRepository {

    private final Map<Artifact, File> repo;


    public MockRepository() {
        this(new HashMap<Artifact, File>());
    }

    public MockRepository(Map<Artifact, File> repo) {
        this.repo = repo;
    }

    public MockRepository(Set<Artifact> repo) {
        this.repo = new HashMap<Artifact, File>();
        for (Artifact artifact: repo) {
            this.repo.put(artifact, null);
        }
    }

    public Map<Artifact, File> getRepo() {
        return repo;
    }

    public boolean contains(Artifact artifact) {
        return repo.containsKey(artifact);
    }

    public File getLocation(final Artifact artifact) {
        File file = repo.get(artifact);
        if (file == null) {
            file = new File("/") {
                @Override
                public String getAbsolutePath() {
                    return artifact.toString();
                }
            };
        }
        return file;
    }

    public SortedSet<Artifact> list() {
        return new TreeSet<Artifact>(repo.keySet());
    }

    public SortedSet<Artifact> list(Artifact query) {
        SortedSet<Artifact> set = new TreeSet<Artifact>();
        if (!repo.isEmpty()) {
            for (Artifact artifact : repo.keySet()) {
                if (query.matches(artifact)) {
                    set.add(artifact);
                }
            }
        } else
        if (query.getGroupId() != null && query.getArtifactId() != null && query.getVersion() != null && query.getType() == null) {
            set.add(new Artifact(query.getGroupId(), query.getArtifactId(), query.getVersion(), "jar"));
        } else if (query.isResolved()) {
            set.add(query);
        }
        return set;
    }
    public final static GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MockRepository.class, "Repository");
        infoBuilder.addInterface(ListableRepository.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
}

