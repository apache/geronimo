/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.naming.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.NamingManager;

/**
 * A read-only Context in the java: namespace.
 * <p>
 * This version assumes it and all its subcontext are read-only and any attempt
 * to modify (e.g. through bind) will result in an OperationNotSupportedException.
 * Each Context in the tree builds a cache of the entries in all sub-contexts
 * to optimise the performance of lookup.
 * </p>
 * <p>This implementation is intended to optimise the performance of lookup(String)
 * to about the level of a HashMap get. It has been observed that the scheme
 * resolution phase performed by the JVM takes considerably longer, so for
 * optimum performance lookups should be coded like:</p>
 * <code>
 *   Context componentContext = new InitialContext().lookup("java:comp");
 *   String envEntry = (String) componentContext.lookup("env/myEntry");
 *   String envEntry2 = (String) componentContext.lookup("env/myEntry2");
 * </code>
 *
 * @version $Revision: 1.6 $ $Date: 2003/11/16 05:24:38 $
 */
public class ReadOnlyContext implements Context {
    private final Hashtable env;        // environment for this context
    private final Map bindings;         // bindings at my level
    private final Map treeBindings;     // all bindings under me

    /**
     * Construct a ReadOnlyContext with the bindings as defined by the Map
     * @param bindings the bindings for this level; may include sub-contexts
     */
    public ReadOnlyContext(Map bindings) {
        this.env = new Hashtable();
        this.bindings = new HashMap(bindings);
        treeBindings = new HashMap(bindings);
        for (Iterator i = bindings.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Object value = entry.getValue();
            if (value instanceof ReadOnlyContext) {
                String key = (String) entry.getKey() + '/';
                Map subBindings = ((ReadOnlyContext) value).treeBindings;
                for (Iterator j = subBindings.entrySet().iterator(); j.hasNext();) {
                    Map.Entry subEntry = (Map.Entry) j.next();
                    treeBindings.put(key + subEntry.getKey(), subEntry.getValue());
                }
            }
        }
    }

    ReadOnlyContext(Hashtable env) {
        this.env = new Hashtable(env);
        this.bindings = Collections.EMPTY_MAP;
        this.treeBindings = Collections.EMPTY_MAP;
    }

    ReadOnlyContext(ReadOnlyContext clone, Hashtable env) {
        this.bindings = clone.bindings;
        this.treeBindings = clone.treeBindings;
        this.env = new Hashtable(env);
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        return env.put(propName, propVal);
    }

    public Hashtable getEnvironment() throws NamingException {
        return (Hashtable) env.clone();
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        return env.remove(propName);
    }

    public Object lookup(String name) throws NamingException {
        if (name.length() == 0) {
            return this;
        }
        Object result = treeBindings.get(name);
        if (result == null) {
            int pos = name.indexOf(':');
            if (pos > 0) {
                String scheme = name.substring(0, pos);
                Context ctx = NamingManager.getURLContext(scheme, env);
                if (ctx == null) {
                    throw new NamingException("scheme " + scheme + " not recognized");
                }
                return ctx.lookup(name);
            }
            throw new NameNotFoundException(name);
        }
        if (result instanceof LinkRef) {
            LinkRef ref = (LinkRef) result;
            result = lookup(ref.getLinkName());
        }
        if (result instanceof ReadOnlyContext) {
            result = new ReadOnlyContext((ReadOnlyContext) result, env);
        }
        return result;
    }

    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public Object lookupLink(String name) throws NamingException {
        return lookup(name);
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        Name result = (Name) prefix.clone();
        result.addAll(name);
        return result;
    }

    public String composeName(String name, String prefix) throws NamingException {
        CompositeName result = new CompositeName(prefix);
        result.addAll(new CompositeName(name));
        return result.toString();
    }

    public NamingEnumeration list(String name) throws NamingException {
        Object o = lookup(name);
        if (o == this) {
            return new ListEnumeration();
        } else if (o instanceof Context) {
            return ((Context) o).list("");
        } else {
            throw new NotContextException();
        }
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        Object o = lookup(name);
        if (o == this) {
            return new ListBindingEnumeration();
        } else if (o instanceof Context) {
            return ((Context) o).listBindings("");
        } else {
            throw new NotContextException();
        }
    }

    public Object lookupLink(Name name) throws NamingException {
        return lookupLink(name.toString());
    }

    public NamingEnumeration list(Name name) throws NamingException {
        return list(name.toString());
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        return listBindings(name.toString());
    }

    public void bind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void bind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void close() throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Context createSubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void destroySubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NameParser getNameParser(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rebind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rebind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rename(String oldName, String newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void unbind(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void unbind(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }


    private abstract class LocalNamingEnumeration implements NamingEnumeration {
        private Iterator i = bindings.entrySet().iterator();

        public boolean hasMore() throws NamingException {
            return i.hasNext();
        }

        public boolean hasMoreElements() {
            return i.hasNext();
        }

        protected Map.Entry getNext() {
            return (Map.Entry) i.next();
        }

        public void close() throws NamingException {
        }
    }

    private class ListEnumeration extends LocalNamingEnumeration {
        public Object next() throws NamingException {
            return nextElement();
        }

        public Object nextElement() {
            Map.Entry entry = getNext();
            return new NameClassPair((String) entry.getKey(), entry.getValue().getClass().getName());
        }
    }

    private class ListBindingEnumeration extends LocalNamingEnumeration {
        public Object next() throws NamingException {
            return nextElement();
        }

        public Object nextElement() {
            Map.Entry entry = getNext();
            return new Binding((String) entry.getKey(), entry.getValue());
        }
    }
}
