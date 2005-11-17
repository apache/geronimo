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

package org.apache.geronimo.naming.enc;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
abstract class AbstractReadOnlyContext implements Context, Serializable {
    private final String nameInNamespace;
    private final String path;

    protected AbstractReadOnlyContext(String nameInNamespace) {
        if (nameInNamespace == null) throw new NullPointerException("nameInNamespace is null");

        this.nameInNamespace = nameInNamespace;
        if (nameInNamespace.length() > 0) {
            path = nameInNamespace + "/";
        } else {
            path = nameInNamespace;
        }
    }

    protected abstract Map getGlobalBindings();
    protected abstract Map getLocalBindings();

    //
    //  Lookup methods
    //

    public final Object lookup(String name) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");

        if (name.length() == 0) {
            return this;
        }

        String fullName = path + name;

        // lookup the bound object
        Map globalBindings = getGlobalBindings();
        Object result = globalBindings.get(fullName);
        if (result == null) {
            if (fullName.indexOf(':') > 0) {
                Context ctx = new InitialContext();
                return ctx.lookup(fullName);
            } else if (new CompositeName(fullName).size() == 0) {
                return this;
            }
            throw new NameNotFoundException(fullName);
        }

        // we should only ever see a CachingReference
        if (result instanceof CachingReference) {
            result = ((CachingReference)result).get();
        }

        return result;
    }

    public final Object lookup(Name name) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");

        return lookup(name.toString());
    }

    public final Object lookupLink(Name name) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");

        return lookupLink(name.toString());
    }

    public final Object lookupLink(String name) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");

        return lookup(name);
    }

    //
    //  List Operations
    //

    public final NamingEnumeration list(String name) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");

        Object o = lookup(name);
        if (o == this) {
            return list();
        } else if (o instanceof AbstractReadOnlyContext) {
            return ((AbstractReadOnlyContext) o).list();
        } else {
            throw new NotContextException();
        }
    }

    public final NamingEnumeration listBindings(String name) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");

        Object o = lookup(name);
        if (o == this) {
            return listBindings();
        } else if (o instanceof AbstractReadOnlyContext) {
            return ((AbstractReadOnlyContext) o).listBindings();
        } else {
            throw new NotContextException();
        }
    }

    private NamingEnumeration list() {
        Map localBindings = getLocalBindings();
        return new ListEnumeration(localBindings);
    }

    private NamingEnumeration listBindings() {
        Map localBindings = getLocalBindings();
        return new ListBindingEnumeration(localBindings);
    }

    public final NamingEnumeration list(Name name) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");

        return list(name.toString());
    }

    public final NamingEnumeration listBindings(Name name) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");

        return listBindings(name.toString());
    }

    private static final class ListEnumeration implements NamingEnumeration {
        private final Iterator iterator;

        public ListEnumeration(Map localBindings) {
            this.iterator = localBindings.entrySet().iterator();
        }

        public boolean hasMore() {
            return iterator.hasNext();
        }

        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        public Object next() {
            return nextElement();
        }

        public Object nextElement() {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            String className = null;
            if (value instanceof CachingReference) {
                CachingReference cachingReference = (CachingReference) value;
                className = cachingReference.getClassName();
            } else {
                className = value.getClass().getName();
            }
            return new NameClassPair(name, className);
        }

        public void close() {
        }
    }

    private static final class ListBindingEnumeration implements NamingEnumeration {
        private final Iterator iterator;

        public ListBindingEnumeration(Map localBindings) {
            this.iterator = localBindings.entrySet().iterator();
        }

        public boolean hasMore() {
            return iterator.hasNext();
        }

        public boolean hasMoreElements() {
            return iterator.hasNext();
        }

        public Object next() {
            return nextElement();
        }

        public Object nextElement() {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            return new EnterpriseBinding(name, value);
        }

        public void close() {
        }
    }

    private static final class EnterpriseBinding extends Binding {
        private final Object value;

        public EnterpriseBinding(String name, Object value) {
            super(name, value);
            this.value = value;
        }

        public void setName(String name) {
            throw new UnsupportedOperationException("EnterpriseNamingContext can not be modified");
        }

        public String getClassName() {
            if (value instanceof CachingReference) {
                CachingReference cachingReference = (CachingReference) value;
                return cachingReference.getClassName();
            }
            return value.getClass().getName();
        }

        public void setClassName(String name) {
            throw new UnsupportedOperationException("EnterpriseNamingContext can not be modified");
        }

        public Object getObject() {
            if (value instanceof CachingReference) {
                try {
                    CachingReference cachingReference = (CachingReference) value;
                    return cachingReference.get();
                } catch (NamingException e) {
                    throw new BindingResolutionException("Unable to resolve binding " + getName(), e);
                }
            }
            return value;
        }

        public void setObject(Object obj) {
            throw new UnsupportedOperationException("EnterpriseNamingContext can not be modified");
        }

        public boolean isRelative() {
            return false;
        }

        public void setRelative(boolean r) {
            throw new UnsupportedOperationException("EnterpriseNamingContext can not be modified");
        }
    }

    //
    // Name manipulation
    //

    public final String getNameInNamespace() {
        return nameInNamespace;
    }

    public final NameParser getNameParser(Name name) {
        return EnterpriseNamingContextNameParser.INSTANCE;
    }

    public final NameParser getNameParser(String name) {
        return EnterpriseNamingContextNameParser.INSTANCE;
    }

    public final Name composeName(Name name, Name prefix) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");
        if (prefix == null) throw new NullPointerException("prefix is null");

        Name result = (Name) prefix.clone();
        result.addAll(name);
        return result;
    }

    public final String composeName(String name, String prefix) throws NamingException {
        if (name == null) throw new NullPointerException("name is null");
        if (prefix == null) throw new NullPointerException("prefix is null");

        CompositeName result = new CompositeName(prefix);
        result.addAll(new CompositeName(name));
        return result.toString();
    }

    //
    //  Unsupported Operations
    //

    public final Hashtable getEnvironment() {
        return new Hashtable();
    }

    public final Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final Object removeFromEnvironment(String propName) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void bind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void bind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void close() throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final Context createSubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void destroySubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void rebind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void rebind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void rename(Name oldName, Name newName) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void rename(String oldName, String newName) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void unbind(Name name) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }

    public final void unbind(String name) throws NamingException {
        throw new OperationNotSupportedException("EnterpriseNamingContext can not be modified");
    }
}
