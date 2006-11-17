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

package org.apache.geronimo.deployment.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

/**
 * The URLType class assigns type to resources, i.e. files or URLs.
 * <p>
 * The following types are available:
 * <ul>
 *  <li><b>UNPACKED_ARCHIVE</b> - directory with META-INF/MANIFEST.MF 
 *  <li><b>PACKED_ARCHIVE</b> - file with META-INF/MANIFEST.MF
 *  <li><b>COLLECTION</b> - directory with no META-INF/MANIFEST.MF
 *  <li><b>RESOURCE</b> -  none of the above
 * </ul>
 *
 * @version $Rev$ $Date$
 */
public class URLType {
    public static final String MANIFEST_LOCATION = "META-INF/MANIFEST.MF";

    public static final URLType RESOURCE = new URLType("RESOURCE");
    public static final URLType COLLECTION = new URLType("COLLECTION");
    public static final URLType PACKED_ARCHIVE = new URLType("PACKED_ARCHIVE");
    public static final URLType UNPACKED_ARCHIVE = new URLType("UNPACKED_ARCHIVE");

    public static URLType getType(File file) throws IOException {
        if (file.isDirectory()) {
            // file is a directory - see if it has a manifest
            // we check for an actual manifest file to keep things consistent with a packed archive
            if (new File(file, MANIFEST_LOCATION).exists()) {
                return UNPACKED_ARCHIVE;
            } else {
                return COLLECTION;
            }
        } else {
            // we have a regular file - see if it contains a manifest
            try {
                JarFile jar = null;
                try {
                    jar = new JarFile(file);
                    jar.getManifest();
                } finally {
                    if (jar != null) {
                        jar.close();
                    }
                }
                return PACKED_ARCHIVE;
            } catch (ZipException e) {
                return RESOURCE;
            }
        }
    }

    /**
     * Returns the type of url
     * 
     * @param url 
     * @return type of the url
     * @throws IOException whenever there're problems with accessing portion of the url
     */
    public static URLType getType(URL url) throws IOException {
        if (url.toString().endsWith("/")) {
            URL metaInfURL = new URL(url, MANIFEST_LOCATION);
            URLConnection urlConnection = metaInfURL.openConnection();
            urlConnection.connect();
            try {
                InputStream is = urlConnection.getInputStream();
                is.close();
                return UNPACKED_ARCHIVE;
            } catch (IOException e) {
                return COLLECTION;
            }
        } else {
            URL jarURL = new URL("jar:" + url.toString() + "!/");
            JarURLConnection jarConnection = (JarURLConnection) jarURL.openConnection();
            try {
                jarConnection.getManifest();
                return PACKED_ARCHIVE;
            } catch (ZipException e) {
                return RESOURCE;
            }
        }
    }

    private final String desc;

    private URLType(final String desc) {
        this.desc = desc;
    }

    public boolean equals(Object obj) {
        return this == obj;
    }

    public String toString() {
        return desc;
    }
}
