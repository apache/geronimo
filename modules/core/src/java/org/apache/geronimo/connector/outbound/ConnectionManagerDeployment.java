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

import javax.management.ObjectName;
import javax.management.MBeanOperationInfo;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;

import org.apache.geronimo.connector.deployment.ConnectionManagerFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;

import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;

/**
 * ConnectionManagerDeployment is an mbean that sets up a ProxyConnectionManager
 * and connection manager stack according to the policies described in the attributes.
 * It's used by deserialized copies of the proxy to get a reference to the actual stack.
 *
 * @version $Revision: 1.5 $ $Date: 2003/12/13 23:33:53 $
 * */
public class ConnectionManagerDeployment

        implements GeronimoMBeanTarget, ConnectionManagerFactory {

    private final static String MBEAN_SERVER_DELEGATE =
            "JMImplementation:type=MBeanServerDelegate";

    /**
     * The original Serializable ProxyConnectionManager that provides the
     * ConnectionManager implementation.
     */
    private ProxyConnectionManager cm;

    //connection manager configuration choices
    private boolean useConnectionRequestInfo;
    private boolean useSubject;
    private boolean useTransactionCaching;
    private boolean useLocalTransactions;
    private boolean useTransactions;
    private int maxSize;
    private int blockingTimeout;


    //dependencies

    /**
     * (proxy for) the security domain object
     */
    private SecurityDomain securityDomain;

    /**
     * Identifying string used by unshareable resource detection
     */
    private String jndiName;

    private GeronimoMBeanContext context;


    /**
     * Actual CachedConnectionManager we relate to.
     */
    private ConnectionTracker connectionTracker;

    public void setMBeanContext(GeronimoMBeanContext context) {
        this.context = context;
    }

    public boolean canStart() {
        return true;
    }

    /**
     * Order of constructed interceptors:
     *
     * ConnectionTrackingInterceptor (connectionTracker != null)
     * ConnectionHandleInterceptor
     * TransactionCachingInterceptor (useTransactions & useTransactionCaching)
     * TransactionEnlistingInterceptor (useTransactions)
     * SubjectInterceptor (securityDomain != null)
     * SinglePoolConnectionInterceptor or MultiPoolConnectionInterceptor
     * LocalXAResourceInsertionInterceptor or XAResourceInsertionInterceptor (useTransactions (&localTransactions))
     * MCFConnectionInterceptor
     */
    public void doStart() {
        //check for consistency between attributes
        if (securityDomain == null) {
            assert useSubject == false: "To use Subject in pooling, you need a SecurityDomain";
        }

        //Set up the interceptor stack
        ConnectionInterceptor stack = new MCFConnectionInterceptor(this);
        if (useTransactions) {
            if (useLocalTransactions) {
                stack = new LocalXAResourceInsertionInterceptor(stack);
            } else {
                stack = new XAResourceInsertionInterceptor(stack);
            }
        }
        if (useSubject || useConnectionRequestInfo) {
            stack = new MultiPoolConnectionInterceptor(
                    stack,
                    maxSize,
                    blockingTimeout,
                    useSubject,
                    useConnectionRequestInfo);
        } else {
            stack = new SinglePoolConnectionInterceptor(
                    stack,
                    null,
                    null,
                    maxSize,
                    blockingTimeout);
        }
        if (securityDomain != null) {
            stack = new SubjectInterceptor(stack, securityDomain);
        }
        if (useTransactions) {
            stack = new TransactionEnlistingInterceptor(stack);
            if (useTransactionCaching) {
                stack = new TransactionCachingInterceptor(stack, connectionTracker);
            }
        }
        stack = new ConnectionHandleInterceptor(stack);
        if (connectionTracker != null) {
            stack = new ConnectionTrackingInterceptor(
                    stack,
                    jndiName,
                    connectionTracker,
                    securityDomain);
        }

        ObjectName name = JMXUtil.getObjectName(MBEAN_SERVER_DELEGATE);
        try {
            String agentID = (String) context.getServer().getAttribute(name, "MBeanServerId");
            cm = new ProxyConnectionManager(agentID, context.getObjectName(), stack);
        } catch (Exception e) {
            throw new RuntimeException("Problem getting agentID from MBeanServerDelegate", e);
        }

    }

    public boolean canStop() {
        return true;
    }

    /* (non-Javadoc)
         * @see org.apache.geronimo.core.service.AbstractStateManageable#doStop()
         */
    public void doStop() {
        cm = null;
        securityDomain = null;
        connectionTracker = null;

    }

    public void doFail() {
    }

    public ConnectionInterceptor getStack() {
        return cm.getStack();
    }


    public Object createConnectionFactory(ManagedConnectionFactory mcf) throws ResourceException {
        return mcf.createConnectionFactory(cm);
    }

    public int getBlockingTimeout() {
        return blockingTimeout;
    }

    public void setBlockingTimeout(int blockingTimeout) {
        this.blockingTimeout = blockingTimeout;
    }

    public ConnectionTracker getConnectionTracker() {
        return connectionTracker;
    }

    public void setConnectionTracker(ConnectionTracker connectionTracker) {
        this.connectionTracker = connectionTracker;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public SecurityDomain getSecurityDomain() {
        return securityDomain;
    }

    public void setSecurityDomain(SecurityDomain securityDomain) {
        this.securityDomain = securityDomain;
    }

    public boolean isUseConnectionRequestInfo() {
        return useConnectionRequestInfo;
    }

    public void setUseConnectionRequestInfo(boolean useConnectionRequestInfo) {
        this.useConnectionRequestInfo = useConnectionRequestInfo;
    }

    //TODO determine this from [geronimo-]ra.xml transaction attribute.
    public boolean isUseTransactions() {
        return useTransactions;
    }

    public void setUseTransactions(boolean useTransactions) {
        this.useTransactions = useTransactions;
    }

    public boolean isUseLocalTransactions() {
        return useLocalTransactions;
    }

    public void setUseLocalTransactions(boolean useLocalTransactions) {
        this.useLocalTransactions = useLocalTransactions;
    }

    public boolean isUseSubject() {
        return useSubject;
    }

    public void setUseSubject(boolean useSubject) {
        this.useSubject = useSubject;
    }

    public boolean isUseTransactionCaching() {
        return useTransactionCaching;
    }

    public void setUseTransactionCaching(boolean useTransactionCaching) {
        this.useTransactionCaching = useTransactionCaching;
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mBeanInfo = new GeronimoMBeanInfo();

        mBeanInfo.setTargetClass(ConnectionManagerDeployment.class);
        mBeanInfo.addEndpoint(new GeronimoMBeanEndpoint("ConnectionTracker", ConnectionTracker.class, ObjectName.getInstance("geronimo.connector:role=ConnectionTrackingCoordinator"), true));
        mBeanInfo.addEndpoint(new GeronimoMBeanEndpoint("SecurityDomain", SecurityDomain.class, ObjectName.getInstance("geronimo.connector:role=SecurityDomain"), false));

        mBeanInfo.addAttributeInfo(new GeronimoAttributeInfo("BlockingTimeout", true, true, "Milliseconds to wait for a connection to be returned"));
        mBeanInfo.addAttributeInfo(new GeronimoAttributeInfo("JndiName", true, true, "Name to use to identify this guy (needs refactoring of naming conventions)"));
        mBeanInfo.addAttributeInfo(new GeronimoAttributeInfo("MaxSize", true, true, "Maximum number of ManagedConnections to create in each pool"));
        mBeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UseConnectionRequestInfo", true, true, "Select pool using app-supplied ConnectionRequestInfo"));
        mBeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UseTransactions", true, true, "Use local or xa transactions vs. no transactions"));
        mBeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UseLocalTransactions", true, true, "Use local rather than xa transactions"));
        mBeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UseTransactionCaching", true, true, "Always use the same connection in a transaction"));
        mBeanInfo.addAttributeInfo(new GeronimoAttributeInfo("UseSubject", true, true, "Select pool using SecurityDomain supplied subject"));

        mBeanInfo.addOperationInfo(new GeronimoOperationInfo("getStack"));

        mBeanInfo.addOperationInfo(new GeronimoOperationInfo("createConnectionFactory",
                new GeronimoParameterInfo[] {new GeronimoParameterInfo("ManagedConnectionFactory", ManagedConnectionFactory.class, "ManagedConnectionFactory that will create the ConnectionFactory")},
                MBeanOperationInfo.ACTION,
                "PRIVATE OPERATION. Have the supplied ManagedConnectionFactory create a connection factory"));
        return mBeanInfo;
    }

}
