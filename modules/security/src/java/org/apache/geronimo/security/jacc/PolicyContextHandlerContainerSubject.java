/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.security.jacc;

import java.security.AccessController;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;


/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:09 $
 */
public class PolicyContextHandlerContainerSubject implements PolicyContextHandler {
    public static final String HANDLER_KEY = "javax.security.auth.Subject.container";

    public boolean supports(String key) throws PolicyContextException {
        return key.equals(HANDLER_KEY);
    }

    public String[] getKeys() throws PolicyContextException {
        return new String[]{HANDLER_KEY};
    }

    public Object getContext(String key, Object data) throws PolicyContextException {
        try {
            return Subject.getSubject(AccessController.getContext());
        } catch (Exception e) {
            throw new PolicyContextException(e);
        }
    }
}
