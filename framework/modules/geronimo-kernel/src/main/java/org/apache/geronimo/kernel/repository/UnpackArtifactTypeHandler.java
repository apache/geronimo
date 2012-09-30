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
package org.apache.geronimo.kernel.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class UnpackArtifactTypeHandler implements ArtifactTypeHandler {
    
    private final static int TRANSFER_NOTIFICATION_SIZE = 10240;  // announce every this many bytes
    private final static int TRANSFER_BUF_SIZE = 10240;  // try this many bytes at a time

    public void install(InputStream source, int size, Artifact artifact, FileWriteMonitor monitor, File target) throws IOException {
        // assure that the target directory exists
        File parent = target.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Unable to create directory " + parent.getAbsolutePath());
        }

        // copy it
        if (monitor != null) {
            monitor.writeStarted(artifact.toString(), size);
        }

        int total = 0;
        ZipInputStream in = new ZipInputStream(source);
        try {
            int threshold = UnpackArtifactTypeHandler.TRANSFER_NOTIFICATION_SIZE;
            byte[] buffer = new byte[TRANSFER_BUF_SIZE];

            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                File file = new File(target, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    if (!entry.getName().equals("META-INF/startup-jar")) {
                        file.getParentFile().mkdirs();
                        OutputStream out = new FileOutputStream(file);
                        try {
                            int count;
                            while ((count = in.read(buffer)) > 0) {
                                out.write(buffer, 0, count);
                                if (monitor != null) {
                                    total += count;
                                    if (total > threshold) {
                                        threshold += UnpackArtifactTypeHandler.TRANSFER_NOTIFICATION_SIZE;
                                        monitor.writeProgress(total);
                                    }
                                }
                            }
                        } finally {
                            IOUtils.flush(out);
                            out.close();
                        }
                        in.closeEntry();
                    }
                }
            }
        } catch (IOException e) {
            FileUtils.recursiveDelete(target);
            throw e;
        } finally {
            in.close();
            if (monitor != null) {
                monitor.writeComplete(total);
            }
        }
    }

    @Override
    public void install(File source, Artifact artifactId, FileWriteMonitor monitor, File target) throws IOException {
        if (source.isFile()) {
            long size = (monitor == null) ? -1 : getJarSize(source);
            FileInputStream is = new FileInputStream(source);
            try {
                install(is, (int) size, artifactId, monitor, target);
            } finally {
                IOUtils.close(is);
            }
        } else {
            FileUtils.recursiveCopy(source, target);
        }        
    }
    
    private long getJarSize(File source) throws IOException {
        long size = 0;
        ZipFile zip = null;
        try {
            zip = new ZipFile(source);
            for (Enumeration<? extends ZipEntry> entries=zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();
                size += entry.getSize();
            }
        } catch (ZipException ze) {
            size = (int)source.length();
        } finally {
            JarUtils.close(zip);
        }
        return size;
    }
}
