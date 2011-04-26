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
package org.apache.geronimo.deployment.service;

import java.util.LinkedHashSet;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Rev$ $Date$
 */
public class EnvironmentBuilderTest extends TestCase {

    public void testNoParents() throws Exception {
        LinkedHashSet parentId = EnvironmentBuilder.toArtifacts(new ArtifactType[] {});
        assertEquals(0, parentId.size());
    }

    public void testImportParent1() throws Exception {
        ArtifactType anImport = ArtifactType.Factory.newInstance();
        anImport.setGroupId("groupId");
        anImport.setType("type");
        anImport.setArtifactId("artifactId");
        anImport.setVersion("version");
        LinkedHashSet parentId = EnvironmentBuilder.toArtifacts(new ArtifactType[] {anImport});
        assertEquals(1, parentId.size());
        assertEquals(new Artifact("groupId", "artifactId", "version", "type"), parentId.iterator().next());
    }

    private static final String ENV_1 = "<dep:environment xmlns:dep=\"http://geronimo.apache.org/xml/ns/deployment-1.1\">\n" +
            "  <dep:dependencies>\n" +
            "    <dep:dependency>\n" +
            "      <dep:groupId>${pom.groupId}</dep:groupId>\n" +
            "      <dep:artifactId>j2ee-server</dep:artifactId>\n" +
            "      <dep:version>${pom.currentVersion}</dep:version>\n" +
            "      <dep:type>car</dep:type>\n" +
            "    </dep:dependency>\n" +
            "  </dep:dependencies>\n" +
            "  <dep:hidden-classes/>\n" +
            "  <dep:non-overridable-classes/>\n" +
            "</dep:environment>";

    public void xtestPropertyEditor() throws Exception {
        PropertyEditor editor = new EnvironmentBuilder();
        editor.setAsText(ENV_1);
        Environment environment = (Environment) editor.getValue();
        editor.setValue(environment);
        String text = editor.getAsText();
        assertEquals(text, ENV_1);
    }

    public void xtestPropertyEditorRegistration() throws Exception {
        new GBeanData(ServiceConfigBuilder.class);
        PropertyEditor propertyEditor = PropertyEditorManager.findEditor(Environment.class);
        assertTrue(propertyEditor instanceof EnvironmentBuilder);
    }
}
