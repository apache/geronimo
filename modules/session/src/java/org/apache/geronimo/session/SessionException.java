/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.session;

/**
 * An exception occurred working with a given session.
 * 
 * @version $Revision: $
 */
public class SessionException extends Exception {

    private static final long serialVersionUID = 1880705415197036320L;

    private final String sessionId;

    public SessionException(String message, String sessionId) {
        super(message);
        this.sessionId = sessionId;
    }

    /**
     * Returns the Session Id that causes the exception
     */
    public String getSessionId() {
        return sessionId;
    }
}
