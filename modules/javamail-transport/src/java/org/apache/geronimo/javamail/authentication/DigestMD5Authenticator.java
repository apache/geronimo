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
import java.security.SecureRandom;
import java.util.ArrayList;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import org.apache.geronimo.mail.util.Base64;
import org.apache.geronimo.mail.util.Hex;

/**
 * Process a DIGEST-MD5 authentication, using the challenge/response mechanisms.
 */
public class DigestMD5Authenticator implements ClientAuthenticator {

    protected static final int AUTHENTICATE_CLIENT = 0;

    protected static final int AUTHENTICATE_SERVER = 1;

    protected static final int AUTHENTICATION_COMPLETE = 2;

    // the host server name
    protected String host;

    // the user we're authenticating
    protected String username;

    // the user's password (the "shared secret")
    protected String password;

    // the target login realm
    protected String realm;

    // our message digest for processing the challenges.
    MessageDigest digest;

    // the string we send to the server on the first challenge.
    protected String clientResponse;

    // the response back from an authentication challenge.
    protected String authenticationResponse = null;

    // our list of realms received from the server (normally just one).
    protected ArrayList realms;

    // the nonce value sent from the server
    protected String nonce;

    // indicates whether we've gone through the entire challenge process.
    protected int stage = AUTHENTICATE_CLIENT;

