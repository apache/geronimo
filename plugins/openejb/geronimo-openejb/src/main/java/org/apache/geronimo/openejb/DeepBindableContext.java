/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.openejb;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.JndiFactory;
import org.apache.xbean.naming.context.ContextAccess;
import org.apache.xbean.naming.context.ContextFlyweight;
import org.apache.xbean.naming.context.WritableContext;
import org.apache.xbean.naming.global.GlobalContextManager;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

/**
 * Not currently used as a gbean so the annotations could be removed.
 * @version $Rev$ $Date$
 */
@GBean
public class DeepBindableContext extends WritableContext {

    public DeepBindableContext(@ParamAttribute(name = "nameInNamespace") String nameInNamespace,
                               @ParamAttribute(name = "cacheReferences") boolean cacheReferences,
                               @ParamAttribute(name = "supportReferenceable") boolean supportReferenceable,
                               @ParamAttribute(name = "checkDereferenceDifferent") boolean checkDereferenceDifferent,
                               @ParamAttribute(name = "assumeDereferenceBound") boolean assumeDereferenceBound) throws NamingException {
        super(nameInNamespace, Collections.<String, Object>emptyMap(), ContextAccess.MODIFIABLE, cacheReferences, supportReferenceable, checkDereferenceDifferent, assumeDereferenceBound);
    }

    void addDeepBinding(String name, Object value) throws NamingException {
        addDeepBinding(new CompositeName(name), value, false, true);
    }

    void removeDeepBinding(Name name) throws NamingException {
        removeDeepBinding(name, true, false);
    }

    ContextWrapper newContextWrapper() throws NamingException {
        return new ContextWrapper(this);
    }

    ContextWrapper newContextWrapper(Context rootContext) throws NamingException {
        return new ContextWrapper(rootContext);
    }

    class ContextWrapper implements Context {
        private final Context rootContext;
        private final String shortPrefix;
        private final String longPrefix;


        ContextWrapper(Context rootContext) throws NamingException {
            this.rootContext = rootContext;
            shortPrefix = DeepBindableContext.this.getNameInNamespace();
            longPrefix = "java:" + shortPrefix;
        }

        Context getRootContext() {
            return rootContext;
        }

        public Object lookup(Name name) throws NamingException {
            return rootContext.lookup(name);
        }

        public Object lookup(String name) throws NamingException {
            return rootContext.lookup(name);
        }

        public void bind(Name name, Object value) throws NamingException {
            bind(name.toString(), value);
        }

        public void bind(String name, Object value) throws NamingException {
            if (name.startsWith(longPrefix + "/")) {
                name = name.substring(longPrefix.length() + 1);
            } else if (name.startsWith(shortPrefix + "/")) {
                name = name.substring(shortPrefix.length() + 1);
            }
            addDeepBinding(name, value);
        }

        public void rebind(Name name, Object o) throws NamingException {
            rootContext.rebind(name, o);
        }

        public void rebind(String s, Object o) throws NamingException {
            rootContext.rebind(s, o);
        }

        public void unbind(Name name) throws NamingException {
            if (name.get(0).equals(shortPrefix) || name.get(0).equals(longPrefix)) {
                name = (Name) name.clone();
                name.remove(0);
            }
            removeDeepBinding(name);
        }

        public void unbind(String name) throws NamingException {
            unbind(getNameParser(name).parse(name));
         }

        public void rename(Name name, Name name1) throws NamingException {
            rootContext.rename(name, name1);
        }

        public void rename(String s, String s1) throws NamingException {
            rootContext.rename(s, s1);
        }

        public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
            return rootContext.list(name);
        }

        public NamingEnumeration<NameClassPair> list(String s) throws NamingException {
            return rootContext.list(s);
        }

        public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
            return rootContext.listBindings(name);
        }

        public NamingEnumeration<Binding> listBindings(String s) throws NamingException {
            return rootContext.listBindings(s);
        }

        public void destroySubcontext(Name name) throws NamingException {
            rootContext.destroySubcontext(name);
        }

        public void destroySubcontext(String s) throws NamingException {
            rootContext.destroySubcontext(s);
        }

        public Context createSubcontext(Name name) throws NamingException {
            return rootContext.createSubcontext(name);
        }

        public Context createSubcontext(String s) throws NamingException {
            return rootContext.createSubcontext(s);
        }

        public Object lookupLink(Name name) throws NamingException {
            return rootContext.lookupLink(name);
        }

        public Object lookupLink(String s) throws NamingException {
            return rootContext.lookupLink(s);
        }

        public NameParser getNameParser(Name name) throws NamingException {
            return rootContext.getNameParser(name);
        }

        public NameParser getNameParser(String s) throws NamingException {
            return rootContext.getNameParser(s);
        }

        public Name composeName(Name name, Name name1) throws NamingException {
            return rootContext.composeName(name, name1);
        }

        public String composeName(String s, String s1) throws NamingException {
            return rootContext.composeName(s, s1);
        }

        public Object addToEnvironment(String s, Object o) throws NamingException {
            return rootContext.addToEnvironment(s, o);
        }

        public Object removeFromEnvironment(String s) throws NamingException {
            return rootContext.removeFromEnvironment(s);
        }

        public Hashtable<?, ?> getEnvironment() throws NamingException {
            return rootContext.getEnvironment();
        }

        public void close() throws NamingException {
            rootContext.close();
        }

        public String getNameInNamespace() throws NamingException {
            return rootContext.getNameInNamespace();
        }
    }
}
