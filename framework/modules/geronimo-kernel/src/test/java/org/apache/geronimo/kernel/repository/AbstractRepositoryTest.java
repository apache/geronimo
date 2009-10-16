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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.SortedSet;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class AbstractRepositoryTest extends TestCase {
    protected ListableRepository repository;
    protected File rootRepoDir;

    protected void setUp() throws Exception {
        super.setUp();
        // Don't want .svn dirs messing up my count!
        deleteSVN(rootRepoDir);
    }

    private void deleteSVN(File dir) {
        if(!dir.isDirectory() || !dir.canRead()) {
            throw new IllegalStateException("Invalid dir "+dir.getAbsolutePath());
        }
        File[] children = dir.listFiles();
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            if(child.isDirectory()) {
                if(child.getName().equals(".svn")) {
                    recursiveDelete(child);
                } else {
                    deleteSVN(child);
                }
            }
        }
    }

    private void recursiveDelete(File dir) {
        if(!dir.isDirectory() || !dir.canRead()) {
            throw new IllegalStateException("Invalid dir "+dir.getAbsolutePath());
        }
        File[] children = dir.listFiles();
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            if(child.isDirectory()) {
                recursiveDelete(child);
            } else {
                if(!child.delete()) {
                    throw new IllegalStateException("Cannot delete "+child.getAbsolutePath());
                }
            }
        }
        if(!dir.delete()) {
            throw new IllegalStateException("Cannot delete "+dir.getAbsolutePath());
        }
    }

    public void testListAll() {
        SortedSet artifacts = repository.list();
        assertTrue(artifacts.contains(new Artifact("org.foo", "test", "2.0.1", "properties")));
        assertFalse(artifacts.contains(new Artifact("Unknown", "artifact", "2.0.1", "properties")));
        assertEquals(4, artifacts.size());
    }

    public void testListSpecifyArtifact() {
        SortedSet artifacts = repository.list(new Artifact(null, "test", (Version)null, null));

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.3", "properties")));
        assertTrue(artifacts.contains(new Artifact("org.foo", "test", "2.0.1", "properties")));
        assertEquals(3, artifacts.size());
    }

    public void testListSpecifyArtifactGroup() {
        SortedSet artifacts = repository.list(new Artifact("org.bar", "test", (Version)null, null));

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.3", "properties")));
        assertEquals(2, artifacts.size());
    }

    public void testListSpecifyArtifactType() {
        SortedSet artifacts = repository.list(new Artifact(null, "test", (Version)null, "properties"));

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.3", "properties")));
        assertTrue(artifacts.contains(new Artifact("org.foo", "test", "2.0.1", "properties")));
        assertEquals(3, artifacts.size());
    }

    public void testListSpecifyArtifactVersion() {
        SortedSet artifacts = repository.list(new Artifact(null, "test", "1.5", null));

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertEquals(1, artifacts.size());
    }

    public void testListSpecifyArtifactGroupType() {
        SortedSet artifacts = repository.list(new Artifact("org.bar", "test", (Version)null, "properties"));

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.3", "properties")));
        assertEquals(2, artifacts.size());
    }

    public void testListSpecifyArtifactGroupVersion() {
        SortedSet artifacts = repository.list(new Artifact("org.bar", "test", "1.5", null));

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertEquals(1, artifacts.size());
    }

    public void testListSpecifyArtifactVersionType() {
        SortedSet artifacts = repository.list(new Artifact(null, "test", "1.5", "properties"));

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertEquals(1, artifacts.size());
    }

    public void testListSpecifyAll() {
        SortedSet artifacts = repository.list(new Artifact("org.bar", "test", "1.5", "properties"));

        assertTrue(artifacts.contains(new Artifact("org.bar", "test", "1.5", "properties")));
        assertEquals(1, artifacts.size());
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
