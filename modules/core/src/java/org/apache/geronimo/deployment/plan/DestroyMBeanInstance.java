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
package org.apache.geronimo.deployment.plan;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.management.j2ee.State;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/18 22:31:23 $
 */
public class DestroyMBeanInstance implements DeploymentTask {
    private final Log log = LogFactory.getLog(this.getClass());
    private final MBeanServer server;
    private final ObjectName name;

    public DestroyMBeanInstance(MBeanServer server, ObjectName name) {
        this.server = server;
        this.name = name;
    }

    public boolean canRun() throws DeploymentException {
        try {
            log.trace("Checking if MBean is stopped: name=" + name);
            if (((Integer) server.getAttribute(name, "State")).intValue() != State.STOPPED_INDEX) {
                log.trace("Cannot run because MBean is not stopped: name=" + name);
                return false;
            }
            log.trace("MBean is stopped: name=" + name);
        } catch (AttributeNotFoundException e) {
            // ok -- MBean is not state manageable
            log.trace("MBean does not have a State attibute");
        } catch (InstanceNotFoundException e) {
            // instance already removed -- we are good to go
        } catch (Exception e) {
            // problem getting the attribute, MBean has most likely failed
            log.trace("An error occurred while checking if MBean is stopped; MBean will be unregistered: name=" + name);
        }
        return true;
    }

    public void perform() {
        try {
            server.unregisterMBean(name);
        } catch (InstanceNotFoundException e) {
            log.warn("MBean was already removed " + name, e);
            return;
        } catch (MBeanRegistrationException e) {
            log.error("Error while unregistering MBean " + name, e);
        }
    }

    public void undo() {
    }

    public String toString() {
        return "DestroyMBeanInstance " + name;
    }
}
