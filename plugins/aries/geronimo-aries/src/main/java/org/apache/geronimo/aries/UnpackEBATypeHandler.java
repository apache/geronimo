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
package org.apache.geronimo.aries;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactTypeHandler;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;

/**
 * @version $Rev:385232 $ $Date$
 */
public class UnpackEBATypeHandler implements ArtifactTypeHandler {

    @Override
    public void install(InputStream source, int size, Artifact artifactId, FileWriteMonitor monitor, File target) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void install(File source, Artifact artifactId, FileWriteMonitor monitor, File target) throws IOException {
        if (source.isFile()) {
            throw new IllegalStateException("Source must be a directory");
        }
                
        unpack(source, target, new byte[IOUtils.DEFAULT_COPY_BUFFER_SIZE]);
    }
    
    private void unpack(File srcDir, File dstDir, byte[] buffer) throws IOException {
        File[] children = srcDir.listFiles();
        for (File child : children) {
            File destination = new File(dstDir, child.getName());
            if (child.isDirectory()) {
                if ("META-INF".equals(child.getName())) {
                    FileUtils.recursiveCopy(child, destination, buffer);
                } else {
                    unpack(child, destination, buffer);
                }
            } else {               
                ZipFile zipIn = null;
                try {
                    zipIn = new ZipFile(child);
                    JarUtils.unzipToDirectory(zipIn, destination);
                } catch (Exception e) {
                    FileUtils.copyFile(child, destination, buffer);
                } finally {
                    JarUtils.close(zipIn);
                }
            }
        }
    }
}
