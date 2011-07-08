/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.geronimo.mavenplugins.property;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/** 
 * @goal set-property
 */
public class SetPropertyMojo extends AbstractLogEnabled implements Mojo {
    
    private Log log;

    /**
     * @parameter
     * @required
     */
    protected String propertyName;
    
    /**
     * @parameter
     */
    protected String propertyValuePrefix;
    
    /**
     * @parameter default-value = "true"
     */
    protected boolean listFiles;
    
    /**
     * @parameter
     */
    protected Dependency[] classpath;
    
    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * The artifact repository to use.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The artifact factory to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactFactory artifactFactory;
        
    public void execute() throws MojoExecutionException, MojoFailureException {
        StringBuilder buffer = new StringBuilder();
        
        if (classpath != null && classpath.length > 0) {
            if (propertyValuePrefix != null) {
                buffer.append(propertyValuePrefix);
            }
            for (int i = 0; i < classpath.length; i++) {
                Dependency dependency = classpath[i];
                Artifact artifact = artifactFactory.createArtifact(dependency.getGroupId(), 
                                                                   dependency.getArtifactId(), 
                                                                   dependency.getVersion(), 
                                                                   null, 
                                                                   dependency.getType());
                String path = localRepository.pathOf(artifact);
                File file = new File(localRepository.getBasedir(), path);
                if (!file.exists()) {
                    getLog().warn("File " + file + " does not exist");
                }
                
                if (listFiles) {
                    buffer.append(file.getAbsolutePath());
                } else {
                    buffer.append(file.getParentFile().getAbsolutePath());
                }
                
                if (i + 1 < classpath.length) {
                    buffer.append(File.pathSeparator);
                }
            }
        }

        getLog().debug("Setting "  + propertyName + " property to " + buffer);
        
        project.getProperties().put(propertyName, buffer.toString());
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public Log getLog() {
        if (log == null) {
            setLog(new SystemStreamLog());
        }
        return log;
    }
           
}
