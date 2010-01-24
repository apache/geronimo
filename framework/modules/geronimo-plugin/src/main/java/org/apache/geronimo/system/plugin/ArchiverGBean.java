/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.Tar;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.TarFileSet;
import org.apache.tools.ant.types.ZipFileSet;

/**
 * @version $Rev$ $Date$
 */
public class ArchiverGBean implements ServerArchiver {

    private final ServerInfo serverInfo;

    private List<String> excludes = new ArrayList<String>();

    public ArchiverGBean(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public void addExclude(String pattern) {
        this.excludes.add(pattern);
    }

    public void removeExclude(String pattern) {
        this.excludes.remove(pattern);
    }

    private void removeExcludes(File source, Map<String, File> all) {
        Map<String, File> matches = new HashMap<String, File>();
        for (String exclude : this.excludes) {
            FileUtils.find(source, exclude, matches);
        }

        for (String exclude : matches.keySet()) {
            all.remove(exclude);
        }
    }

    public File archive(String sourcePath, String destPath, Artifact artifact) throws //ArchiverException,
            IOException {
        File source = serverInfo.resolve(sourcePath);
        File dest = serverInfo.resolve(destPath);
        String serverName = artifact.getArtifactId() + "-" + artifact.getVersion();
        dest = new File(dest, serverName + "-bin." + artifact.getType());
        Project project = new Project();
        MatchingTask archiver;
        if ("tar.gz".equals(artifact.getType())) {
            Tar tar = new Tar();
            Tar.TarCompressionMethod tarCompressionMethod = new Tar.TarCompressionMethod();
            tarCompressionMethod.setValue("gzip");
            tar.setCompression(tarCompressionMethod);
            Tar.TarLongFileMode fileMode = new Tar.TarLongFileMode();
            fileMode.setValue(Tar.TarLongFileMode.GNU);
            tar.setLongfile(fileMode);
            tar.setDestFile(dest);
            TarFileSet rc = new TarFileSet();
            rc.setDir(source);
            rc.setPrefix(serverName);
            rc.setProject(project);
            rc.setExcludes("bin/");
            tar.add(rc);

            rc = new TarFileSet();
            rc.setDir(source);
            rc.setPrefix(serverName);
            rc.setProject(project);
            rc.setIncludes("bin/");
            rc.setExcludes("bin/*.bat");
            rc.setFileMode("755");
            tar.add(rc);

            rc = new TarFileSet();
            rc.setDir(source);
            rc.setPrefix(serverName);
            rc.setProject(project);
            rc.setIncludes("bin/*.bat");
            tar.add(rc);

            archiver = tar;
        } else if ("zip".equals(artifact.getType())) {
            Zip zip = new Zip();
            zip.setDestFile(dest);
            ZipFileSet fs = new ZipFileSet();
            fs.setDir(source);
            fs.setPrefix(serverName);
            fs.setProject(project);
            zip.addFileset(fs);
            archiver = zip;
        } else {
            throw new IllegalArgumentException("Unknown target type: " + artifact.getType());
        }
        archiver.setProject(project);
        archiver.execute();
        return dest;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ArchiverGBean.class);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.setConstructor(new String[]{"ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
