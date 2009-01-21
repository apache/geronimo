/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.jaxws.wsdl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.wsdl.xml.WSDLLocator;

import org.xml.sax.InputSource;

public class JarWSDLLocator implements WSDLLocator {

    private final List<InputStream> streams = new ArrayList<InputStream>();

    private final JarFile moduleFile;
    private final URI wsdlURI;

    private URI latestImportURI;

    public JarWSDLLocator(JarFile moduleFile, URI wsdlURI) {
        this.moduleFile = moduleFile;
        this.wsdlURI = wsdlURI;
    }

    public InputSource getBaseInputSource() {
        return resolve("", wsdlURI.toString());
    }

    public String getBaseURI() {
        return wsdlURI.toString();
    }

    public InputSource getImportInputSource(String parentLocation,
                                            String relativeLocation) {
        return resolve(parentLocation, relativeLocation);
    }
    
    protected InputSource resolve(String parentLocation,
                                  String relativeLocation) {
        URI parentURI = URI.create(parentLocation);
        URI relativeURI = URI.create(relativeLocation);
        InputStream importInputStream;
        if (relativeURI.isAbsolute()) {
            latestImportURI = relativeURI;
            importInputStream = getExternalFile(latestImportURI);
        } else if (parentURI.isAbsolute()) {
            latestImportURI = resolveRelative(parentURI, relativeURI);
            importInputStream = getExternalFile(latestImportURI);
        } else {
            latestImportURI = parentURI.resolve(relativeLocation);
            importInputStream = getModuleFile(latestImportURI);
        }
        streams.add(importInputStream);
        InputSource inputSource = new InputSource(importInputStream);
        inputSource.setSystemId(getLatestImportURI());
        return inputSource;
    }

    public String getLatestImportURI() {
        return latestImportURI.toString();
    }

    private URI resolveRelative(URI parentURI, URI relativeURI) {
        if ("jar".equals(parentURI.getScheme())) {
            String str = parentURI.toString();
            int i = str.indexOf('!');
            if (i != -1) {
                String jarBase = str.substring(0, i + 1);
                String jarEntry = str.substring(i + 1);
                
                URI jarEntryURI = URI.create(jarEntry);
                URI resolvedRelatvieURI = jarEntryURI.resolve(relativeURI);
                
                return URI.create(jarBase + resolvedRelatvieURI.toString());
            }
        } 
        return parentURI.resolve(relativeURI);
    }
    
    private InputStream getExternalFile(URI file) {
        try {
            return file.toURL().openStream();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to import external file: " + latestImportURI, e);
        }
    }
    
    private InputStream getModuleFile(URI file) {
        ZipEntry entry = moduleFile.getEntry(file.toString());
        if (entry == null) {
            throw new RuntimeException(
                "File does not exist in the module: " + file);
        }
        try {                
            return moduleFile.getInputStream(entry);
        } catch (Exception e) {
            throw new RuntimeException(
                "Could not open stream to import file", e);
        }
    }
    
    public void close() {
        for (InputStream inputStream : this.streams) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
        streams.clear();
    }
}
