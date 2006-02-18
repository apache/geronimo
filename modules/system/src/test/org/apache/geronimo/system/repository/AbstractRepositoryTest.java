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
package org.apache.geronimo.system.repository;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.SortedSet;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;

/**
 * @version $Rev$ $Date$
 */
public class AbstractRepositoryTest extends TestCase {
    protected ListableRepository repository;
    protected File rootRepoDir;

    public void testListAll() {
        SortedSet artifacts = repository.list();

        assertTrue(artifacts.contains(new Artifact("org.foo", "test", "2.0.1", "properties")));
        assertFalse(artifacts.contains(new Artifact("Unknown", "artifact", "2.0.1", "properties")));
    }

    public void testListVersions() {
        SortedSet artifacts = repository.list("org.bar", "test", "properties");

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.3", "properties")));
        assertEquals(2, artifacts.size());
    }

    public void testLocation() throws Exception {
        Artifact artifact = new Artifact("org.foo", "test", "2.0.1", "properties");
        assertTrue(repository.contains(artifact));

        File location = repository.getLocation(artifact);
        assertTrue(location.exists());
        assertTrue(location.isFile());
        assertTrue(location.canRead());

        Properties properties = new Properties();
        properties.load(new FileInputStream(location));
        assertEquals(artifact.getGroupId(), properties.get("groupId"));
        assertEquals(artifact.getArtifactId(), properties.get("artifactId"));
        assertEquals(artifact.getVersion().toString(), properties.get("version"));
        assertEquals(artifact.getType(), properties.get("type"));
    }
}
