/*
 *  Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Support for Mojo implementations.
 *
 * @version $Id: MojoSupport.java 422054 2006-07-14 21:25:59Z jdillon $
 */
public abstract class MojoSupport
    extends AbstractMojo
{
    protected Log log;

    protected void init() {
        log = getLog();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        init();

        try {
            doExecute();
        }
        catch (Exception e) {
            if (e instanceof MojoExecutionException) {
                throw (MojoExecutionException)e;
            }
            else if (e instanceof MojoFailureException) {
                throw (MojoFailureException)e;
            }
            else {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    protected void doExecute() throws Exception {
        // Sub-class should override
    }
}
