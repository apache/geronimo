/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.security.jacc;

import java.security.SecurityPermission;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Hashtable;
import java.util.Set;


/**
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:53 $
 */
public final class PolicyContext {

    private static ThreadLocal contextId = new ThreadLocal();
    private static ThreadLocal handlerData = new ThreadLocal();
    private static Hashtable handlers = new Hashtable();
    private final static SecurityPermission SET_POLICY = new SecurityPermission("setPolicy");

    public static void setContextID(String contextID) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_POLICY);

        contextId.set(contextID);
    }

    public static String getContextID() {
        return (String) contextId.get();
    }

    public static void setHandlerData(Object data) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_POLICY);

        handlerData.set(data);
    }

    public static void registerHandler(String key, PolicyContextHandler handler, boolean replace) throws PolicyContextException {
        if (key == null) throw new IllegalArgumentException("Key must not be null");
        if (handler == null) throw new IllegalArgumentException("Handler must not be null");
        if (!replace && handlers.containsKey(key)) throw new IllegalArgumentException("A handler has already been registered under '" + key + "' and replace is false.");

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_POLICY);

        handlers.put(key, handler);
    }

    public static Set getHandlerKeys() {
        return handlers.keySet();
    }

    public static Object getContext(String key) throws PolicyContextException {
        if (key == null) throw new IllegalArgumentException("Key must not be null");

        PolicyContextHandler handler = (PolicyContextHandler) handlers.get(key);

        if (handler == null) throw new IllegalArgumentException("No handler can be found for the key '" + key + "'");
        if (!handler.supports(key)) throw new IllegalArgumentException("Registered handler no longer supports the key '" + key + "'");

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SET_POLICY);

        return handler.getContext(key, handlerData.get());
    }
}
