/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.logmanager;

public class WebAccessLogCriteria {

    private String requestHost;

    private String authUser;

    private String requestMethod;

    private String requestedURI;

    private String message;

    /**
     * Get an instance of WebAccessLogCriteria that matches anyother instances
     * of new WebAccessLogCriteria.
     *
     * @return
     */
    public static WebAccessLogCriteria getGlobalMatcher() {
        return new WebAccessLogCriteria("");
    }

    public WebAccessLogCriteria(String message, String requestHost,
            String authUser, String requestMethod, String requestedURI) {
        this.message = message;
        this.requestHost = requestHost;
        this.authUser = authUser;
        this.requestMethod = requestMethod;
        this.requestedURI = requestedURI;
    }

    public WebAccessLogCriteria(String message, String requestHost,
            String authUser, String requestMethod) {
        this(message, requestHost, authUser, requestMethod, null);
    }

    public WebAccessLogCriteria(String message, String requestHost,
            String authUser) {
        this(message, requestHost, authUser, null);
    }

    public WebAccessLogCriteria(String message, String requestHost) {
        this(message, requestHost, null);
    }

    public WebAccessLogCriteria(String message) {
        this(message, null);
    }

    public String toString() {
        return message;
    }

    /**
     * Check equality of two object using the following rules:
     * <ol>
     * <li>If one of the arguments is null return true.</li>
     * <li>If both arguments are not null check for equality.</li>
     * </ol>
     */
    private boolean checkEquality(Object lhs, Object rhs) {
        return (lhs == null || rhs == null || lhs.equals(rhs));
    }

    /**
     * Check's criteria matches this one. This method checks all the
     * relevant(all except message) fields for equality but will also consider
     * to fields a match if one of them is null.
     */
    public boolean matches(WebAccessLogCriteria rhs) {
        return (checkEquality(requestHost, rhs.requestHost)
                && checkEquality(authUser, rhs.authUser)
                && checkEquality(requestMethod, rhs.requestMethod) && checkEquality(
                requestedURI, rhs.requestedURI));
    }

}