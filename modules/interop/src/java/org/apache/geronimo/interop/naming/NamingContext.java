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
import javax.naming.Context;

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
    private HashMap                 failedBindings = new HashMap();

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

        // If it is corbaname type bind, give it one more chance to bind 
        // if not already bound.

        if (value == null)
        {
            value = tryBindCorbaName(name);
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

    /*
     * The allows the server to bind an object whose name beings with "lookup=".
     * The lookup= instructs the name service to perform a lookup on another name
     * service.
     */
    protected Object bindCorbaName(String name, String value)
    {
        String url = value.substring("lookup=".length());

        /*
         * value will only have the following two patterns:
         *
         * lookup=corbaname...
         * lookup=corbaloc...
         *
         * These are placed into the URL that is sent to the context factory.
         * The context factory then determine how to perform a lookup on a
         * given corbaname or corbaloc url.
         */

        java.util.Properties p = new java.util.Properties();

        /*
         * corbaname and corbaloc urls are not supported by the OpenEJB name service
         */
        p.put(Context.INITIAL_CONTEXT_FACTORY, "" ); // org.openejb.client.RemoteInitialContextFactory ??
        p.put(Context.PROVIDER_URL, url);

        Context initialContext = null;
        Object object = null;
        try
        {
            initialContext = new javax.naming.InitialContext(p);
            object = initialContext.lookup("");
        }
        catch (javax.naming.NamingException ne)
        {
            failedBindings(name, value);
            NameServiceLog.getInstance().warnBindFailed(logContext, name, url, ne);
            return null;
        }
        catch (java.lang.IllegalArgumentException ie)
        {
            NameServiceLog.getInstance().warnBindFailed(logContext, name, url, ie);
            return null;
        }
        catch (Exception ex)
        {
            failedBindings(name, value);
            NameServiceLog.getInstance().warnBindFailed(logContext, name, url, ex);
            return null;
        }

        if (object == null)
        {
            NameServiceLog.getInstance().warnIllegalBindValue(logContext, Object.class, name, url);
            return null;
        }

        map.put(name, object);

        return object;
    }

    protected Object dynamicLookup(String name) {
        return null;
    }

    protected String formatEmptyName() {
        return "formatEmptyName:";
    }

    // bind for corbaname failed at server startup. We will try to bind once
    // again.
    private Object tryBindCorbaName(String name)
    {
        Object obj = null;
        Object val = failedBindings.get(name);
        if( val != null)
        {
            obj = bindCorbaName(name, (String)val);
        }
        return obj;
    }

    /**
     * If corbaname bindings fail, give it one more chance at the time of
     * lookup
     */
    private void failedBindings(String name, String value)
    {
        Object val = failedBindings.get(name);
        if( val == null)
        {
            failedBindings.put(name, value);
        }
        else
        {
            //If the binding already exists in the map, then we have already given
            //it one more chance to bind. Time to remove it.
            failedBindings.remove(name);
        }
    }

}
