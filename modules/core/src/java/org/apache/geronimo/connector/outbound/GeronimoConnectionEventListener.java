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

import java.util.HashSet;
import java.util.Set;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;

/**
 * ConnectionEventListener.java
 *
 *
 * Created: Thu Oct  2 14:57:43 2003
 *
 * @version 1.0
 */
public class GeronimoConnectionEventListener
	implements ConnectionEventListener {

	private final ManagedConnectionInfo mci;
	private final ConnectionInterceptor stack;
	private final Set connectionHandles = new HashSet();

	public GeronimoConnectionEventListener(
		final ConnectionInterceptor stack,
		final ManagedConnectionInfo mci) {
		this.stack = stack;
		this.mci = mci;
	} // ConnectionEventListener constructor

	/**
	 * The <code>connectionClosed</code> method
	 *
	 * @param connectionEvent a <code>ConnectionEvent</code> value
	 */
	public void connectionClosed(ConnectionEvent connectionEvent) {
		if (connectionEvent.getSource() != mci.getManagedConnection()) {
			throw new IllegalArgumentException(
				"ConnectionClosed event received from wrong ManagedConnection. Expected "
					+ mci.getManagedConnection()
					+ ", actual "
					+ connectionEvent.getSource());
		} // end of if ()
		ConnectionInfo ci = new ConnectionInfo(mci);
		ci.setConnectionHandle(connectionEvent.getConnectionHandle());
		stack.returnConnection(ci, ConnectionReturnAction.RETURN_HANDLE);
	}

	/**
	 * The <code>connectionErrorOccurred</code> method
	 *
	 * @param connectionEvent a <code>ConnectionEvent</code> value
	 */
	public void connectionErrorOccurred(ConnectionEvent connectionEvent) {
		if (connectionEvent.getSource() != mci.getManagedConnection()) {
			throw new IllegalArgumentException(
				"ConnectionError event received from wrong ManagedConnection. Expected "
					+ mci.getManagedConnection()
					+ ", actual "
					+ connectionEvent.getSource());
		} // end of if ()
		ConnectionInfo ci = new ConnectionInfo(mci);
		ci.setConnectionHandle(connectionEvent.getConnectionHandle());
		stack.returnConnection(ci, ConnectionReturnAction.DESTROY);
	}

	/**
	 * The <code>localTransactionStarted</code> method
	 *
	 * @param event a <code>ConnectionEvent</code> value
	 * @todo implement this method
	 */
	public void localTransactionStarted(ConnectionEvent event) {
	}

	/**
	 * The <code>localTransactionCommitted</code> method
	 *
	 * @param event a <code>ConnectionEvent</code> value
	 * @todo implement this method
	 */
	public void localTransactionCommitted(ConnectionEvent event) {
	}

	/**
	 * The <code>localTransactionRolledback</code> method
	 *
	 * @param event a <code>ConnectionEvent</code> value
	 * @todo implement this method
	 */
	public void localTransactionRolledback(ConnectionEvent event) {
	}

	public void addConnectionHandle(Object handle) {
		connectionHandles.add(handle);
	}

	public void removeConnectionHandle(Object handle) {
		connectionHandles.remove(handle);
	}

	public boolean hasConnectionHandles() {
		return !connectionHandles.isEmpty();
	}

	public void clearConnectionHandles() {
		connectionHandles.clear();
	}

} // ConnectionEventListener
