/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel.deployment.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:38:33 $
 */
public class URLType {
    public static final String MANIFEST_LOCATON = "META-INF/MANIFEST.MF";

    public static final URLType RESOURCE = new URLType("RESOURCE");
    public static final URLType COLLECTION = new URLType("COLLECTION");
    public static final URLType PACKED_ARCHIVE = new URLType("PACKED_ARCHIVE");
    public static final URLType UNPACKED_ARCHIVE = new URLType("UNPACKED_ARCHIVE");

    public static URLType getType(File file) throws IOException {
        if (file.isDirectory()) {
            // file is a directory - see if it has a manifest
            // we check for an actual manifest file to keep things consistent with a packed archive
            if (new File(file, MANIFEST_LOCATON).exists()) {
                return UNPACKED_ARCHIVE;
            } else {
                return COLLECTION;
            }
        } else {
            // we have a regular file - see if it contains a manifest
            try {
                JarFile jar = new JarFile(file);
                jar.getManifest();
                jar.close();
                return PACKED_ARCHIVE;
            } catch (ZipException e) {
                return RESOURCE;
            }
        }
    }

    public static URLType getType(URL url) throws IOException {
        if (url.toString().endsWith("/")) {
            URL metaInfURL = new URL(url, MANIFEST_LOCATON);
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
