/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.kernel.osgi;

import java.util.List;

import org.apache.geronimo.kernel.osgi.HeaderParser;
import org.apache.geronimo.kernel.osgi.HeaderParser.HeaderElement;

import junit.framework.TestCase;

public class HeaderParserTest extends TestCase {

    public void testSimple() throws Exception {
        List<HeaderElement> paths = HeaderParser.parseHeader("/foo.xml, /foo/bar.xml");
        assertEquals(2, paths.size());
        assertEquals("/foo.xml", paths.get(0).getName());
        assertEquals(0, paths.get(0).getAttributes().size());
        assertEquals(0, paths.get(0).getDirectives().size());
        assertEquals("/foo/bar.xml", paths.get(1).getName());
        assertEquals(0, paths.get(1).getAttributes().size());
        assertEquals(0, paths.get(1).getDirectives().size());
    }
    
    public void testComplex() throws Exception {
        List<HeaderElement> paths = HeaderParser.parseHeader("OSGI-INF/blueprint/comp1_named.xml;ignored-directive:=true,OSGI-INF/blueprint/comp2_named.xml;some-other-attribute=1");
        assertEquals(2, paths.size());
        assertEquals("OSGI-INF/blueprint/comp1_named.xml", paths.get(0).getName());
        assertEquals(0, paths.get(0).getAttributes().size());
        assertEquals(1, paths.get(0).getDirectives().size());
        assertEquals("true", paths.get(0).getDirective("ignored-directive"));
        assertEquals("OSGI-INF/blueprint/comp2_named.xml", paths.get(1).getName());
        assertEquals(1, paths.get(1).getAttributes().size());
        assertEquals("1", paths.get(1).getAttribute("some-other-attribute"));
        assertEquals(0, paths.get(1).getDirectives().size());
    }

    public void testPaths() throws Exception {
        List<HeaderElement> paths = HeaderParser.parseHeader("OSGI-INF/blueprint/comp1_named.xml;ignored-directive:=true,OSGI-INF/blueprint/comp2_named.xml;foo.xml;a=b;1:=2;c:=d;4=5");
        assertEquals(3, paths.size());
        assertEquals("OSGI-INF/blueprint/comp1_named.xml", paths.get(0).getName());
        assertEquals(0, paths.get(0).getAttributes().size());
        assertEquals(1, paths.get(0).getDirectives().size());
        assertEquals("true", paths.get(0).getDirective("ignored-directive"));
        assertEquals("OSGI-INF/blueprint/comp2_named.xml", paths.get(1).getName());
        assertEquals(0, paths.get(1).getAttributes().size());
        assertEquals(0, paths.get(1).getDirectives().size());
        assertEquals("foo.xml", paths.get(2).getName());
        assertEquals(2, paths.get(2).getAttributes().size());
        assertEquals("b", paths.get(2).getAttribute("a"));
        assertEquals("5", paths.get(2).getAttribute("4"));
        assertEquals(2, paths.get(2).getDirectives().size());
        assertEquals("d", paths.get(2).getDirective("c"));
        assertEquals("2", paths.get(2).getDirective("1"));
    }
    
    public void testExportPackages() throws Exception {
        List<HeaderElement> paths = HeaderParser.parseHeader("org.apache.geronimo.kernel.rmi;uses:=\"javax.rmi.ssl,org.apache.geronimo.gbean,org.slf4j\",org.apache.geronimo.kernel.proxy");
        assertEquals(2, paths.size());
        
        assertEquals("org.apache.geronimo.kernel.rmi", paths.get(0).getName());
        assertEquals(0, paths.get(0).getAttributes().size());
        assertEquals(1, paths.get(0).getDirectives().size());
        assertEquals("\"javax.rmi.ssl,org.apache.geronimo.gbean,org.slf4j\"", paths.get(0).getDirective("uses"));
        
        assertEquals("org.apache.geronimo.kernel.proxy", paths.get(1).getName());
        assertEquals(0, paths.get(1).getAttributes().size());
        assertEquals(0, paths.get(1).getDirectives().size());
    }
    
    public void testImportPackages() throws Exception {
        List<HeaderElement> paths = HeaderParser.parseHeader("com.thoughtworks.xstream;version=\"1.3\",com.thoughtworks.xstream.converters");
        assertEquals(2, paths.size());
        
        assertEquals("com.thoughtworks.xstream", paths.get(0).getName());
        assertEquals(1, paths.get(0).getAttributes().size());
        assertEquals("\"1.3\"", paths.get(0).getAttribute("version"));
        assertEquals(0, paths.get(0).getDirectives().size());
        
        assertEquals("com.thoughtworks.xstream.converters", paths.get(1).getName());
        assertEquals(0, paths.get(1).getAttributes().size());
        assertEquals(0, paths.get(1).getDirectives().size());
    }
}
