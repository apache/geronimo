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

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.transaction.xa.Xid;

/**
 * Factory for transaction ids.
 * The Xid is constructed of three parts:
 * <ol><li>8 byte count (LSB first)</li>
 * <li>4 byte system id</li>
 * <li>4 or 16 byte IP address of host</li>
 * <ol>
 * @version $Revision: 1.2 $ $Date: 2004/02/23 20:28:43 $
 * todo Should have a way of setting baseId
 */
public class XidFactory {
    byte[] baseId = new byte[Xid.MAXGTRIDSIZE];
    long count = 1;

    public XidFactory() {
        byte[] hostid;
        try {
            hostid = InetAddress.getLocalHost().getAddress();
        } catch (UnknownHostException e) {
            hostid = new byte[]{127, 0, 0, 1};
        }
        int uid = System.identityHashCode(this);
        baseId[8] = (byte) uid;
        baseId[9] = (byte) (uid >>> 8);
        baseId[10] = (byte) (uid >>> 16);
        baseId[11] = (byte) (uid >>> 24);
        System.arraycopy(hostid, 0, baseId, 12, hostid.length);
    }

    public Xid createXid() {
        byte[] globalId = (byte[]) baseId.clone();
        long id;
        synchronized (this) {
            id = count++;
        }
        globalId[0] = (byte) id;
        globalId[1] = (byte) (id >>> 8);
        globalId[2] = (byte) (id >>> 16);
        globalId[3] = (byte) (id >>> 24);
        globalId[4] = (byte) (id >>> 32);
        globalId[5] = (byte) (id >>> 40);
        globalId[6] = (byte) (id >>> 48);
        globalId[7] = (byte) (id >>> 56);
        return new XidImpl(globalId);
    }

    public Xid createBranch(Xid globalId, int branch) {
        byte[] branchId = (byte[]) baseId.clone();
        branchId[0] = (byte) branch;
        branchId[1] = (byte) (branch >>> 8);
        branchId[2] = (byte) (branch >>> 16);
        branchId[3] = (byte) (branch >>> 24);
        return new XidImpl(globalId, branchId);
    }
}
