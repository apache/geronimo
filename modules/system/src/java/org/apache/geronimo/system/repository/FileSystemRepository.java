/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.io.File;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * A listable, writeable repository that stores its entries in a directory on
 * the local filesystem.
 *
 * @version $Rev$ $Date$
 */
public class FileSystemRepository implements Repository, ListableRepository, WriteableRepository, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(FileSystemRepository.class);
    private final static int TRANSFER_NOTIFICATION_SIZE = 10240;  // announce every this many bytes
    private final static int TRANSFER_BUF_SIZE = 10240;  // try this many bytes at a time
    private final URI root;
    private final ServerInfo serverInfo;
    private URI rootURI;
    private File rootFile;

    public FileSystemRepository(URI root, ServerInfo serverInfo) {
        if (!root.toString().endsWith("/")) {
            try {
                root = new URI(root.toString() + "/");
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid repository root (does not end with / ) and can't add myself", e);
            }
        }
        this.root = root;
        this.serverInfo = serverInfo;
    }

    public boolean hasURI(URI uri) {
        uri = resolve(uri);
        if(uri == null) {
            return false;
        }
        if ("file".equals(uri.getScheme())) {
            File f = new File(uri);
            return f.exists() && f.canRead();
        } else {
            return false;
        }
    }

    public URL getURL(URI uri) throws MalformedURLException {
        URL url = resolve(uri).toURL();
        if (!url.getProtocol().equals("file")) {
            return null;
        }
        return url;
    }

    private URI resolve(final URI uri) {
        String[] bits = uri.toString().split("/");
        if(bits.length < 3) {
            log.warn("Unable to resolve URI '"+uri+"' as a repository entry");
            return null;
        }
        StringBuffer buf = new StringBuffer(bits[0]).append('/');
        String type = bits.length >= 4 ? bits[3] : "jar";
        buf.append(type).append('s').append('/').append(bits[1]).append('-').append(bits[2]).append('.').append(type);
        return rootURI.resolve(buf.toString());
    }


    //thanks to Brett Porter for this regex lifted from a maven1-2 porting tool
    private static final Pattern pattern = Pattern.compile("(.+)/(.+)s/(.+)-([0-9].+)\\.([^0-9]+)");

    public URI[] listURIs() throws URISyntaxException {
        String[] results = getFiles(rootFile, "");
        URI[] out = new URI[results.length];
        Matcher matcher = pattern.matcher("");
        for (int i = 0; i < out.length; i++) {
            matcher.reset(results[i]);
            if (matcher.matches()) {
                String groupId = matcher.group(1);
                String artifactId = matcher.group(3);
                String version = matcher.group(4);
                String type = matcher.group(5);
                StringBuffer buf = new StringBuffer(groupId).append("/").append(artifactId).append("/").append(version).append("/").append(type);
                out[i] = new URI(buf.toString());
            } else {
                //??
            }

        }
        return out;
    }

    public String[] getFiles(File base, String prefix) {
        if (!base.canRead() || !base.isDirectory()) {
            throw new IllegalArgumentException(base.getAbsolutePath());
        }
        List list = new ArrayList();
        File[] hits = base.listFiles();
        for (int i = 0; i < hits.length; i++) {
            File hit = hits[i];
            if (hit.canRead()) {
                if (hit.isDirectory()) {
                    list.addAll(Arrays.asList(getFiles(hit, prefix.equals("") ? hit.getName() : prefix + "/" + hit.getName())));
                } else {
                    list.add(prefix.equals("") ? hit.getName() : prefix + "/" + hit.getName());
                }
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public void copyToRepository(File source, URI destination, FileWriteMonitor monitor) throws IOException {
        if (!source.exists() || !source.canRead() || source.isDirectory()) {
            throw new IllegalArgumentException("Cannot read source file at " + source.getAbsolutePath());
        }
        copyToRepository(new FileInputStream(source), destination, monitor);
    }

    public void copyToRepository(InputStream source, URI destination, FileWriteMonitor monitor) throws IOException {
        if (!rootFile.canWrite()) {
            throw new IllegalStateException("This repository is not writable: " + rootFile.getAbsolutePath() + ")");
        }

        File dest = new File(resolve(destination));
        if (dest.exists()) {
            throw new IllegalArgumentException("Destination " + dest.getAbsolutePath() + " already exists!");
        }
        final File parent = dest.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Unable to create directories from " + rootFile.getAbsolutePath() + " to " + parent.getAbsolutePath());
        }
        if (monitor != null) {
            monitor.writeStarted(destination.toString());
        }
        int total = 0;
        try {
            int threshold = TRANSFER_NOTIFICATION_SIZE;
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
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

    public void doStart() throws Exception {
        if (rootURI == null) {
            if (serverInfo != null) {
                rootURI = serverInfo.resolve(root);
            } else {
                rootURI = root;
            }
            if (!rootURI.getScheme().equals("file")) {
                throw new IllegalStateException("FileSystemRepository must have a root that's a local directory (not " + rootURI + ")");
            }
            rootFile = new File(rootURI);
            if (!rootFile.exists() || !rootFile.isDirectory() || !rootFile.canRead()) {
                throw new IllegalStateException("FileSystemRepository must have a root that's a valid readable directory (not " + rootFile.getAbsolutePath() + ")");
            }
        }
        log.debug("Repository root is " + rootFile.getAbsolutePath());
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(FileSystemRepository.class);

        infoFactory.addAttribute("root", URI.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.addInterface(Repository.class);
        infoFactory.addInterface(ListableRepository.class);
        infoFactory.addInterface(WriteableRepository.class);

        infoFactory.setConstructor(new String[]{"root", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
