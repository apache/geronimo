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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.deployment.client.DeploymentNotification;
import org.apache.geronimo.kernel.deployment.goal.DeploymentGoal;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;

/**
 * Handles the nuts & bolts of deployment -- acting on a set of goals, and
 * providing status notifications along the way.
 *
 * @version $Revision: 1.6 $ $Date: 2003/11/17 10:57:40 $
 */
public class DeploymentController implements GeronimoMBeanTarget {

    private static final Log log = LogFactory.getLog(DeploymentController.class);

    private GeronimoMBeanContext context;
    private Collection planners = Collections.EMPTY_LIST;
    private final Set goals = new HashSet();
    private final LinkedHashSet plans = new LinkedHashSet();
    private final DeploymentWaiter waiter = new DeploymentWaiter();
    private final DeploymentIDGenerator ids = new DeploymentIDGenerator();
    private long notificationSequence = 0;
    private Object notificationLock = new Object();


    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setAutostart(true);
        mbeanInfo.setTargetClass(DeploymentController.class.getName());
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("runDeploymentJob",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("goals", "[L"+DeploymentGoal.class.getName()+";", "Goals to achieve as part of this job")
                },
                0,
                "Execute a number of deployment goals together"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("prepareDeploymentJob",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("goals", "[L"+DeploymentGoal.class.getName()+";", "Goals to achieve as part of this job")
                },
                0,
                "Execute a number of deployment goals together, but don't start until instructed to"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("startDeploymentJob",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("jobID", Integer.TYPE, "ID of the deployment job to start")
                },
                0,
                "Indicates that the specified (prepared) deployment job can be run"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("updateDeploymentStatus",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("jobID", Integer.TYPE.getName(), "The job whose status you're updating"),
                    new GeronimoParameterInfo("message", String.class.getName(), "A text description of or note on the deployment status"),
                    new GeronimoParameterInfo("type", String.class.getName(), "The type of deployment message (see DeploymentNotification.java)"),
                    new GeronimoParameterInfo("tmID", TargetModuleID.class.getName(), "The module whose status is being updated")
                },
                0,
                "Updates the status of a deployment job"));
        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("Planners", DeploymentPlanner.class.getName(),
                ObjectName.getInstance("geronimo.deployment:role=DeploymentPlanner,*")));
        return mbeanInfo;
    }

    /**
     * Registers a new deployment job.  This method will return immediately,
     * even if the job will take a while to run.  An ID will be assigned to
     * the deployment, and notifications will be sent, but it is assumed the
     * caller is not interested in tracking the notifications for the job.
     */
    public void runDeploymentJob(DeploymentGoal[] goals) {
        if(goals.length == 0) {
            return;
        }
        int id = ids.nextID();
        for(int i=0; i<goals.length; i++) {
            goals[i].getTargetModule().setDeploymentID(id);
        }
        waiter.addJob(goals);
    }

    /**
     * Registers a new deployment job.  This method will return immediately,
     * and the job will be registered but not executed.  An ID will be
     * assigned to the deployment, and the caller can use this ID to start
     * the job and monitor it via the notifications emitted for the job.
     * You must call startDeploymentJob with this ID or the job will never be
     * run.  The purpose of this is that the caller can add a notification
     * listener before actually starting the job and ensure that all
     * notifications for the job will be received.
     */
    public int prepareDeploymentJob(DeploymentGoal[] goals) {
        if(goals.length == 0) { //todo: throws exception or send immediate success notification?
            return -1;
        }
        int id = ids.nextID();
        for(int i=0; i<goals.length; i++) {
            goals[i].getTargetModule().setDeploymentID(id);
        }
        waiter.queueJob(id, goals);
        return id;
    }

    /**
     * Indicates that the specified job can begin execution.  The status of
     * the job can be monitored via notifications for its deployment ID.
     */
    public void startDeploymentJob(int deploymentID) {
        waiter.startJob(deploymentID);
    }

    private synchronized void executeJob(DeploymentGoal[] job) {
        if(job.length == 0) {
            return;
        }
        int id = job[0].getTargetModule().getDeploymentID();
        try {
            goals.addAll(Arrays.asList(job));
            for(int i=0; i<job.length; i++) {
                updateDeploymentStatus(id, "Starting deployment job.", DeploymentNotification.DEPLOYMENT_UPDATE, job[i].getTargetModule());
            }
            generatePlans();
            executePlans();
            for(int i=0; i<job.length; i++) {
                updateDeploymentStatus(id, "Successfully completed deployment job.", DeploymentNotification.DEPLOYMENT_COMPLETED, job[i].getTargetModule());
            }
        } catch (DeploymentException e) {
            for(int i=0; i<job.length; i++) { //todo: send success for the goals that succeeded, only failure for ones that failed
                updateDeploymentStatus(id, "Deployment job failed.", DeploymentNotification.DEPLOYMENT_FAILED, job[i].getTargetModule());
            }
            log.warn("Unable to complete deployment job " + id, e);
        }
    }

    /**
     *
     */
    public void updateDeploymentStatus(int jobID, String message, String type, TargetModuleID tmID) {
        long seq;
        synchronized(notificationLock) {
            seq = ++notificationSequence;
        }
        context.sendNotification(new DeploymentNotification(type, this, seq, message, jobID, tmID));
    }

    private void generatePlans() throws DeploymentException {
        try {
            while (true) {
                boolean madeProgress = false;
                for (Iterator i = planners.iterator(); i.hasNext();) {
                    DeploymentPlanner planner = (DeploymentPlanner) i.next();
//                    log.info("Talking to planner: " + planner);
                    boolean progress = planner.plan(goals, plans);
                    if (progress) {
                        madeProgress = true;
                    }
                }
                if (goals.isEmpty() || !madeProgress) {
                    return;
                }
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    private void executePlans() throws DeploymentException {
        while (!plans.isEmpty()) {
            List planList = new ArrayList(plans);

            // get all of the runnable plans
            List runnablePlans = new ArrayList();
            for (Iterator i = planList.iterator(); i.hasNext();) {
                DeploymentPlan plan = (DeploymentPlan) i.next();
                try {
                    if (plan.canRun()) {
                        runnablePlans.add(plan);
                    }
                } catch (DeploymentException e) {
                    // plan threw an exception, which means it is completely not runnable and should be discarded
                    log.debug("Plan canRun() threw an exception.  The plan has been discarded: plan=" + plan, e);
                    plans.remove(plan);
                }
            }

            if (plans.isEmpty()) {
                log.debug("All plans were discarded");
                return;
            }

            if (runnablePlans.isEmpty()) {
                log.debug("No plans are runnable");
                return;
            }

            // execute all of the runnable plans
            for (Iterator i = runnablePlans.iterator(); i.hasNext();) {
                DeploymentPlan plan = (DeploymentPlan) i.next();
                plan.execute();
                plans.remove(plan);
            }
        }
    }

    public void setPlanners(Collection planners) {
        System.out.println("Setting the planners collection to " + planners);
        this.planners = planners;
    }

    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    public boolean canStart() {
        return true;
    }

    public void doStart() {
        Thread t = new Thread(waiter, "Geronimo Deployment Queue");
        waiter.setRunner(t);
        t.start();
    }

    public boolean canStop() {
        return true;
    }

    public void doStop() {
    }

    public void doFail() {
    }

    private class DeploymentWaiter implements Runnable {
        private LinkedList work = new LinkedList();
        private boolean finished = false;
        private Thread runner;
        private Map waiting = new HashMap();

        public void run() {
            DeploymentGoal[] job;
            while(!finished) {
                job = null;
                synchronized(work) {
                    if(work.isEmpty()) {
                        try {
                            work.wait();
                        } catch(InterruptedException e) {}
                    }
                    job = (DeploymentGoal[])work.removeFirst();
                }
                if(job != null) {
                    executeJob(job); // needs to acquire lock on DeploymentController, may take a while
                }
            }
        }

        public void queueJob(int jobID, DeploymentGoal[] job) {
            waiting.put(new Integer(jobID), job);
        }

        public void startJob(int jobID) {
            DeploymentGoal[] job = (DeploymentGoal[])waiting.remove(new Integer(jobID));
            if(job != null) {
                addJob(job);
            }
        }

        public void addJob(DeploymentGoal[] job) {
            if(job == null) {
                log.error("Job should not be null", new RuntimeException());
                return;
            }
            synchronized(work) {
                work.addLast(job);
                work.notify();
            }
        }

        public void setRunner(Thread t) {
            runner = t;
        }

        /**
         * Prevents any future work from being done.  Does not stop a
         * deployment job that is currently in progress.
         */
        public void stop() {
            finished = true;
            if(runner != null) {
                runner.interrupt();
                runner = null;
            }
        }
    }

    private static class DeploymentIDGenerator {
        int counter = 0;

        public void start() {
            //todo: load the saved job ID?
        }

        public synchronized int nextID() {
            return ++counter;
        }

        public void stop() {
            //todo: persist the current job ID?
        }
    }
}
