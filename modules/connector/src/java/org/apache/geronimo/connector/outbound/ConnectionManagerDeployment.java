/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.connector.outbound;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.LazyAssociatableConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.deployment.DeploymentException;

/**
 * ConnectionManagerDeployment is an mbean that sets up a ProxyConnectionManager
 * and connection manager stack according to the policies described in the attributes.
 * It's used by deserialized copies of the proxy to get a reference to the actual stack.
 *
 * @version $Revision: 1.7 $ $Date: 2004/04/08 20:35:32 $
 * */
public class ConnectionManagerDeployment implements ConnectionManagerFactory, GBean, ConnectionManager, LazyAssociatableConnectionManager {

    public static final GBeanInfo GBEAN_INFO;

    //connection manager configuration choices
    private boolean useConnectionRequestInfo;
    private boolean useSubject;
    private boolean useTransactionCaching;
    private boolean useLocalTransactions;
    private boolean useTransactions;
    private int maxSize;
    private int blockingTimeout;
    /**
     * Identifying string used by unshareable resource detection
     */
    private String name;
    //dependencies
    private RealmBridge realmBridge;
    private ConnectionTracker connectionTracker;

    private ConnectionInterceptor stack;

    //default constructor for use as endpoint
    public ConnectionManagerDeployment() {
    }

    public ConnectionManagerDeployment(boolean useConnectionRequestInfo,
                                       boolean useSubject,
                                       boolean useTransactionCaching,
                                       boolean useLocalTransactions,
                                       boolean useTransactions,
                                       int maxSize,
                                       int blockingTimeout,
                                       RealmBridge realmBridge,
                                       ConnectionTracker connectionTracker) {
        this.useConnectionRequestInfo = useConnectionRequestInfo;
        this.useLocalTransactions = useLocalTransactions;
        this.useSubject = useSubject;
        this.useTransactionCaching = useTransactionCaching;
        this.useTransactions = useTransactions;
        this.maxSize = maxSize;
        this.blockingTimeout = blockingTimeout;
        this.realmBridge = realmBridge;
        this.connectionTracker = connectionTracker;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart()  throws WaitingException, Exception{
        setUpConnectionManager();

    }

    /**
     * Order of constructed interceptors:
     *
     * ConnectionTrackingInterceptor (connectionTracker != null)
     * ConnectionHandleInterceptor
     * TransactionCachingInterceptor (useTransactions & useTransactionCaching)
     * TransactionEnlistingInterceptor (useTransactions)
     * SubjectInterceptor (realmBridge != null)
     * SinglePoolConnectionInterceptor or MultiPoolConnectionInterceptor
     * LocalXAResourceInsertionInterceptor or XAResourceInsertionInterceptor (useTransactions (&localTransactions))
     * MCFConnectionInterceptor
     */
    private void setUpConnectionManager() throws DeploymentException {
        //check for consistency between attributes
        if (realmBridge == null && useSubject) {
            throw new  DeploymentException("To use Subject in pooling, you need a SecurityDomain");
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
        if (realmBridge != null) {
            stack = new SubjectInterceptor(stack, realmBridge);
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
                    name,
                    connectionTracker,
                    realmBridge);
        }
        this.stack = stack;
    }

    public void doStop() {
        realmBridge = null;
        connectionTracker = null;
        stack = null;
    }

    public void doFail() {
    }


    public Object createConnectionFactory(ManagedConnectionFactory mcf) throws ResourceException {
        return mcf.createConnectionFactory(this);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public RealmBridge getRealmBridge() {
        return realmBridge;
    }

    public void setRealmBridge(RealmBridge realmBridge) {
        this.realmBridge = realmBridge;
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

    //Even if realmBridge is present, if reauthentication is supported, you might not want to use
    //the subject as pooling crieteria.
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
        ManagedConnectionInfo mci = new ManagedConnectionInfo(managedConnectionFactory, connectionRequestInfo);
        ConnectionInfo ci = new ConnectionInfo(mci);
        ci.setConnectionHandle(connection);
        stack.getConnection(ci);
    }

    ConnectionInterceptor getStack() {
        return stack;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConnectionManagerDeployment.class.getName());

        infoFactory.addAttribute(new GAttributeInfo("Name", true));
        infoFactory.addAttribute(new GAttributeInfo("BlockingTimeout", true));
        infoFactory.addAttribute(new GAttributeInfo("MaxSize", true));
        infoFactory.addAttribute(new GAttributeInfo("UseTransactions", true));
        infoFactory.addAttribute(new GAttributeInfo("UseLocalTransactions", true));
        infoFactory.addAttribute(new GAttributeInfo("UseTransactionCaching", true));
        infoFactory.addAttribute(new GAttributeInfo("UseConnectionRequestInfo", true));
        infoFactory.addAttribute(new GAttributeInfo("UseSubject", true));

        infoFactory.addOperation(new GOperationInfo("createConnectionFactory", new String[]{ManagedConnectionFactory.class.getName()}));

        infoFactory.addReference(new GReferenceInfo("ConnectionTracker", ConnectionTracker.class.getName()));
        infoFactory.addReference(new GReferenceInfo("RealmBridge", RealmBridge.class.getName()));

        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"UseConnectionRequestInfo", "UseSubject", "UseTransactionCaching", "UseLocalTransactions", "UseTransactions",
                             "MaxSize", "BlockingTimeout", "RealmBridge", "ConnectionTracker"},
                new Class[]{Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE,
                            Integer.TYPE, Integer.TYPE, RealmBridge.class, ConnectionTracker.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
