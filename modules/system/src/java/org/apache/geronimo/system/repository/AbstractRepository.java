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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractRepository implements WriteableRepository {
    protected static final Log log = LogFactory.getLog(AbstractRepository.class);
    private final static int TRANSFER_NOTIFICATION_SIZE = 10240;  // announce every this many bytes
    private final static int TRANSFER_BUF_SIZE = 10240;  // try this many bytes at a time
    protected final File rootFile;

    public AbstractRepository(URI root, ServerInfo serverInfo) {
        this(resolveRoot(root, serverInfo));
    }

    public AbstractRepository(File rootFile) {
        if (rootFile == null) throw new NullPointerException("root is null");

        if (!rootFile.exists() || !rootFile.isDirectory() || !rootFile.canRead()) {
            throw new IllegalStateException("Maven2Repository must have a root that's a valid readable directory (not " + rootFile.getAbsolutePath() + ")");
        }

        this.rootFile = rootFile;
        log.debug("Repository root is " + rootFile.getAbsolutePath());
    }

    private static File resolveRoot(URI root, ServerInfo serverInfo) {
        if (root == null) throw new NullPointerException("root is null");
//        if (serverInfo == null) throw new NullPointerException("serverInfo is null");

        if (!root.toString().endsWith("/")) {
            try {
                root = new URI(root.toString() + "/");
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid repository root (does not end with / ) and can't add myself", e);
            }
        }

        URI resolvedUri;
        if (serverInfo != null) {
            resolvedUri = serverInfo.resolve(root);
        } else {
            resolvedUri = root;
        }

        if (!resolvedUri.getScheme().equals("file")) {
            throw new IllegalStateException("FileSystemRepository must have a root that's a local directory (not " + resolvedUri + ")");
        }

        File rootFile = new File(resolvedUri);
        return rootFile;
    }

    public boolean contains(Artifact artifact) {
        File location = getLocation(artifact);
        return location.isFile() && location.canRead();
    }

    public abstract File getLocation(Artifact artifact);

    public abstract List list();

    public void copyToRepository(File source, Artifact destination, FileWriteMonitor monitor) throws IOException {
        if (!source.exists() || !source.canRead() || source.isDirectory()) {
            throw new IllegalArgumentException("Cannot read source file at " + source.getAbsolutePath());
        }
        copyToRepository(new FileInputStream(source), destination, monitor);
    }

    public void copyToRepository(InputStream source, Artifact destination, FileWriteMonitor monitor) throws IOException {
        // is this a wrtiable repository
        if (!rootFile.canWrite()) {
            throw new IllegalStateException("This repository is not writable: " + rootFile.getAbsolutePath() + ")");
        }

        // where are we going to install the file
        File location = getLocation(destination);

        // assure that there isn't already a file installed at the specified location
        if (location.exists()) {
            throw new IllegalArgumentException("Destination " + location.getAbsolutePath() + " already exists!");
        }

        // assure that the target directory exists
        File parent = location.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Unable to create directories from " + rootFile.getAbsolutePath() + " to " + parent.getAbsolutePath());
        }

        // copy it
        if (monitor != null) {
            monitor.writeStarted(destination.toString());
        }
        int total = 0;
        try {
            int threshold = TRANSFER_NOTIFICATION_SIZE;
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(location));
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
