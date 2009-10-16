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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationResolver;
import org.apache.geronimo.kernel.mock.MockConfigStore;
import org.apache.geronimo.kernel.mock.MockRepository;

/**
 * @version $Rev$ $Date$
 */
public class ArtifactResolverTest extends TestCase {
    private final Artifact loader = new Artifact("loader", "loader", "1", "car");
    private final Artifact version1 = new Artifact("version", "version", "1", "jar");
    private final Artifact version2 = new Artifact("version", "version", "2", "jar");
    private final Artifact version3 = new Artifact("version", "version", "3", "jar");

    public void testSelectHighestFromRepo() throws Exception {
        ArtifactManager artifactManager = new DefaultArtifactManager();
        MockRepository mockRepository = getRepo();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, mockRepository);

        Artifact unresolvedArtifact = new Artifact("version", "version", (Version) null, "jar");
        Artifact artifact = artifactResolver.resolveInClassLoader(unresolvedArtifact);
        assertEquals(version3, artifact);
    }

    private MockRepository getRepo() {
        Set<Artifact> repo = new HashSet<Artifact>();
        repo.add(version1);
        repo.add(version2);
        repo.add(version3);
        return new MockRepository(repo);
    }

    public void testAlreadyLoaded() throws Exception {
        ArtifactManager artifactManager = new DefaultArtifactManager();
        artifactManager.loadArtifacts(loader, Collections.singleton(version2));

        MockRepository mockRepository = getRepo();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, mockRepository);

        Artifact unresolvedArtifact = new Artifact("version", "version", (Version) null, "jar");
        Artifact artifact = artifactResolver.resolveInClassLoader(unresolvedArtifact);
        assertEquals(version2, artifact);
    }

    public void testMultipleSelected() throws Exception {
        ArtifactManager artifactManager = new DefaultArtifactManager();
        Set<Artifact> loaded = new HashSet<Artifact>();
        loaded.add(version1);
        loaded.add(version2);
        artifactManager.loadArtifacts(loader, loaded);

        MockRepository mockRepository = getRepo();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, mockRepository);

        Artifact unresolvedArtifact = new Artifact("version", "version", (Version) null, "jar");
        Artifact artifact = artifactResolver.resolveInClassLoader(unresolvedArtifact);
        assertEquals(version2, artifact);
    }

    //TODO resurrect this test

//    public void testParentLoaded() throws Exception {
//        MockRepository mockRepository = getRepo();
//
//        ArtifactManager artifactManager = new DefaultArtifactManager();
//        Set<Artifact> loaded = new HashSet<Artifact>();
//        loaded.add(version1);
//        loaded.add(version2);
//        artifactManager.loadArtifacts(loader, loaded);
//
//        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, mockRepository);
//
//        // create parent which uses version1 explicitly
//        Environment environment = new Environment();
//        environment.setConfigId(loader);
//        environment.addDependency(version1, ImportType.CLASSES);
//
//        ConfigurationData parentConfigurationData = new ConfigurationData(environment, new Jsr77Naming());
//        parentConfigurationData.setConfigurationStore(new MockConfigStore());
//
//        ConfigurationResolver configurationResolver = new ConfigurationResolver(parentConfigurationData,
//                Collections.singleton(mockRepository),
//                artifactResolver);
//
//        Configuration parent = new Configuration(
//                classLoaderHolder, parentConfigurationData,
//                dependencyNode, allServiceParents, null);
//
//        LinkedHashSet<Configuration> parents = new LinkedHashSet<Configuration>();
//        parents.add(parent);
//
//        Artifact unresolvedArtifact = new Artifact("version", "version", (Version) null, "jar");
//        Artifact artifact = artifactResolver.resolveInClassLoader(unresolvedArtifact, parents);
//        assertEquals(version1, artifact);
//    }

}
