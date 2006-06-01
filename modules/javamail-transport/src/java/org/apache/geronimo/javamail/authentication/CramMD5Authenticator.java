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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.mail.MessagingException;

import org.apache.geronimo.mail.util.Hex;

public class CramMD5Authenticator implements ClientAuthenticator {

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
    public CramMD5Authenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Respond to the hasInitialResponse query. This mechanism does not have an
     * initial response.
     * 
     * @return Always returns false.
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
        return complete;
    }

    /**
     * Retrieve the authenticator mechanism name.
     * 
     * @return Always returns the string "CRAM-MD5"
     */
    public String getMechanismName() {
        return "CRAM-MD5";
    }

    /**
     * Evaluate a CRAM-MD5 login challenge, returning the a result string that
     * should satisfy the clallenge.
     * 
     * @param challenge
     *            The decoded challenge data, as a byte array.
     * 
     * @return A formatted challege response, as an array of bytes.
     * @exception MessagingException
     */
    public byte[] evaluateChallenge(byte[] challenge) throws MessagingException {
        // we create the challenge from the userid and password information (the
        // "shared secret").
        byte[] passBytes;

        try {
            // get the password in an UTF-8 encoding to create the token
            passBytes = password.getBytes("UTF-8");
            // compute the password digest using the key
            byte[] digest = computeCramDigest(passBytes, challenge);

            // create a unified string using the user name and the hex encoded
            // digest
            String responseString = username + " " + new String(Hex.encode(digest));
            complete = true;
            return responseString.getBytes();
        } catch (UnsupportedEncodingException e) {
            // got an error, fail this
            throw new MessagingException("Invalid character encodings");
        }

    }

    /**
     * Compute a CRAM digest using the hmac_md5 algorithm. See the description
     * of RFC 2104 for algorithm details.
     * 
     * @param key
     *            The key (K) for the calculation.
     * @param input
     *            The encrypted text value.
     * 
     * @return The computed digest, as a byte array value.
     * @exception NoSuchAlgorithmException
     */
    protected byte[] computeCramDigest(byte[] key, byte[] input) throws MessagingException {
        // CRAM digests are computed using the MD5 algorithm.
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new MessagingException("Unable to access MD5 message digest", e);
        }

        // if the key is longer than 64 bytes, then we get a digest of the key
        // and use that instead.
        // this is required by RFC 2104.
        if (key.length > 64) {
            digest.update(key);
            key = digest.digest();
        }

        // now we create two 64 bit padding keys, initialized with the key
        // information.
        byte[] ipad = new byte[64];
        byte[] opad = new byte[64];

        System.arraycopy(key, 0, ipad, 0, key.length);
        System.arraycopy(key, 0, opad, 0, key.length);

        // and these versions are munged by XORing with "magic" values.

        for (int i = 0; i < 64; i++) {
            ipad[i] ^= 0x36;
            opad[i] ^= 0x5c;
        }

        // now there are a pair of MD5 operations performed, and inner and an
        // outer. The spec defines this as
        // H(K XOR opad, H(K XOR ipad, text)), where H is the MD5 operation.

        // inner operation
        digest.reset();
        digest.update(ipad);
        digest.update(input); // this appends the text to the pad
        byte[] md5digest = digest.digest();

        // outer operation
        digest.reset();
        digest.update(opad);
        digest.update(md5digest);
        return digest.digest(); // final result
    }
}
