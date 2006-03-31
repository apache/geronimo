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

import javax.mail.MessagingException;

/**
 * Simplified version of the Java 5 SaslClient interface. This is used to
 * implement a javamail authentication framework that mimics the Sasl framework
 * on a 1.4.2 JVM. Only the methods required by the Javamail code are
 * implemented here, but it should be a simple migration to the fuller SASL
 * interface.
 */
public interface ClientAuthenticator {
    /**
     * Evaluate a challenge and return a response that can be sent back to the
     * server. Bot the challenge information and the response information are
     * "raw data", minus any special encodings used by the transport. For
     * example, SMTP DIGEST-MD5 authentication protocol passes information as
     * Base64 encoded strings. That encoding must be removed before calling
     * evaluateChallenge() and the resulting respose must be Base64 encoced
     * before transmission to the server.
     * 
     * It is the authenticator's responsibility to keep track of the state of
     * the evaluations. That is, if the authentication process requires multiple
     * challenge/response cycles, then the authenticator needs to keep track of
     * context of the challenges.
     * 
     * @param challenge
     *            The challenge data.
     * 
     * @return An appropriate response for the challenge data.
     */

    public byte[] evaluateChallenge(byte[] challenge) throws MessagingException;

    /**
     * Indicates that the authenticator has data that should be sent when the
     * authentication process is initiated. For example, the SMTP PLAIN
     * authentication sends userid/password without waiting for a challenge
     * response.
     * 
     * If this method returns true, then the initial response is retrieved using
     * evaluateChallenge() passing null for the challenge information.
     * 
     * @return True if the challenge/response process starts with an initial
     *         response on the client side.
     */
    public boolean hasInitialResponse();

    /**
     * Indicates whether the client believes the challenge/response sequence is
     * now complete.
     * 
     * @return true if the client has evaluated what it believes to be the last
     *         challenge, false if there are additional stages to evaluate.
     */

    public boolean isComplete();

    /**
     * Return the mechanism name implemented by this authenticator.
     * 
     * @return The string name of the authentication mechanism. This name should
     *         match the names commonly used by the mail servers (e.g., "PLAIN",
     *         "LOGIN", "DIGEST-MD5", etc.).
     */
    public String getMechanismName();
}
