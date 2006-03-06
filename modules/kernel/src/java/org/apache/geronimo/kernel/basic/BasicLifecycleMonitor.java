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

package org.apache.geronimo.kernel.basic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleMonitor;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.gbean.runtime.LifecycleBroadcaster;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev$ $Date$
 */
public class BasicLifecycleMonitor implements LifecycleMonitor {
    private static final Log log = LogFactory.getLog(BasicLifecycleMonitor.class);


    // todo we should only hold weak references to the listeners
    private final Map boundListeners = new HashMap();
    private final Map listenerPatterns = new HashMap();

    public BasicLifecycleMonitor(Kernel kernel) {

        // register for state change notifications with all mbeans that match the target patterns
        Set names = kernel.listGBeans((AbstractNameQuery)null);
        for (Iterator objectNameIterator = names.iterator(); objectNameIterator.hasNext();) {
            addSource((AbstractName) objectNameIterator.next());
        }
    }

    public synchronized void destroy() {
        boundListeners.clear();
        listenerPatterns.clear();
    }

    private synchronized void addSource(AbstractName source) {
        if (boundListeners.containsKey(source)) {
            // already registered
            return;
        }

        // find all listeners interested in events from this source
        HashSet listeners = new HashSet();
        for (Iterator listenerIterator = listenerPatterns.entrySet().iterator(); listenerIterator.hasNext();) {
            Map.Entry entry = (Map.Entry) listenerIterator.next();
            Set patterns = (Set) entry.getValue();
            for (Iterator patternIterator = patterns.iterator(); patternIterator.hasNext();) {
                AbstractNameQuery pattern = (AbstractNameQuery) patternIterator.next();
                if (pattern.matches(source)) {
                    LifecycleListener listener = (LifecycleListener) entry.getKey();
                    listeners.add(listener);
                }
            }
        }

        boundListeners.put(source, listeners);
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
                if (pattern.matches(source)) {
                    Set listeners = (Set) entry.getValue();
                    listeners.add(listener);
                }
            }
        }
        listenerPatterns.put(listener, patterns);
    }

    public synchronized void removeLifecycleListener(LifecycleListener listener) {
        for (Iterator iterator = boundListeners.values().iterator(); iterator.hasNext();) {
            Set set = (Set) iterator.next();
            set.remove(listener);
        }
        listenerPatterns.remove(listener);
    }

    private synchronized Set getTargets(AbstractName source) {
        Set targets = (Set) boundListeners.get(source);
        if (targets == null) {
            // no one is interested in this event
            return Collections.EMPTY_SET;
        } else {
            return new HashSet(targets);
        }
    }

    private void fireLoadedEvent(AbstractName refInfoName) {
        Set targets = getTargets(refInfoName);
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
        Set targets = getTargets(source);
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
        Set targets = getTargets(source);
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
        Set targets = getTargets(source);
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
        Set targets = getTargets(source);
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
        Set targets = getTargets(source);
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
        Set targets = getTargets(source);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.unloaded(source);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    public LifecycleBroadcaster createLifecycleBroadcaster(AbstractName abstractName) {
        return new RawLifecycleBroadcaster(abstractName);
    }

    private class RawLifecycleBroadcaster implements LifecycleBroadcaster {
        private final AbstractName abstractName;

        public RawLifecycleBroadcaster(AbstractName abstractName) {
            this.abstractName = abstractName;
        }

        public void fireLoadedEvent() {
            addSource(abstractName);
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


}
