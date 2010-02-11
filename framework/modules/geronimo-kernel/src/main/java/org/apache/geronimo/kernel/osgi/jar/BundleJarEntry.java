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
package org.apache.geronimo.kernel.osgi.jar;

import java.io.IOException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @version $Rev$ $Date$
 */
public class BundleJarEntry extends JarEntry {
    private final URL entryURL;
    private final Manifest manifest;

    public BundleJarEntry(String name, URL entryURL, Manifest manifest) {
        super(removeSlash(name));
        this.entryURL = entryURL;
        this.manifest = manifest;
    }

    private static String removeSlash(String name) {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name;
    }
    
    public URL getEntryURL() {
        return entryURL;
    }
    
    public Attributes getAttributes() throws IOException {
        if (manifest == null) {
            return null;
        }
        return manifest.getAttributes(getName());
    }

    public Certificate[] getCertificates() {
        return null;
    }

    public void setTime(long time) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Can not change the time of unpacked jar entry");
    }

    public long getTime() {
        return -1;
    }

    public void setSize(long size) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Can not change the size of unpacked jar entry");
    }

    public long getSize() {
        return -1;
    }

    public long getCompressedSize() {
        return getSize();
    }

    public void setCompressedSize(long compressedSize) {
        throw new UnsupportedOperationException("Can not change the compressed size of unpacked jar entry");
    }

    public long getCrc() {
        return super.getCrc();  
    }

    public void setCrc(long crc) {
        throw new UnsupportedOperationException("Can not change the crc of unpacked jar entry");
    }

    public int getMethod() {
        return ZipEntry.STORED;
    }

    public void setMethod(int method) {
        throw new UnsupportedOperationException("Can not change the method of unpacked jar entry");
    }

    public byte[] getExtra() {
        return null;
    }

    public void setExtra(byte[] extra) {
        throw new UnsupportedOperationException("Can not change the extra data of unpacked jar entry");
    }

    public String getComment() {
        return null;
    }

    public void setComment(String comment) {
        throw new UnsupportedOperationException("Can not change the comment of unpacked jar entry");
    }

    public boolean isDirectory() {
        return entryURL.getPath().endsWith("/");
    }

}
