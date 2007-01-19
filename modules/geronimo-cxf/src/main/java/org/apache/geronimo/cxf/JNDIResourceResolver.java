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
package org.apache.geronimo.cxf;

import java.io.InputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.cxf.Bus;
import org.apache.cxf.resource.ResourceResolver;

import org.apache.geronimo.naming.java.RootContext;

public class JNDIResourceResolver implements ResourceResolver {

    private static final Logger LOG = 
        Logger.getLogger(JNDIResourceResolver.class.getName());

    private Context componentContext;

    public JNDIResourceResolver(Context context) {
        this.componentContext = context;
    }
    
    public final InputStream getAsStream(final String string) {
        return null;
    }

    public final <T> T resolve(final String name, final Class<T> clz) {
        // Ignore those
        if ( ("bus".equals(name) && clz.equals(Bus.class)) ||
             ("mtomEnabled".equals(name) && clz.equals(boolean.class)) ) {
            return null;
        }

        Context oldContext = RootContext.getComponentContext();
        try {
            RootContext.setComponentContext(componentContext);

            Context ctx = new InitialContext();
            ctx = (Context)ctx.lookup("java:comp/env");
                
            LOG.fine("Looking up '" + name + "'");

            Object o = ctx.lookup(name);
                        
            return clz.cast(o);
            
        } catch (NamingException e) {
            LOG.log(Level.FINE, "JNDI lookup failed", e);
            return null;
        } finally {
            RootContext.setComponentContext(oldContext);
        }
    }
}
