package org.apache.geronimo.transaction;

import javax.transaction.xa.Xid;

/**
 */
public class ImportedTransactionActiveException extends Exception {

    private final Xid xid;

    public ImportedTransactionActiveException(Xid xid) {
        this.xid = xid;
    }

    public Xid getXid() {
        return xid;
    }

}
