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

import java.util.List;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.service.GeronimoMBean;

/**
 * Creates, registers, and starts a class space
 *
 * @version $Revision: 1.5 $ $Date: 2003/12/10 13:00:09 $
 */
public class CreateClassSpace implements DeploymentTask {
    private final Log log = LogFactory.getLog(this.getClass());
    private final MBeanServer server;
    private final ClassSpaceMetadata metadata;
    private boolean created = false;
    private boolean addedURLs = false;

    /**
     * Creates a CreateClassSpace task.
     * @param server the mbean server to register the class space
     * @param metadata the class space data
     */
    public CreateClassSpace(MBeanServer server, ClassSpaceMetadata metadata) {
        this.server = server;
        this.metadata = metadata;
    }

    /**
     * @return false if the parent class space exists and is not registered in MBeanServer; otherwise true
     */
    public boolean canRun() {
        final ObjectName parent = metadata.getParent();
        if (parent != null && !server.isRegistered(parent)) {
            log.trace("Cannot run because parent class space is not registered: parent=" + parent);
            return false;
        }
        return true;
    }

    /**
     * Creates, registers and starts a class space.
     * Will only create a class space if there is no class space with the specified name,
     * and the metadata is not set to NEVER create a new space.
     * @throws DeploymentException if there is an issue creating the class space
     */
    public void perform() throws DeploymentException {
        ObjectName name = metadata.getName();
        List urls = metadata.getUrls();
        if (!server.isRegistered(name)) {
            if (metadata.getCreate() == ClassSpaceMetadata.CREATE_NEVER) {
                throw new DeploymentException("No class space is registerd with name: objectName=" + metadata.getName());
            }

            // Get the mbean descriptor
            try {
                GeronimoMBean mbean = (GeronimoMBean) server.instantiate("org.apache.geronimo.kernel.service.GeronimoMBean");
                mbean.setMBeanInfo(metadata.getGeronimoMBeanInfo());
                server.registerMBean(mbean, metadata.getName());
                created = true;
            } catch (Exception e) {
                throw new DeploymentException("Could not register class space with MBeanServer", e);
            }

            // set the parent
            if (metadata.getParent() != null) {
                try {
                    server.setAttribute(name, new Attribute("parent", metadata.getParent()));
                } catch (Exception e) {
                    throw new DeploymentException("A class space is already registered with name: objectName=" + metadata.getName(), e);
                }
            }

            // start the class space
            try {
                server.invoke(name, "start", null, null);
            } catch (Exception e) {
                throw new DeploymentException("Could not start class space: objectName=" + metadata.getName(), e);
            }
        } else {
            if (metadata.getCreate() == ClassSpaceMetadata.CREATE_ALWAYS) {
                throw new DeploymentException("A class space is already registered with name: objectName=" + metadata.getName());
            }
        }

        // add the deployment's urls to the class space
        try {
            server.invoke(
                    name,
                    "addDeployment",
                    new Object[]{metadata.getDeploymentName(), urls},
                    new String[]{"javax.management.ObjectName", "java.util.List"});
            addedURLs = true;
        } catch (InstanceNotFoundException e) {
            throw new DeploymentException(e);
        } catch (MBeanException e) {
            throw new DeploymentException(e);
        } catch (ReflectionException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Undoes the class space creation.  If the task added URLs to an existing class space,
     * the urls will be dropped from the existing class space, but the urls are not really removed
     * until the class space is restarted.  This method will attempt to restart any such class
     * space, but since live cycle of a component is not synchronous, it is possible that the class
     * space will not be fully restarted, leaving part of the server stopped (or stopping).  This is
     * the best choice for safty, but users must be careful when deploying urls into an existing
     * class space.
     */
    public void undo() {
        final ObjectName name = metadata.getName();
        if (created) {
            try {
                // should be ok to simply unregister as no dependent components could have started
                server.unregisterMBean(name);
            } catch (InstanceNotFoundException e) {
                log.warn("ClassSpace MBean was already removed " + name, e);
                return;
            } catch (MBeanRegistrationException e) {
                log.error("Error while unregistering ClassSpace MBean " + name, e);
            }
        } else if (addedURLs) {
            // @todo  we have a problem here... we added URLs to class space may have been used so it may now contain bad classes (not a problem when constructing a new space)
            log.warn("Stopping class space - added urls to existing class space during deployment which subsequently failed: name=" + name);

            // remove the deployment from the class space
            try {
                // @todo add a restart or recycle method to class space
                server.invoke(
                        name,
                        "dropDeployment",
                        new Object[]{metadata.getDeploymentName()},
                        new String[]{"javax.management.ObjectName"});
            } catch (Exception e) {
                log.error("Could not stop class space: objectName=" + name, e);
            }

            // @todo add a restart or recycle method to class space
            // stop the class space
            try {
                server.invoke(name, "stop", null, null);
            } catch (Exception e) {
                log.error("Could not stop class space: objectName=" + name, e);
            }

            // try to restart the class space
            try {
                if (((Integer) server.getAttribute(name, "state")).intValue() == State.STOPPED_INDEX) {
                    server.invoke(name, "start", null, null);
                }
            } catch (Exception e) {
                log.error("Could not restart class space: objectName=" + name, e);
            }
        }
    }

    public String toString() {
        return "CreateClassSpace " + metadata.getName();
    }
}
