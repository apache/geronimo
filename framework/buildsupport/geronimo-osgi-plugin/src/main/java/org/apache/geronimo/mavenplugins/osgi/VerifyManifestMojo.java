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

package org.apache.geronimo.mavenplugins.osgi;

import java.io.File;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.geronimo.mavenplugins.osgi.utils.BundleResolver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.osgi.framework.BundleException;

/** 
 * @goal verify-manifest
 */
public class VerifyManifestMojo extends AbstractLogEnabled implements Mojo {

    private Log log;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;
        
    /**
     * Output directory.
     *
     * @parameter expression="${project.build.directory}/classes"
     * @required
     */
    protected File targetDir = null;
    
    /**
     * Profile name.
     * 
     * @parameter      
     */
    protected String profileName = null;
    
    /**
     * @parameter   
     */
    protected boolean failOnError = true;

    public void execute() throws MojoExecutionException, MojoFailureException {
        
        File manifest = new File(targetDir, JarFile.MANIFEST_NAME);
        if (!manifest.exists()) {
            return;
        }
        
        if (profileName != null) {
            System.setProperty("osgi.java.profile", profileName);
        }
        
        BundleResolver stateController = new BundleResolver(getLogger());
        
        List<String> classpath;
        try {
            classpath = (List<String>)project.getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
        for (String path : classpath) {
            try {
                File cp = new File(path);
                if (log.isDebugEnabled()) {
                    log.debug("Adding bundle: " + cp);
                }
                if (cp.exists()) {
                    stateController.addBundle(cp);
                }
            } catch (BundleException e) {
                log.error(e.getMessage(), e);
            }
        }
        
        stateController.resolveState();
        BundleDescription b = stateController.getBundleDescription(targetDir);                
        if (b != null) {
            log.info("Resolving OSGi bundle: " + b.getSymbolicName());
            try {
                stateController.assertResolved(b);
                log.info("OSGi bundle is resolved: " + b.getSymbolicName());
            } catch (BundleException e) {
                stateController.analyzeErrors(b);
                if (failOnError) {
                    throw new MojoExecutionException("OSGi bundle resolution failed");
                }
            }
        }    
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
