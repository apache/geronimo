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

package org.apache.geronimo.naming.geronimo;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.geronimo.naming.java.ReadOnlyContext;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:08 $
 *
 * */
public class GeronimoContext extends ReadOnlyContext {

    GeronimoContext() {
        super();
    }

    GeronimoContext(GeronimoContext context, Hashtable environment) {
        super(context, environment);
    }

    protected synchronized Map internalBind(String name, Object value) throws NamingException {
        return super.internalBind(name, value);
    }

    protected ReadOnlyContext newContext() {
        return new GeronimoContext();
    }

    protected synchronized Set internalUnbind(String name) throws NamingException {
        assert name != null;
        assert !name.equals("");
        Set removeBindings = new HashSet();
        int pos = name.indexOf('/');
        if (pos == -1) {
            if (treeBindings.remove(name) == null) {
                throw new NamingException("Nothing was bound at " + name);
            }
            bindings.remove(name);
            removeBindings.add(name);
        } else {
            String segment = name.substring(0, pos);
            assert segment != null;
            assert !segment.equals("");
            Object o = treeBindings.get(segment);
            if (o == null) {
                throw new NamingException("No context was bound at " + name);
            } else if (!(o instanceof GeronimoContext)) {
                throw new NamingException("Something else bound where a subcontext should be " + o);
            }
            GeronimoContext gerContext = (GeronimoContext)o;

            String remainder = name.substring(pos + 1);
            Set subBindings = gerContext.internalUnbind(remainder);
            for (Iterator iterator = subBindings.iterator(); iterator.hasNext();) {
                String subName = segment + "/" + (String) iterator.next();
                treeBindings.remove(subName);
                removeBindings.add(subName);
            }
            if (gerContext.bindings.isEmpty()) {
                bindings.remove(segment);
                treeBindings.remove(segment);
                removeBindings.add(segment);
            }
        }
        return removeBindings;
    }


    public synchronized Object lookup(String name) throws NamingException {
        return super.lookup(name);
    }

    public Object lookup(Name name) throws NamingException {
        return super.lookup(name);
    }

    public synchronized Object lookupLink(String name) throws NamingException {
        return super.lookupLink(name);
    }

    public synchronized Name composeName(Name name, Name prefix) throws NamingException {
        return super.composeName(name, prefix);
    }

    public synchronized String composeName(String name, String prefix) throws NamingException {
        return super.composeName(name, prefix);
    }

    public synchronized NamingEnumeration list(String name) throws NamingException {
        return super.list(name);
    }

    public synchronized NamingEnumeration listBindings(String name) throws NamingException {
        return super.listBindings(name);
    }

    public synchronized Object lookupLink(Name name) throws NamingException {
        return super.lookupLink(name);
    }

    public synchronized NamingEnumeration list(Name name) throws NamingException {
        return super.list(name);
    }

    public synchronized NamingEnumeration listBindings(Name name) throws NamingException {
        return super.listBindings(name);
    }

}
