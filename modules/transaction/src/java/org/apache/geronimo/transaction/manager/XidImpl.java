/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.transaction.manager;

import java.io.Serializable;
import java.util.Arrays;
import javax.transaction.xa.Xid;

/**
 * Unique id for a transaction.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:19 $
 */
public class XidImpl implements Xid, Serializable {
    private static int FORMAT_ID = 0x4765526f;  // Gero
    private final byte[] globalId;
    private final byte[] branchId;
    private final int hash;

    /**
     * Constructor taking a global id (for the main transaction)
     * @param globalId the global transaction id
     */
    public XidImpl(byte[] globalId) {
        this.globalId = globalId;
        this.hash = hash(0, globalId);
        branchId = new byte[Xid.MAXBQUALSIZE];
    }

    /**
     * Constructor for a branch id
     * @param global the xid of the global transaction this branch belongs to
     * @param branch the branch id
     */
    public XidImpl(Xid global, byte[] branch) {
        int hash;
        if (global instanceof XidImpl) {
            globalId = ((XidImpl) global).globalId;
            hash = ((XidImpl) global).hash;
        } else {
            globalId = global.getGlobalTransactionId();
            hash = hash(0, globalId);
        }
        branchId = branch;
        this.hash = hash(hash, branchId);
    }

    private int hash(int hash, byte[] id) {
        for (int i = 0; i < id.length; i++) {
            hash = (hash * 37) + id[i];
        }
        return hash;
    }

    public int getFormatId() {
        return FORMAT_ID;
    }

    public byte[] getGlobalTransactionId() {
        return (byte[]) globalId.clone();
    }

    public byte[] getBranchQualifier() {
        return (byte[]) branchId.clone();
    }

    public boolean equals(Object obj) {
        if (obj instanceof XidImpl == false) {
            return false;
        }
        XidImpl other = (XidImpl) obj;
        return Arrays.equals(globalId, other.globalId) && Arrays.equals(branchId, other.branchId);
    }

    public int hashCode() {
        return hash;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("[globalId=");
        for (int i = 0; i < globalId.length; i++) {
            s.append(Integer.toHexString(globalId[i]));
        }
        s.append(",branchId=");
        for (int i = 0; i < branchId.length; i++) {
            s.append(Integer.toHexString(branchId[i]));
        }
        s.append("]");
        return s.toString();
    }
}
