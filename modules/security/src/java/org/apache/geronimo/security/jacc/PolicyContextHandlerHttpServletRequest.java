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

import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;


/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:09 $
 */
public class PolicyContextHandlerHttpServletRequest implements PolicyContextHandler {
    public static final String HANDLER_KEY = "javax.servlet.http.HttpServletRequest";

    public boolean supports(String key) throws PolicyContextException {
        return HANDLER_KEY.equals(key);
    }

    public String[] getKeys() throws PolicyContextException {
        return new String[]{HANDLER_KEY};
    }

    public Object getContext(String key, Object data) throws PolicyContextException {
        // todo: Wire in the return of the HttpServletRequest object
        return null;
    }
}
