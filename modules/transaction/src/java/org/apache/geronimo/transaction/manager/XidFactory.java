package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface XidFactory {
    Xid createXid();

    Xid createBranch(Xid globalId, int branch);

    boolean matchesGlobalId(byte[] globalTransactionId);

    boolean matchesBranchId(byte[] branchQualifier);

    Xid recover(int formatId, byte[] globalTransactionid, byte[] branchQualifier);
}
