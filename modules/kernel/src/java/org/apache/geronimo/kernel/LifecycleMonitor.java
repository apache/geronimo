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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.NotificationBroadcaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * @version $Rev: 71492 $ $Date: 2004-11-14 21:31:50 -0800 (Sun, 14 Nov 2004) $
 */
public class LifecycleMonitor implements NotificationListener {
    private static final Log log = LogFactory.getLog(LifecycleMonitor.class);

    private final MBeanServer server;

    // todo we should only hold weak references to the listeners
    private final Map boundListeners = new HashMap();
    private final Map listenerPatterns = new HashMap();

    /**
     * @deprecated don't use this yet... it may change or go away
     */
    public LifecycleMonitor(MBeanServer server) {
        this.server = server;

        // listen for all mbean registration events
        try {
            NotificationFilterSupport mbeanServerFilter = new NotificationFilterSupport();
            mbeanServerFilter.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
            mbeanServerFilter.enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
            server.addNotificationListener(JMXUtil.DELEGATE_NAME, this, mbeanServerFilter, null);
        } catch (Exception e) {
            // this will never happen... all of the above is well formed
            throw new AssertionError(e);
        }

        // register for state change notifications with all mbeans that match the target patterns
        Set names = server.queryNames(null, null);
        for (Iterator objectNameIterator = names.iterator(); objectNameIterator.hasNext();) {
            addSource((ObjectName) objectNameIterator.next());
        }

        for (Iterator iterator = boundListeners.keySet().iterator(); iterator.hasNext();) {
            ObjectName source = (ObjectName) iterator.next();
            try {
                if (server.isInstanceOf(source, NotificationBroadcaster.class.getName())) {
                    server.addNotificationListener(source, this, NotificationType.STATE_CHANGE_FILTER, null);
                }
            } catch (InstanceNotFoundException e) {
                // the instance died before we could get going... not a big deal
                break;
            } catch (Throwable e) {
                log.warn("Could not add state change listener to: " + source + " on behalf of objectName", e);
            }
        }
    }

    public synchronized void destroy() {
        try {
            server.removeNotificationListener(JMXUtil.DELEGATE_NAME, this);
        } catch (Exception ignore) {
            // don't care... we tried
        }

        // unregister for all notifications
        for (Iterator iterator = boundListeners.keySet().iterator(); iterator.hasNext();) {
            ObjectName target = (ObjectName) iterator.next();
            try {
                server.removeNotificationListener(target, this, NotificationType.STATE_CHANGE_FILTER, null);
            } catch (Exception ignore) {
                // don't care... we tried
            }
        }

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

    private void fireCreatedEvent(ObjectName objectName) {
        Set targets = getTargets(objectName);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.created(objectName);
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

    private void fireDeleteEvent(ObjectName source) {
        Set targets = getTargets(source);
        for (Iterator iterator = targets.iterator(); iterator.hasNext();) {
            LifecycleListener listener = (LifecycleListener) iterator.next();
            try {
                listener.deleted(source);
            } catch (Throwable e) {
                log.warn("Exception occured while notifying listener", e);
            }
        }
    }

    public void handleNotification(Notification notification, Object o) {
        String type = notification.getType();

        if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(type)) {
            ObjectName source = ((MBeanServerNotification) notification).getMBeanName();
            addSource(source);

            // register for state change notifications
            try {
                server.addNotificationListener(source, this, NotificationType.STATE_CHANGE_FILTER, null);
            } catch (InstanceNotFoundException e) {
                // the instance died before we could get going... not a big deal
                return;
            }
        } else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
            removeSource(((MBeanServerNotification) notification).getMBeanName());
        } else {
            final ObjectName source = (ObjectName) notification.getSource();
            if (NotificationType.OBJECT_CREATED.equals(type)) {
                fireCreatedEvent(source);
            } else if (NotificationType.STATE_STARTING.equals(type)) {
                fireStartingEvent(source);
            } else if (NotificationType.STATE_RUNNING.equals(type)) {
                fireRunningEvent(source);
            } else if (NotificationType.STATE_STOPPING.equals(type)) {
                fireStoppingEvent(source);
            } else if (NotificationType.STATE_STOPPED.equals(type)) {
                fireStoppedEvent(source);
            } else if (NotificationType.OBJECT_DELETED.equals(type)) {
                fireDeleteEvent(source);
            }
        }
    }
}
