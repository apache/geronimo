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

import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * ConnectionRequest.java
 *
 *
 * Created: Thu Sep 25 14:29:07 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class ManagedConnectionInfo {

	private ManagedConnectionFactory mcf;
	private ConnectionRequestInfo cri;
	private Subject subject;
	private Transaction tx;
	private ManagedConnection mc;
	private XAResource xares;
	private long lastUsed;
	private ConnectionInterceptor poolInterceptor;

	private GeronimoConnectionEventListener listener;

	public ManagedConnectionInfo(
		ManagedConnectionFactory mcf,
		ConnectionRequestInfo cri) {
		this.mcf = mcf;
		this.cri = cri;
	} // ManagedConnectionInfo constructor

	/**
	 * Get the Mcf value.
	 * @return the Mcf value.
	 */
	public ManagedConnectionFactory getManagedConnectionFactory() {
		return mcf;
	}

	/**
	 * Set the Mcf value.
	 * @param newMcf The new Mcf value.
	 */
	public void setManagedConnectionFactory(ManagedConnectionFactory mcf) {
		this.mcf = mcf;
	}

	/**
	 * Get the Cri value.
	 * @return the Cri value.
	 */
	public ConnectionRequestInfo getConnectionRequestInfo() {
		return cri;
	}

	/**
	 * Set the Cri value.
	 * @param newCri The new Cri value.
	 */
	public void setConnectionRequestInfo(ConnectionRequestInfo cri) {
		this.cri = cri;
	}

	/**
	 * Get the Subject value.
	 * @return the Subject value.
	 */
	public Subject getSubject() {
		return subject;
	}

	/**
	 * Set the Subject value.
	 * @param newSubject The new Subject value.
	 */
	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	/**
	 * Get the Tx value.
	 * @return the Tx value.
	 */
	public Transaction getTransaction() {
		return tx;
	}

	/**
	 * Set the Tx value.
	 * @param newTx The new Tx value.
	 */
	public void setTransaction(Transaction tx) {
		this.tx = tx;
	}

	/**
	 * Get the Mc value.
	 * @return the Mc value.
	 */
	public ManagedConnection getManagedConnection() {
		return mc;
	}

	/**
	 * Set the Mc value.
	 * @param newMc The new Mc value.
	 */
	public void setManagedConnection(ManagedConnection mc) {
		this.mc = mc;
	}

	/**
	 * Get the Xares value.
	 * @return the Xares value.
	 */
	public XAResource getXAResource() {
		return xares;
	}

	/**
	 * Set the Xares value.
	 * @param newXares The new Xares value.
	 */
	public void setXAResource(XAResource xares) {
		this.xares = xares;
	}

	public long getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(long lastUsed) {
		this.lastUsed = lastUsed;
	}

	public void setPoolInterceptor(ConnectionInterceptor poolInterceptor) {
		this.poolInterceptor = poolInterceptor;
	}

	public ConnectionInterceptor getPoolInterceptor() {
		return poolInterceptor;
	}

	public void setConnectionEventListener(GeronimoConnectionEventListener listener) {
		this.listener = listener;
	}

	public void addConnectionHandle(Object handle) {
		listener.addConnectionHandle(handle);
	}

	public void removeConnectionHandle(Object handle) {
		listener.removeConnectionHandle(handle);
	}

	public boolean hasConnectionHandles() {
		return listener.hasConnectionHandles();
	}

	public void clearConnectionHandles() {
		listener.clearConnectionHandles();
	}

	public boolean securityMatches(ManagedConnectionInfo other) {
		return (
			subject == null
				? other.getSubject() == null
				: subject.equals(other.getSubject()))
			&& (cri == null
				? other.getConnectionRequestInfo() == null
				: cri.equals(other.getConnectionRequestInfo()));
	}

} // ManagedConnectionInfo
