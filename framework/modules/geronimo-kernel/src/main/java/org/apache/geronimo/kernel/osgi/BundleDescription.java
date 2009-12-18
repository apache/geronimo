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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.geronimo.kernel.osgi.HeaderParser.HeaderElement;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

/**
 * @version $Rev$, $Date$
 */
public class BundleDescription  {

    private Map headers;
    
    public BundleDescription(Manifest manifest) {
        this.headers = manifestToMap(manifest);
    }
    
    public BundleDescription(Dictionary dictionary) {
        this.headers = new DictionaryMap(dictionary);
    }
    
    public BundleDescription(Map headers) {
        this.headers = headers;
    }
   
    /**
     * Returns a list of packages that are listed in <i>Import-Package</i> header.
     */
    public List<ImportPackage> getImportPackage() {
        String headerValue = (String) headers.get(Constants.IMPORT_PACKAGE);
        List<ImportPackage> imports = new ArrayList<ImportPackage>();
        List<HeaderElement> elements = HeaderParser.parseHeader(headerValue);
        for (HeaderElement element : elements) {
            ImportPackage p = new ImportPackage(element.getName(), element.getAttributes(), element.getDirectives());
            imports.add(p);
        }
        return imports;
    }
    
    /**
     * Returns a list of packages that are listed in <i>Export-Package</i> header.
     */
    public List<ExportPackage> getExportPackage() {
        String headerValue = (String) headers.get(Constants.EXPORT_PACKAGE);
        List<ExportPackage> exports = new ArrayList<ExportPackage>();
        List<HeaderElement> elements = HeaderParser.parseHeader(headerValue);
        for (HeaderElement element : elements) {
            ExportPackage p = new ExportPackage(element.getName(), element.getAttributes(), element.getDirectives());
            exports.add(p);
        }
        return exports;
        
    }
    
    /**
     * Returns a list of packages that are listed in <i>Import-Package</i> header
     * and are <b>not</b> listed in <i>Export-Package</i> header.
     */
    public List<ImportPackage> getExternalImports() {
        List<ImportPackage> imports = getImportPackage();
        List<ExportPackage> exports = getExportPackage();
        List<ImportPackage> realImports = new ArrayList<ImportPackage>();
        for (ImportPackage p : imports) {
            if (!isExported(exports, p)) {
                realImports.add(p);
            }
        }
        return realImports;
    }
    
    private static boolean isExported(List<ExportPackage> exports, ImportPackage p) {
        for (ExportPackage export : exports) {            
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
    
    /**
     * Returns a list of packages that are listed in <i>DynamicImport-Package</i> header.
     */
    public List<HeaderEntry> getDynamicImportPackage() {
        String headerValue = (String) headers.get(Constants.DYNAMICIMPORT_PACKAGE);
        List<HeaderEntry> imports = new ArrayList<HeaderEntry>();
        List<HeaderElement> elements = HeaderParser.parseHeader(headerValue);
        for (HeaderElement element : elements) {
            HeaderEntry p = new HeaderEntry(element.getName(), element.getAttributes(), element.getDirectives());
            imports.add(p);
        }
        return imports;
    }
    
    public SymbolicName getSymbolicName() {
        String headerValue = (String) headers.get(Constants.BUNDLE_SYMBOLICNAME);
        List<HeaderElement> elements = HeaderParser.parseHeader(headerValue);
        if (elements.size() == 1) {
            HeaderElement element = elements.get(0);
            return new SymbolicName(element.getName(), element.getAttributes(), element.getDirectives());
        }
        return null;
    }
    
    public Map getHeaders() {
        return headers;
    }
    
    private static Map<String, String> manifestToMap(Manifest manifest) {
        Attributes attributes = manifest.getMainAttributes();
        Map<String, String> headers = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            headers.put(key, value);
        }
        return headers;
    }
    
    public static class HeaderEntry {
    
        private String name;
        private Map<String, String> attributes;
        private Map<String, String> directives;
        
        public HeaderEntry(String name, 
                          Map<String, String> attributes, 
                          Map<String, String> directives) {
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
        
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Name: ").append(name);
            builder.append(", Attributes: ").append(attributes);
            builder.append(", Directives: ").append(directives);
            return builder.toString();
        }
        
        protected VersionRange getVersionRange() {
            String version = attributes.get(Constants.VERSION_ATTRIBUTE);
            if (version == null) {
                version = "0.0.0";
            }
            return VersionRange.parse(version);
        }
    }
    
    public static class ExportPackage extends HeaderEntry {

        private Version version;
        
        public ExportPackage(String name,
                             Map<String, String> attributes,
                             Map<String, String> directives) {
            super(name, attributes, directives);
            version = getVersionRange().getLow();
        }
        
        public Version getVersion() {
            return version;
        }
    }
        
    public static class ImportPackage extends HeaderEntry {

        private boolean optional;
        private VersionRange versionRange;
        
        public ImportPackage(String name,
                             Map<String, String> attributes,
                             Map<String, String> directives) {
            super(name, attributes, directives);
            
            String resolution = directives.get(Constants.RESOLUTION_DIRECTIVE);
            optional = Constants.RESOLUTION_OPTIONAL.equals(resolution);
            
            versionRange = super.getVersionRange();
        }
        
        public boolean isOptional() {
            return optional;
        }
        
        public boolean isMandatory() {
            return !optional;
        }
        
        public VersionRange getVersionRange() {
            return versionRange;
        }
    }
    
    public static class SymbolicName extends HeaderEntry {

        public SymbolicName(String name,
                            Map<String, String> attributes,
                            Map<String, String> directives) {
            super(name, attributes, directives);
        }
        
    }
}
