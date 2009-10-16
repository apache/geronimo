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

package org.apache.geronimo.system.jmx;

import java.util.Iterator;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GOperationSignature;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.NoSuchOperationException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.lifecycle.LifecycleListener;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * @version $Rev$ $Date$
 */
public final class MBeanGBeanBridge implements MBeanRegistration, DynamicMBean, NotificationEmitter {
    private static final Logger log = LoggerFactory.getLogger(MBeanGBeanBridge.class);

    /**
     * The kernel
     */
    private final Kernel kernel;

    /**
     * The unique name of this service.
     */
    private final AbstractName abstractName;
    private final ObjectName objectName;
    private final AbstractNameQuery pattern;

    /**
     * The mbean info
     */
    private final MBeanInfo mbeanInfo;

    /**
     * The broadcaster for notifications
     */
    private final NotificationBroadcasterSupport notificationBroadcaster = new NotificationBroadcasterSupport();
    private final LifecycleBridge lifecycleBridge;

    public MBeanGBeanBridge(Kernel kernel, AbstractName abstractName, ObjectName objectName, MBeanInfo mbeanInfo) {
        this.kernel = kernel;
        this.abstractName = abstractName;
        this.pattern = new AbstractNameQuery(abstractName);
        this.mbeanInfo = mbeanInfo;
        this.objectName = objectName;
        lifecycleBridge = new LifecycleBridge(abstractName, objectName, notificationBroadcaster);
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public NotificationBroadcasterSupport getNotificationBroadcasterSupport() {
        return notificationBroadcaster;
    }
    
    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        return objectName;
    }

    public void postRegister(Boolean registrationDone) {
        if (Boolean.TRUE.equals(registrationDone)) {
            // fire the loaded event from the gbeanMBean.. it was already fired from the GBeanInstance when it was created
            kernel.getLifecycleMonitor().addLifecycleListener(lifecycleBridge, pattern);
            lifecycleBridge.loaded(abstractName);
        }
    }

    public void preDeregister() {
        kernel.getLifecycleMonitor().removeLifecycleListener(lifecycleBridge);
        lifecycleBridge.unloaded(abstractName);
    }

    public void postDeregister() {
    }

    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    public Object getAttribute(String attributeName) throws ReflectionException, AttributeNotFoundException {
        try {
            return kernel.getAttribute(abstractName, attributeName);
        } catch (NoSuchAttributeException e) {
            throw (AttributeNotFoundException)new AttributeNotFoundException(attributeName).initCause(e);
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    public void setAttribute(Attribute attribute) throws ReflectionException, AttributeNotFoundException {
        String attributeName = attribute.getName();
        Object attributeValue = attribute.getValue();
        try {
            kernel.setAttribute(abstractName, attributeName, attributeValue);
        } catch (NoSuchAttributeException e) {
            throw (AttributeNotFoundException)new AttributeNotFoundException(attributeName).initCause(e);
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList results = new AttributeList(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            String name = attributes[i];
            try {
                Object value = getAttribute(name);
                results.add(new Attribute(name, value));
            } catch (JMException e) {
                log.warn("Exception while getting attribute " + name, e);
            }
        }
        return results;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList results = new AttributeList(attributes.size());
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            Attribute attribute = (Attribute) iterator.next();
            try {
                setAttribute(attribute);
                results.add(attribute);
            } catch (JMException e) {
                log.warn("Exception while setting attribute " + attribute.getName(), e);
            }
        }
        return results;
    }

    public Object invoke(String operationName, Object[] arguments, String[] types) throws ReflectionException {
        try {
            return kernel.invoke(abstractName, operationName, arguments, types);
        } catch (NoSuchOperationException e) {
            throw new ReflectionException((NoSuchMethodException)new NoSuchMethodException(new GOperationSignature(operationName, types).toString()).initCause(e));
        } catch (Exception e) {
            throw new ReflectionException(e);
        }
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[]{
            new MBeanNotificationInfo(NotificationType.TYPES, "javax.management.Notification", "J2EE Notifications")
        };
    }

    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) {
        notificationBroadcaster.addNotificationListener(listener, filter, handback);
    }

    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        notificationBroadcaster.removeNotificationListener(listener);
    }

    public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException {
        notificationBroadcaster.removeNotificationListener(listener, filter, handback);
    }

    public String toString() {
        return abstractName.toString();
    }

    private static class LifecycleBridge implements LifecycleListener {
        /**
         * Sequence number used for notifications
         */
        private long sequence;

        /**
         * AbstractName of this MBean
         */
        private final AbstractName mbeanAbstractName;

        /**
         * ObjectName of this MBean
         */
        private final ObjectName objectName;

        /**
         * The notification broadcaster to use
         */
        private final NotificationBroadcasterSupport notificationBroadcaster;

        public LifecycleBridge(AbstractName mbeanAbstractName, ObjectName objectName, NotificationBroadcasterSupport notificationBroadcaster) {
            this.mbeanAbstractName = mbeanAbstractName;
            this.objectName = objectName;
            this.notificationBroadcaster = notificationBroadcaster;
        }

        public void loaded(AbstractName abstractName) {
            if (mbeanAbstractName.equals(abstractName)) {
                notificationBroadcaster.sendNotification(new Notification(NotificationType.OBJECT_CREATED, objectName, nextSequence()));
            }
        }

        public void starting(AbstractName abstractName) {
            if (mbeanAbstractName.equals(abstractName)) {
                notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STARTING, objectName, nextSequence()));
            }
        }

        public void running(AbstractName abstractName) {
            if (mbeanAbstractName.equals(abstractName)) {
                notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_RUNNING, objectName, nextSequence()));
            }
        }

        public void stopping(AbstractName abstractName) {
            if (mbeanAbstractName.equals(abstractName)) {
                notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STOPPING, objectName, nextSequence()));
            }
        }

        public void stopped(AbstractName abstractName) {
            if (mbeanAbstractName.equals(abstractName)) {
                notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_STOPPED, objectName, nextSequence()));
            }
        }

        public void failed(AbstractName abstractName) {
            if (mbeanAbstractName.equals(abstractName)) {
                notificationBroadcaster.sendNotification(new Notification(NotificationType.STATE_FAILED, objectName, nextSequence()));
            }
        }

        public void unloaded(AbstractName abstractName) {
            if (mbeanAbstractName.equals(abstractName)) {
                notificationBroadcaster.sendNotification(new Notification(NotificationType.OBJECT_DELETED, objectName, nextSequence()));
            }
        }

        public synchronized long nextSequence() {
            return sequence++;
        }
    }
}
