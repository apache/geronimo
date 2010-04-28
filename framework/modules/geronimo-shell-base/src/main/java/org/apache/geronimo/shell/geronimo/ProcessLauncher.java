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

package org.apache.geronimo.shell.geronimo;

import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Helper to execute a process and perform some verification logic to determine if the process is up or not.
 * @version $Rev$ $Date$
 */
public class ProcessLauncher {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    String name;

    protected void process() throws Exception {
    }

    protected boolean verifier() {
        return false;
    };

    int verifyWaitDelay = 1000;

    int timeout = -1;

    boolean background = false;

    Throwable error;

    boolean timedOut = false;
    
    PrintStream out;

    public ProcessLauncher(Logger log, String name, boolean background, PrintStream out) {
        this.background = background;
        this.log = log;
        this.name = name;
        this.out = out;
    }

    private class Inner implements Runnable {
        public void run() {
            try {
                process();
            } catch (Exception e) {
                error = e;
            }
        }
    }

    private class TimingTimerTask extends TimerTask {
        @Override
        public void run() {
            timedOut = true;
        }
    }

    public void launch() throws Exception {
        assert name != null;

        Runnable runner = new Inner();

        Thread t = new Thread(runner, name + " Runner");

        out.println("Launching " + name + "...");
        //System.console().flush();

        StopWatch watch = new StopWatch();
        watch.start();

        t.start();

        if (verifier()) {
            Timer timer = new Timer(name + " Timer", true);

            TimerTask timeoutTask = new TimingTimerTask();
            if (timeout > 0) {
                timer.schedule(timeoutTask, timeout * 1000);

            }

            boolean started = false;

            log.debug("Waiting for "+name+" ...");

            while (!started) {
                if (timedOut) {
                    throw new Exception("Unable to verify if "+name+" was started in the given time (" + timeout
                            + " seconds)");
                }

                if (error != null) {
                    throw new Exception("Failed to start: "+name, error);
                }

                if (verifier()) {
                    started = true;
                } else {
                    Thread.sleep(verifyWaitDelay);
                }
            }

            timeoutTask.cancel();
        }

        out.println(name + " started in " + watch);
        //System.console().flush();

        if (!background) {
            log.debug("Waiting for " + name + " to shutdown...");

            t.join();

            log.debug(name + " has shutdown");
        }
    }
}
