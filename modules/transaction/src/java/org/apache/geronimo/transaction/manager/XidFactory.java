package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Revision: 1.6 $ $Date: 2004/06/08 17:33:43 $
 *
 * */
public interface XidFactory {
    Xid createXid();

    Xid createBranch(Xid globalId, int branch);

    boolean matchesGlobalId(byte[] globalTransactionId);

    boolean matchesBranchId(byte[] branchQualifier);

    Xid recover(int formatId, byte[] globalTransactionid, byte[] branchQualifier);
}
