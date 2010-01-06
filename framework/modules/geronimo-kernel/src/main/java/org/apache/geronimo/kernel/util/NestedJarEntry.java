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

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;

/**
 * @version $Rev$ $Date$
 */
public class NestedJarEntry extends JarEntry {
    private final JarEntry baseEntry;
    private final Manifest manifest;

    public NestedJarEntry(String name, JarEntry baseEntry, Manifest manifest) {
        super(name);
        this.baseEntry = baseEntry;
        this.manifest = manifest;
    }

    public JarEntry getBaseEntry() {
        return baseEntry;
    }

    public Attributes getAttributes() throws IOException {
        if (manifest == null) {
            return null;
        }
        return manifest.getAttributes(getName());
    }

    /**
     * Always return null.  This could be implementd by verifing the signatures
     * in the manifest file against the actual file, but we don't need this for
     * Geronimo.
     * @return null
     */
    public Certificate[] getCertificates() {
        return null;
    }

    public long getTime() {
        return baseEntry.getTime();
    }

    public void setTime(long time) {
        baseEntry.setTime(time);
    }

    public long getSize() {
        return baseEntry.getSize();
    }

    public void setSize(long size) {
        baseEntry.setSize(size);
    }

    public long getCompressedSize() {
        return baseEntry.getCompressedSize();
    }

    public void setCompressedSize(long csize) {
        baseEntry.setCompressedSize(csize);
    }

    public long getCrc() {
        return baseEntry.getCrc();
    }

    public void setCrc(long crc) {
        baseEntry.setCrc(crc);
    }

    public int getMethod() {
        return baseEntry.getMethod();
    }

    public void setMethod(int method) {
        baseEntry.setMethod(method);
    }

    public byte[] getExtra() {
        return baseEntry.getExtra();
    }

    public void setExtra(byte[] extra) {
        baseEntry.setExtra(extra);
    }

    public String getComment() {
        return baseEntry.getComment();
    }

    public void setComment(String comment) {
        baseEntry.setComment(comment);
    }

    public boolean isDirectory() {
        return baseEntry.isDirectory();
    }

    public String toString() {
        return baseEntry.toString();
    }

    public int hashCode() {
        return baseEntry.hashCode();
    }

    public Object clone() {
        return new NestedJarEntry(getName(), baseEntry, manifest);
    }
}
