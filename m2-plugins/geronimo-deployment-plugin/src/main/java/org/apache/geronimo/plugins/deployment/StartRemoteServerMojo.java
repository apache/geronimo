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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.geronimo.plugins.util.ServerBehavior;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal startRemoteServer
 * 
 * @version $Rev:$ $Date:$
 */
public class StartRemoteServerMojo extends AbstractModuleMojo {

    /**
     * @parameter
     */
    private String geronimoTarget;

    /**
     * @parameter default-value=""
     */
    private String vmArgs = "";

    /**
     * @parameter
     */
    private String[] configs;

    /**
     * @parameter
     */
    private String debugPort;
    
    

    private PrintStream logStream = System.out;    
    private PrintStream resultStream;
    
    private final String goalName = "Start Remote Server";

    public void execute() throws MojoExecutionException {        
        resultStream = getResultsStream();        
        logStream = getLogStream(goalName);
        
        try {
            startRemoteServer();
        }
        catch (Exception e) {
            logResults(resultStream, goalName, "fail");
            handleError(e, logStream);
            return;
        }
        logResults(resultStream, goalName, "success");        
    }    
    
    

    /**
     * @throws MojoExecutionException
     */
    private void startRemoteServer() throws Exception {
        ArrayList cmd = new ArrayList();
        File root = new File(this.geronimoTarget);
        File systemFile = new File(root, "bin/server.jar");
        String s = java.io.File.separator;
        String java = System.getProperty("java.home") + s + "bin" + s + "java";

        cmd.add(java);

        if (debugPort != null) {
            cmd.add("-Xdebug");
            cmd.add("-Xnoagent");
            cmd.add("-Djava.compiler=NONE");
            cmd.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + debugPort);
        }

        for (StringTokenizer st = new StringTokenizer(this.vmArgs); st.hasMoreTokens();) {
            cmd.add(st.nextToken());
        }

        cmd.add("-ea");
        cmd.add("-jar");

        if (systemFile.exists()) {
            try {
                cmd.add(systemFile.getCanonicalPath());
            }
            catch (IOException e) {
            }
        }
        else {
            throw new Exception(systemFile.getAbsolutePath() + " does not exist");                                 
        }

        cmd.add("--quiet");

        if (this.configs != null && this.configs.length > 0) {
            cmd.add("--override");
            for (int i=0; i < this.configs.length; i++) {
                cmd.add(this.configs[i]);
            }
        }
        String[] command = (String[]) cmd.toArray(new String[0]);

        Runtime runtime = Runtime.getRuntime();
        Process server;
        try {
            server = runtime.exec(command);

            // Pipe the processes STDOUT to ours
            InputStream outStream = server.getInputStream();
            Thread serverOut = new Thread(new Pipe(outStream, logStream));
            serverOut.setDaemon(true);
            serverOut.start();

            // Pipe the processes STDERR to ours
            InputStream errStream = server.getErrorStream();
            Thread serverErr = new Thread(new Pipe(errStream, logStream));
            serverErr.setDaemon(true);
            serverErr.start();

            ServerBehavior sb = new ServerBehavior(getUri(), getMaxTries(), getRetryIntervalMilliseconds());
            sb.setLogStream(logStream);
            if (!sb.isFullyStarted()) {
                server.destroy();                                
                throw new Exception("Server did not start");                
            }
        }
        catch (Exception e1) {
            throw new Exception(e1);            
        }        
    }



    public void destroy() {
        logStream.close();
    }

    private final class Pipe implements Runnable {

        private final InputStream in;

        private final OutputStream out;

        public Pipe(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        public void run() {
            int i;
            try {
                do {
                    i = in.read();
                    out.write(i);
                }
                while (i != -1);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
