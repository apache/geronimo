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
package org.apache.geronimo.deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.goal.DeployURL;
import org.apache.geronimo.deployment.goal.RedeployURL;
import org.apache.geronimo.deployment.goal.UndeployURL;
import org.apache.geronimo.deployment.plan.DeploymentPlan;
import org.apache.geronimo.deployment.scanner.URLInfo;
import org.apache.geronimo.deployment.scanner.URLType;
import org.apache.geronimo.jmx.JMXUtil;

/**
 *
 *
 *
 * @version $Revision: 1.5 $ $Date: 2003/08/14 00:02:34 $
 */
public class DeploymentController implements MBeanRegistration, DeploymentControllerMBean {
    private static final ObjectName DEFAULT_NAME = JMXUtil.getObjectName("geronimo.deployment:role=DeploymentController");

    private static final Log log = LogFactory.getLog(DeploymentController.class);

    private MBeanServer server;
    private RelationServiceMBean relationService;
    private ObjectName objectName;
    private final Map scanResults = new HashMap();
    private final Map deployedURLs = new HashMap();
    private final Set goals = new HashSet();
    private final LinkedHashSet plans = new LinkedHashSet();

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        server = mBeanServer;
        this.objectName = objectName == null ? DEFAULT_NAME : objectName;

        relationService = JMXUtil.getRelationService(server);
        RoleInfo[] roleInfo = {
            new RoleInfo("DeploymentController", getClass().getName()),
            new RoleInfo("DeploymentPlanner", "org.apache.geronimo.deployment.DeploymentPlannerMBean", true, true, 0, RoleInfo.ROLE_CARDINALITY_INFINITY, null)
        };
        relationService.createRelationType("DeploymentController-DeploymentPlanner", roleInfo);

        roleInfo = new RoleInfo[]{
            new RoleInfo("DeploymentController", getClass().getName()),
            new RoleInfo("DeploymentScanner", "org.apache.geronimo.deployment.scanner.DeploymentScannerMBean", true, true, 0, RoleInfo.ROLE_CARDINALITY_INFINITY, null)
        };
        relationService.createRelationType("DeploymentController-DeploymentScanner", roleInfo);

        return this.objectName;
    }

    public void postRegister(Boolean aBoolean) {
        Role controllerRole = new Role("DeploymentController", Collections.singletonList(this.objectName));
        RoleList roleList = new RoleList();
        roleList.add(controllerRole);

        try {
            relationService.createRelation("DeploymentController-DeploymentPlanner", "DeploymentController-DeploymentPlanner", roleList);
            relationService.createRelation("DeploymentController-DeploymentScanner", "DeploymentController-DeploymentScanner", roleList);
        } catch (Exception e) {
            IllegalStateException e1 = new IllegalStateException();
            e1.initCause(e);
            throw e1;
        }
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public synchronized void planDeployment(ObjectName source, Set urlInfos) {
        // find new and existing urlInfos
        for (Iterator i = urlInfos.iterator(); i.hasNext();) {
            URLInfo urlInfo = (URLInfo) i.next();
            URL url = urlInfo.getUrl();
            if (!isDeployed(url)) {
                goals.add(new DeployURL(url, urlInfo.getType()));
            } else {
                goals.add(new RedeployURL(url));
            }
        }

        // create remove goals for all urlInfos that were found last time but not now
        Set lastScan = (Set) scanResults.get(source);
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

    private boolean isDeployed(URL url) {
        try {
            ObjectName pattern = new ObjectName("*:role=DeploymentUnit,url=" + ObjectName.quote(url.toString()) + ",*");
            return !server.queryNames(pattern, null).isEmpty();
        } catch (MalformedObjectNameException e) {
            throw new AssertionError();
        }
    }

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
            List planners = relationService.getRole("DeploymentController-DeploymentPlanner", "DeploymentPlanner");
            Object[] args = {goals, plans};
            String[] types = {"java.util.Set", "java.util.Set"};
            while (true) {
                boolean madeProgress = false;
                for (Iterator i = planners.iterator(); i.hasNext();) {
                    ObjectName planner = (ObjectName) i.next();
                    Boolean progress = (Boolean) server.invoke(planner, "plan", args, types);
                    if (progress.booleanValue()) {
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
}
