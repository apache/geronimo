package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.Set;
import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.ConnectorComponentContext;
import org.apache.geronimo.connector.outbound.ConnectorTransactionContext;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:10 $
 *
 * */
public interface TrackedConnectionAssociator {
    ConnectorComponentContext enter(ConnectorComponentContext newConnectorComponentContext
            )
            throws ResourceException;

    void exit(ConnectorComponentContext reenteringConnectorComponentContext,
            Set unshareableResources)
            throws ResourceException;

    ConnectorTransactionContext setConnectorTransactionContext(ConnectorTransactionContext newConnectorTransactionContext) throws ResourceException;

    Set setUnshareableResources(Set unshareableResources);

    void resetConnectorTransactionContext(ConnectorTransactionContext connectorTransactionContext);
}
