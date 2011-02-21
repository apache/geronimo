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


package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.io.IOException;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.ArchiverGBean;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Creates an archive containing a server assembly including all the dependencies in this projects pom.
 *
 * @version $Rev$ $Date$
 * @goal archive
 */
public class ArchiveMojo extends AbstractCarMojo {

    /**
     * The target directory of the project.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     * @readonly
     */
    private File destDir;


    /**
     * The location of the server repository.
     *
     * @parameter expression="${project.build.directory}/assembly"
     * @required
     */
    private File targetServerDirectory;
    
    /**
     * Files to exclude from the archive
     *
     * @parameter
     */
    private String[] excludes;
    
    /**
     * whether to create tar.gz archive
     * @parameter expression="${tarAssemblies}" default-value="true"
     */
    private boolean tarAssemblies;

    /**
     * The target file to set as the project's artifact.
     *
     * @parameter expression="${project.file}"
     * @required
     */
    private File targetFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // this installs the pom using the default artifact handler configured in components.xml
        getLog().debug("Setting artifact file: " + targetFile);
        org.apache.maven.artifact.Artifact artifact = project.getArtifact();
        artifact.setFile(targetFile);
        try {
            //now pack up the server.
            ServerInfo serverInfo = new BasicServerInfo(targetServerDirectory.getAbsolutePath(), false);
            ArchiverGBean archiver = new ArchiverGBean(serverInfo);
            if (excludes != null) {
                for (String exclude : excludes) {
                    archiver.addExclude(exclude);
                }
            }
            if(tarAssemblies){
                archive("tar.gz", archiver);
            }
            archive("zip", archiver);
        } catch (Exception e) {
            throw new MojoExecutionException("Could not archive plugin", e);
        }
    }

    private void archive(String type, ArchiverGBean archiver) throws IOException {
        Artifact artifact1 = new Artifact(project.getArtifact().getGroupId(), project.getArtifact().getArtifactId(), project.getArtifact().getVersion(), type);
        File target1 = archiver.archive("", destDir.getAbsolutePath(), artifact1);
        projectHelper.attachArtifact( project, artifact1.getType(), "bin", target1 );
    }
}
