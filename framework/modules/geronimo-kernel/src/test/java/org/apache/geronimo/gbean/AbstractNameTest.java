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
package org.apache.geronimo.gbean;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.net.URI;

import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Version;

/**
 * @version $Rev$ $Date$
 */
public class AbstractNameTest extends TestCase {
    public void testSimple() throws Exception {
        Artifact artifact = new Artifact("groupId", "artifactId", "version", "type");

        Map nameMap = new LinkedHashMap();
        nameMap.put("a", "aaa");
        nameMap.put("b", "bbb");
        nameMap.put("c", "ccc");

        AbstractName abstractName = new AbstractName(artifact, nameMap, new ObjectName("test:nothing=true"));
        URI uri = abstractName.toURI();
        assertEquals(abstractName, new AbstractName(uri));
    }

    public void testMinimalArtifact() throws Exception {
        Artifact artifact = new Artifact(null, "artifactId", (Version)null, null);

        Map nameMap = new LinkedHashMap();
        nameMap.put("a", "aaa");
        nameMap.put("b", "bbb");
        nameMap.put("c", "ccc");

        AbstractName abstractName = new AbstractName(artifact, nameMap, new ObjectName("test:nothing=true"));
        URI uri = abstractName.toURI();
        assertEquals(abstractName, new AbstractName(uri));
    }

    public void testCreateURI() throws Exception {
        Artifact artifact = new Artifact("groupId", "artifactId", "version", "type");

        URI uri = new URI(null, null, artifact.toString(), "a=aaa", null);

        AbstractName abstractName = new AbstractName(artifact,
                Collections.singletonMap("a", "aaa"),
                new ObjectName("test:nothing=true"));
        assertEquals(abstractName, new AbstractName(uri));
    }

    public void testEmptyName() throws Exception {
        Artifact artifact = new Artifact("groupId", "artifactId", "version", "type");

        URI uri = new URI(null, null, artifact.toString(), "", null);
        try {
            new AbstractName(uri);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testNoName() throws Exception {
        Artifact artifact = new Artifact("groupId", "artifactId", "version", "type");

        URI uri = new URI(null, null, artifact.toString(), null, null);
        try {
            new AbstractName(uri);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testEmptyArtifact() throws Exception {
        URI uri = new URI(null, null, "", "a=aaa", null);
        try {
            new AbstractName(uri);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testInvalidArtifact() throws Exception {
        URI uri = new URI(null, null, "foo", "a=aaa", null);
        try {
            new AbstractName(uri);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        uri = new URI(null, null, "foo/", "a=aaa", null);
        try {
            new AbstractName(uri);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        uri = new URI(null, null, "/foo/", "a=aaa", null);
        try {
            new AbstractName(uri);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        uri = new URI(null, null, "foo////", "a=aaa", null);
        try {
            new AbstractName(uri);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testNoArtifact() throws Exception {
        URI uri = new URI(null, null, null, "a=aaa", null);
        try {
            new AbstractName(uri);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}

