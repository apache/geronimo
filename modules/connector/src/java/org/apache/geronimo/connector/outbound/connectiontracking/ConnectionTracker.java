package org.apache.geronimo.connector.outbound.connectiontracking;

import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.transaction.TransactionContext;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/31 19:27:16 $
 *
 * */
public interface ConnectionTracker {
    void handleObtained(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo);

    void handleReleased(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo);

    TransactionContext getTransactionContext();
}
