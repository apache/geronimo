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
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MailEvent;
import javax.mail.event.TransportListener;
public abstract class Service {
    private boolean _connected;
    private List _connectionListeners = new LinkedList();
    protected boolean debug;
    protected Session session;
    protected URLName url;
    public void addConnectionListener(ConnectionListener listener) {
        _connectionListeners.add(listener);
    }
    public void close() throws MessagingException {
        // if we're not connected, ignore
        setConnected(false);
    }
    public void connect() throws MessagingException {
        String host = session.getProperty("mail.host");
        String user = session.getProperty("mail.user");
        connect(host, -1, user, null);
    }
    public void connect(String host, int port, String user, String password)
        throws MessagingException {
        if (_connected) {
            throw new IllegalStateException("Already connected");
        }
        boolean retry = true;
        while (retry) {
            try {
                // TODO prompt the user for user/password somehow
                user = user;
                password = password;
                retry = !protocolConnect(host, port, user, password);
            } catch (AuthenticationFailedException e) {
                // IGNORE; go round loop again and will re-prompt
            }
        }
        setConnected(true);
        // don't know what the URL needs to be?
        setURLName(getURLName());
    }
    public void connect(String host, String user, String password)
        throws MessagingException {
        connect(host, -1, user, password);
    }
    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            close();
        }
    }
    public URLName getURLName() {
        return url;
    }
    public boolean isConnected() {
        return _connected;
    }
    protected void notifyConnectionListeners(int type) {
        ConnectionEvent event = new ConnectionEvent(this, type);
        Iterator it = _connectionListeners.iterator();
        while (it.hasNext()) {
            TransportListener listener = (TransportListener) it.next();
            event.dispatch(listener);
        }
    }
    protected boolean protocolConnect(
        String host,
        int port,
        String user,
        String password)
        throws MessagingException {
        return false;
    }
    protected void queueEvent(MailEvent event, Vector listeners) {
        Enumeration enumeration = listeners.elements();
        while (enumeration.hasMoreElements()) {
            Object element = enumeration.nextElement();
            event.dispatch(listeners);
        }
    }
    public void removeConnectionListener(ConnectionListener listener) {
        _connectionListeners.remove(listener);
    }
    protected void setConnected(boolean connected) {
        boolean old = _connected;
        _connected = connected;
        if (old != _connected) {
            if (connected) {
                notifyConnectionListeners(ConnectionEvent.OPENED);
            } else {
                notifyConnectionListeners(ConnectionEvent.CLOSED);
            }
        }
    }
    protected void setURLName(URLName url) {
        this.url = url;
    }
    public String toString() {
        if (url == null) {
            return super.toString();
        } else {
            return url.toString();
        }
    }
}
