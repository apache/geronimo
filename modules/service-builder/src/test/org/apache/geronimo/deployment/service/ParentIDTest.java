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
package org.apache.geronimo.deployment.service;

import java.net.URI;
import java.util.List;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.xbeans.DependencyType;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ParentIDTest extends TestCase {

    public void testNoParents() throws Exception {
        List parentId = ServiceConfigBuilder.getParentID(null, new DependencyType[] {});
        assertEquals(0, parentId.size());
    }

    public void testAttributeParent() throws Exception {
        List parentId = ServiceConfigBuilder.getParentID("attribute", new DependencyType[] {});
        assertEquals(1, parentId.size());
    }

    public void testImportParent1() throws Exception {
        DependencyType anImport = DependencyType.Factory.newInstance();
        anImport.setUri("import");
        List parentId = ServiceConfigBuilder.getParentID(null, new DependencyType[] {anImport});
        assertEquals(1, parentId.size());
        assertEquals("import", ((URI)parentId.get(0)).getPath());
    }

    public void testImportParent2() throws Exception {
        DependencyType anImport = DependencyType.Factory.newInstance();
        anImport.setGroupId("groupId");
        anImport.setType("type");
        anImport.setArtifactId("artifactId");
        anImport.setVersion("version");
        List parentId = ServiceConfigBuilder.getParentID(null, new DependencyType[] {anImport});
        assertEquals(1, parentId.size());
        assertEquals("groupId/types/artifactId-version.type", ((URI)parentId.get(0)).getPath());
    }

    public void testBothParent() throws Exception {
        DependencyType import1 = DependencyType.Factory.newInstance();
        import1.setUri("import1");
        DependencyType import2 = DependencyType.Factory.newInstance();
        import2.setUri("import2");
        List parentId = ServiceConfigBuilder.getParentID("attribute", new DependencyType[] {import1, import2});
        assertEquals(3, parentId.size());
        assertEquals("attribute", ((URI)parentId.get(0)).getPath());
        assertEquals("import1", ((URI)parentId.get(1)).getPath());
        assertEquals("import2", ((URI)parentId.get(2)).getPath());
    }


}
