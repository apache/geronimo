/**
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.kernel.osgi.HeaderParser.HeaderElement;
import org.osgi.framework.Constants;

/**
 * @version $Rev$, $Date$
 */
public class BundleDescription  {

    private Map headers;
    
    public BundleDescription(Map headers) {
        this.headers = headers;
    }
   
    /**
     * Returns a list of packages that are listed in <i>Import-Package</i> header.
     */
    public List<Package> getImportPackage() {
        String headerValue = (String) headers.get(Constants.IMPORT_PACKAGE);
        List<Package> imports = new ArrayList<Package>();
        List<HeaderElement> elements = HeaderParser.parseHeader(headerValue);
        for (HeaderElement element : elements) {
            Package p = new Package(element.getName(), element.getAttributes(), element.getDirectives());
            imports.add(p);
        }
        return imports;
    }
    
    /**
     * Returns a list of packages that are listed in <i>Export-Package</i> header.
     */
    public List<Package> getExportPackage() {
        String headerValue = (String) headers.get(Constants.EXPORT_PACKAGE);
        List<Package> imports = new ArrayList<Package>();
        List<HeaderElement> elements = HeaderParser.parseHeader(headerValue);
        for (HeaderElement element : elements) {
            Package p = new Package(element.getName(), element.getAttributes(), element.getDirectives());
            imports.add(p);
        }
        return imports;
        
    }
    
    /**
     * Returns a list of packages that are listed in <i>Import-Package</i> header
     * and are <b>not</b> listed in <i>Export-Package</i> header.
     */
    public List<Package> getExternalImports() {
        List<Package> imports = getImportPackage();
        List<Package> exports = getExportPackage();
        List<Package> realImports = new ArrayList<Package>();
        for (Package p : imports) {
            if (!isExported(exports, p)) {
                realImports.add(p);
            }
        }
        return realImports;
    }
    
    private static boolean isExported(List<Package> exports, Package p) {
        for (Package export : exports) {
            if (export.getName().equals(p.getName())) {
                return true;
            }
        }
        return false;
    }
    
    public List<String> getRequireBundle() {
        String headerValue = (String) headers.get(Constants.REQUIRE_BUNDLE);
        List<String> required = new ArrayList<String>();
        List<HeaderElement> elements = HeaderParser.parseHeader(headerValue);
        for (HeaderElement element : elements) {
            required.add(element.getName());
        }
        return required;
    }
    
    public static class Package {
    
        private String name;
        private Map<String, String> attributes;
        private Map<String, String> directives;
        
        public Package(String name, Map<String, String> attributes, Map<String, String> directives) {
            this.name = name;
            this.attributes = attributes;
            this.directives = directives;
        }
        
        public String getName() {
            return name;
        }
        
        public Map<String, String> getAttributes() {
            return attributes;
        }
        
        public Map<String, String> getDirectives() {
            return directives;
        }       
        
    }
    
}
