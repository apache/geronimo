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

package org.apache.geronimo.naming.java;

import java.util.Hashtable;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Context;

/**
 * The root context for the java: namespace.
 * Automatically handles switching the "java:comp" sub-context to the
 * appropriate one for the current thread.
 *
 * @version $Rev$ $Date$
 */
public class RootContext extends ReadOnlyContext {
    private static InheritableThreadLocal compContext = new InheritableThreadLocal();

    public Object lookup(String name) throws NamingException {
        if (name.startsWith("java:")) {
            name = name.substring(5);
            if (name.length() == 0) {
                return this;
            }

            Context compCtx = (Context) compContext.get();
            if (compCtx == null) {
                // the component context was not set for this thread
                throw new NameNotFoundException(name);
            }

            if ("comp".equals(name)) {
                return compCtx;
            } else if (name.startsWith("comp/")) {
                return compCtx.lookup(name.substring(5));
            } else if ("/comp".equals(name)) {
                return compCtx;
            } else if (name.startsWith("/comp/")) {
                return compCtx.lookup(name.substring(6));
            } else {
                throw new NameNotFoundException("Unrecognized name, does not start with expected 'comp': " + name);
            }
        }
        return super.lookup(name);
    }

    /**
     * Set the component context for the current thread. This will be returned
     * for all lookups of "java:comp"
     * @param ctx the current components context
     */
    public static void setComponentContext(Context ctx) {
        compContext.set(ctx);
    }

    /**
     * Get the component context for the current thread.
     * @return the current components context
     */
    public static Context getComponentContext() {
        return (Context) compContext.get();
    }
}
