/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
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
 * @version $Revision: 1.2 $ $Date: 2003/11/22 20:08:54 $
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