    /**
     * Main constructor.
     * 
     * @param host
     *            The server host name.
     * @param username
     *            The login user name.
     * @param password
     *            The login password.
     * @param realm
     *            The target login realm (can be null).
     */
    public DigestMD5Authenticator(String host, String username, String password, String realm) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.realm = realm;
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
        return stage == AUTHENTICATION_COMPLETE;
    }

    /**
     * Retrieve the authenticator mechanism name.
     * 
     * @return Always returns the string "DIGEST-MD5"
     */
    public String getMechanismName() {
        return "DIGEST-MD5";
    }

    /**
     * Evaluate a DIGEST-MD5 login challenge, returning the a result string that
     * should satisfy the clallenge.
     * 
     * @param challenge
     *            The decoded challenge data, as a string.
     * 
     * @return A formatted challege response, as an array of bytes.
     * @exception MessagingException
     */
    public byte[] evaluateChallenge(byte[] challenge) throws MessagingException {

        // DIGEST-MD5 authentication goes in two stages. First state involves us
        // validating with the
        // server, the second stage is the server validating with us, using the
        // shared secret.
        switch (stage) {
        // stage one of the process.
        case AUTHENTICATE_CLIENT: {
            // get the response and advance the processing stage.
            byte[] response = authenticateClient(challenge);
            stage = AUTHENTICATE_SERVER;
            return response;
        }

        // stage two of the process.
        case AUTHENTICATE_SERVER: {
            // get the response and advance the processing stage to completed.
            byte[] response = authenticateServer(challenge);
            stage = AUTHENTICATION_COMPLETE;
            return response;
        }

        // should never happen.
        default:
            throw new MessagingException("Invalid LOGIN challenge");
        }
    }

    /**
     * Evaluate a DIGEST-MD5 login server authentication challenge, returning
     * the a result string that should satisfy the clallenge.
     * 
     * @param challenge
     *            The decoded challenge data, as a string.
     * 
     * @return A formatted challege response, as an array of bytes.
     * @exception MessagingException
     */
    public byte[] authenticateServer(byte[] challenge) throws MessagingException {
        // parse the challenge string and validate.
        if (!parseChallenge(challenge)) {
            return null;
        }

        try {
            // like all of the client validation steps, the following is order
            // critical.
            // first add in the URI information.
            digest.update((":smtp/" + host).getBytes("US-ASCII"));
            // now mix in the response we sent originally
            String responseString = clientResponse + new String(Hex.encode(digest.digest()));
            digest.update(responseString.getBytes("US-ASCII"));

            // now convert that into a hex encoded string.
            String validationText = new String(Hex.encode(digest.digest()));

            // if everything went well, this calculated value should match what
            // we got back from the server.
            // our response back is just a null string....
            if (validationText.equals(authenticationResponse)) {
                return new byte[0];
            }
            throw new AuthenticationFailedException("Invalid DIGEST-MD5 response from server");
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Invalid character encodings");
        }

    }

    /**
     * Evaluate a DIGEST-MD5 login client authentication challenge, returning
     * the a result string that should satisfy the clallenge.
     * 
     * @param challenge
     *            The decoded challenge data, as a string.
     * 
     * @return A formatted challege response, as an array of bytes.
     * @exception MessagingException
     */
    public byte[] authenticateClient(byte[] challenge) throws MessagingException {
        // parse the challenge string and validate.
        if (!parseChallenge(challenge)) {
            return null;
        }

        SecureRandom randomGenerator;
        // before doing anything, make sure we can get the required crypto
        // support.
        try {
            randomGenerator = new SecureRandom();
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new MessagingException("Unable to access cryptography libraries");
        }

        // if not configured for a realm, take the first realm from the list, if
        // any
        if (realm == null) {
            // if not handed any realms, just use the host name.
            if (realms.isEmpty()) {
                realm = host;
            } else {
                // pretty arbitrary at this point, so just use the first one.
                realm = (String) realms.get(0);
            }
        }

        // use secure random to generate a collection of bytes. that is our
        // cnonce value.
        byte[] cnonceBytes = new byte[32];

        randomGenerator.nextBytes(cnonceBytes);
        // and get this as a base64 encoded string.
        String cnonce = new String(Base64.encode(cnonceBytes));

        // Now the digest computation part. This gets a bit tricky, and must be
        // done in strict order.

        try {
            // this identifies where we're logging into.
            String idString = username + ":" + realm + ":" + password;
            // we get a digest for this string, then use the digest for the
            // first stage
            // of the next digest operation.
            digest.update(digest.digest(idString.getBytes("US-ASCII")));

            // now we add the nonce strings to the digest.
            String nonceString = ":" + nonce + ":" + cnonce;
            digest.update(nonceString.getBytes("US-ASCII"));

            // hex encode this digest, and add on the string values
            // NB, we only support "auth" for the quality of protection value
            // (qop). We save this in an
            // instance variable because we'll need this to validate the
            // response back from the server.
            clientResponse = new String(Hex.encode(digest.digest())) + ":" + nonce + ":00000001:" + cnonce + ":auth:";

            // now we add in identification values to the hash.
            String authString = "AUTHENTICATE:smtp/" + host;
            digest.update(authString.getBytes("US-ASCII"));

            // this gets added on to the client response
            String responseString = clientResponse + new String(Hex.encode(digest.digest()));
            // and this gets fed back into the digest
            digest.update(responseString.getBytes("US-ASCII"));

            // and FINALLY, the challege digest is hex encoded for sending back
            // to the server (whew).
            String challengeResponse = new String(Hex.encode(digest.digest()));

            // now finally build the keyword/value part of the challenge
            // response. These can be
            // in any order.
            StringBuffer response = new StringBuffer();

            response.append("username=\"");
            response.append(username);
            response.append("\"");

            response.append(",realm=\"");
            response.append(realm);
            response.append("\"");

            // we only support auth qop values, and the nonce-count (nc) is
            // always 1.
            response.append(",qop=auth");
            response.append(",nc=00000001");

            response.append(",nonce=\"");
            response.append(nonce);
            response.append("\"");

            response.append(",cnonce=\"");
            response.append(cnonce);
            response.append("\"");

            response.append(",digest-uri=\"smtp/");
            response.append(host);
            response.append("\"");

            response.append(",response=");
            response.append(challengeResponse);

            return response.toString().getBytes("US-ASCII");

        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Invalid character encodings");
        }
    }

    /**
     * Parse the challege string, pulling out information required for our
     * challenge response.
     * 
     * @param challenge
     *            The challenge data.
     * 
     * @return true if there were no errors parsing the string, false otherwise.
     * @exception MessagingException
     */
    protected boolean parseChallenge(byte[] challenge) throws MessagingException {
        realms = new ArrayList();

        DigestParser parser = new DigestParser(new String(challenge));

        // parse the entire string...but we ignore everything but the options we
        // support.
        while (parser.hasMore()) {
            NameValuePair pair = parser.parseNameValuePair();

            String name = pair.name;

            // realm to add to our list?
            if (name.equalsIgnoreCase("realm")) {
                realms.add(pair.value);
            }
            // we need the nonce to evaluate the client challenge.
            else if (name.equalsIgnoreCase("nonce")) {
                nonce = pair.value;
            }
            // rspauth is the challenge replay back, which allows us to validate
            // that server is also legit.
            else if (name.equalsIgnoreCase("rspauth")) {
                authenticationResponse = pair.value;
            }
        }

        return true;
    }

    /**
     * Inner class for parsing a DIGEST-MD5 challenge string, which is composed
     * of "name=value" pairs, separated by "," characters.
     */
    class DigestParser {
        // the challenge we're parsing
        String challenge;

        // length of the challenge
        int length;

        // current parsing position
        int position;

        /**
         * Normal constructor.
         * 
         * @param challenge
         *            The challenge string to be parsed.
         */
        public DigestParser(String challenge) {
            this.challenge = challenge;
            this.length = challenge.length();
            position = 0;
        }

        /**
         * Test if there are more values to parse.
         * 
         * @return true if we've not reached the end of the challenge string,
         *         false if the challenge has been completely consumed.
         */
        private boolean hasMore() {
            return position < length;
        }

        /**
         * Return the character at the current parsing position.
         * 
         * @return The string character for the current parse position.
         */
        private char currentChar() {
            return challenge.charAt(position);
        }

        /**
         * step forward to the next character position.
         */
        private void nextChar() {
            position++;
        }

        /**
         * Skip over any white space characters in the challenge string.
         */
        private void skipSpaces() {
            while (position < length && Character.isWhitespace(currentChar())) {
                position++;
            }
        }

        /**
         * Parse a quoted string used with a name/value pair, accounting for
         * escape characters embedded within the string.
         * 
         * @return The string value of the character string.
         */
        private String parseQuotedValue() {
            // we're here because we found the starting double quote. Step over
            // it and parse to the closing
            // one.
            nextChar();

            StringBuffer value = new StringBuffer();

            while (hasMore()) {
                char ch = currentChar();

                // is this an escape char?
                if (ch == '\\') {
                    // step past this, and grab the following character
                    nextChar();
                    // we have an invalid quoted string....
                    if (!hasMore()) {
                        return null;
                    }
                    value.append(currentChar());
                }
                // end of the string?
                else if (ch == '"') {
                    // step over this so the caller doesn't process it.
                    nextChar();
                    // return the constructed string.
                    return value.toString();
                } else {
                    // step over the character and contine with the next
                    // characteer1
                    value.append(ch);
                }
                nextChar();
            }
            /* fell off the end without finding a closing quote! */
            return null;
        }

        /**
         * Parse a token value used with a name/value pair.
         * 
         * @return The string value of the token. Returns null if nothing is
         *         found up to the separater.
         */
        private String parseTokenValue() {

            StringBuffer value = new StringBuffer();

            while (hasMore()) {
                char ch = currentChar();
                switch (ch) {
                // process the token separators.
                case ' ':
                case '\t':
                case '(':
                case ')':
                case '<':
                case '>':
                case '@':
                case ',':
                case ';':
                case ':':
                case '\\':
                case '"':
                case '/':
                case '[':
                case ']':
                case '?':
                case '=':
                case '{':
                case '}':
                    // no token characters found? this is bad.
                    if (value.length() == 0) {
                        return null;
                    }
                    // return the accumulated characters.
                    return value.toString();

                default:
                    // is this a control character? That's a delimiter (likely
                    // invalid for the next step,
                    // but it is a token terminator.
                    if (ch < 32 || ch > 127) {
                        // no token characters found? this is bad.
                        if (value.length() == 0) {
                            return null;
                        }
                        // return the accumulated characters.
                        return value.toString();
                    }
                    value.append(ch);
                    break;
                }
                // step to the next character.
                nextChar();
            }
            // no token characters found? this is bad.
            if (value.length() == 0) {
                return null;
            }
            // return the accumulated characters.
            return value.toString();
        }

        /**
         * Parse out a name token of a name/value pair.
         * 
         * @return The string value of the name.
         */
        private String parseName() {
            // skip to the value start
            skipSpaces();

            // the name is a token.
            return parseTokenValue();
        }

        /**
         * Parse out a a value of a name/value pair.
         * 
         * @return The string value associated with the name.
         */
        private String parseValue() {
            // skip to the value start
            skipSpaces();

            // start of a quoted string?
            if (currentChar() == '"') {
                // parse it out as a string.
                return parseQuotedValue();
            }
            // the value must be a token.
            return parseTokenValue();
        }

        /**
         * Parse a name/value pair in an DIGEST-MD5 string.
         * 
         * @return A NameValuePair object containing the two parts of the value.
         * @exception MessagingException
         */
        public NameValuePair parseNameValuePair() throws MessagingException {
            // get the name token
            String name = parseName();
            if (name == null) {
                throw new MessagingException("Name syntax error");
            }

            // the name should be followed by an "=" sign
            if (!hasMore() || currentChar() != '=') {
                throw new MessagingException("Name/value pair syntax error");
            }

            // step over the equals
            nextChar();

            // now get the value part
            String value = parseValue();
            if (value == null) {
                throw new MessagingException("Name/value pair syntax error");
            }

            // skip forward to the terminator, which should either be the end of
            // the line or a ","
            skipSpaces();
            // all that work, only to have a syntax error at the end (sigh)
            if (hasMore()) {
                if (currentChar() != ',') {
                    throw new MessagingException("Name/value pair syntax error");
                }
                // step over, and make sure we position ourselves at either the
                // end or the first
                // real character for parsing the next name/value pair.
                nextChar();
                skipSpaces();
            }
            return new NameValuePair(name, value);
        }
    }

    /**
     * Simple inner class to represent a name/value pair.
     */
    public class NameValuePair {
        public String name;

        public String value;

        NameValuePair(String name, String value) {
            this.name = name;
            this.value = value;
        }

    }
}
