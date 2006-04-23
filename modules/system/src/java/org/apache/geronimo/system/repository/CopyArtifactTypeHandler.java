/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.repository;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;

import org.apache.geronimo.kernel.repository.ArtifactTypeHandler;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;

/**
 * @version $Rev$ $Date$
 */
public class CopyArtifactTypeHandler implements ArtifactTypeHandler {
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
        try {
            int threshold = TRANSFER_NOTIFICATION_SIZE;
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
            BufferedInputStream in = new BufferedInputStream(source);
            byte[] buf = new byte[TRANSFER_BUF_SIZE];
            int count;
            while ((count = in.read(buf)) > -1) {
                out.write(buf, 0, count);
                if (monitor != null) {
                    total += count;
                    if (total > threshold) {
                        threshold += TRANSFER_NOTIFICATION_SIZE;
                        monitor.writeProgress(total);
                    }
                }
            }
            out.flush();
            out.close();
            in.close();
        } finally {
            if (monitor != null) {
                monitor.writeComplete(total);
            }
        }
    }
}
