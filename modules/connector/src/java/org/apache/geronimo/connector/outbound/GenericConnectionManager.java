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

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.security.bridge.RealmBridge;

/**
 * GenericConnectionManager sets up a connection manager stack according to the
 * policies described in the attributes.
 *
 * @version $Revision: 1.5 $ $Date: 2004/06/08 17:38:00 $
 */
public class GenericConnectionManager extends AbstractConnectionManager {

    private String objectName;

    //connection manager configuration choices
    private TransactionSupport transactionSupport;
    private PoolingSupport pooling;
    //dependencies

    protected RealmBridge realmBridge;
    protected ConnectionTracker connectionTracker;

    //default constructor for use as endpoint
    public GenericConnectionManager() {
    }

    public GenericConnectionManager(TransactionSupport transactionSupport,
            PoolingSupport pooling,
            String objectName,
            RealmBridge realmBridge,
            ConnectionTracker connectionTracker) {
        this.transactionSupport = transactionSupport;
        this.pooling = pooling;
        this.objectName = objectName;
        this.realmBridge = realmBridge;
        this.connectionTracker = connectionTracker;
    }

    /**
     * Order of constructed interceptors:
     * <p/>
     * ConnectionTrackingInterceptor (connectionTracker != null)
     * ConnectionHandleInterceptor
     * TransactionCachingInterceptor (useTransactions & useTransactionCaching)
     * TransactionEnlistingInterceptor (useTransactions)
     * SubjectInterceptor (realmBridge != null)
     * SinglePoolConnectionInterceptor or MultiPoolConnectionInterceptor
     * LocalXAResourceInsertionInterceptor or XAResourceInsertionInterceptor (useTransactions (&localTransactions))
     * MCFConnectionInterceptor
     */
    protected void setUpConnectionManager() throws IllegalStateException {
        //check for consistency between attributes
        if (realmBridge == null && pooling instanceof PartitionedPool && ((PartitionedPool) pooling).isPartitionBySubject()) {
            throw new IllegalStateException("To use Subject in pooling, you need a SecurityDomain");
        }

        //Set up the interceptor stack
        MCFConnectionInterceptor tail = new MCFConnectionInterceptor();
        ConnectionInterceptor stack = tail;

        stack = transactionSupport.addXAResourceInsertionInterceptor(stack, objectName);
        stack = pooling.addPoolingInterceptors(stack);
        //experimental threadlocal caching
        //moved to XATransactions
//        if (transactionSupport instanceof XATransactions && ((XATransactions)transactionSupport).isUseThreadCaching()) {
//            stack = new ThreadLocalCachingConnectionInterceptor(stack, false);
//        }
        stack = transactionSupport.addTransactionInterceptors(stack);

        if (realmBridge != null) {
            stack = new SubjectInterceptor(stack, realmBridge);
        }

        stack = new ConnectionHandleInterceptor(stack);
        if (connectionTracker != null) {
            stack = new ConnectionTrackingInterceptor(stack,
                    objectName,
                    connectionTracker);
        }
        tail.setStack(stack);
        this.stack = stack;
    }

    public TransactionSupport getTransactionSupport() {
        return transactionSupport;
    }

    public void setTransactionSupport(TransactionSupport transactionSupport) {
        this.transactionSupport = transactionSupport;
    }

    public PoolingSupport getPooling() {
        return pooling;
    }

    public void setPooling(PoolingSupport pooling) {
        this.pooling = pooling;
    }

    public RealmBridge getRealmBridge() {
        return realmBridge;
    }

    public void setRealmBridge(RealmBridge realmBridge) {
        this.realmBridge = realmBridge;
    }

    public ConnectionTracker getConnectionTracker() {
        return connectionTracker;
    }

    public void setConnectionTracker(ConnectionTracker connectionTracker) {
        this.connectionTracker = connectionTracker;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(GenericConnectionManager.class, AbstractConnectionManager.GBEAN_INFO);

        infoFactory.addAttribute("ObjectName", String.class, true);
        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("TransactionSupport", TransactionSupport.class, true);
        infoFactory.addAttribute("Pooling", PoolingSupport.class, true);

        infoFactory.addReference("ConnectionTracker", ConnectionTracker.class);
        infoFactory.addReference("RealmBridge", RealmBridge.class);

        infoFactory.setConstructor(new String[]{
            "TransactionSupport",
            "Pooling",
            "ObjectName",
            "RealmBridge",
            "ConnectionTracker"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GenericConnectionManager.GBEAN_INFO;
    }


}
