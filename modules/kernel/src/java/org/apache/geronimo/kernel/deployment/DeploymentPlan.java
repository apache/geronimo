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
package org.apache.geronimo.kernel.deployment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.task.DeploymentTask;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:38:33 $
 */
public class DeploymentPlan {
    private final Log log = LogFactory.getLog(this.getClass());
    private final List tasks = new LinkedList();

    public DeploymentPlan() {
    }

    public DeploymentPlan(DeploymentTask task) {
        tasks.add(task);
    }

    public void addTask(DeploymentTask task) {
        tasks.add(task);
    }

    public boolean canRun() throws DeploymentException {
        boolean canRun = true;
        for (Iterator i = tasks.iterator(); i.hasNext();) {
            DeploymentTask task = (DeploymentTask) i.next();
            log.trace("Checking if task can run " + task);

            // always check each task, so the task can throw an exception if the task can never run
            canRun = canRun && task.canRun();
        }
        return canRun;
    }

    public void execute() throws DeploymentException {
        LinkedList undoList = new LinkedList();
        try {
            for (Iterator i = tasks.iterator(); i.hasNext();) {
                DeploymentTask task = (DeploymentTask) i.next();
                log.trace("Performing task " + task);
                task.perform();
                undoList.add(task);
            }
        } catch (DeploymentException e) {
            while (!undoList.isEmpty()) {
                DeploymentTask task = (DeploymentTask) undoList.removeLast();
                log.trace("Performing undo task " + task);
                task.undo();
            }
            throw e;
        }
    }
}
