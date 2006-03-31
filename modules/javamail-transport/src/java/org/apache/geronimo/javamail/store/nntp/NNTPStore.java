/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.nntp;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import org.apache.geronimo.javamail.store.nntp.newsrc.NNTPNewsrc;
import org.apache.geronimo.javamail.store.nntp.newsrc.NNTPNewsrcFile;
import org.apache.geronimo.javamail.store.nntp.newsrc.NNTPNewsrcGroup;
import org.apache.geronimo.javamail.transport.nntp.NNTPConnection;
import org.apache.geronimo.mail.util.SessionUtil;

/**
 * NNTP implementation of javax.mail.Store POP protocol spec is implemented in
 * org.apache.geronimo.javamail.store.pop3.NNTPConnection
 * 
 * @version $Rev$ $Date$
 */
public class NNTPStore extends Store {

    protected static final String NNTP_AUTH = "auth";

    protected static final String NNTP_PORT = "port";

    protected static final String NNTP_NEWSRC = "newsrc";

    protected static final String protocol = "nntp";

    protected static final int DEFAULT_NNTP_PORT = 119;

    // the active connection object.
    protected NNTPConnection connection;

    // the newsrc file where we store subscriptions and seen message markers.
    protected NNTPNewsrc newsrc;

    // the root folder
    protected NNTPRootFolder root;

    // our session provided debug output stream.
    protected PrintStream debugStream;

    /**
     * Construct an NNTPStore item. This will load the .newsrc file associated
     * with the server.
     * 
     * @param session
     *            The owning javamail Session.
     * @param urlName
     *            The Store urlName, which can contain server target
     *            information.
     */
    public NNTPStore(Session session, URLName urlName) {
        super(session, urlName);

        // get our debug output.
        debugStream = session.getDebugOut();

    }

    /**
     * @see javax.mail.Store#getDefaultFolder()
     * 
     * This returns a root folder object for all of the news groups.
     */
    public Folder getDefaultFolder() throws MessagingException {
        checkConnectionStatus();
        if (root == null) {
            return new NNTPRootFolder(this, connection.getHost(), connection.getWelcomeString());
        }
        return root;
    }

    /**
     * @see javax.mail.Store#getFolder(java.lang.String)
     */
    public Folder getFolder(String name) throws MessagingException {
        return getDefaultFolder().getFolder(name);
    }

    /**
     * 
     * @see javax.mail.Store#getFolder(javax.mail.URLName)
     */
    public Folder getFolder(URLName url) throws MessagingException {
        return getDefaultFolder().getFolder(url.getFile());
    }

    /**
     * @see javax.mail.Service#protocolConnect(java.lang.String, int,
     *      java.lang.String, java.lang.String)
     */
    protected synchronized boolean protocolConnect(String host, int port, String username, String password)
            throws MessagingException {
        if (debug) {
            debugOut("Connecting to server " + host + ":" + port + " for user " + username);
        }

        // first check to see if we need to authenticate. If we need this, then
        // we must have a username and
        // password specified. Failing this may result in a user prompt to
        // collect the information.
        boolean mustAuthenticate = getBooleanProperty(NNTP_AUTH, false);

        // if we need to authenticate, and we don't have both a userid and
        // password, then we fail this
        // immediately. The Service.connect() method will try to obtain the user
        // information and retry the
        // connection one time.
        if (mustAuthenticate && (username == null || password == null)) {
            return false;
        }

        // if the port is defaulted, then see if we have something configured in
        // the session.
        // if not configured, we just use the default default.
        if (port == -1) {
            // check for a property and fall back on the default if it's not
            // set.
            port = getIntProperty(NNTP_PORT, DEFAULT_NNTP_PORT);
        }

        // create socket and connect to server.
        connection = new NNTPConnection(protocol, session, host, port, username, password, debug);
        connection.connect();

        // see if we have a newsrc file location specified
        String newsrcFile = getProperty(NNTP_NEWSRC);

        File source = null;

        // not given as a property? Then look for a file in user.home
        if (newsrcFile != null) {
            source = new File(newsrcFile);
        } else {
            // ok, look for a file in the user.home directory. If possible,
            // we'll try for a file
            // with the hostname appended.
            String home = SessionUtil.getProperty("user.home");

            // try for a host-specific file first. If not found, use (and
            // potentially create) a generic
            // .newsrc file.
            newsrcFile = ".newsrc-" + host;
            source = new File(home, newsrcFile);
            if (!source.exists()) {
                source = new File(home, ".newsrc");
            }
        }

        // now create a newsrc read and load the file.
        newsrc = new NNTPNewsrcFile(source);
        newsrc.load();

        // we're going to return success here, but in truth, the server may end
        // up asking for our
        // bonafides at any time, and we'll be expected to authenticate then.
        return true;
    }

