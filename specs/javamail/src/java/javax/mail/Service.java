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

package javax.mail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MailEvent;
import javax.mail.event.TransportListener;

/**
 * @version $Rev$ $Date$
 */
public abstract class Service {
    private boolean _connected;
    private List _connectionListeners = new LinkedList();
    protected boolean debug;
    protected Session session;
    protected URLName url;

    protected Service(Session session, URLName url) {
        this.session = session;
        this.url = url;
    }

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
                retry = !protocolConnect(host, port, user, password);
            } catch (AuthenticationFailedException e) {
                // TODO I18N
                try {
                    PasswordAuthentication pa =
                            session.requestPasswordAuthentication(InetAddress.getByName(host),
                                    port,
                                    null,
                                    "Please enter your password",
                                    user);
                    password = pa.getPassword();
                    user = pa.getUserName();
                } catch (UnknownHostException uhe) {
                    throw new MessagingException(uhe.toString());
                }
            }
        }
        setConnected(true);
        // Either the provider will implement getURL, or it will have already set it using setURL.
        // In either case, this is safe.
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

    protected boolean protocolConnect(String host,
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
