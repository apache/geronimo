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

package org.apache.geronimo.kernel.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.runtime.LifecycleBroadcaster;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class BasicLifecycleMonitor implements LifecycleMonitor {
    private static final Logger log = LoggerFactory.getLogger(BasicLifecycleMonitor.class);

    // todo we should only hold weak references to the listeners
    /**
     * Map of AbstractName to set of Listeners interested in this name.
     */
    private final Map boundListeners = new HashMap();

    /**
     * Map of listener to patterns they are interested in.
     */
    private final Map listenerPatterns = new LinkedHashMap();

    public BasicLifecycleMonitor(Kernel kernel) {

        // register for state change notifications with all mbeans that match the target patterns
        Set names = kernel.listGBeans((AbstractNameQuery)null);
        for (Iterator objectNameIterator = names.iterator(); objectNameIterator.hasNext();) {
            AbstractName source = (AbstractName) objectNameIterator.next();
            GBeanData gBeanData;
            try {
                gBeanData = kernel.getGBeanData(source);
            } catch (GBeanNotFoundException e) {
                //this should never happen
                throw new AssertionError(e);
            }
            addSource(source, gBeanData.getGBeanInfo().getInterfaces());
        }
    }

    public synchronized void destroy() {
        boundListeners.clear();
        listenerPatterns.clear();
    }

    private synchronized void addSource(AbstractName source, Set interfaceTypes) {
        if (boundListeners.containsKey(source)) {
            // already registered
            return;
        }

        // find all listeners interested in events from this source
        SourceInfo sourceInfo = new SourceInfo(interfaceTypes);
        Set listeners = sourceInfo.getListeners();
        for (Iterator listenerIterator = listenerPatterns.entrySet().iterator(); listenerIterator.hasNext();) {
            Map.Entry entry = (Map.Entry) listenerIterator.next();
            Set patterns = (Set) entry.getValue();
            for (Iterator patternIterator = patterns.iterator(); patternIterator.hasNext();) {
                AbstractNameQuery pattern = (AbstractNameQuery) patternIterator.next();
                if (pattern.matches(source, interfaceTypes)) {
                    LifecycleListener listener = (LifecycleListener) entry.getKey();
                    listeners.add(listener);
                }
            }
        }

        boundListeners.put(source, sourceInfo);
    }

    private synchronized void removeSource(AbstractName source) {
        boundListeners.remove(source);
    }

    public synchronized void addLifecycleListener(LifecycleListener listener, AbstractNameQuery pattern) {
        addLifecycleListener(listener, Collections.singleton(pattern));
    }

    public synchronized void addLifecycleListener(LifecycleListener listener, Set patterns) {
        for (Iterator patternIterator = patterns.iterator(); patternIterator.hasNext();) {
            AbstractNameQuery pattern = (AbstractNameQuery) patternIterator.next();
            for (Iterator iterator = boundListeners.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                AbstractName source = (AbstractName) entry.getKey();
                SourceInfo sourceInfo = (SourceInfo) entry.getValue();
                if (pattern.matches(source, sourceInfo.getInterfaceTypes())) {
                    Set listeners = sourceInfo.getListeners();
                    listeners.add(listener);
                }
            }
        }
        listenerPatterns.put(listener, patterns);
    }

    public synchronized void removeLifecycleListener(LifecycleListener listener) {
        for (Iterator iterator = boundListeners.values().iterator(); iterator.hasNext();) {
            SourceInfo sourceInfo = (SourceInfo) iterator.next();
            sourceInfo.getListeners().remove(listener);
        }
        listenerPatterns.remove(listener);
    }

    private synchronized Collection getTargets(AbstractName source) {
        SourceInfo targets = (SourceInfo) boundListeners.get(source);
        if (targets == null) {
            // no one is interested in this event
            return Collections.EMPTY_SET;
        } else {
            return new ArrayList<LifecycleListener>(targets.getListeners());
        }
    }

    private void fireLoadedEvent(AbstractName refInfoName) {
        Collection targets = getTargets(refInfoName);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.loaded(refInfoName);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    private void fireStartingEvent(AbstractName source) {
        Collection targets = getTargets(source);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.starting(source);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    private void fireRunningEvent(AbstractName source) {
        Collection targets = getTargets(source);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.running(source);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    private void fireStoppingEvent(AbstractName source) {
        Collection targets = getTargets(source);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.stopping(source);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    private void fireStoppedEvent(AbstractName source) {
        Collection targets = getTargets(source);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.stopped(source);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    private void fireFailedEvent(AbstractName source) {
        Collection targets = getTargets(source);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.failed(source);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    private void fireUnloadedEvent(AbstractName source) {
        Collection targets = getTargets(source);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.unloaded(source);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    public LifecycleBroadcaster createLifecycleBroadcaster(AbstractName abstractName, Set interfaceTypes) {
        return new RawLifecycleBroadcaster(abstractName, interfaceTypes);
    }

    private class RawLifecycleBroadcaster implements LifecycleBroadcaster {
        private final AbstractName abstractName;
        private final Set interfaceTypes;

        public RawLifecycleBroadcaster(AbstractName abstractName, Set interfaceTypes) {
            this.abstractName = abstractName;
            this.interfaceTypes = interfaceTypes;
        }

        public void fireLoadedEvent() {
            addSource(abstractName, interfaceTypes);
            BasicLifecycleMonitor.this.fireLoadedEvent(abstractName);
        }

        public void fireStartingEvent() {
            BasicLifecycleMonitor.this.fireStartingEvent(abstractName);
        }

        public void fireRunningEvent() {
            BasicLifecycleMonitor.this.fireRunningEvent(abstractName);
        }

        public void fireStoppingEvent() {
            BasicLifecycleMonitor.this.fireStoppingEvent(abstractName);
        }

        public void fireStoppedEvent() {
            BasicLifecycleMonitor.this.fireStoppedEvent(abstractName);
        }

        public void fireFailedEvent() {
            BasicLifecycleMonitor.this.fireFailedEvent(abstractName);
        }

        public void fireUnloadedEvent() {
            BasicLifecycleMonitor.this.fireUnloadedEvent(abstractName);
            removeSource(abstractName);
        }
    }

    private final class SourceInfo {
        private final Set interfaceTypes;
        private final HashSet listeners = new LinkedHashSet();

        public SourceInfo(Set interfaceTypes) {
            this.interfaceTypes = interfaceTypes;
        }

        public Set getInterfaceTypes() {
            return interfaceTypes;
        }

        public Set getListeners() {
            return listeners;
        }
    }

}
