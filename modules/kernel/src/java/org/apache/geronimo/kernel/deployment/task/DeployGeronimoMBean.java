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
package org.apache.geronimo.kernel.deployment.task;

import java.net.URL;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.classspace.ClassSpaceException;
import org.apache.geronimo.kernel.classspace.ClassSpaceUtil;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.service.GeronimoMBean;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfoXMLLoader;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/11 04:40:11 $
 */
public class DeployGeronimoMBean implements DeploymentTask {
    private static final Log log = LogFactory.getLog(DeployGeronimoMBean.class);
    private final MBeanServer server;
    private final MBeanMetadata metadata;
    private boolean registered;

    public DeployGeronimoMBean(MBeanServer server, MBeanMetadata mbeanMetadata) {
        this.server = server;
        this.metadata = mbeanMetadata;
    }

    public boolean canRun() throws DeploymentException {
        ObjectName loaderName = metadata.getLoaderName();
        if (loaderName != null && !server.isRegistered(loaderName)) {
            log.trace("Cannot run because class space is not registered: loaderName=" + loaderName);
            return false;
        }
        return true;
    }

    public void perform() throws DeploymentException {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        ClassLoader newCL;

        // create an MBean instance
        try {
            // Set the class loader
            try {
                newCL = ClassSpaceUtil.setContextClassLoader(server, metadata.getLoaderName());
            } catch (ClassSpaceException e) {
                throw new DeploymentException(e);
            }

            // Create and register the MBean
            try {
                if (log.isTraceEnabled()) {
                    log.trace("Creating GeronimoMBean name=" + metadata.getName());
                }
                GeronimoMBean mbean = (GeronimoMBean) server.instantiate("org.apache.geronimo.kernel.service.GeronimoMBean");
                mbean.setClassSpace(metadata.getLoaderName());
                String descriptorName = metadata.getGeronimoMBeanDescriptor();
                URL url = newCL.getResource(descriptorName);
                if(url == null) {
                    throw new DeploymentException("GeronimoMBean descriptor not found: " + descriptorName);
                }
                GeronimoMBeanInfo geronimoMBeanInfo = GeronimoMBeanInfoXMLLoader.loadMBean(url);
                mbean.setMBeanInfo(geronimoMBeanInfo);
                server.registerMBean(mbean, metadata.getName());
                registered = true;

                // Add the mbean to it's parent
                ObjectName parentName = metadata.getParentName();
                if (parentName != null) {
                    server.invoke(metadata.getParentName(), "addChild", new Object[]{metadata.getName()}, new String[]{"javax.management.ObjectName"});
                }
            } catch (Exception e) {
                throw new DeploymentException(e, metadata);
            }
        } catch (DeploymentException e) {
            undo();
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }


    public void undo() {
        if (!registered) {
            return;
        }
        ObjectName objectName = metadata.getName();
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        try {
            // Set the class loader
            try {
                ClassSpaceUtil.setContextClassLoader(server, metadata.getLoaderName());
            } catch (ClassSpaceException e) {
                log.warn("Could not set context class loader", e);
                return;
            }

            try {
                server.invoke(metadata.getParentName(), "removeChild", new Object[]{objectName}, new String[]{"javax.management.ObjectName"});
            } catch (InstanceNotFoundException e) {
                log.warn("Could not remove from parent", e);
            } catch (MBeanException e) {
                log.error("Error while removing MBean " + objectName + " from parent", e);
            } catch (ReflectionException e) {
                log.error("Error while removing MBean " + objectName + " from parent", e);
            }

            try {
                server.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                log.warn("MBean was already removed " + objectName, e);
                return;
            } catch (MBeanRegistrationException e) {
                log.error("Error while unregistering MBean " + objectName, e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    public String toString() {
        return "DeployGeronimoMBean " + metadata.getName();
    }
}
