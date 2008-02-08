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
package org.apache.geronimo.gjndi;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.ContextNotEmptyException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleAdapter;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.xbean.naming.context.ContextAccess;
import org.apache.xbean.naming.context.WritableContext;

/**
 * @version $Rev$ $Date$
 */
public class KernelContextGBean extends WritableContext implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(KernelContextGBean.class);

    private final Kernel kernel;
    private final AbstractNameQuery abstractNameQuery;
    private final LifecycleListener listener = new ContextLifecycleListener();
    private final Map<AbstractName, Set<Name>> bindingsByAbstractName = new HashMap<AbstractName, Set<Name>>();

    public KernelContextGBean(String nameInNamespace, AbstractNameQuery abstractNameQuery, Kernel kernel) throws NamingException {
        super(nameInNamespace, Collections.EMPTY_MAP, ContextAccess.MODIFIABLE, false);
        this.abstractNameQuery = abstractNameQuery;
        this.kernel = kernel;
    }

    public synchronized void doStart() {
        kernel.getLifecycleMonitor().addLifecycleListener(listener, abstractNameQuery);
        Set<AbstractName> set = kernel.listGBeans(abstractNameQuery);
        for (AbstractName abstractName : set) {
            try {
                if (kernel.isRunning(abstractName)) {
                    addBinding(abstractName);
                }
            } catch (NamingException e) {
                log.error("Error adding binding for " + abstractName, e);
            }
        }

    }

    public void doStop() {
        destroy();
    }

    public void doFail() {
        destroy();
    }

    private synchronized void destroy() {
        kernel.getLifecycleMonitor().removeLifecycleListener(listener);
        Set<AbstractName> abstractNames = new HashSet<AbstractName>(bindingsByAbstractName.keySet());
        for (AbstractName abstractName : abstractNames) {
            removeBinding(abstractName);
        }
        bindingsByAbstractName.clear();
    }

    private class ContextLifecycleListener extends LifecycleAdapter {
        public void running(AbstractName abstractName) {
            try {
                addBinding(abstractName);
            } catch (NamingException e) {
                log.error("Error adding binding for " + abstractName);
            }
        }

        public void stopping(AbstractName abstractName) {
            removeBinding(abstractName);
        }

        public void stopped(AbstractName abstractName) {
            removeBinding(abstractName);
        }

        public void failed(AbstractName abstractName) {
            removeBinding(abstractName);
        }

        public void unloaded(AbstractName abstractName) {
            removeBinding(abstractName);
        }
    }

    /**
     * Binds the specified gbean.  This method uses createBindingName and preprocessValue before binding the object.
     *
     * @param abstractName the abstract name of the gbean to bind
     * @throws NamingException if an error occurs during binding
     */
    protected synchronized void addBinding(AbstractName abstractName) throws NamingException {
        if (bindingsByAbstractName.containsKey(abstractName)) {
            // previously bound
            return;
        }

        // get the gbean
        Object instance;
        try {
            instance = kernel.getGBean(abstractName);
        } catch (GBeanNotFoundException e) {
            throw (NamingException)new NamingException("GBean not found: " + abstractName).initCause(e);
        }

        // create the bindings for this object
        Map<Name, Object> bindings = createBindings(abstractName, instance);
        if (bindings == null || bindings.isEmpty()) {
            return;
        }

        // bind the value
        for (Map.Entry<Name, Object> entry : bindings.entrySet()) {
            Name name = entry.getKey();
            Object value = entry.getValue();
            addBinding(abstractName, name, value);
        }

        // remember where we bound this value
        bindingsByAbstractName.put(abstractName, bindings.keySet());
    }

    private Map<Name, LinkedHashMap<AbstractName, Object>> bindingsByName = new HashMap<Name, LinkedHashMap<AbstractName, Object>>();

    private synchronized void addBinding(AbstractName abstractName, Name name, Object value) throws NamingException {
        LinkedHashMap<AbstractName, Object> bindings = bindingsByName.get(name);
        if (bindings == null) {
            addDeepBinding(name, value, true, true);

            bindings = new LinkedHashMap<AbstractName, Object>();
            bindings.put(abstractName, value);
            bindingsByName.put(name, bindings);
            log.info("bound gbean " + abstractName + " at name " + name);
        } else {
            bindings.put(abstractName, value);
        }
    }

    /**
     * Unbinds the specified gbean.
     *
     * @param abstractName the abstract name of the gbean to unbind
     */
    protected synchronized void removeBinding(AbstractName abstractName) {
        Set<Name> bindingNames = bindingsByAbstractName.remove(abstractName);
        if (bindingNames == null) return;

        for (Name name : bindingNames) {

            LinkedHashMap<AbstractName, Object> bindings = bindingsByName.get(name);
            if (bindings == null) continue;

            if (first(bindings).getKey().equals(abstractName)) {
                bindings.remove(abstractName);
                Map.Entry<AbstractName, Object> newEntry = first(bindings);
                if (newEntry != null) {
                    Object newValue = newEntry.getValue();
                    try {
                        addDeepBinding(name, newValue, true, true);
                    } catch (NamingException e) {
                        boolean logged = false;
                        try {
                            removeDeepBinding(name, true);
                        } catch (NamingException e1) {
                            logged = true;
                            log.error("Unable to remove binding " + name + " to " + abstractName, e);
                        }
                        if (!logged) log.error("Unable to rebind binding " + name + " to " + newEntry.getKey());
                    }
                } else {
                    bindingsByName.remove(name);
                    try {
                        removeDeepBinding(name, true, true);
                    } catch (ContextNotEmptyException e) {
                        //ignore
                    } catch (NamingException e) {
                        log.error("Unable to remove binding " + name + " to " + abstractName, e);
                    }
                    log.info("unbound gbean " + abstractName + " at name " + name);
                }
            } else {
                bindings.remove(abstractName);
            }
        }
    }

    private static Map.Entry<AbstractName, Object> first(LinkedHashMap<AbstractName, Object> map) {
        if (map.isEmpty()) return null;
        return map.entrySet().iterator().next();
    }

    protected Map<Name, Object> createBindings(AbstractName abstractName, Object value) throws NamingException {
        // generate a name for this binding
        Name name = createBindingName(abstractName, value);
        if (name == null) return null;

        // give sub classes a chance to preprocess the value
        value = preprocessVaue(abstractName, name, value);
        if (value == null) return null;

        return Collections.singletonMap(name, value);
    }

    /**
     * Create a name under which we will bind the specified gbean with the specified value.
     * By default, this method simply returns the "name" element of the abstract name
     *
     * @param abstractName the abstract name of the gbean to bind
     * @param value        the gbean instance
     * @return the name under which the gbean should be bound
     * @throws javax.naming.NamingException should something go wrong
     */
    protected Name createBindingName(AbstractName abstractName, Object value) throws NamingException {
        String shortName = (String) abstractName.getName().get("name");
        return getNameParser().parse(shortName);
    }

    /**
     * Preprocess the value before it is bound.  This is usefult for wrapping values with reference objects.
     * By default, this method simply return the value.
     *
     * @param abstractName the abstract name of the gbean to bind
     * @param name         the name under which the gbean will be bound
     * @param value        the gbean instance
     * @return the value to bind
     * @throws javax.naming.NamingException should something go wrong
     */
    protected Object preprocessVaue(AbstractName abstractName, Name name, Object value) throws NamingException {
        return value;
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(KernelContextGBean.class, "Context");
        builder.addAttribute("nameInNamespace", String.class, true);
        builder.addAttribute("abstractNameQuery", AbstractNameQuery.class, true);
        builder.setConstructor(new String[]{"nameInNamespace", "abstractNameQuery", "kernel"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
