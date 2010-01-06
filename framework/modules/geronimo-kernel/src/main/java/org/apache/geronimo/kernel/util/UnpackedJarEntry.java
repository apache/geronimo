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

import java.util.jar.JarEntry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.io.IOException;
import java.io.File;
import java.security.cert.Certificate;

/**
 * @version $Rev$ $Date$
 */
public class UnpackedJarEntry extends JarEntry {
    private final File file;
    private final Manifest manifest;

    public UnpackedJarEntry(String name, File file, Manifest manifest) {
        super(name);
        this.file = file;
        this.manifest = manifest;
    }

    public File getFile() {
        return file;
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

    /**
     * An unpacked jar is read only, so this method always throws an UnsupportedOperationException.
     * @param time ignored
     * @throws UnsupportedOperationException always
     */
    public void setTime(long time) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Can not change the time of unpacked jar entry");
    }

    public long getTime() {
        return file.lastModified();
    }

    /**
     * An unpacked jar is read only, so this method always throws an UnsupportedOperationException.
     * @param size ignored
     * @throws UnsupportedOperationException always
     */
    public void setSize(long size) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Can not change the size of unpacked jar entry");
    }

    public long getSize() {
        if (file.isDirectory()) {
            return -1;
        } else {
            return file.length();
        }
    }

    /**
     * An unpacked jar is not compressed, so this method returns getSize().
     * @return getSize()
     */
    public long getCompressedSize() {
        return getSize();
    }

    /**
     * An unpacked jar is read only, so this method always throws an UnsupportedOperationException.
     * @param compressedSize ignored
     * @throws UnsupportedOperationException always
     */
    public void setCompressedSize(long compressedSize) {
        throw new UnsupportedOperationException("Can not change the compressed size of unpacked jar entry");
    }

    public long getCrc() {
        return super.getCrc();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * An unpacked jar is read only, so this method always throws an UnsupportedOperationException.
     * @param crc ignored
     * @throws UnsupportedOperationException always
     */
    public void setCrc(long crc) {
        throw new UnsupportedOperationException("Can not change the crc of unpacked jar entry");
    }

    public int getMethod() {
        return ZipEntry.STORED;
    }

    /**
     * An unpacked jar is read only, so this method always throws an UnsupportedOperationException.
     * @param method ignored
     * @throws UnsupportedOperationException always
     */
    public void setMethod(int method) {
        throw new UnsupportedOperationException("Can not change the method of unpacked jar entry");
    }

    /**
     * Always returns null.
     * @return null
     */
    public byte[] getExtra() {
        return null;
    }

    /**
     * An unpacked jar is read only, so this method always throws an UnsupportedOperationException.
     * @param extra ignored
     * @throws UnsupportedOperationException always
     */
    public void setExtra(byte[] extra) {
        throw new UnsupportedOperationException("Can not change the extra data of unpacked jar entry");
    }

    /**
     * Always returns null.
     * @return null
     */
    public String getComment() {
        return null;
    }

    /**
     * An unpacked jar is read only, so this method always throws an UnsupportedOperationException.
     * @param comment ignored
     * @throws UnsupportedOperationException always
     */
    public void setComment(String comment) {
        throw new UnsupportedOperationException("Can not change the comment of unpacked jar entry");
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public Object clone() {
        return new UnpackedJarEntry(getName(), file, manifest);
    }
}
