/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.transaction.log;

import java.io.Serializable;
import java.util.Arrays;

import javax.transaction.xa.Xid;

import pyrasun.binlog.LogEntryKey;

/**
 * Unique id for a transaction.  This implementation is backed by a single byte buffer
 * so can do less copying than one backed by several byte buffers for the different components.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/06 04:00:51 $
 */
public class XidImpl2 implements Xid, Serializable {
    private static int HEADER_SIZE = 4;
    private static int ACTION_POS = 0;
    private static int GLOBALID_SIZE_POS = 1;
    private static int BRANCHID_SIZE_POS = 2;
    //3 unused
    private static int FORMAT_ID = 0x4765526f;  // Gero
    private static int FORMAT_SIZE = 4;

    private static byte[] FORMAT_ID_BYTES = "Gero".getBytes();

    private final byte[] buffer = new byte[HEADER_SIZE + FORMAT_SIZE + Xid.MAXGTRIDSIZE + Xid.MAXBQUALSIZE];
    private int hash;
    private Object key;

    /**
     * Constructor taking a global id (for the main transaction)
     * @param globalId the global transaction id
     */
    public XidImpl2(byte[] globalId) {
        System.arraycopy(FORMAT_ID_BYTES, 0, buffer, HEADER_SIZE, FORMAT_SIZE);
        buffer[GLOBALID_SIZE_POS] = (byte)globalId.length;
        System.arraycopy(globalId, 0, buffer, HEADER_SIZE + FORMAT_SIZE, Xid.MAXGTRIDSIZE);

        //this.hash = hash(buffer);
    }

    /**
     * Constructor for a branch id
     * @param global the xid of the global transaction this branch belongs to
     * @param branch the branch id
     */
    public XidImpl2(Xid global, byte[] branch) {
        if (global instanceof XidImpl2) {
            System.arraycopy(((XidImpl2)global).buffer, 0, buffer, 0, HEADER_SIZE +FORMAT_SIZE + Xid.MAXGTRIDSIZE);
        } else {
            System.arraycopy(FORMAT_ID_BYTES, 0, buffer, HEADER_SIZE, FORMAT_SIZE);
            byte[] globalId = global.getGlobalTransactionId();
            System.arraycopy(globalId, 0, buffer, HEADER_SIZE + FORMAT_SIZE, globalId.length);
        }
        buffer[BRANCHID_SIZE_POS] = (byte)branch.length;
        System.arraycopy(branch, 0, buffer, HEADER_SIZE + FORMAT_SIZE + Xid.MAXGTRIDSIZE, Xid.MAXBQUALSIZE);
        //hash = hash(buffer);
    }

    private int hash(byte[] id) {
        int hash = 0;
        for (int i = 0; i < id.length; i++) {
            hash = (hash * 37) + id[i];
        }
        return hash;
    }

    public int getFormatId() {
        return FORMAT_ID;
    }

    public byte[] getGlobalTransactionId() {
        byte[] globalId = new byte[buffer[GLOBALID_SIZE_POS]];
        System.arraycopy(buffer, HEADER_SIZE + FORMAT_SIZE, globalId, 0, buffer[GLOBALID_SIZE_POS]);
        return globalId;
    }

    public byte[] getBranchQualifier() {
        byte[] branchId = new byte[buffer[BRANCHID_SIZE_POS]];
        System.arraycopy(buffer, HEADER_SIZE + FORMAT_SIZE + Xid.MAXGTRIDSIZE, branchId, 0, buffer[BRANCHID_SIZE_POS]);
        return branchId;
    }

    public boolean equals(Object obj) {
        if (obj instanceof XidImpl2 == false) {
            return false;
        }
        XidImpl2 other = (XidImpl2) obj;
        return Arrays.equals(buffer, other.buffer);
    }

    public int hashCode() {
        if (hash == 0) {
            hash = hash(buffer);
        }
        return hash;
    }

    public String toString() {
        StringBuffer s = new StringBuffer("[formatId=Gero,");
        s.append("globalId=");
        for (int i = FORMAT_SIZE; i < FORMAT_SIZE + Xid.MAXGTRIDSIZE; i++) {
            s.append(Integer.toHexString(buffer[i]));
        }
        s.append(",branchId=");
        for (int i = FORMAT_SIZE + Xid.MAXGTRIDSIZE; i < buffer.length; i++) {
            s.append(Integer.toHexString(buffer[i]));
        }
        s.append("]");
        return s.toString();
    }

    byte[] getBuffer(byte action) {
        buffer[ACTION_POS] = action;
        return buffer;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getKey() {
        return key;
    }
}
