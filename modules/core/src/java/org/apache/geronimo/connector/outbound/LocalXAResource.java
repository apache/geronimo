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

package org.apache.geronimo.connector.outbound;

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * LocalXAResource adapts a local transaction to be controlled by a
 * JTA transaction manager.  Of course, it cannot provide xa
 * semantics.
 *
 *
 * Created: Tue Sep 30 00:32:44 2003
 *
 * @version 1.0
 */
public class LocalXAResource implements XAResource {

    private final LocalTransaction localTx;
    private Xid xid;
    private int txTimeout;

    public LocalXAResource(LocalTransaction localTx) {
        this.localTx = localTx;
    } // LocalXAResource constructor

    // Implementation of javax.transaction.xa.XAResource

    /**
     * The <code>commit</code> method
     *
     * @param xid a <code>Xid</code> value
     * @param flag a <code>boolean</code> value
     * @exception XAException if an error occurs
     */
    public void commit(Xid xid, boolean flag) throws XAException {
        if (this.xid == null || !this.xid.equals(xid)) {
            throw new XAException();
        } // end of if ()
        try {
            localTx.commit();
        } catch (ResourceException e) {
            XAException xae = new XAException();
            //xae.setLinkedException(e);
            throw xae;
        } // end of try-catch
        finally {
            this.xid = null;
        } // end of finally

    }

    /**
     * The <code>forget</code> method
     *
     * @param xid a <code>Xid</code> value
     * @exception XAException if an error occurs
     */
    public void forget(Xid xid) throws XAException {
        this.xid = null;
    }

    /**
     * The <code>getTransactionTimeout</code> method
     *
     * @return an <code>int</code> value
     * @exception XAException if an error occurs
     */
    public int getTransactionTimeout() throws XAException {
        return txTimeout;
    }

    /**
     * The <code>isSameRM</code> method
     *
     * @param XAResource a <code>XAResource</code> value
     * @return a <code>boolean</code> value
     * @exception XAException if an error occurs
     */
    public boolean isSameRM(XAResource xares) throws XAException {
        return this == xares;
    }

    /**
     * The <code>recover</code> method
     *
     * @param n an <code>int</code> value
     * @return a <code>Xid[]</code> value
     * @exception XAException if an error occurs
     */
    public Xid[] recover(int n) throws XAException {
        return null;
    }

    /**
     * The <code>rollback</code> method
     *
     * @param xid a <code>Xid</code> value
     * @exception XAException if an error occurs
     */
    public void rollback(Xid xid) throws XAException {
        if (this.xid == null || !this.xid.equals(xid)) {
            throw new XAException();
        } // end of if ()
        try {
            localTx.rollback();
        } catch (ResourceException e) {
            XAException xae = new XAException();
            //xae.setLinkedException(e);
            throw xae;
        } // end of try-catch
        finally {
            this.xid = null;
        } // end of finally

    }

    /**
     * The <code>setTransactionTimeout</code> method
     *
     * @param n an <code>int</code> value
     * @return a <code>boolean</code> value
     * @exception XAException if an error occurs
     */
    public boolean setTransactionTimeout(int txTimeout) throws XAException {
        this.txTimeout = txTimeout;
        return true;
    }

    /**
     * The <code>start</code> method
     *
     * @param xid a <code>Xid</code> value
     * @param n an <code>int</code> value
     * @exception XAException if an error occurs
     */
    public void start(Xid xid, int flag) throws XAException {
        if (flag == XAResource.TMNOFLAGS) {
            if (xid != null) {
                throw new XAException();
            } // end of if ()
            this.xid = xid;
            try {
                localTx.begin();
            } catch (ResourceException e) {
                throw new XAException(); //"could not start local tx", e);
            } // end of try-catch

        } // end of if ()
        if (flag == XAResource.TMRESUME && xid != this.xid) {
            throw new XAException();
        } // end of if ()
        throw new XAException("unknown state");
    }

    /**
     * The <code>end</code> method
     *
     * @param xid a <code>Xid</code> value
     * @param n an <code>int</code> value
     * @exception XAException if an error occurs
     */
    public void end(Xid xid, int flag) throws XAException {
        if (xid != this.xid) {
            throw new XAException();
        } // end of if ()
        //we could keep track of if the flag is TMSUCCESS...
    }

    /**
     * The <code>prepare</code> method
     *
     * @param xid a <code>Xid</code> value
     * @return an <code>int</code> value
     * @exception XAException if an error occurs
     */
    public int prepare(Xid xid) throws XAException {
        //log warning that semantics are incorrect...
        return XAResource.XA_OK;
    }

} // LocalXAResource
