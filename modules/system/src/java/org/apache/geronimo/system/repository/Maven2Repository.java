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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class Maven2Repository {
    private static final Log log = LogFactory.getLog(FileSystemRepository.class);
    private final static int TRANSFER_NOTIFICATION_SIZE = 10240;  // announce every this many bytes
    private final static int TRANSFER_BUF_SIZE = 10240;  // try this many bytes at a time
    private File rootFile;

    public Maven2Repository(URI root, ServerInfo serverInfo) {
        this(resolveRoot(root, serverInfo));
    }

    private static File resolveRoot(URI root, ServerInfo serverInfo) {
        if (root == null) throw new NullPointerException("root is null");
        if (serverInfo == null) throw new NullPointerException("serverInfo is null");

        if (!root.toString().endsWith("/")) {
            try {
                root = new URI(root.toString() + "/");
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid repository root (does not end with / ) and can't add myself", e);
            }
        }

        URI resolvedUri = serverInfo.resolve(root);

        if (!resolvedUri.getScheme().equals("file")) {
            throw new IllegalStateException("FileSystemRepository must have a root that's a local directory (not " + resolvedUri + ")");
        }

        File rootFile = new File(resolvedUri);
        return rootFile;
    }

    public Maven2Repository(File rootFile) {
        if (rootFile == null) throw new NullPointerException("root is null");

        if (!rootFile.exists() || !rootFile.isDirectory() || !rootFile.canRead()) {
            throw new IllegalStateException("Maven2Repository must have a root that's a valid readable directory (not " + rootFile.getAbsolutePath() + ")");
        }

        this.rootFile = rootFile;
        log.debug("Repository root is " + rootFile.getAbsolutePath());
    }

    public boolean contains(Artifact artifact) {
        File location = getLocation(artifact);
        return location.isFile() && location.canRead();
    }

    public File getLocation(Artifact artifact) {
        File path = new File(rootFile, artifact.getGroupId().replace('.', File.separatorChar));
        path = new File(path, artifact.getArtifactId());
        path = new File(path, artifact.getVersion().toString());
        path = new File(path, artifact.getArtifactId() + "-" + artifact.getVersion() + "." + artifact.getType());

        return path;
    }

    public List list() {
        List artifacts = new ArrayList();
        File[] groupIds = rootFile.listFiles();
        for (int i = 0; i < groupIds.length; i++) {
            File groupId = groupIds[i];
            if (groupId.canRead() && groupId.isDirectory()) {
                File[] versionDirs = groupId.listFiles();
                for (int j = 0; j < versionDirs.length; j++) {
                    File versionDir = versionDirs[j];
                    if (versionDir.canRead() && versionDir.isDirectory()) {
                        artifacts.addAll(getArtifacts(null, versionDir));
                    }
                }
            }
        }
        return artifacts;
    }

    private List getArtifacts(String groupId, File versionDir) {
        // org/apache/xbean/xbean-classpath/2.2-SNAPSHOT/xbean-classpath-2.2-SNAPSHOT.jar
        List artifacts = new ArrayList();
        String artifactId = versionDir.getParentFile().getName();

        File[] files = versionDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.canRead()) {
                if (file.isDirectory()) {

                    String nextGroupId;
                    if (groupId == null) {
                        nextGroupId = artifactId;
                    } else {
                        nextGroupId = groupId + "." + artifactId;
                    }

                    artifacts.addAll(getArtifacts(nextGroupId, file));
                } else if (groupId != null) {
                    String version = versionDir.getName();
                    String fileHeader = artifactId + "-" + version + ".";

                    String fileName = file.getName();
                    if (fileName.startsWith(fileHeader)) {
                        // type is everything after the file header
                        String type = fileName.substring(fileHeader.length());

                        if (!type.endsWith(".sha1") && !type.endsWith(".md5")) {
                            artifacts.add(new Artifact(groupId,
                                    artifactId,
                                    version,
                                    type,
                                    true));
                        }
                    }
                }
            }
        }
        return artifacts;
    }


    public void install(File source, Artifact destination, FileWriteMonitor monitor) throws IOException {
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(FileSystemRepository.class);

        infoFactory.addAttribute("root", URI.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.addInterface(Maven2Repository.class);

        infoFactory.setConstructor(new String[]{"root", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
