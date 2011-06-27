/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.xbean.naming.context.ImmutableContext;

/**
 * The root context for the java: namespace.
 * Automatically handles switching the "java:comp" sub-context to the
 * appropriate one for the current thread.
 *
 * @version $Rev$ $Date$
 */
public class RootContext extends ImmutableContext {
    private static InheritableThreadLocal<Context> compContext = new InheritableThreadLocal<Context>();
    
    private static InheritableThreadLocal<URI> openejbRemoteContextURI = new InheritableThreadLocal<URI>();

    public RootContext() throws NamingException {
        super(Collections.<String, Object>emptyMap());
    }

    @Override
    public Object lookup(String name) throws NamingException {
        if (name.startsWith("java:")) {
            Context compCtx = compContext.get();
            if (compCtx == null) {
                // the component context was not set for this thread
                throw new NameNotFoundException("No thread context set, looking up: " + name);
            }
            if (name.length() == 5) {
                return compCtx;
            }
            name = name.charAt(5) == '/'? name.substring(6): name.substring(5);
            
            try {
                return compCtx.lookup(name);
            } catch (NamingException e1) {

                if (openejbRemoteContextURI.get() != null && (name.startsWith("global/"))) {

                    Properties p = new Properties();
                    p.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
                    p.put("java.naming.provider.url", openejbRemoteContextURI.get().toString());
                    p.put("openejb.client.moduleId", "openejb/global");

                    try {
                        InitialContext ctx = new InitialContext(p);
                        Object value = ctx.lookup(name);
                        return value;
                    } catch (NamingException e2) {
                        throw (NameNotFoundException) new NameNotFoundException().initCause(e2);
                    }
                } else {

                    throw e1;
                }
            }

        }
        return super.lookup(name);
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
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
     * Set the remote Context uri, app client 
     * need to set this to utilize the openejb remote
     * jndi system to do fallback global jndi lookup.
     * @param uri
     */
    public static void setOpenejbRemoteContextURI(URI uri) {
      openejbRemoteContextURI.set(uri);
    }    

    /**
     * Get the component context for the current thread.
     * @return the current components context
     */
    public static Context getComponentContext() {
        return compContext.get();
    }
}
