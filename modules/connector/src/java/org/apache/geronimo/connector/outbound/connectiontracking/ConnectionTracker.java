package org.apache.geronimo.connector.outbound.connectiontracking;

import org.apache.geronimo.connector.outbound.ConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionTrackingInterceptor;
import org.apache.geronimo.connector.outbound.ConnectorTransactionContext;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:10 $
 *
 * */
public interface ConnectionTracker {
    void handleObtained(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo);

    void handleReleased(
            ConnectionTrackingInterceptor connectionTrackingInterceptor,
            ConnectionInfo connectionInfo);

    ConnectorTransactionContext getConnectorTransactionContext();
}
