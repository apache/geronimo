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

package org.apache.geronimo.transaction.manager;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.transaction.xa.Xid;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Factory for transaction ids.
 * The Xid is constructed of three parts:
 * <ol><li>8 byte count (LSB first)</li>
 * <li>4 byte system id</li>
 * <li>4 or 16 byte IP address of host</li>
 * <ol>
 * @version $Rev$ $Date$
 * todo Should have a way of setting baseId
 */
public class XidFactoryImpl implements XidFactory {
    private final byte[] baseId = new byte[Xid.MAXGTRIDSIZE];
    private long count = 1;

    public XidFactoryImpl(byte[] tmId) {
       System.arraycopy(tmId, 0, baseId, 8, tmId.length);
    }

    public XidFactoryImpl() {
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

    public boolean matchesGlobalId(byte[] globalTransactionId) {
        if (globalTransactionId.length != Xid.MAXGTRIDSIZE) {
            return false;
        }
        for (int i = 8; i < globalTransactionId.length; i++) {
            if (globalTransactionId[i] != baseId[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesBranchId(byte[] branchQualifier) {
        if (branchQualifier.length != Xid.MAXBQUALSIZE) {
            return false;
        }
        for (int i = 8; i < branchQualifier.length; i++) {
            if (branchQualifier[i] != baseId[i]) {
                return false;
            }
        }
        return true;
    }

    public Xid recover(int formatId, byte[] globalTransactionid, byte[] branchQualifier) {
        return new XidImpl(formatId, globalTransactionid, branchQualifier);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(XidFactoryImpl.class);
        infoFactory.addInterface(XidFactory.class);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
