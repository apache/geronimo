package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.Set;

import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.ConnectorComponentContext;
import org.apache.geronimo.connector.outbound.ConnectorTransactionContext;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/09 04:13:20 $
 *
 * */
public interface TrackedConnectionAssociator {
    ConnectorComponentContext enter(ConnectorComponentContext newConnectorComponentContext,
                                    Set unshareableResources)
            throws ResourceException;

    void exit(ConnectorComponentContext reenteringConnectorComponentContext,
              Set unshareableResources)
            throws ResourceException;

    ConnectorTransactionContext setConnectorTransactionContext(ConnectorTransactionContext newConnectorTransactionContext);
}
