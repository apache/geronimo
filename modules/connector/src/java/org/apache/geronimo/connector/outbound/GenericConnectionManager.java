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
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.bridge.RealmBridge;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * GenericConnectionManager sets up a connection manager stack according to the
 * policies described in the attributes.
 *
 * @version $Rev$ $Date$
 */
public class GenericConnectionManager extends AbstractConnectionManager {

    //default constructor for use as endpoint
    public GenericConnectionManager() {
        super();
    }

    public GenericConnectionManager(TransactionSupport transactionSupport,
                                    PoolingSupport pooling,
                                    RealmBridge realmBridge,
                                    ConnectionTracker connectionTracker,
                                    TransactionContextManager transactionContextManager,
                                    String objectName,
                                    ClassLoader classLoader
                                    ) {
        super(new InterceptorsImpl(transactionSupport, pooling, objectName, realmBridge, connectionTracker, transactionContextManager, classLoader));
    }

    private static class InterceptorsImpl implements AbstractConnectionManager.Interceptors {

        private final ConnectionInterceptor stack;
        private final ConnectionInterceptor recoveryStack;
        private final PoolingSupport poolingSupport;

        /**
         * Order of constructed interceptors:
         * <p/>
         * ConnectionTrackingInterceptor (connectionTracker != null)
         * TCCLInterceptor
         * ConnectionHandleInterceptor
         * TransactionCachingInterceptor (useTransactions & useTransactionCaching)
         * TransactionEnlistingInterceptor (useTransactions)
         * SubjectInterceptor (realmBridge != null)
         * SinglePoolConnectionInterceptor or MultiPoolConnectionInterceptor
         * LocalXAResourceInsertionInterceptor or XAResourceInsertionInterceptor (useTransactions (&localTransactions))
         * MCFConnectionInterceptor
         */
        public InterceptorsImpl(TransactionSupport transactionSupport,
                                PoolingSupport pooling,
                                String objectName,
                                RealmBridge realmBridge,
                                ConnectionTracker connectionTracker,
                                TransactionContextManager transactionContextManager,
                                ClassLoader classLoader) {
            //check for consistency between attributes
            if (realmBridge == null && pooling instanceof PartitionedPool && ((PartitionedPool) pooling).isPartitionBySubject()) {
                throw new IllegalStateException("To use Subject in pooling, you need a SecurityDomain");
            }

            //Set up the interceptor stack
            MCFConnectionInterceptor tail = new MCFConnectionInterceptor();
            ConnectionInterceptor stack = tail;

            stack = transactionSupport.addXAResourceInsertionInterceptor(stack, objectName);
            stack = pooling.addPoolingInterceptors(stack);
            this.poolingSupport = pooling;
            //experimental threadlocal caching
            //moved to XATransactions
//        if (transactionSupport instanceof XATransactions && ((XATransactions)transactionSupport).isUseThreadCaching()) {
//            stack = new ThreadLocalCachingConnectionInterceptor(stack, false);
//        }
            stack = transactionSupport.addTransactionInterceptors(stack, transactionContextManager);

            if (realmBridge != null) {
                stack = new SubjectInterceptor(stack, realmBridge);
            }

            ConnectionInterceptor recoveryStack = stack;
            this.recoveryStack = new TCCLInterceptor(recoveryStack, classLoader);


            stack = new ConnectionHandleInterceptor(stack);
            stack = new TCCLInterceptor(stack, classLoader);
            if (connectionTracker != null) {
                stack = new ConnectionTrackingInterceptor(stack,
                        objectName,
                        connectionTracker);
            }
            tail.setStack(stack);
            this.stack = stack;
        }

        public ConnectionInterceptor getStack() {
            return stack;
        }

        public ConnectionInterceptor getRecoveryStack() {
            return recoveryStack;
        }

        public PoolingSupport getPoolingAttributes() {
            return poolingSupport;
        }
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder(GenericConnectionManager.class, AbstractConnectionManager.GBEAN_INFO);

        infoBuilder.addAttribute("transactionSupport", TransactionSupport.class, true);
        infoBuilder.addAttribute("pooling", PoolingSupport.class, true);

        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.addReference("ConnectionTracker", ConnectionTracker.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoBuilder.addReference("RealmBridge", RealmBridge.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.JTA_RESOURCE);

        infoBuilder.setConstructor(new String[]{
            "transactionSupport",
            "pooling",
            "RealmBridge",
            "ConnectionTracker",
            "TransactionContextManager",
            "objectName",
            "classLoader"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GenericConnectionManager.GBEAN_INFO;
    }

}
