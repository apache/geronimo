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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.transaction.UserTransaction;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.reference.ClassLoaderAwareReference;
import org.apache.geronimo.naming.reference.KernelAwareReference;

/**
 * @version $Rev: 6682 $ $Date$
 */
public final class EnterpriseNamingContext extends AbstractReadOnlyContext {
    private final Map localBindings;

    private final Map globalBindings;

    public static Context createEnterpriseNamingContext(Map componentContext, UserTransaction userTransaction, Kernel kernel, ClassLoader classLoader) throws NamingException {
        Map map = new HashMap();
        if (componentContext != null) {
            map.putAll(componentContext);
        }

        for (Iterator iterator = map.values().iterator(); iterator.hasNext();) {
            Object value = iterator.next();
            if (value instanceof KernelAwareReference) {
                ((KernelAwareReference) value).setKernel(kernel);
            }
            if (value instanceof ClassLoaderAwareReference) {
                ((ClassLoaderAwareReference) value).setClassLoader(classLoader);
            }
        }

        if (userTransaction != null) {
            map.put("UserTransaction", userTransaction);
        }

        Context enc = EnterpriseNamingContext.createEnterpriseNamingContext(map);
        return enc;
    }

    public static Context createEnterpriseNamingContext(Map context) throws NamingException {
        return new EnterpriseNamingContext(context);
    }

    public EnterpriseNamingContext(Map context) throws NamingException {
        super("");
        validateBindings(context);
        preprocessBindings(context);

        Node rootContext = buildTree(context);

        Map localBindings = new HashMap(rootContext.size());
        for (Iterator iterator = rootContext.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Node) {
                Context nestedContext = new NestedEnterpriseNamingContext(name, (Node) value);
                localBindings.put(name, nestedContext);
            } else {
                localBindings.put(name, value);
            }
        }
        this.localBindings = Collections.unmodifiableMap(localBindings);


        Map globalBindings = buildGlobalBindings("", localBindings);
        this.globalBindings = Collections.unmodifiableMap(globalBindings);
    }

    private static void validateBindings(Map bindings) {
        for (Iterator iterator = bindings.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Context) {
                throw new IllegalArgumentException("EnterpriseNamingContext can not contain a nested Context object: name=" + name);
            }
            if (value instanceof LinkRef) {
                throw new IllegalArgumentException("EnterpriseNamingContext can not contain a nested LinkRef object: name=" + name);
            }
        }
    }

    private static void preprocessBindings(Map bindings) {
        for (Iterator iterator = new HashMap(bindings).entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Reference) {
                bindings.put(name, new CachingReference(name, (Reference)value));
            }
        }
    }

    private static Node buildTree(Map context) throws NamingException {
        Node rootContext = new Node();

        // ENC must always contain an env context
        rootContext.put("env", new Node());

        for (Iterator iterator = context.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();

            Node parentContext = rootContext;

            Name compoundName = EnterpriseNamingContextNameParser.INSTANCE.parse(name);
            for (Enumeration parts = compoundName.getAll(); parts.hasMoreElements(); ) {
                String part = (String) parts.nextElement();
                // the last element in the path is the name of the value
                if (parts.hasMoreElements()) {
                    // nest context into parent
                    Node bindings = (Node) parentContext.get(part);
                    if (bindings == null) {
                        bindings = new Node();
                        parentContext.put(part, bindings);
                    }

                    parentContext = bindings;
                }
            }

            parentContext.put(compoundName.get(compoundName.size() - 1), value);
        }
        return rootContext;
    }

    private static Map buildGlobalBindings(String nameInNamespace, Map context) {
        String path = nameInNamespace;
        if (path.length() > 0) {
            path += "/";
        }

        Map globalBindings = new HashMap();
        for (Iterator iterator = context.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof NestedEnterpriseNamingContext) {
                NestedEnterpriseNamingContext nestedContext = (NestedEnterpriseNamingContext)value;
                globalBindings.putAll(buildGlobalBindings(nestedContext.getNameInNamespace(), nestedContext.localBindings));
            }
            globalBindings.put(path + name, value);
        }
        return globalBindings;
    }

    protected Map getGlobalBindings() {
        return globalBindings;
    }

    protected Map getLocalBindings() {
        return localBindings;
    }

    /**
     * Nested context which shares the global bindings map.
     */
    public final class NestedEnterpriseNamingContext extends AbstractReadOnlyContext {
        private final Map localBindings;

        public NestedEnterpriseNamingContext(String nameInNamespace, Node bindings) {
            super(nameInNamespace);
            if (nameInNamespace.length() == 0) throw new IllegalArgumentException("nameInNamespace is empty");

            Map localBindings = new HashMap(bindings.size());
            for (Iterator iterator = bindings.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Node) {
                    Context context = new NestedEnterpriseNamingContext(nameInNamespace + "/" + name, (Node) value);
                    localBindings.put(name, context);
                } else {
                    localBindings.put(name, value);
                }
            }
            this.localBindings = Collections.unmodifiableMap(localBindings);
        }

        protected Map getGlobalBindings() {
            return globalBindings;
        }

        protected Map getLocalBindings() {
            return localBindings;
        }
    }

    /**
     * Lame subclass of hashmap used to differentiate between a Map in the context an a nested element during tree building
     */
    private static final class Node extends HashMap {
    }
}
