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

package org.apache.geronimo.connector.outbound;

import java.io.Serializable;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

/**
 * ProxyConnectionManager.java
 *
 *
 * Created: Tue Sep 23 21:35:32 2003
 *
 * @version 1.0
 *
 */
public class ProxyConnectionManager
        implements Serializable, ConnectionManager, LazyAssociatableConnectionManager {

    /**
     * The field agentID holds the agentID of the mbean server
     * we use to lookup the stack if we are deserialized.
     */
    private final String agentID;

    /**
     * The field <code>ConnectionManagerName</code> holds the object name of
     * the ConnectionManagerDeployment that sets us up.
     *
     */
    private final ObjectName CMName;

    private transient ConnectionInterceptor stack;

    public ProxyConnectionManager(
            String agentID,
            ObjectName ConnectionManagerName,
            ConnectionInterceptor stack) {
        this.agentID = agentID;
        this.CMName = ConnectionManagerName;
        this.stack = stack;
    }

    /**
     * in: mcf != null, is a deployed mcf
     * out: useable connection object.
     * @param managedConnectionFactory
     * @param connectionRequestInfo
     * @return
     * @throws ResourceException
     */
    public Object allocateConnection(
            ManagedConnectionFactory managedConnectionFactory,
            ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {
        internalGetStack();
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, connectionRequestInfo);
        ConnectionInfo ci = new ConnectionInfo(mci);
        stack.getConnection(ci);
        return ci.getConnectionHandle();
    }

    /**
     * in: non-null connection object, from non-null mcf.
     * connection object is not associated with a managed connection
     * out: supplied connection object is assiciated with a non-null ManagedConnection from mcf.
     * @param connection
     * @param managedConnectionFactory
     * @param connectionRequestInfo
     * @throws ResourceException
     */
    public void associateConnection(
            Object connection,
            ManagedConnectionFactory managedConnectionFactory,
            ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {
        internalGetStack();
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, connectionRequestInfo);
        ConnectionInfo ci = new ConnectionInfo(mci);
        ci.setConnectionHandle(connection);
        stack.getConnection(ci);
    }

    private void internalGetStack() throws ResourceException {
        if (stack == null) {
            MBeanServer server =
                    (MBeanServer) MBeanServerFactory.findMBeanServer(agentID).get(
                            0);
            try {
                this.stack =
                        (ConnectionInterceptor) server.invoke(CMName, "getStack", null, null);
            } catch (InstanceNotFoundException e) {
                throw new ResourceException("Could not get stack from jmx", e);
            } catch (MBeanException e) {
                throw new ResourceException("Could not get stack from jmx", e);
            } catch (ReflectionException e) {
                throw new ResourceException("Could not get stack from jmx", e);
            }
        }
    }

    /**
     * The <code>getStack</code> method is called through jmx to get
     * the actual ConnectionInterceptor stack for deserialized copies
     * of this object.
     *
     * @return a <code>ConnectionInterceptor</code> value
     */
    public ConnectionInterceptor getStack() {
        return stack;
    }

}
