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
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.DissociatableManagedConnection;
import javax.security.auth.Subject;

import java.util.Collection;
import java.util.Set;
import java.util.Iterator;

/**
 * MetaCallConnectionInterceptor.java handles communication with the
 * CachedConnectionManager.  On method call entry, cached handles are
 * checked for the correct Subject.  On method call exit, cached
 * handles are disassociated if possible. On getting or releasing
 * a connection the CachedConnectionManager is notified.
 *
 *
 * Created: Fri Oct 10 19:14:16 2003
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/13 22:22:30 $
 */
public class MetaCallConnectionInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;
    private final String jndiName;
    private final CachedConnectionManager ccm;
    private final SecurityDomain securityDomain;

    public MetaCallConnectionInterceptor(
            final ConnectionInterceptor next,
            final String jndiName,
            final CachedConnectionManager ccm,
            final SecurityDomain securityDomain) {
        this.next = next;
        this.jndiName = jndiName;
        this.ccm = ccm;
        this.securityDomain = securityDomain;
    }

    public void getConnection(ConnectionInfo ci) throws ResourceException {
        next.getConnection(ci);
        ccm.handleObtained(this, ci);
    }

    public void returnConnection(
            ConnectionInfo ci,
            ConnectionReturnAction cra) {
        ccm.handleReleased(this, ci);
        next.returnConnection(ci, cra);
    }

    public void enter(Collection connectionInfos, Set unshareable)
            throws ResourceException {
        if (unshareable.contains(jndiName)) {
            //should probably check to see if subjects are consistent,
            //and if not raise an exception.  Also need to check if
            //the spec says anything about this.
            return;
        } // end of if ()
        if (securityDomain == null) {
            return;
        }
        Subject currentSubject = null;
        try {
            currentSubject = securityDomain.getSubject();
        } catch (SecurityException e) {
            throw new ResourceException("Can not obtain Subject for login", e);
        } // end of try-catch
        if (currentSubject == null) {
            //check to see if mci.getSubject() is null?
            return;
        } // end of if ()
        for (Iterator i = connectionInfos.iterator(); i.hasNext();) {
            ConnectionInfo ci = (ConnectionInfo) i.next();
            ManagedConnectionInfo mci = ci.getManagedConnectionInfo();
            //Is this check correct?  perhaps more credentials got
            //added without changing the relevant credential we use.
            if (!currentSubject.equals(mci.getSubject())) {
                //make a ci to process removing the handle from the old mc
                ConnectionInfo oldCI = new ConnectionInfo();
                oldCI.setManagedConnectionInfo(mci);
                oldCI.setConnectionHandle(ci.getConnectionHandle());

                //make a new mci for the mc we will ask for
                ManagedConnectionInfo newMCI =
                        new ManagedConnectionInfo(
                                mci.getManagedConnectionFactory(),
                                mci.getConnectionRequestInfo());
                newMCI.setSubject(currentSubject);
                ci.setManagedConnectionInfo(newMCI);
                next.getConnection(ci);
                //process the removal of the handle from the previous mc
                returnConnection(oldCI, ConnectionReturnAction.RETURN_HANDLE);
            } // end of if ()
        } // end of for ()

    }

    public void exit(Collection connectionInfos, Set unshareableResources)
            throws ResourceException {
        if (unshareableResources.contains(jndiName)) {
            return;
        } // end of if ()
        for (Iterator i = connectionInfos.iterator(); i.hasNext();) {
            ConnectionInfo ci = (ConnectionInfo) i.next();
            ManagedConnectionInfo mci = ci.getManagedConnectionInfo();
            ManagedConnection mc = mci.getManagedConnection();
            if (mc instanceof DissociatableManagedConnection) {
                i.remove();
                ((DissociatableManagedConnection) mc).dissociateConnections();
                mci.clearConnectionHandles();
                returnConnection(ci, ConnectionReturnAction.RETURN_HANDLE);
            } // end of if ()
        }
    }
} // MetaCallConnectionInterceptor
