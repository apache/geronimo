/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.pop3;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

/**
 * POP3 implementation of javax.mail.Store POP protocol spec is implemented in
 * org.apache.geronimo.javamail.store.pop3.POP3Connection
 * 
 * @version $Rev$ $Date$
 */

public class POP3Store extends Store {

    private POP3Connection pop3Con;

    public POP3Store(Session session, URLName urlName) {
        super(session, urlName);
    }

    /**
     * @see javax.mail.Store#getDefaultFolder()
     * 
     * There is only INBOX supported in POP3 so the default folder is inbox
     */
    public Folder getDefaultFolder() throws MessagingException {
        return getFolder("INBOX");
    }

    /**
     * @see javax.mail.Store#getFolder(java.lang.String)
     */
    public Folder getFolder(String name) throws MessagingException {

        checkConnectionStatus();

        if (!"INBOX".equalsIgnoreCase(name)) {
            throw new MessagingException("Only INBOX is supported in POP3");
        }
        return new POP3Folder(this, session, pop3Con);
    }

    /**
     * @see javax.mail.Store#getFolder(javax.mail.URLName)
     */
    public Folder getFolder(URLName url) throws MessagingException {
        return getFolder(url.getFile());
    }

    /**
     * @see javax.mail.Service#protocolConnect(java.lang.String, int,
     *      java.lang.String, java.lang.String)
     */
    protected synchronized boolean protocolConnect(String host, int portNum, String user, String passwd)
            throws MessagingException {

        // Never store the user, passwd for security reasons

        // if these values are null, no connection attempt should be made
        if (host == null || passwd == null || user == null) {
            return false;
        }

        // validate port num
        if (portNum < 1) {
            String portstring = session.getProperty("mail.pop3.port");
            if (portstring != null) {
                try {
                    portNum = Integer.parseInt(portstring);
                } catch (NumberFormatException e) {
                    portNum = 110;
                }
            }
        }

        /*
         * Obtaining a connection to the server.
         * 
         */
        pop3Con = new POP3Connection(this.session, host, portNum);
        try {
            pop3Con.open();
        } catch (Exception e) {
            throw new MessagingException("Connection failed", e);
        }

        /*
         * Sending the USER command with username
         * 
         */
        POP3Response resUser = null;
        try {
            resUser = pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_USER(user));
        } catch (Exception e) {
            throw new MessagingException("Connection failed", e);
        }

        if (POP3Constants.ERR == resUser.getStatus()) {

            /*
             * Authentication failed so sending QUIT
             * 
             */
            try {
                pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_QUIT());
            } catch (Exception e) {
                // We don't care about the response or if any error happens
                // just trying to comply with the spec.
                // Most likely the server would have terminated the connection
                // by now.
            }

            throw new AuthenticationFailedException(resUser.getFirstLine());
        }

        /*
         * Sending the PASS command with password
         * 
         */
        POP3Response resPwd = null;
        try {
            resPwd = pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_PASS(passwd));
        } catch (Exception e) {
            throw new MessagingException("Connection failed", e);
        }

        if (POP3Constants.ERR == resPwd.getStatus()) {

            /*
             * Authentication failed so sending QUIT
             * 
             */
            try {
                pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_QUIT());
            } catch (Exception e) {
                // We don't care about the response or if any error happens
                // just trying to comply with the spec.
                // Most likely the server would have terminated the connection
                // by now.
            }

            throw new AuthenticationFailedException(resPwd.getFirstLine());
        }

        return true;
    }

    /**
     * @see javax.mail.Service#isConnected()
     */
    public boolean isConnected() {
        POP3Response res = null;
        try {
            res = pop3Con.sendCommand(POP3CommandFactory.getCOMMAND_NOOP());
        } catch (Exception e) {
            return false;
        }

        return (POP3Constants.OK == res.getStatus());
    }

    /**
     * @see javax.mail.Service#close()
     */
    public void close() throws MessagingException {
        // This is done to ensure proper event notification.
        super.close();
        try {
            pop3Con.close();
        } catch (Exception e) {
            // A message is already set at the connection level
            // unfortuantely there is no constructor that takes only
            // the root exception
            new MessagingException("", e);
        }
    }

    private void checkConnectionStatus() throws MessagingException {
        if (!this.isConnected()) {
            throw new MessagingException("Not connected ");
        }
    }
}
