/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.main;

import java.io.PrintStream;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.serverinfo.ServerConstants;

/**
 * A startup monitor that shows the progress of loading and starting
 * modules using a text based progress bar and the use of line
 * feeds to update the progress display, therefore minimizing the
 * number of lines output to the terminal.
 * <p/>
 * This implementation displays a fixed-size bar.
 * <p/>
 * A summary will also be produced containing a list of ports
 * Geronimo is listening on, the configIds of application modules
 * that were started and the context roots of Web applications that were started.
 *
 * @version $Revision: 1.0$
 */
public class SimpleProgressBarStartupMonitor implements StartupMonitor {
    private final static char STATUS_NOT_READY = ' ';
    private final static char STATUS_LOADING = '-';
    private final static char STATUS_LOADED = '>';
    private final static char STATUS_STARTED = '*';
    private final static char STATUS_FAILED = 'x';
    private final static int MAX_WIDTH = 110;
    private PrintStream out;
    private String currentOperation;
    private Artifact[] modules;
    private char[] moduleStatus = new char[0];
    private long started;
    private int percent = 0;
    private Kernel kernel;
    private int operationLimit = 50;
    private boolean finished = false;
    private UpdateThread thread;

    private int barSize = 40;
    private int barProgress;

    public synchronized void systemStarting(long startTime) {
        out = System.out;
        started = startTime;
    }

    public synchronized void systemStarted(Kernel kernel) {
        out.println("Starting Geronimo Application Server v" + ServerConstants.getVersion());
        this.kernel = kernel;
        currentOperation = "Loading";
    }

    public synchronized void foundModules(Artifact[] modules) {
        this.modules = modules;
        moduleStatus = new char[modules.length];
        for (int i = 0; i < moduleStatus.length; i++) {
            moduleStatus[i] = STATUS_NOT_READY;
        }
        operationLimit = MAX_WIDTH
                - 3 // two brackets, space afterward
                - barSize // module tokens
                - 5 // 3 digits of percent plus % plus space afterward
                - 5;// 3 digits of time plus s plus space afterward
        repaint();
        thread = new UpdateThread();
        thread.start();
    }

    public synchronized void calculatePercent() {
        if (finished) {
            this.percent = 100;
            this.barProgress = this.barSize;
            return;
        }
        int percent = 0;
        if (kernel != null) percent += 5;
        int total = moduleStatus.length * 2;
        int progress = 0;
        for (int i = 0; i < moduleStatus.length; i++) {
            char c = moduleStatus[i];
            switch (c) {
                case STATUS_LOADED:
                    progress += 1;
                    break;
                case STATUS_STARTED:
                case STATUS_FAILED:
                    progress += 2;
                    break;
            }
        }
        percent += Math.round(95f * progress / total);
        this.percent = percent;
        this.barProgress =  (this.barSize * this.percent) / 100;
    }

    public synchronized void moduleLoading(Artifact module) {
        currentOperation = " Loading " + module.getArtifactId();
        for (int i = 0; i < modules.length; i++) {
            if (modules[i].equals(module)) {
                moduleStatus[i] = STATUS_LOADING;
                break;
            }
        }
        repaint();
    }

    public synchronized void moduleLoaded(Artifact module) {
        for (int i = 0; i < modules.length; i++) {
            if (modules[i].equals(module)) {
                moduleStatus[i] = STATUS_LOADED;
                break;
            }
        }
        calculatePercent();
        repaint();
    }

    public synchronized void moduleStarting(Artifact module) {
        //currentOperation = " Starting " + module.getArtifactId();
    }

    public synchronized void moduleStarted(Artifact module) {
        for (int i = 0; i < modules.length; i++) {
            if (modules[i].equals(module)) {
                moduleStatus[i] = STATUS_STARTED;
                break;
            }
        }
        calculatePercent();
        repaint();
    }

    public synchronized void startupFinished() {
        finished = true;
        currentOperation = "Startup complete";
        calculatePercent();
        thread.done = true;
        thread.interrupt();
    }

    public synchronized void serverStartFailed(Exception problem) {
        currentOperation = "Startup failed";
        repaint();
        out.println();
        problem.printStackTrace(out);
    }

    private synchronized void repaint() {
        StringBuilder buf = new StringBuilder();
        buf.append("\r[");
        for (int i = 0; i < barSize; i++) {
            if (i < barProgress) {
                buf.append("*");
            } else {
                buf.append(" ");
            }
        }
        buf.append("] ");
        if (percent < 10) {
            buf.append("  ");
        } else if (percent >= 10 && percent < 100) {
            buf.append(" ");
        }
        buf.append(percent).append("% ");
        int time = Math.round((System.currentTimeMillis() - started) / 1000f);
        if (time < 10) {
            buf.append(' ');
        }
        if (time < 100) {
            buf.append(' ');
        }
        buf.append(time).append("s ");
        if (currentOperation.length() > operationLimit) {
            if (operationLimit > 3) {
                buf.append(currentOperation.substring(0, operationLimit - 3)).append("...");
            } else {
                for (int i = 0; i < operationLimit; i++) {
                    buf.append('.');
                }
            }
        } else {
            buf.append(currentOperation);
            for (int i = currentOperation.length(); i < operationLimit; i++) {
                buf.append(' ');
            }
        }
        out.print(buf.toString());
        out.flush();
    }

    private class UpdateThread extends Thread {
        private volatile boolean done = false;

        public UpdateThread() {
            super("Progress Display Update Thread");
            setDaemon(true);
        }

        public void run() {
            while (!done) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    continue;
                }
                repaint();
            }

            repaint();
            out.println();
            StartupMonitorUtil.wrapUp(SimpleProgressBarStartupMonitor.this.out, SimpleProgressBarStartupMonitor.this.kernel);
        }
    }
}
