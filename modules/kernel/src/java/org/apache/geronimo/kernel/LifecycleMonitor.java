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

package org.apache.geronimo.kernel;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.runtime.LifecycleBroadcaster;

/**
 * @version $Rev: 71492 $ $Date: 2004-11-14 21:31:50 -0800 (Sun, 14 Nov 2004) $
 */
public class LifecycleMonitor {
    private static final Log log = LogFactory.getLog(LifecycleMonitor.class);

    private final Kernel kernel;

    // todo we should only hold weak references to the listeners
    private final Map boundListeners = new HashMap();
    private final Map listenerPatterns = new HashMap();

    /**
     * @deprecated don't use this yet... it may change or go away
     */
    public LifecycleMonitor(Kernel kernel) {
        this.kernel = kernel;

        // register for state change notifications with all mbeans that match the target patterns
        Set names = this.kernel.listGBeans((ObjectName)null);
        for (Iterator objectNameIterator = names.iterator(); objectNameIterator.hasNext();) {
            addSource((ObjectName) objectNameIterator.next());
        }
    }

    public synchronized void destroy() {
        boundListeners.clear();
        listenerPatterns.clear();
    }

    private synchronized void addSource(ObjectName source) {
        if (boundListeners.containsKey(source)) {
            // alreayd registered
            return;
        }

        // find all listeners interested in events from this source
        HashSet listeners = new HashSet();
        for (Iterator listenerIterator = listenerPatterns.entrySet().iterator(); listenerIterator.hasNext();) {
            Map.Entry entry = (Map.Entry) listenerIterator.next();
            Set patterns = (Set) entry.getValue();
            for (Iterator patternIterator = patterns.iterator(); patternIterator.hasNext();) {
                ObjectName pattern = (ObjectName) patternIterator.next();
                if (pattern.apply(source)) {
                    LifecycleListener listener = (LifecycleListener) entry.getKey();
                    listeners.add(listener);
                }
            }
        }

        boundListeners.put(source, listeners);
    }

    private synchronized void removeSource(ObjectName source) {
        boundListeners.remove(source);
    }

    public synchronized void addLifecycleListener(LifecycleListener listener, ObjectName pattern) {
        addLifecycleListener(listener, Collections.singleton(pattern));
    }

    public synchronized void addLifecycleListener(LifecycleListener listener, Set patterns) {
        for (Iterator patternIterator = patterns.iterator(); patternIterator.hasNext();) {
            ObjectName pattern = (ObjectName) patternIterator.next();
            for (Iterator iterator = boundListeners.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                ObjectName source = (ObjectName) entry.getKey();
                if (pattern.apply(source)) {
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

    private synchronized Set getTargets(ObjectName source) {
        Set targets = (Set) boundListeners.get(source);
        if (targets == null) {
            // no one is interested in this event
            return Collections.EMPTY_SET;
        } else {
            return new HashSet(targets);
        }
    }

    private void fireLoadedEvent(ObjectName objectName) {
        Set targets = getTargets(objectName);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.loaded(objectName);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    private void fireStartingEvent(ObjectName source) {
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

    private void fireRunningEvent(ObjectName source) {
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

    private void fireStoppingEvent(ObjectName source) {
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

    private void fireStoppedEvent(ObjectName source) {
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

    private void fireFailedEvent(ObjectName source) {
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

    private void fireUnloadedEvent(ObjectName source) {
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

    /**
     * @deprecated is this for internal use by the GBeanInstance and will be remove later
     */
    public LifecycleBroadcaster createLifecycleBroadcaster(ObjectName objectName) {
        return new RawLifecycleBroadcaster(objectName);
    }

    private class RawLifecycleBroadcaster implements LifecycleBroadcaster {
        private final ObjectName objectName;

        public RawLifecycleBroadcaster(ObjectName objectName) {
            this.objectName = objectName;
        }

        public void fireLoadedEvent() {
            addSource(objectName);
            LifecycleMonitor.this.fireLoadedEvent(objectName);
        }

        public void fireStartingEvent() {
            LifecycleMonitor.this.fireStartingEvent(objectName);
        }

        public void fireRunningEvent() {
            LifecycleMonitor.this.fireRunningEvent(objectName);
        }

        public void fireStoppingEvent() {
            LifecycleMonitor.this.fireStoppingEvent(objectName);
        }

        public void fireStoppedEvent() {
            LifecycleMonitor.this.fireStoppedEvent(objectName);
        }

        public void fireFailedEvent() {
            LifecycleMonitor.this.fireFailedEvent(objectName);
        }

        public void fireUnloadedEvent() {
            LifecycleMonitor.this.fireUnloadedEvent(objectName);
            removeSource(objectName);
        }
    }
}
