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

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.deployment.task.StartMBeanInstance;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2003/12/28 23:06:42 $
 */
public class DeploymentInfo {
    private final URL url;
    private final ObjectName name;
    private final ObjectName parent;
    private final Set children = new HashSet();

    public DeploymentInfo(ObjectName name, ObjectName parent, URL url) {
        this.name = name;
        this.parent = parent;
        this.url = url;
    }


    public URL getURL() {
        return url;
    }

    public ObjectName getName() {
        return name;
    }

    public ObjectName getParent() {
        return parent;
    }

    public Collection getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    public synchronized void addChild(ObjectName childName) {
        children.add(childName);
    }

    public synchronized void removeChild(ObjectName childName) {
        children.remove(childName);
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(DeploymentInfo.class);
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("URL", true, false, "URL of deployed package"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Name", true, false, "Name of deployed package"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Parent", true, false, "Parent DeploymentInfo ObjectName of deployed package"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Children", true, false, "Child DeploymentInfo ObjectName of deployed package"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("addChild",
                new GeronimoParameterInfo[]{new GeronimoParameterInfo("ChildName", ObjectName.class, "ObjectName of child deployment info")},
                GeronimoOperationInfo.ACTION,
                "Add a child deployment info"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("removeChild",
                new GeronimoParameterInfo[]{new GeronimoParameterInfo("ChildName", ObjectName.class, "ObjectName of child deployment info")},
                GeronimoOperationInfo.ACTION,
                "remove a child deployment info"));
        return mbeanInfo;
    }

    public static DeploymentPlan planDeploymentInfo(MBeanServer server, ObjectName loaderName, ObjectName deploymentInfoName, ObjectName parentName, URL deploymentURL) {
        DeploymentPlan plan = new DeploymentPlan();
        DeploymentInfo deploymentInfo = new DeploymentInfo(deploymentInfoName, parentName, deploymentURL);
        GeronimoMBeanInfo mbeanInfo = DeploymentInfo.getGeronimoMBeanInfo();
        mbeanInfo.setTarget(deploymentInfo);
        MBeanMetadata metadata = new MBeanMetadata(deploymentInfoName, mbeanInfo, loaderName, parentName);
        plan.addTask(new DeployGeronimoMBean(server, metadata));
        plan.addTask(new StartMBeanInstance(server, metadata));
        return plan;
    }
}
