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
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.deployment.ConnectionManagerFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.kernel.service.AbstractManagedObject;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * ConnectionManagerDeployment is an mbean that sets up a ProxyConnectionManager
 * and connection manager stack according to the policies described in the attributes.
 * It's used by deserialized copies of the proxy to get a reference to the actual stack.
 *
 * @version $Revision: 1.3 $ $Date: 2003/12/09 04:16:25 $
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
     * The actual TransactionManager we get
     */
    private TransactionManager transactionManager;

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

    /* (non-Javadoc)
         * @see org.apache.geronimo.core.service.AbstractStateManageable#doStart()
         */
    public void doStart() {
        //check for consistency between attributes
        useTransactions = (transactionManager != null);
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
            stack =
                    new MultiPoolConnectionInterceptor(
                            stack,
                            maxSize,
                            blockingTimeout,
                            useSubject,
                            useConnectionRequestInfo);
        } else {
            stack =
                    new SinglePoolConnectionInterceptor(
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
            stack =
                    new TransactionEnlistingInterceptor(stack, transactionManager);
            if (useTransactionCaching) {
                stack =
                        new TransactionCachingInterceptor(
                                stack,
                                connectionTracker);
            }
        }
        stack = new ConnectionHandleInterceptor(stack);
        if (connectionTracker != null) {
            stack =
                    new ConnectionTrackingInterceptor(
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
        transactionManager = null;
        connectionTracker = null;

    }

    public void doFail() {
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public ConnectionInterceptor getStack() {
        return cm.getStack();
    }


    /**
     * @return
     * @jmx.managed-operation
     */
    public Object createConnectionFactory(ManagedConnectionFactory mcf) throws ResourceException {
        return mcf.createConnectionFactory(cm);
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public int getBlockingTimeout() {
        return blockingTimeout;
    }

    /**
     * @param blockingTimeout
     * @jmx.managed-attribute
     */
    public void setBlockingTimeout(int blockingTimeout) {
        this.blockingTimeout = blockingTimeout;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public ConnectionTracker getConnectionTracker() {
        return connectionTracker;
    }

    /**
     * @param connectionTracker
     * @jmx.managed-attribute
     */
    public void setConnectionTracker(ConnectionTracker connectionTracker) {
        this.connectionTracker = connectionTracker;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * @param jndiName
     * @jmx.managed-attribute
     */
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @param maxSize
     * @jmx.managed-attribute
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public SecurityDomain getSecurityDomain() {
        return securityDomain;
    }

    /**
     * @param securityDomain
     * @jmx.managed-attribute
     */
    public void setSecurityDomain(SecurityDomain securityDomain) {
        this.securityDomain = securityDomain;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * @param transactionManager
     * @jmx.managed-attribute
     */
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public boolean isUseConnectionRequestInfo() {
        return useConnectionRequestInfo;
    }

    /**
     * @param useConnectionRequestInfo
     * @jmx.managed-attribute
     */
    public void setUseConnectionRequestInfo(boolean useConnectionRequestInfo) {
        this.useConnectionRequestInfo = useConnectionRequestInfo;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public boolean isUseLocalTransactions() {
        return useLocalTransactions;
    }

    /**
     * @param useLocalTransactions
     * @jmx.managed-attribute
     */
    public void setUseLocalTransactions(boolean useLocalTransactions) {
        this.useLocalTransactions = useLocalTransactions;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public boolean isUseSubject() {
        return useSubject;
    }

    /**
     * @param useSubject
     * @jmx.managed-attribute
     */
    public void setUseSubject(boolean useSubject) {
        this.useSubject = useSubject;
    }

    /**
     * @return
     * @jmx.managed-attribute
     */
    public boolean isUseTransactionCaching() {
        return useTransactionCaching;
    }

    /**
     * @param useTransactionCaching
     * @jmx.managed-attribute
     */
    public void setUseTransactionCaching(boolean useTransactionCaching) {
        this.useTransactionCaching = useTransactionCaching;
    }


}
