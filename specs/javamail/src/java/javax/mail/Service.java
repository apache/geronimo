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

import java.util.Vector;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;
import javax.mail.event.MailEvent;

/**
 * @version $Rev$ $Date$
 */
public abstract class Service {
    /**
     * The session from which this service was created.
     */
    protected Session session;
    /**
     * The URLName of this service
     */
    protected URLName url;
    /**
     * Debug flag for this service, set from the Session's debug flag.
     */
    protected boolean debug;

    private boolean connected;
    private final Vector connectionListeners = new Vector(2);
    private final EventQueue queue = new EventQueue();

    /**
     * Construct a new Service.
     * @param session the session from which this service was created
     * @param url the URLName of this service
     */
    protected Service(Session session, URLName url) {
        this.session = session;
        this.url = url;
    }

    /**
     * A generic connect method that takes no parameters allowing subclasses
     * to implement an appropriate authentication scheme.
     * The default implementation calls <code>connect(null, null, null)</code>
     * @throws AuthenticationFailedException if authentication fails
     * @throws MessagingException for other failures
     */
    public void connect() throws MessagingException {
        connect(null, null, null);
    }

    /**
     * Connect to the specified host using a simple username/password authenticaion scheme
     * and the default port.
     * The default implementation calls <code>connect(host, -1, user, password)</code>
     *
     * @param host the host to connect to
     * @param user the user name
     * @param password the user's password
     * @throws AuthenticationFailedException if authentication fails
     * @throws MessagingException for other failures
     */
    public void connect(String host, String user, String password) throws MessagingException {
        connect(host, -1, user, password);
    }

    /**
     * Connect to the specified host at the specified port using a simple username/password authenticaion scheme.
     *
     * If this Service is already connected, an IllegalStateException is thrown.
     *
     * @param host the host to connect to
     * @param port the port to connect to; pass -1 to use the default for the protocol
     * @param user the user name
     * @param password the user's password
     * @throws AuthenticationFailedException if authentication fails
     * @throws MessagingException for other failures
     * @throws IllegalStateException if this service is already connected
     */
    public void connect(String host, int port, String user, String password) throws MessagingException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }

        // todo figure out what this is really meant to do
        // todo get default
        boolean connected = protocolConnect(host, port, user, password);
        if (!connected) {
            // todo get info from session
            connected = protocolConnect(host, port, user, password);
            // todo call setURLName
            // todo save password if obtained from user
        }
        setConnected(connected);
        notifyConnectionListeners(ConnectionEvent.OPENED);
    }

    /**
     * Attempt the protocol-specific connection; subclasses should override this to establish
     * a connection in the appropriate manner.
     *
     * This method should return true if the connection was established.
     * It may return false to cause the {@link #connect(String, int, String, String)} method to
     * reattempt the connection after trying to obtain user and password information from the user.
     * Alternatively it may throw a AuthenticatedFailedException to abandon the conection attempt.
     *
     * @param host
     * @param port
     * @param user
     * @param password
     * @return
     * @throws AuthenticationFailedException if authentication fails
     * @throws MessagingException for other failures
     */
    protected boolean protocolConnect(String host, int port, String user, String password) throws MessagingException {
        return false;
    }

    /**
     * Check if this service is currently connected.
     * The default implementation simply returns the value of a private boolean field;
     * subclasses may wish to override this method to verify the physical connection.
     *
     * @return true if this service is connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Notification to subclasses that the connection state has changed.
     * This method is called by the connect() and close() methods to indicate state change;
     * subclasses should also call this method if the connection is automatically closed
     * for some reason.
     *
     * @param connected the connection state
     */
    protected void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Close this service and terminate its physical connection.
     * The default implementation simply calls setConnected(false) and then
     * sends a CLOSED event to all registered ConnectionListeners.
     * Subclasses overriding this method should still ensure it is closed; they should
     * also ensure that it is called if the connection is closed automatically, for
     * for example in a finalizer.
     *
     *@throws MessagingException if there were errors closing; the connection is still closed
     */
    public void close() throws MessagingException {
        setConnected(false);
        notifyConnectionListeners(ConnectionEvent.CLOSED);
    }

    /**
     * Return a copy of the URLName representing this service with the password and file information removed.
     *
     * @return the URLName for this service
     */
    public URLName getURLName() {

        return url == null ? null : new URLName(url.getProtocol(), url.getHost(), url.getPort(), null, url.getUsername(), null);
    }

    /**
     * Set the url field.
     * @param url the new value
     */
    protected void setURLName(URLName url) {
        this.url = url;
    }

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    protected void notifyConnectionListeners(int type) {
        queue.queueEvent(new ConnectionEvent(this, type), connectionListeners);
    }

    public String toString() {
        return url == null ? super.toString() : url.toString();
    }

    protected void queueEvent(MailEvent event, Vector listeners) {
        queue.queueEvent(event, listeners);
    }

    protected void finalize() throws Throwable {
        queue.stop();
        connectionListeners.clear();
        super.finalize();
    }
}
