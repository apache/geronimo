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

/**
 * The root context for the java: namespace.
 * Automatically handles switching the "java:comp" sub-context to the
 * appropriate one for the current thread.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:08 $
 */
public class RootContext extends ReadOnlyContext {
    private static InheritableThreadLocal compContext = new InheritableThreadLocal();

    RootContext(Hashtable env) {
        super(env);
    }

    public Object lookup(String name) throws NamingException {
        if (name.startsWith("java:")) {
            name = name.substring(5);
            if (name.length() == 0) {
                return this;
            }

            ReadOnlyContext compCtx = (ReadOnlyContext) compContext.get();
            if (compCtx == null) {
                // the component context was not set for this thread
                throw new NameNotFoundException();
            }
            compCtx = new ReadOnlyContext(compCtx, getEnvironment());

            if ("comp".equals(name)) {
                return compCtx;
            } else if (name.startsWith("comp/")) {
                return compCtx.lookup(name.substring(5));
            } else {
                throw new NameNotFoundException();
            }
        }
        return super.lookup(name);
    }

    /**
     * Set the component context for the current thread. This will be returned
     * for all lookups of "java:comp"
     * @param ctx the current components context
     */
    public static void setComponentContext(ReadOnlyContext ctx) {
        compContext.set(ctx);
    }

    /**
     * Get the component context for the current thread.
     * @return the current components context
     */
    public static ReadOnlyContext getComponentContext() {
        return (ReadOnlyContext) compContext.get();
    }
}
