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

package org.apache.geronimo.javamail.authentication;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;

public class LoginAuthenticator implements ClientAuthenticator {

    // constants for the authentication stages
    protected static final int USERNAME = 0;

    protected static final int PASSWORD = 1;

    protected static final int COMPLETE = 2;

    // the user we're authenticating
    protected String username;

    // the user's password (the "shared secret")
    protected String password;

    // indicates whether we've gone through the entire challenge process.
    protected int stage = USERNAME;

    /**
     * Main constructor.
     * 
     * @param username
     *            The login user name.
     * @param password
     *            The login password.
     */
    public LoginAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Respond to the hasInitialResponse query. This mechanism does not have an
     * initial response.
     * 
     * @return Always returns false;
     */
    public boolean hasInitialResponse() {
        return false;
    }

    /**
     * Indicate whether the challenge/response process is complete.
     * 
     * @return True if the last challenge has been processed, false otherwise.
     */
    public boolean isComplete() {
        return stage == COMPLETE;
    }

    /**
     * Retrieve the authenticator mechanism name.
     * 
     * @return Always returns the string "LOGIN"
     */
    public String getMechanismName() {
        return "LOGIN";
    }

    /**
     * Evaluate a PLAIN login challenge, returning the a result string that
     * should satisfy the clallenge.
     * 
     * @param challenge
     *            The decoded challenge data, as a byte array
     * 
     * @return A formatted challege response, as an array of bytes.
     * @exception MessagingException
     */
    public byte[] evaluateChallenge(byte[] challenge) throws MessagingException {

        // process the correct stage for the challenge
        switch (stage) {
        // should never happen
        case COMPLETE:
            throw new MessagingException("Invalid LOGIN challenge");

        case USERNAME: {
            byte[] userBytes;

            try {
                // get the username and password in an UTF-8 encoding to create
                // the token
                userBytes = username.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                // got an error, fail this (this should never happen).
                throw new MessagingException("Invalid encoding");
            }

            // next time through we're looking for a password.
            stage = PASSWORD;
            // the user bytes are the entire challenge respose.
            return userBytes;
        }

        case PASSWORD: {
            byte[] passBytes;

            try {
                // get the username and password in an UTF-8 encoding to create
                // the token
                passBytes = password.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                // got an error, fail this (this should never happen).
                throw new MessagingException("Invalid encoding");
            }
            // we're finished
            stage = COMPLETE;
            return passBytes;
        }
        }
        // should never get here.
        throw new MessagingException("Invalid LOGIN challenge");
    }
}
