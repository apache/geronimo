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
package org.apache.geronimo.gjndi.binding;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingException;

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

/**
 * @version $Rev$ $Date$
 */
public class GBeanBinding implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(GBeanBinding.class);

    private final Context context;
    private final String name;
    private final AbstractNameQuery abstractNameQuery;
    private final Kernel kernel;

    private final LifecycleListener listener = new GBeanLifecycleListener();
    private final LinkedHashMap<AbstractName, Object> bindings = new LinkedHashMap<AbstractName, Object>();

    public GBeanBinding(Context context, String name, AbstractNameQuery abstractNameQuery, Kernel kernel) {
        this.context = context;
        this.name = name;
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
        Set<AbstractName> abstractNames = new HashSet<AbstractName>(bindings.keySet());
        for (AbstractName abstractName : abstractNames) {
            removeBinding(abstractName);
        }
        bindings.clear();
    }

    private class GBeanLifecycleListener extends LifecycleAdapter {
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
        if (bindings.containsKey(abstractName)) {
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

        // preprocess the instance
        instance = preprocessVaue(abstractName, instance);

        addBinding(abstractName, instance);
    }

    private synchronized void addBinding(AbstractName abstractName, Object value) throws NamingException {
        if (bindings.isEmpty()) {
            context.bind(name, value);
        }
        bindings.put(abstractName, value);
    }

    /**
     * Unbinds the specified gbean.
     *
     * @param abstractName the abstract name of the gbean to unbind
     */
    protected synchronized void removeBinding(AbstractName abstractName) {
        Map.Entry entry = first(bindings);
        if (entry != null && entry.getKey().equals(abstractName)) {
            Object oldValue = bindings.remove(abstractName);
            entry = first(bindings);
            if (entry != null) {
                Object newAbstractName = entry.getValue();
                Object newValue = entry.getValue();
                try {
                    context.rebind(name, newValue);
                } catch (NamingException e) {
                    boolean unbound = unbind(abstractName, oldValue);
                    // avoid double logging
                    if (unbound) log.error("Unable to rebind binding " + name + " to " + newAbstractName);
                }
            } else {
                unbind(abstractName, oldValue);
            }
        } else {
            bindings.remove(abstractName);
        }
    }

    private boolean unbind(AbstractName abstractName, Object value) {
        // first check if we are still bound
        try {
            if (context.lookup(name) != value) {
                return true;
            }
        } catch (NamingException ignored) {
            // binding doesn't exist
            return true;
        }

        try {
            context.unbind(name);
            return true;
        } catch (NamingException e1) {
            log.error("Unable to remove binding " + name + " to " + abstractName, e1);
        }
        return false;
    }

    private static Map.Entry first(LinkedHashMap map) {
        if (map.isEmpty()) return null;
        return (Map.Entry) map.entrySet().iterator().next();
    }

    /**
     * Preprocess the value before it is bound.  This is usefult for wrapping values with reference objects.
     * By default, this method simply return the value.
     *
     * @param abstractName the abstract name of the gbean to bind
     * @param value        the gbean instance
     * @return the value to bind
     */
    protected Object preprocessVaue(AbstractName abstractName, Object value) throws NamingException {
        return value;
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(GBeanBinding.class, "GBeanBinding");
        builder.addReference("Context", Context.class);
        builder.addAttribute("name", String.class, true);
        builder.addAttribute("abstractNameQuery", AbstractNameQuery.class, true);
        builder.setConstructor(new String[]{"Context", "name", "abstractNameQuery", "kernel"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