    /**
     * @see javax.mail.Service#close()
     */
    public void close() throws MessagingException {
        // This is done to ensure proper event notification.
        super.close();
        // persist the newsrc file, if possible
        newsrc.close();
        connection.close();
        connection = null;
    }

    private void checkConnectionStatus() throws MessagingException {
        if (!this.isConnected()) {
            throw new MessagingException("Not connected ");
        }
    }

    /**
     * Internal debug output routine.
     * 
     * @param value
     *            The string value to output.
     */
    void debugOut(String message) {
        debugStream.println("NNTPTransport DEBUG: " + message);
    }

    /**
     * Internal debugging routine for reporting exceptions.
     * 
     * @param message
     *            A message associated with the exception context.
     * @param e
     *            The received exception.
     */
    void debugOut(String message, Throwable e) {
        debugOut("Received exception -> " + message);
        debugOut("Exception message -> " + e.getMessage());
        e.printStackTrace(debugStream);
    }

    /**
     * Retrieve the server connection created by this store.
     * 
     * @return The active connection object.
     */
    NNTPConnection getConnection() {
        return connection;
    }

    /**
     * Retrieve the Session object this Store is operating under.
     * 
     * @return The attached Session instance.
     */
    Session getSession() {
        return session;
    }

    /**
     * Retrieve all of the groups we nave persistent store information about.
     * 
     * @return The set of groups contained in the newsrc file.
     */
    Iterator getNewsrcGroups() {
        return newsrc.getGroups();
    }

    /**
     * Retrieve the newsrc group information for a named group. If the file does
     * not currently include this group, an unsubscribed group will be added to
     * the file.
     * 
     * @param name
     *            The name of the target group.
     * 
     * @return The NNTPNewsrcGroup item corresponding to this name.
     */
    NNTPNewsrcGroup getNewsrcGroup(String name) {
        return newsrc.getGroup(name);
    }

    /**
     * Get a property associated with this mail protocol.
     * 
     * @param name
     *            The name of the property.
     * 
     * @return The property value (returns null if the property has not been
     *         set).
     */
    String getProperty(String name) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "nntp" or "nntp-post").
        String fullName = "mail." + protocol + "." + name;
        return session.getProperty(fullName);
    }

    /**
     * Get a property associated with this mail session. Returns the provided
     * default if it doesn't exist.
     * 
     * @param name
     *            The name of the property.
     * @param defaultValue
     *            The default value to return if the property doesn't exist.
     * 
     * @return The property value (returns defaultValue if the property has not
     *         been set).
     */
    String getProperty(String name, String defaultValue) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "nntp" or "nntp-post").
        String fullName = "mail." + protocol + "." + name;
        return SessionUtil.getProperty(session, fullName, defaultValue);
    }

    /**
     * Get a property associated with this mail session as an integer value.
     * Returns the default value if the property doesn't exist or it doesn't
     * have a valid int value.
     * 
     * @param name
     *            The name of the property.
     * @param defaultValue
     *            The default value to return if the property doesn't exist.
     * 
     * @return The property value converted to an int.
     */
    int getIntProperty(String name, int defaultValue) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "nntp" or "nntp-post").
        String fullName = "mail." + protocol + "." + name;
        return SessionUtil.getIntProperty(session, fullName, defaultValue);
    }

    /**
     * Get a property associated with this mail session as an boolean value.
     * Returns the default value if the property doesn't exist or it doesn't
     * have a valid int value.
     * 
     * @param name
     *            The name of the property.
     * @param defaultValue
     *            The default value to return if the property doesn't exist.
     * 
     * @return The property value converted to a boolean
     */
    boolean getBooleanProperty(String name, boolean defaultValue) {
        // the name we're given is the least qualified part of the name. We
        // construct the full property name
        // using the protocol (either "nntp" or "nntp-post").
        String fullName = "mail." + protocol + "." + name;
        return SessionUtil.getBooleanProperty(session, fullName, defaultValue);
    }
}
