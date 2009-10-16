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

package org.apache.geronimo.mavenplugins.geronimo.reporting;

import org.apache.geronimo.mavenplugins.geronimo.GeronimoMojoSupport;

import java.io.File;
import java.util.Date;
import java.util.Arrays;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Support for Geronimo mojos which can be processed by a set of {@link Reporter}s.
 * 
 * @version $Rev$ $Date$
 */
public abstract class ReportingMojoSupport
    extends GeronimoMojoSupport
{
    /**
     * Enable logging mode.
     *
     * @parameter expression="${logOutput}" default-value="false"
     */
    protected boolean logOutput = false;

    //
    // TODO: Figure out how to inject the mojo's goal name
    //

    /**
     * The directory where log files will be put under.
     * 
     * @parameter expression="${logOutputDirectory}" default-value="${project.build.directory}/geronimo-logs"
     */
    protected File logOutputDirectory;

    /**
     * When logOutput is enabled, the output is logged to the file location specified here.  If this
     * value is not present, then "${logOutputDirectory}/<em>goal-name</em>" log will be used.
     *
     * @parameter
     */
    protected File logFile = null;

    /**
     * A set of reporters which will do something interesting with the execution results.
     *
     * @parameter
     */
    protected Reporter[] reporters = null;


    /**
     * Whether to stop or proceed when errors and failures are encountered
     *
     * @parameter expression="${stopOnFailure}" default-value="true"
     */
    protected boolean stopOnFailure;

    /**
     * Provides hooks into the reporting interface to allow for customized reports to be generated
     * for goal executions.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        init();

        if ( log.isDebugEnabled() ) {
            if ( reporters != null ) {
                log.debug("Reporters: " + Arrays.asList(reporters));
            }
            else {
                log.debug("No reporters configured");
            }
        }

        reportBegin();

        try {
            doExecute();
        }
        catch ( Exception e ) {
            reportError(e);

            if ( stopOnFailure ) {
                if ( e instanceof MojoExecutionException ) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
                else if ( e instanceof MojoFailureException ) {
                    MojoFailureException x = new MojoFailureException(e.getMessage());
                    x.initCause(e);
                    throw x;
                }
                else {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
            else {
                log.warn("Ignoring failure !");
            }
        }
        finally {
            reportEnd();
        }
    }

    protected File getLogFile() {
        if (logFile == null) {
            return new File(logOutputDirectory, getFullClassName() + ".log");
        }
        
        try {
            return logFile.getCanonicalFile();
        }
        catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sub-class must override to provide the goal name.
     *
     * @return  The name of the goal.
     */
    protected abstract String getFullClassName();

    //
    // Reporter
    //

    private void reportBegin() {
        if ( reporters == null ) {
            return;
        }

        final Reportable source = new Reportable() {
            final Date start = new Date();

            public Date getStartTime() {
                return start;
            }

            public String getName() {
                return getFullClassName();
            }

            public File getLogFile() {
                return ReportingMojoSupport.this.getLogFile();
            }
        };

        for ( int i =0; i < reporters.length; i++ ) {
            reporters[i].reportBegin(source);
        }
    }

    private void reportError(final Throwable cause) {
        assert cause != null;

        if ( reporters == null ) {
            return;
        }

        for ( int i=0; i < reporters.length; i++ ) {
            reporters[i].reportError(cause);
        }
    }

    private void reportEnd() {
        if ( reporters == null ) {
            return;
        }

        for ( int i=0; i < reporters.length; i++ ) {
            reporters[i].reportEnd();
        }
    }
}
