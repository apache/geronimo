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

package org.apache.geronimo.kernel.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * @version $Rev$ $Date$
 */
public class BundleUtil {

    // the header that identifies a bundle as being a WAB
    public final static String WEB_CONTEXT_PATH_HEADER = "Web-ContextPath";

    public final static String EBA_GROUP_ID = "application";

    public final static String REFERENCE_FILE_PREFIX = "reference:file://";

    public static String getVersion(org.osgi.framework.Version version) {
        String str = version.getMajor() + "." + version.getMinor() + "." + version.getMicro();
        String qualifier = version.getQualifier();
        if (qualifier != null && qualifier.trim().length() > 0) {
            str += "-" + version.getQualifier().trim();
        }
        return str;
    }

    public static Artifact createArtifact(String group, String symbolicName, Version version) {
        return new Artifact(group, symbolicName, getVersion(version), "eba");
    }

    public static File toFile(Bundle bundle) {
        return toFile(bundle.getLocation());
    }

    public static File toFile(URL url) {
        return toFile(url.toExternalForm());
    }

    /**
     * Translate the reference:file:// style URL  to the underlying file instance
     * @param url
     * @return
     */
    public static File toFile(String url) {
        if (url.startsWith(REFERENCE_FILE_PREFIX)) {
            File file = new File(url.substring(REFERENCE_FILE_PREFIX.length()));
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }
    
    public static String toReferenceFileLocation(File file) throws IOException {
        return REFERENCE_FILE_PREFIX + file.getCanonicalPath();
    }
}
