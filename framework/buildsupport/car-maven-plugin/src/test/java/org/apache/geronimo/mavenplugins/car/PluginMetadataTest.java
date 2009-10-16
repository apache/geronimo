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


package org.apache.geronimo.mavenplugins.car;

import java.io.StringReader;

import junit.framework.TestCase;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;

/**
 * @version $Rev$ $Date$
 */
public class PluginMetadataTest extends TestCase {


//    private static final String PROLOGUE = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    private static final String INSTANCE1="<plugin-artifact>\n" +
            " <jvm-version>1.5</jvm-version>\n" +
            " <jvm-version>1.5.2</jvm-version>\n" +
            " <prerequisite>\n" +
            "  <id>\n" +
            "   <groupId>commons-logging</groupId>\n" +
            "   <artifactId>commons-logging</artifactId>\n" +
            "  </id>\n" +
            "  <resource-type>joke</resource-type>\n" +
            "  <description>this is an explanation</description>\n" +
            " </prerequisite>\n" +
            " <obsoletes>\n" +
            "  <groupId>commons-logging</groupId>\n" +
            "  <artifactId>commons-logging</artifactId>\n" +
            " </obsoletes>\n" +
            " <source-repository>http://foo.com</source-repository>\n" +
            " <source-repository>http://bar.com</source-repository>\n" +
            " <copy-file dest-dir=\"bar\" relative-to=\"WEB-INF\">META-INF/foo.xml</copy-file>\n" +
            " <artifact-alias key=\"org.apache.geronimo.test/foo//car\">org.apache.geronimo.test/bar/1.0/car</artifact-alias>\n" +
            " <config-substitution key=\"key2\">value2</config-substitution>\n" +
            " <config-substitution key=\"key1\">value1</config-substitution>\n" +
            "</plugin-artifact>";

    //TODO test #{ to ${ replacement
    public void testReadInstance() throws Exception {
        PluginArtifactType instance = PluginXmlUtil.loadPluginArtifactMetadata(new StringReader(INSTANCE1));
        assertEquals(2, instance.getConfigSubstitution().size());
    }
}
