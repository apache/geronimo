/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.transaction.manager;

import java.io.Serializable;
import java.util.Arrays;
import javax.transaction.xa.Xid;

/**
 * Unique id for a transaction.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/23 20:28:43 $
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
