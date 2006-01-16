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
 * An attempt was made to create a new session which was already created by
 * another process or thread.
 * 
 * @version $Revision: $
 */
public class SessionAlreadyExistsException extends SessionException {

    private static final long serialVersionUID = 7152083589480056760L;

    public SessionAlreadyExistsException(String sessionId) {
        super("The session already exists for ID: " + sessionId, sessionId);
    }
}
