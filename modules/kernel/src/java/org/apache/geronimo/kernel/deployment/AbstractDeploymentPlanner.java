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

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import javax.management.MBeanServer;

import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.deployment.goal.DeploymentGoal;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * @version $REVISION$ Nov 13, 2003$
 *
 * */
public abstract class AbstractDeploymentPlanner implements DeploymentPlanner, GeronimoMBeanTarget {

    private static final Log log = LogFactory.getLog(AbstractDeploymentPlanner.class);

    private GeronimoMBeanContext context;

    /**
     * Supply our own GeronimoMBeanInfo for xml-free deployment.
     * @return
     */
    public static GeronimoMBeanInfo getGeronimoMBeanInfo(String subclassName) {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(subclassName);
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("plan",
                new GeronimoParameterInfo[] {
                    new GeronimoParameterInfo("Goals",
                            java.util.Set.class.getName(),
                            "Goals needing deployment"),
                    new GeronimoParameterInfo("Plans",
                            java.util.Set.class.getName(),
                            "Deployment plans to add to")
                },
                0,
                "Plan to deploy as many goals as possible"));

        return mbeanInfo;
    }


    public boolean plan(Set goals, Set plans) throws DeploymentException {
        log.info(context.getObjectName().toString() + " Got to plan method with goals " + goals);
        boolean progress = false;
        Set x = new HashSet(goals);
        for (Iterator i = x.iterator(); i.hasNext();) {
            DeploymentGoal goal = (DeploymentGoal) i.next();
            log.info("Considering goal " + goal + " by planner " + context.getObjectName());
            if (goal instanceof DeployURL) {
                progress = addURL((DeployURL) goal, goals, plans);
            } else if (goal instanceof RedeployURL) {
                progress = redeployURL((RedeployURL) goal, goals);
            } else if (goal instanceof UndeployURL) {
                progress = removeURL((UndeployURL) goal, goals, plans);
            }
        }
        return progress;
    }

    protected abstract boolean addURL(DeployURL deployURL, Set goals, Set plans) throws DeploymentException;

    protected abstract boolean redeployURL(RedeployURL redeployURL, Set goals) throws DeploymentException;

    protected abstract boolean removeURL(UndeployURL undeployURL, Set goals, Set plans) throws DeploymentException;


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

    protected MBeanServer getServer() {
        return context.getServer();
    }

}
