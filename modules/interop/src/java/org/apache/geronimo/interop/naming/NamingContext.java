/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.naming;

import java.util.HashMap;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.geronimo.interop.adapter.Adapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NamingContext {

    private final Log log = LogFactory.getLog(NamingContext.class);

    public static final NamingContext getInstance(Class baseClass) {
        NamingContext context;
        synchronized (contextMap) {
            context = (NamingContext) contextMap.get(baseClass);
            if (context == null) {
                context = new NamingContext();
                contextMap.put(baseClass, context);
                context.init(baseClass);
            }
        }
        return context;
    }

    private static ThreadLocal      current = new ThreadLocal();
    private static HashMap          contextMap = new HashMap();
    private static boolean          quiet = false; // TODO: Configure
    private static boolean          verbose = true; // TODO: Configure
    private String                  logContext;
    private HashMap                 map = new HashMap();

    public static final NamingContext getCurrent() {
        return (NamingContext) current.get();
    }

    public static final NamingContext push(NamingContext that) {
        NamingContext restore = getCurrent();
        current.set(that);
        return restore;
    }

    public static void pop(NamingContext restore) {
        current.set(restore);
    }

    public HashMap getMap() {
        return map;
    }

    public Object lookup(String name, String prefix) throws NamingException {

        log.debug( "NameContext.lookup(): name = " + name + ", prefix = " + prefix );

        if (prefix != null) {
            name += prefix + "/" + name;
        }

        // Note: this part of the method is performance critical. Please
        // refrain from using string concatenation, synchronization and
        // other slow calls here. All possible initialization should
        // be performed in 'init' so as to permit this method to be as
        // fast as possible (i.e. a simple unsynchronized HashMap lookup).

        Object value = map.get(name);

        if (value == null) {
            value = dynamicLookup(name);
            if (value != null) {
                map.put(name, value); // TODO: allow refresh.
            }
        }

        if (value == null) {
            NameNotFoundException notFound = new NameNotFoundException(name.length() == 0 ? formatEmptyName() : name);
            if (!quiet) {
                NameServiceLog.getInstance().warnNameNotFound(logContext, notFound);
            }
            throw notFound;
        } else {
            return value;
        }
    }

    public Object lookupReturnNullIfNotFound(String name, String prefix) {
        if (prefix != null) {
            name += prefix + "/" + name;
        }
        return map.get(name);
    }

    protected void init(Class baseClass) {
        // TODO: Nothing really to do as this would init all the env-prop res-ref ... from a component
        //       this logic isn't required for the CORBA container.
    }

    protected synchronized void bindAdapter(Adapter adp) {
        String names[] = adp.getBindNames();
        for( int i=0; i<names.length; i++ ) {
            log.debug( "NameContext.bindAdapter(): name[" + i + "] = " + names[i] + ", adp = " + adp );
            map.put(names[i], adp);
        }
    }

    protected synchronized void unbindAdapter( Adapter adp ) {
        String names[] = adp.getBindNames();
        for( int i=0; i<names.length; i++ )
        {
            log.debug( "NameContext.bindAdapter(): name[" + i + "] = " + names[i] + ", adp = " + adp );
            map.remove( names[i] );
        }
    }

    protected boolean adapterExists(String name) {
        System.out.println("TODO: NamingComponent.componentExists(): name = " + name);
        return false;
    }

    protected Object dynamicLookup(String name) {
        return null;
    }

    protected String formatEmptyName() {
        return "formatEmptyName:";
    }

}
