/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.console.cli.controller;

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.TargetModuleID;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;

/**
 * Watch the progress of a long-running operation.
 *
 * @version $Rev$ $Date$
 */
public class ProgressMonitor extends TextController implements ProgressListener {
    private ProgressObject progress;
    private boolean started = false;

    public ProgressMonitor(DeploymentContext context, ProgressObject progress) {
        super(context);
        this.progress = progress;
        progress.addProgressListener(this);
    }

    public void execute() {
        initialize(); //todo: allow cancel/stop
    }

    private synchronized void initialize() {
        if(!progress.getDeploymentStatus().isRunning()) {
            message("--"+progress.getDeploymentStatus().getMessage());
            printCompletion();
            return;
        } else {
            try {
                wait();
            } catch(InterruptedException e) {
            }
            printCompletion();
        }
    }

    public synchronized void handleProgressEvent(ProgressEvent event) {
        message("--"+event.getDeploymentStatus().getMessage());
        if(!event.getDeploymentStatus().isRunning()) {
            notifyAll();
        }
    }

    private void printBanner() {
        newScreen("Progress Monitor");
        println("Monitoring the progress of a "+progress.getDeploymentStatus().getCommand()+" operation.");
        println("This operation can"+(progress.isCancelSupported() ? "" : "not")+" be canceled");
        println("This operation can"+(progress.isStopSupported() ? "" : "not")+" be stopped");
    }

    private void message(String message) {
        if(!started) {
            printBanner();
            started = true;
        }
        println(message);
    }

    private void printCompletion() {
        println("Operation has "+(progress.getDeploymentStatus().isCompleted() ? "completed" : progress.getDeploymentStatus().isFailed() ? "failed" : "finished"));
        println("Affected target/modules:");
        TargetModuleID[] ids = progress.getResultTargetModuleIDs();
        for(int i = 0; i < ids.length; i++) {
            println("  "+ids[i]);
        }
    }
}
