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
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class Maven1Repository extends AbstractRepository implements ListableRepository {
    public Maven1Repository(URI root, ServerInfo serverInfo) {
        super(root, serverInfo);
    }

    public Maven1Repository(File rootFile) {
        super(rootFile);
    }

    public File getLocation(Artifact artifact) {
        File path = new File(rootFile, artifact.getGroupId().replace('.', File.separatorChar));
        path = new File(path, artifact.getType() + "s");
        path = new File(path, artifact.getArtifactId() + "-" + artifact.getVersion() + "." + artifact.getType());

        return path;
    }

    public SortedSet list(String groupId, String artifactId, String type) {
        SortedSet artifacts = new TreeSet();

        File path = new File(rootFile, groupId);
        path = new File(path, type + "s");

        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String fileName = file.getName();
            if (fileName.startsWith(artifactId + "-") && fileName.endsWith("." + type)) {
                String version = fileName.substring(artifactId.length() + 1);
                version = version.substring(0, version.length() - 1 - type.length());
                artifacts.add(new Artifact(groupId, artifactId, version, type));
            }
        }
        return artifacts;
    }

    //thanks to Brett Porter for this regex lifted from a maven1-2 porting tool
    private static final Pattern MAVEN_1_PATTERN = Pattern.compile("(.+)/(.+)s/(.+)-([0-9].+)\\.([^0-9]+)");

    public SortedSet list() {
        SortedSet artifacts = new TreeSet();
        String[] names = getFiles(rootFile, "");
        Matcher matcher = MAVEN_1_PATTERN.matcher("");
        for (int i = 0; i < names.length; i++) {
            matcher.reset(names[i]);
            if (matcher.matches()) {
                String groupId = matcher.group(1);
                String artifactId = matcher.group(3);
                String version = matcher.group(4);
                String type = matcher.group(5);
                artifacts.add(new Artifact(groupId, artifactId, version, type));
            } else {
            	log.warn("could not resolve URI for malformed repository entry: " + names[i] +
            	" - the filename should look like: <groupId>/<type>s/<artifactId>-<version>.<type>   "+
                "Perhaps you put in a file without a version number in the name?");
            }

        }
       	return artifacts;
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(Maven1Repository.class);

        infoFactory.addAttribute("root", URI.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.addInterface(Maven1Repository.class);

        infoFactory.setConstructor(new String[]{"root", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
