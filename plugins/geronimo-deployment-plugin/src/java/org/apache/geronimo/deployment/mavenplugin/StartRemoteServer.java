/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.mavenplugin;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;


/**
 * @version $Rev$ $Date$
 */
public class StartRemoteServer {

    private String geronimoTarget;
    private String vmArgs = "";
    private String configs;
    private String debugPort;
    private String output;
    private String error;
    private PrintStream out = System.out;
    private PrintStream err = System.err;



    public String getGeronimoTarget() {
        return geronimoTarget;
    }

    public void setGeronimoTarget(String geronimoTarget) {
        this.geronimoTarget = geronimoTarget;
    }

    public String getVmArgs() {
        return vmArgs;
    }

    public void setVmArgs(String vmArgs) {
        this.vmArgs = vmArgs;
    }

    public String getConfigs() {
        return configs;
    }

    public void setConfigs(String configs) {
        this.configs = configs;
    }

    public String getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(String debugPort) {
        this.debugPort = debugPort;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    private PrintStream getOut() {
        try {
            if (getOutput() != null) {
                out = new PrintStream(new FileOutputStream(getOutput()));
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }

        return out;
    }

    private PrintStream getErr() {
        try {
            if (getError() != null) {
                err = new PrintStream(new FileOutputStream(getError()));
            } else if (getOutput() != null) {
                err = getOut();
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
        return err;
    }

    public void execute() throws Exception {
        ArrayList cmd = new ArrayList();
        File root = new File(getGeronimoTarget());
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

        for (StringTokenizer st = new StringTokenizer(getVmArgs()); st.hasMoreTokens();) {
            cmd.add(st.nextToken());
        }

        cmd.add("-ea");
        cmd.add("-jar");
        cmd.add(systemFile.getCanonicalPath());
        cmd.add("-quiet");

        if (getConfigs() != null  && getConfigs().trim().length() > 0) {
            cmd.add("-override");
            for (StringTokenizer st = new StringTokenizer(getConfigs()); st.hasMoreTokens();) {
                cmd.add(st.nextToken());
            }
        }
        String[] command = (String[]) cmd.toArray(new String[0]);

        Runtime runtime = Runtime.getRuntime();
        Process server = runtime.exec(command);


        // Pipe the processes STDOUT to ours
        InputStream out = server.getInputStream();
        Thread serverOut = new Thread(new Pipe(out, getOut()));
        serverOut.setDaemon(true);
        serverOut.start();

        // Pipe the processes STDERR to ours
        InputStream err = server.getErrorStream();
        Thread serverErr = new Thread(new Pipe(err, getErr()));
        serverErr.setDaemon(true);
        serverErr.start();

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
                } while (i != -1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
