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

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;

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
        MockRepository mockRepository = new MockRepository();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, mockRepository);

        Artifact unresolvedArtifact = new Artifact("version", "version", (Version) null, "jar");
        Artifact artifact = artifactResolver.resolve(unresolvedArtifact);
        assertEquals(version3, artifact);
    }

    public void testAlreadyLoaded() throws Exception {
        ArtifactManager artifactManager = new DefaultArtifactManager();
        artifactManager.loadArtifacts(loader, Collections.singleton(version2));

        MockRepository mockRepository = new MockRepository();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, mockRepository);

        Artifact unresolvedArtifact = new Artifact("version", "version", (Version) null, "jar");
        Artifact artifact = artifactResolver.resolve(unresolvedArtifact);
        assertEquals(version2, artifact);
    }

    public void testMultipleSelected() throws Exception {
        ArtifactManager artifactManager = new DefaultArtifactManager();
        Set loaded = new HashSet();
        loaded.add(version1);
        loaded.add(version2);
        artifactManager.loadArtifacts(loader, loaded);

        MockRepository mockRepository = new MockRepository();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, mockRepository);

        Artifact unresolvedArtifact = new Artifact("version", "version", (Version) null, "jar");
        Artifact artifact = artifactResolver.resolve(unresolvedArtifact);
        assertEquals(version2, artifact);
    }

    public void testParentLoaded() throws Exception {
        MockRepository mockRepository = new MockRepository();

        ArtifactManager artifactManager = new DefaultArtifactManager();
        Set loaded = new HashSet();
        loaded.add(version1);
        loaded.add(version2);
        artifactManager.loadArtifacts(loader, loaded);

        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, mockRepository);

        // create parent which uses version1 explicitly
        Environment environment = new Environment();
        environment.addDependency(version1);
        Configuration parent = new Configuration(null,
                Configuration.getConfigurationObjectName(loader).getCanonicalName(),
                ConfigurationModuleType.SERVICE,
                environment,
                null,
                null,
                null,
                Collections.singleton(mockRepository),
                null,
                artifactManager,
                artifactResolver);
        LinkedHashSet parents = new LinkedHashSet();
        parents.add(parent);

        Artifact unresolvedArtifact = new Artifact("version", "version", (Version) null, "jar");
        Artifact artifact = artifactResolver.resolve(parents, unresolvedArtifact);
        assertEquals(version1, artifact);
    }

    private class MockRepository implements ListableRepository {
        public SortedSet list() {
            throw new UnsupportedOperationException();
        }

        public SortedSet list(String groupId, String artifactId, String type) {
            TreeSet set = new TreeSet();
            set.add(version1);
            set.add(version2);
            set.add(version3);
            return set;
        }

        public boolean contains(Artifact artifact) {
            return true;
        }

        public File getLocation(Artifact artifact) {
            return new File(".");
        }
    }
}
