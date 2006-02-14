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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class Maven2Repository extends AbstractRepository {
    public Maven2Repository(URI root, ServerInfo serverInfo) {
        super(root, serverInfo);
    }

    public Maven2Repository(File rootFile) {
        super(rootFile);
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


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(Maven2Repository.class);

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
