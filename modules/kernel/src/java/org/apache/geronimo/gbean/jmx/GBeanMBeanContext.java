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
package org.apache.geronimo.gbean.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.kernel.management.State;

/**
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/20 22:39:05 $
 */
public class GBeanMBeanContext implements GBeanContext {
    /**
     * The MBean server in which the Geronimo MBean is registered.
     */
    private MBeanServer server;

    /**
     * The GeronimoMBean which owns the target.
     */
    private GBeanMBean gmbean;

    /**
     * The object name of the Geronimo MBean.
     */
    private ObjectName objectName;

    /**
     * Creates a new context for a target.
     *
     * @param server a reference to the mbean server in which the Geronimo Mbean is registered
     * @param gmbean the Geronimo Mbean the contains the target
     * @param objectName the registered name of the Geronimo MBean
     */
    public GBeanMBeanContext(MBeanServer server, GBeanMBean gmbean, ObjectName objectName) {
        this.server = server;
        this.gmbean = gmbean;
        this.objectName = objectName;
    }

    /**
     * Gets a reference to the MBean server in which the Geronimo MBean is registered.
     * @return a reference to the MBean server in which the Geronimo MBean is registered
     */
    public MBeanServer getServer() {
        return server;
    }

    /**
     * Gets the registered name of the Geronimo MBean
     * @return the registered name of the Geronimo MBean
     */
    public ObjectName getObjectName() {
        return objectName;
    }

    /**
     * Gets the state of this component as an int.
     * The int return is required by the JSR77 specification.
     *
     * @return the current state of this component
     */
    public int getState() {
        return gmbean.getState();
    }

    /**
     * Attempts to bring the component into the fully running state. If an Exception occurs while
     * starting the component, the component is automaticaly failed.
     *
     * There is no guarantee that the Geronimo MBean will be running when the method returns.
     *
     * @throws Exception if a problem occurs while starting the component
     */
    public void start() throws Exception {
        gmbean.attemptFullStart();
    }

    /**
     * Attempt to bring the component into the fully stopped state. If an exception occurs while
     * stopping the component, tthe component is automaticaly failed.
     *
     * There is no guarantee that the Geronimo MBean will be stopped when the method returns.
     *
     * @throws Exception if a problem occurs while stopping the component
     */
    public void stop() throws Exception {
        final int state = gmbean.getState();
        if (state == State.RUNNING_INDEX || state == State.STARTING_INDEX) {
            gmbean.stop();
        } else if (state == State.STOPPING_INDEX) {
            gmbean.attemptFullStop();
        }
    }

    /**
     * Moves this component to the FAILED state.
     *
     * The component is guaranteed to be in the failed state when the method returns.
     */
    public void fail() {
        gmbean.fail();
    }
}
