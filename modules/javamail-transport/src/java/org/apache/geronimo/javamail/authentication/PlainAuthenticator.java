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

public class PlainAuthenticator implements ClientAuthenticator {

    // the user we're authenticating
    protected String username;

    // the user's password (the "shared secret")
    protected String password;

    // indicates whether we've gone through the entire challenge process.
    protected boolean complete = false;

    /**
     * Main constructor.
     * 
     * @param username
     *            The login user name.
     * @param password
     *            The login password.
     */
    public PlainAuthenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Respond to the hasInitialResponse query. This mechanism does have an
     * initial response, which is the entire challenge sequence.
     * 
     * @return Always returns true.
     */
    public boolean hasInitialResponse() {
        return true;
    }

    /**
     * Indicate whether the challenge/response process is complete.
     * 
     * @return True if the last challenge has been processed, false otherwise.
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Retrieve the authenticator mechanism name.
     * 
     * @return Always returns the string "PLAIN"
     */
    public String getMechanismName() {
        return "PLAIN";
    }

    /**
     * Evaluate a PLAIN login challenge, returning the a result string that
     * should satisfy the clallenge.
     * 
     * @param challenge
     *            The decoded challenge data, as byte array.
     * 
     * @return A formatted challege response, as an array of bytes.
     * @exception MessagingException
     */
    public byte[] evaluateChallenge(byte[] challenge) throws MessagingException {
        try {
            // get the username and password in an UTF-8 encoding to create the
            // token
            byte[] userBytes = username.getBytes("UTF-8");
            byte[] passBytes = password.getBytes("UTF-8");

            // our token has two copies of the username, one copy of the
            // password, and nulls
            // between
            byte[] tokenBytes = new byte[(userBytes.length * 2) + passBytes.length + 2];

            System.arraycopy(userBytes, 0, tokenBytes, 0, userBytes.length);
            System.arraycopy(userBytes, 0, tokenBytes, userBytes.length + 1, userBytes.length);
            System.arraycopy(passBytes, 0, tokenBytes, (userBytes.length * 2) + 2, passBytes.length);

            complete = true;
            return tokenBytes;

        } catch (UnsupportedEncodingException e) {
            // got an error, fail this
            throw new MessagingException("Invalid encoding");
        }
    }
}
