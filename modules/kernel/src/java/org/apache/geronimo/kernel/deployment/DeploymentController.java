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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/11/16 02:22:58 $
 */
public class DeploymentController implements GeronimoMBeanTarget {

    private static final Log log = LogFactory.getLog(DeploymentController.class);

    private GeronimoMBeanContext context;
    private Collection planners = Collections.EMPTY_LIST;
    private final Map scanResults = new HashMap();
    private final Set goals = new HashSet();
    private final LinkedHashSet plans = new LinkedHashSet();


    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setAutostart(true);
        mbeanInfo.setTargetClass(DeploymentController.class.getName());
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("planDeployment",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("Source", ObjectName.class.getName(), "Good question!"),
                    new GeronimoParameterInfo("URLInfos", Set.class.getName(), "Set of URLs to plan deployments for")
                },
                0,
                "plan the set of deployments"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("isDeployed",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("URL", URL.class.getName(), "URL to test")
                },
                0,
                "Determine if the supplied URL is deployed"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("deploy",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("URL", URL.class.getName(), "URL to deploy")
                },
                0,
                "Deploy the URL"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("undeploy",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("URL", URL.class.getName(), "URL to undeploy")
                },
                0,
                "Undeploy the URL"));
        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("Planners", DeploymentPlanner.class.getName(),
                ObjectName.getInstance("geronimo.deployment:role=DeploymentPlanner,*")));
        return mbeanInfo;
    }

    /**
     * @jmx:managed-operation
     */
    public synchronized void planDeployment(ObjectName source, Set urlInfos) {


        Set lastScan = (Set) scanResults.get(source);

        // find new and existing urlInfos
        for (Iterator i = urlInfos.iterator(); i.hasNext();) {
            URLInfo urlInfo = (URLInfo) i.next();
            URL url = urlInfo.getUrl();


            if (!isDeployed(url)) {
                //only add a new deployment goal if we don't already have one. One can already exist if
                //there was no deployer available when the url was scanned
                if ((lastScan == null) || ((lastScan != null) &&!lastScan.contains (urlInfo))){
                    log.info("Adding url goal for " + url);
                    goals.add(new DeployURL(url, urlInfo.getType()));
                }
            } else {
                log.info("Redeploying url " + url);
                goals.add(new RedeployURL(url));
            }
        }

        // create remove goals for all urlInfos that were found last time but not now
        if (lastScan != null) {
            for (Iterator i = lastScan.iterator(); i.hasNext();) {
                URLInfo urlInfo = (URLInfo) i.next();
                URL url = urlInfo.getUrl();

                if (!urlInfos.contains(urlInfo) && isDeployed(url)) {
                    goals.add(new UndeployURL(url));
                }
            }
        }
        scanResults.put(source, urlInfos);

        try {
            generatePlans();
        } catch (DeploymentException e) {
            log.warn("Unable to plan deployment", e);
            return;
        }

        try {
            executePlans();
        } catch (DeploymentException e) {
            log.warn("Unable to execute deployment plan", e);
            return;
        }
    }

    /**
     * @jmx:managed-operation
     */
    public boolean isDeployed(URL url) {
        try {
            ObjectName pattern = new ObjectName("*:role=DeploymentUnit,url=" + ObjectName.quote(url.toString()) + ",*");
            return !context.getServer().queryNames(pattern, null).isEmpty();
        } catch (MalformedObjectNameException e) {
            throw new AssertionError();
        }
    }

    /**
     * @jmx:managed-operation
     */
    public synchronized void deploy(URL url) throws DeploymentException {
        if (isDeployed(url)) {
            return;
        }

        URLType type = null;
        try {
            type = URLType.getType(url);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }

        try {
            DeployURL goal = new DeployURL(url, type);
            goals.add(goal);
            generatePlans();
            executePlans();
        } catch (DeploymentException e) {
            log.warn("Unable to deploy URL " + url, e);
            throw e;
        }
    }

    /**
     * @jmx:managed-operation
     */
    public synchronized void undeploy(URL url) {
        if (!isDeployed(url)) {
            return;
        }
        try {
            UndeployURL goal = new UndeployURL(url);
            goals.add(goal);
            generatePlans();
            executePlans();
        } catch (DeploymentException e) {
            log.warn("Unable to undeploy URL " + url, e);
        }
    }

    private void generatePlans() throws DeploymentException {
        try {
            while (true) {
                boolean madeProgress = false;
                for (Iterator i = planners.iterator(); i.hasNext();) {
                    DeploymentPlanner planner = (DeploymentPlanner) i.next();
                    log.info("Talking to planner: " + planner);
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
    }

    public boolean canStop() {
        return true;
    }

    public void doStop() {
    }

    public void doFail() {
    }
}
