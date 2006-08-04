/**
 *
 * Copyright 2004-2006 The Apache Software Foundation
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

package org.apache.geronimo.plugins.deployment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import org.apache.geronimo.plugins.util.ServerBehavior;

import org.apache.commons.lang.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;

//
// TODO: Rename to StartRemoteServerMojo
//

/**
 * ???
 *
 * @goal startRemoteServer
 * 
 * @version $Rev$ $Date$
 */
public class StartRemoteServerMojo extends AbstractModuleMojo {

    /**
     * @parameter
     */
    private File geronimoTarget;

    /**
     * @parameter expression="${basedir}/target"
     */
    private File workingDirectory;

    //
    // FIXME: Use <args><args>...</args></args> and let M2 handle parsing
    //

    /**
     * @parameter default-value=""
     */
    private String vmArgs;

    /**
     * @parameter
     */
    private String[] configs;

    /**
     * @parameter
     */
    private String debugPort;

    /**
     * Get the path of Java depending the OS.
     *
     * @return the path of the Java
     */
    private String getJavaPath() {
        String javaCommand = "java" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : "");

        File javaExe;

        // For IBM's JDK 1.2
        if (SystemUtils.IS_OS_AIX) {
            javaExe = new File(SystemUtils.JAVA_HOME + "/../sh", javaCommand);
        }
        else if (SystemUtils.IS_OS_MAC_OSX ) {
            javaExe = new File(SystemUtils.JAVA_HOME + "/bin", javaCommand);
        }
        else {
            javaExe = new File(SystemUtils.JAVA_HOME + "/../bin", javaCommand);
        }

        log.debug("Java executable=[" + javaExe.getAbsolutePath() + "]");

        return javaExe.getAbsolutePath();
    }

    protected void doExecute() throws Exception {
        Commandline cmd = new Commandline();

        cmd.setWorkingDirectory(workingDirectory.getAbsolutePath());
        cmd.setExecutable(getJavaPath());

        if (debugPort != null) {
            cmd.createArgument().setValue("-Xdebug");
            cmd.createArgument().setValue("-Xnoagent");
            cmd.createArgument().setValue("-Djava.compiler=NONE");
            cmd.createArgument().setValue("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + debugPort);
        }

        for (StringTokenizer st = new StringTokenizer(this.vmArgs); st.hasMoreTokens();) {
            cmd.createArgument().setValue(st.nextToken());
        }

        cmd.createArgument().setValue("-ea");
        cmd.createArgument().setValue("-jar");

        File serverJar = new File(new File(geronimoTarget, "bin"), "server.jar");
        if (serverJar.exists()) {
            cmd.createArgument().setFile(serverJar);
        }
        else {
            throw new FileNotFoundException(serverJar.getAbsolutePath());
        }

        cmd.createArgument().setValue("--quiet");

        if (this.configs != null && this.configs.length > 0) {
            cmd.createArgument().setValue("--override");

            for (int i=0; i < this.configs.length; i++) {
                cmd.createArgument().setValue(this.configs[i]);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(Commandline.toString(cmd.getCommandline()));
        }

        CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
        try {
            int exitCode = CommandLineUtils.executeCommandLine(cmd, new DefaultConsumer(), err);

            if (exitCode != 0) {
                throw new MojoExecutionException("Exit code: " + exitCode + " - " + err.getOutput());
            }
        }
        catch (CommandLineException e) {
            throw new MojoExecutionException("Unable to execute java command", e);
        }

        ServerBehavior sb = new ServerBehavior(getUri(), getMaxTries(), getRetryIntervalMilliseconds());
        if (!sb.isFullyStarted()) {
            CommandLineUtils.killProcess(cmd.getPid());

            throw new MojoExecutionException("Server did not start");
        }
    }
}
