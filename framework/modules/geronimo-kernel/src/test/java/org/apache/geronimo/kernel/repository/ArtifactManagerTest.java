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

import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.Collections;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ArtifactManagerTest extends TestCase {
    public void testGetLoadeArtifacts() {
        DefaultArtifactManager artifactManager = new DefaultArtifactManager();

        Artifact loader1 = new Artifact("loaderGroup", "loaderArtifact1", "1", "car");
        Set artifacts1 = new HashSet();
        Artifact private1 = new Artifact("private1", "artifact", "1", "jar");
        artifacts1.add(private1);
        artifactManager.loadArtifacts(loader1, artifacts1);

        Artifact loader2 = new Artifact("loaderGroup", "loaderArtifact2", "1", "car");
        Set artifacts2 = new HashSet();
        Artifact private2 = new Artifact("private2", "artifact", "1", "jar");
        artifacts2.add(private2);
        artifactManager.loadArtifacts(loader2, artifacts2);

        SortedSet loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("private1", "artifact", (Version)null, "jar"));
        assertEquals(Collections.singleton(private1), loadedArtifacts);

        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("loaderGroup", "loaderArtifact1", (Version)null, "car"));
        assertEquals(Collections.singleton(loader1), loadedArtifacts);

        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("private2", "artifact", (Version)null, "jar"));
        assertEquals(Collections.singleton(private2), loadedArtifacts);

        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("loaderGroup", "loaderArtifact2", (Version)null, "car"));
        assertEquals(Collections.singleton(loader2), loadedArtifacts);

        artifactManager.unloadAllArtifacts(loader1);

        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("private1", "artifact", (Version)null, "jar"));
        assertEquals(Collections.EMPTY_SET, loadedArtifacts);

        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("loaderGroup", "loaderArtifact1", (Version)null, "car"));
        assertEquals(Collections.EMPTY_SET, loadedArtifacts);

        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("private2", "artifact", (Version)null, "jar"));
        assertEquals(Collections.singleton(private2), loadedArtifacts);

        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("loaderGroup", "loaderArtifact2", (Version)null, "car"));
        assertEquals(Collections.singleton(loader2), loadedArtifacts);
    }

    public void testMultipleVersions() {
        DefaultArtifactManager artifactManager = new DefaultArtifactManager();

        Artifact version1 = new Artifact("version", "version", "1", "jar");
        Artifact version2 = new Artifact("version", "version", "2", "jar");

        Artifact loader1 = new Artifact("loaderGroup", "loaderArtifact1", "1", "car");
        artifactManager.loadArtifacts(loader1, Collections.singleton(version1));

        Artifact loader2 = new Artifact("loaderGroup", "loaderArtifact2", "1", "car");
        artifactManager.loadArtifacts(loader2, Collections.singleton(version2));


        Set artifacts = new HashSet();
        artifacts.add(version1);
        artifacts.add(version2);

        SortedSet loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("version", "version", (Version)null, "jar"));
        assertEquals(artifacts, loadedArtifacts);

        artifactManager.unloadAllArtifacts(loader1);
        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("version", "version", (Version)null, "jar"));
        assertEquals(Collections.singleton(version2), loadedArtifacts);
    }

    public void testDupe() {
        DefaultArtifactManager artifactManager = new DefaultArtifactManager();

        Artifact artifact = new Artifact("dupe", "dupe", "1", "jar");

        Artifact loader1 = new Artifact("loaderGroup", "loaderArtifact1", "1", "car");
        artifactManager.loadArtifacts(loader1, Collections.singleton(artifact));

        Artifact loader2 = new Artifact("loaderGroup", "loaderArtifact2", "1", "car");
        artifactManager.loadArtifacts(loader2, Collections.singleton(artifact));

        SortedSet loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("dupe", "dupe", (Version)null, "jar"));
        assertEquals(Collections.singleton(artifact), loadedArtifacts);

        artifactManager.unloadAllArtifacts(loader1);

        loadedArtifacts = artifactManager.getLoadedArtifacts(new Artifact("dupe", "dupe", (Version)null, "jar"));
        assertEquals(Collections.singleton(artifact), loadedArtifacts);
    }
}
