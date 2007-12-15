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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class Maven2Repository extends AbstractRepository implements WritableListableRepository {

    public Maven2Repository(File rootFile) {
        super(rootFile);
    }

    public File getLocation(Artifact artifact) {
        if(!artifact.isResolved()) {
            throw new IllegalArgumentException("Artifact "+artifact+" is not fully resolved");
        }
        File path = new File(rootFile, artifact.getGroupId().replace('.', File.separatorChar));
        path = new File(path, artifact.getArtifactId());
        path = new File(path, artifact.getVersion().toString());
        path = new File(path, artifact.getArtifactId() + "-" + artifact.getVersion() + "." + artifact.getType());

        return path;
    }

    public SortedSet list() {
        return listInternal(null, null, null);
    }

    public SortedSet list(Artifact query) {
        if(query.getGroupId() != null) { // todo: see if more logic can be shared with the other case
            File path = new File(rootFile, query.getGroupId().replace('.', File.separatorChar));
            path = new File(path, query.getArtifactId());
            if(!path.canRead() || !path.isDirectory()) {
                return new TreeSet();
            }

            SortedSet artifacts = new TreeSet();

            File[] versionDirs = path.listFiles();
            for (int i = 0; i < versionDirs.length; i++) {
                File versionDir = versionDirs[i];
                if (versionDir.canRead() && versionDir.isDirectory()) {
                    String version = versionDir.getName();
                    if(query.getVersion() != null && !query.getVersion().toString().equals(version)) {
                        continue;
                    }
                    // Assumes that artifactId is set
                    final String filePrefix = query.getArtifactId() + "-" + version + ".";
                    File[] list = versionDir.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return name.startsWith(filePrefix);
                        }
                    });
                    for (int j = 0; j < list.length; j++) {
                        File file = list[j];
                        String end = file.getName().substring(filePrefix.length());
                        if(query.getType() != null && !query.getType().equals(end)) {
                            continue;
                        }
                        if(end.indexOf('.') < 0) {
                            artifacts.add(new Artifact(query.getGroupId(), query.getArtifactId(), version, end));
                        }
                    }
                }
            }
            return artifacts;
        } else {
            return listInternal(query.getArtifactId(), query.getType(), query.getVersion() == null ? null : query.getVersion().toString());
        }
    }

    private SortedSet listInternal(String artifactMatch, String typeMatch, String versionMatch) {
        SortedSet artifacts = new TreeSet();
        File[] groupIds = rootFile.listFiles();
        for (int i = 0; i < groupIds.length; i++) {
            File groupId = groupIds[i];
            if (groupId.canRead() && groupId.isDirectory()) {
                File[] versionDirs = groupId.listFiles();
                for (int j = 0; j < versionDirs.length; j++) {
                    File versionDir = versionDirs[j];
                    if (versionDir.canRead() && versionDir.isDirectory()) {
                        artifacts.addAll(getArtifacts(null, versionDir, artifactMatch, typeMatch, versionMatch));
                    }
                }
            }
        }
        return artifacts;
    }

    private List getArtifacts(String groupId, File versionDir, String artifactMatch, String typeMatch, String versionMatch) {
        // org/apache/xbean/xbean-classpath/2.2-SNAPSHOT/xbean-classpath-2.2-SNAPSHOT.jar
        List artifacts = new ArrayList();
        String artifactId = versionDir.getParentFile().getName();

        File[] files = versionDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.canRead()) {
                if (file.isDirectory()) {
                    File test = new File(file, "META-INF");
                    if(test.exists() && test.isDirectory() && test.canRead() && groupId != null) {
                        String version = versionDir.getName();
                        String fileHeader = artifactId + "-" + version + ".";

                        String fileName = file.getName();
                        if (fileName.startsWith(fileHeader)) {
                            // type is everything after the file header
                            String type = fileName.substring(fileHeader.length());

                            if (!type.endsWith(".sha1") && !type.endsWith(".md5")) {
                                if(artifactMatch != null && !artifactMatch.equals(artifactId)) {
                                    continue;
                                }
                                if(typeMatch != null && !typeMatch.equals(type)) {
                                    continue;
                                }
                                if(versionMatch != null && !versionMatch.equals(version)) {
                                    continue;
                                }
                                artifacts.add(new Artifact(groupId,
                                        artifactId,
                                        version,
                                        type));
                            }
                        }
                    } else { // this is just part of the path to the artifact
                        String nextGroupId;
                        if (groupId == null) {
                            nextGroupId = artifactId;
                        } else {
                            nextGroupId = groupId + "." + artifactId;
                        }

                        artifacts.addAll(getArtifacts(nextGroupId, file, artifactMatch, typeMatch, versionMatch));
                    }
                } else if (groupId != null) {
                    String version = versionDir.getName();
                    String fileHeader = artifactId + "-" + version + ".";

                    String fileName = file.getName();
                    if (fileName.startsWith(fileHeader)) {
                        // type is everything after the file header
                        String type = fileName.substring(fileHeader.length());

                        if (!type.endsWith(".sha1") && !type.endsWith(".md5")) {
                            if(artifactMatch != null && !artifactMatch.equals(artifactId)) {
                                continue;
                            }
                            if(typeMatch != null && !typeMatch.equals(type)) {
                                continue;
                            }
                            if(versionMatch != null && !versionMatch.equals(version)) {
                                continue;
                            }
                            artifacts.add(new Artifact(groupId,
                                    artifactId,
                                    version,
                                    type
                            ));
                        }
                    }
                }
            }
        }
        return artifacts;
    }

}
