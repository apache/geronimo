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

package org.apache.geronimo.kernel.log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoLogFactory extends LogFactory {
    private final static Object factoryLock = new Object();
    // todo this should use weak references
    private static final HashMap instancesByClassLoader = new HashMap();

    private static LogFactory logFactory = new BootstrapLogFactory();

    public GeronimoLogFactory() {
    }

    public LogFactory getLogFactory() {
        synchronized (factoryLock) {
            return logFactory;
        }
    }

    public void setLogFactory(LogFactory logFactory) {
        // change the log factory
        GeronimoLogFactory.logFactory = logFactory;

        // update all known logs to use instances of the new factory
        Set logs = getInstances();
        for (Iterator iterator = logs.iterator(); iterator.hasNext();) {
            GeronimoLog log = (GeronimoLog) iterator.next();
            log.setLog(logFactory.getInstance(log.getName()));
        }
    }

    public Set getInstances() {
        synchronized (factoryLock) {
            Set logs = new HashSet();
            for (Iterator iterator = instancesByClassLoader.values().iterator(); iterator.hasNext();) {
                Map instanceMap = ((Map) iterator.next());
                logs.addAll(instanceMap.values());

            }
            return logs;
        }
    }

    public Log getInstance(Class clazz) throws LogConfigurationException {
        synchronized (factoryLock) {
            return getInstance(clazz.getName());
        }
    }

    public Log getInstance(String name) throws LogConfigurationException {
        synchronized (factoryLock) {
            // get the instances for the context classloader
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Map instances = (Map) instancesByClassLoader.get(contextClassLoader);
            if (instances == null) {
                instances = new HashMap();
                instancesByClassLoader.put(contextClassLoader, instances);
            }

            // get the log
            Log log = (Log) instances.get(name);
            if (log == null) {
                log = new GeronimoLog(name, logFactory.getInstance(name));
                instances.put(name, log);
            }
            return log;
        }
    }

    public void release() {
        synchronized (factoryLock) {
// TODO rethink this - it works for now
//            for (Iterator maps = instancesByClassLoader.values().iterator(); maps.hasNext();) {
//                Map instances = (Map) maps.next();
//                for (Iterator logs = instances.values().iterator(); logs.hasNext();) {
//                    GeronimoLog log = (GeronimoLog) logs.next();
//                    log.setLog(null);
//
//                }
//            }
            instancesByClassLoader.clear();
        }
    }

    public Object getAttribute(String name) {
        synchronized (factoryLock) {
            return logFactory.getAttribute(name);
        }
    }

    public String[] getAttributeNames() {
        synchronized (factoryLock) {
            return logFactory.getAttributeNames();
        }
    }

    public void removeAttribute(String name) {
        synchronized (factoryLock) {
            logFactory.removeAttribute(name);
        }
    }

    public void setAttribute(String name, Object value) {
        synchronized (factoryLock) {
            logFactory.setAttribute(name, value);
        }
    }
}

