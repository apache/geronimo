package org.apache.geronimo.connector.outbound.connectiontracking;

import java.util.Set;

import javax.resource.ResourceException;

import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TransactionContext;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/31 19:27:16 $
 *
 * */
public interface TrackedConnectionAssociator {

    InstanceContext enter(InstanceContext newInstanceContext
            )
            throws ResourceException;

    void exit(InstanceContext reenteringInstanceContext,
            Set unshareableResources)
            throws ResourceException;

    TransactionContext setTransactionContext(TransactionContext newTransactionContext) throws ResourceException;

    Set setUnshareableResources(Set unshareableResources);

    void resetTransactionContext(TransactionContext transactionContext);
}
