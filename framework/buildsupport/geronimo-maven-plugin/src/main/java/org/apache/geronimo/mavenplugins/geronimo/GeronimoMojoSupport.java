/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.geronimo;

import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.ArtifactRepository;

import org.codehaus.mojo.pluginsupport.ant.AntMojoSupport;

/**
 * Support for Geronimo mojos.
 *
 * @version $Rev$ $Date$
 */
public abstract class GeronimoMojoSupport
    extends AntMojoSupport
{
    //
    // NOTE: Not all mojos need Ant support, but due to the inability of Maven to inject custom components
    //       with their fields initalized we must use inheritence, see below.
    //

    //
    // NOTE: These fields are used by all mojo's except for install, which does not need to
    //       connect to the server, but there is as of yet, no easy way to share common
    //       code in a Mavne plugin w/o inheritence, so for now these are duplicated for
    //       all mojos.
    //

    /**
     * The hostname of the server to connect to.
     *
     * @parameter expression="${hostname}" default-value="localhost"
     */
    protected String hostname = null;

    /**
     * The port number to connect to the server.
     *
     * @parameter expression="${port}" default-value="1099"
     */
    protected int port = -1;

    /**
     * The username to authenticate with.
     *
     * @parameter expression="${username}" default-value="system"
     */
    protected String username = null;

    /**
     * The password to authenticate with.
     *
     * @parameter expression="${password}" default-value="manager"
     */
    protected String password = null;

    //
    // MojoSupport Hooks
    //

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project = null;

    protected MavenProject getProject() {
        return project;
    }

    /**
     * ???
     *
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    protected ArtifactRepository artifactRepository = null;

    protected ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }
}
