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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import junit.framework.TestCase;

public class BundleDescriptionTest extends TestCase {
    
    public void testSimple() throws Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(Constants.IMPORT_PACKAGE,
                    "com.thoughtworks.xstream;version=\"1.3\",com.thoughtworks.xstream.converters,org.apache.geronimo.kernel.proxy");
        headers.put(Constants.EXPORT_PACKAGE, 
                    "org.apache.geronimo.kernel.rmi;uses:=\"javax.rmi.ssl,org.apache.geronimo.gbean,org.slf4j\",org.apache.geronimo.kernel.proxy");
        
        BundleDescription desc = new BundleDescription(headers);
        
        List<BundleDescription.ImportPackage> imports = desc.getImportPackage();
        assertEquals(3, imports.size());
        assertEquals("com.thoughtworks.xstream", imports.get(0).getName());
        assertEquals("1.3", imports.get(0).getAttributes().get("version"));
        assertEquals(Version.parseVersion("1.3"), imports.get(0).getVersionRange().getLow());
        assertEquals("com.thoughtworks.xstream.converters", imports.get(1).getName());
        assertEquals("org.apache.geronimo.kernel.proxy", imports.get(2).getName());
                
        List<BundleDescription.ExportPackage> exports = desc.getExportPackage();
        assertEquals(2, exports.size());
        assertEquals("org.apache.geronimo.kernel.rmi", exports.get(0).getName());
        assertEquals("javax.rmi.ssl,org.apache.geronimo.gbean,org.slf4j", exports.get(0).getDirectives().get("uses"));
        assertEquals("org.apache.geronimo.kernel.proxy", exports.get(1).getName());
        
        List<BundleDescription.ImportPackage> externalImports = desc.getExternalImports();
        assertEquals(2, externalImports.size());
        assertEquals("com.thoughtworks.xstream", externalImports.get(0).getName());
        assertEquals("1.3", externalImports.get(0).getAttributes().get("version"));
        assertEquals("com.thoughtworks.xstream.converters", externalImports.get(1).getName());        
    }
}

