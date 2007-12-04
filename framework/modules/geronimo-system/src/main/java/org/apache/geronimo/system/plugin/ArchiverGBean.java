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
import java.util.Map;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.IOUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnixStat;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarLongFileMode;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
//maven inconsistency -- if we can use 1.0-alpha-9 uncomment
//import org.codehaus.plexus.archiver.util.DefaultFileSet;

/**
 * @version $Rev:$ $Date:$
 */
public class ArchiverGBean implements ServerArchiver {

    private final ServerInfo serverInfo;


    public ArchiverGBean(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public File archive(String sourcePath, String destPath, Artifact artifact) throws ArchiverException, IOException {
        File source = serverInfo.resolve(sourcePath);
        File dest = serverInfo.resolve(destPath);
        String serverName = artifact.getArtifactId() + "-" + artifact.getVersion();
        dest = new File(dest, serverName + "-bin." + artifact.getType());
        Archiver archiver;
        if ("tar.gz".equals(artifact.getType())) {
            archiver = new TarArchiver();
            TarArchiver.TarCompressionMethod tarCompressionMethod = new TarArchiver.TarCompressionMethod();
            tarCompressionMethod.setValue("gzip");
            ((TarArchiver) archiver).setCompression(tarCompressionMethod);
            TarLongFileMode fileMode = new TarLongFileMode();
            fileMode.setValue(TarLongFileMode.GNU);
            ((TarArchiver) archiver).setLongfile(fileMode);
        } else if ("zip".equals(artifact.getType())) {
            archiver = new ZipArchiver();
        } else {
            throw new IllegalArgumentException("Unknown target type: " + artifact.getType());
        }
        archiver.setIncludeEmptyDirs(true);
        archiver.setDestFile(dest);
/* see if using plexus-archiver 1.0-alpha-7 same as maven lets us share code.  Following is for 1.0-alpha-9
        DefaultFileSet all = new DefaultFileSet();
        all.setDirectory(source);
        archiver.addFileSet(all);
*/
        //workaround code
        Map<String, File> all = IOUtil.listAllFileNames(source);
        for (Map.Entry<String, File> entry : all.entrySet()) {
            String destFileName = serverName + "/" + entry.getKey();
            File sourceFile = entry.getValue();
            if (!destFileName.endsWith(".bat") && sourceFile.isFile()) {
                archiver.addFile(sourceFile, destFileName, UnixStat.DEFAULT_DIR_PERM);
            }
        }

        //end workaround code
        File bin = new File(source, "bin");
        if (bin.exists()) {
            Map<String, File> includes = IOUtil.listAllFileNames(bin);
            for (Map.Entry<String, File> entry : includes.entrySet()) {
                String destFileName = serverName + "/bin/" + entry.getKey();
                File sourceFile = entry.getValue();
                if (!destFileName.endsWith(".bat") && sourceFile.isFile()) {
                    archiver.addFile(sourceFile, destFileName, UnixStat.DEFAULT_DIR_PERM);
                }
            }
        }
        archiver.createArchive();
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
