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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 18:54:16 $
 */
public class MockResource implements XAResource {
    private Xid xid;
    private MockResourceManager manager;
    private int timeout = 0;

    public MockResource(MockResourceManager manager) {
        this.manager = manager;
    }

    public int getTransactionTimeout() throws XAException {
        return timeout;
    }

    public boolean setTransactionTimeout(int seconds) throws XAException {
        return false;
    }

    public Xid getXid() {
        return xid;
    }

    public void start(Xid xid, int flags) throws XAException {
        if (this.xid != null) {
            throw new XAException(XAException.XAER_PROTO);
        }
        if ((flags & XAResource.TMJOIN) != 0) {
            manager.join(xid, this);
        } else {
            manager.newTx(xid, this);
        }
        this.xid = xid;
    }

    public void end(Xid xid, int flags) throws XAException {
        if (this.xid != xid) {
            throw new XAException(XAException.XAER_INVAL);
        }
        this.xid = null;
    }

    public int prepare(Xid xid) throws XAException {
        return 0;
    }

    public void commit(Xid xid, boolean onePhase) throws XAException {
    }

    public void rollback(Xid xid) throws XAException {
        manager.forget(xid, this);
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        if (xaResource instanceof MockResource) {
            return manager == ((MockResource) xaResource).manager;
        }
        return false;
    }

    public void forget(Xid xid) throws XAException {
        throw new UnsupportedOperationException();
    }

    public Xid[] recover(int flag) throws XAException {
        throw new UnsupportedOperationException();
    }

}
