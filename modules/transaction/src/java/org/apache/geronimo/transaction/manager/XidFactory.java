package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/05/06 04:00:51 $
 *
 * */
public interface XidFactory {
    Xid createXid();

    Xid createBranch(Xid globalId, int branch);

    boolean matchesGlobalId(byte[] globalTransactionId);

    boolean matchesBranchId(byte[] branchQualifier);
}
