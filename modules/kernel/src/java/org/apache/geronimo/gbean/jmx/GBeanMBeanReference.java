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

package org.apache.geronimo.gbean.jmx;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.management.NotificationType;
import org.apache.geronimo.kernel.management.State;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:01 $
 */
public class GBeanMBeanReference implements NotificationListener {
    /**
     * Name of this reference.
     */
    private final String name;

    /**
     * Interface this GBeanMBean uses to refer to the other.
     */
    private final Class type;

    /**
     * Is this reference single valued or multi (collection) valued.
     */
    private final boolean singleValued;

    /**
     * The GBeanMBean to which this reference belongs.
     */
    private final GBeanMBean gmbean;

    /**
     * The method that will be called to set the attribute value.  If null, the value will be set with
     * a constructor argument
     */
    private final MethodInvoker setInvoker;

    /**
     * The target objectName patterns to watch for a connection.
     */
    private Set patterns = Collections.EMPTY_SET;

    /**
     * Proxy to the to this connection.
     */
    private Proxy proxy;

    public GBeanMBeanReference(GBeanMBean gmbean, GReferenceInfo referenceInfo, Class constructorType) throws InvalidConfigurationException {
        this.gmbean = gmbean;
        this.name = referenceInfo.getName();
        try {
            this.type = gmbean.getClassLoader().loadClass(referenceInfo.getType());
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load reference type class:" +
                    " name=" + name +
                    " class=" + referenceInfo.getType());
        }

        Class setterType;
        if (constructorType != null) {
            setterType = constructorType;
            setInvoker = null;
        } else {
            Method setterMethod = searchForSetter(gmbean, referenceInfo);
            setInvoker = new FastMethodInvoker(setterMethod);
            setterType = setterMethod.getParameterTypes()[0];
        }

        // single valued?
        if (Collection.class == setterType) {
            singleValued = false;
        } else if (setterType.isAssignableFrom(type)) {
            singleValued = true;
        } else {
            throw new IllegalArgumentException("Setter parameter or constructor type must be Collection or " + type);
        }

        if (Modifier.isFinal(type.getModifiers())) {
            throw new IllegalArgumentException("Proxy interface cannot be a final class: " + type.getName());
        }
    }

    public String getName() {
        return name;
    }

    public Set getPatterns() {
        return patterns;
    }

    public void setPatterns(Set patterns) {
        if (!gmbean.isOffline()) {
            throw new IllegalStateException("Pattern set can not be modified while online");
        }
        if (patterns == null) {
            this.patterns = Collections.EMPTY_SET;
        } else {
            this.patterns = Collections.unmodifiableSet(patterns);
        }
    }

    public int hashCode() {
        return super.hashCode();
    }

    public Object getProxy() {
        if (patterns.isEmpty()) {
            return null;
        } else {
            return proxy.getProxy();
        }
    }

    public synchronized void online() throws Exception {
        // create the proxy
        if (singleValued) {
            proxy = new SingleProxy(gmbean, name, type, patterns);
        } else {
            proxy = new CollectionProxy(gmbean, name, type);
        }


        // listen for all mbean registration events
        try {
            NotificationFilterSupport mbeanServerFilter = new NotificationFilterSupport();
            mbeanServerFilter.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
            mbeanServerFilter.enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
            gmbean.getServer().addNotificationListener(JMXUtil.DELEGATE_NAME, this, mbeanServerFilter, null);
        } catch (Exception e) {
            // this will never happen... all of the above is well formed
            throw new AssertionError(e);
        }

        // register for state change notifications with all mbeans that match the target patterns
        Set registeredTargets = new HashSet();
        for (Iterator targetIterator = patterns.iterator(); targetIterator.hasNext();) {
            ObjectName pattern = (ObjectName) targetIterator.next();
            Set names = gmbean.getServer().queryNames(pattern, null);
            for (Iterator objectNameIterator = names.iterator(); objectNameIterator.hasNext();) {
                ObjectName target = (ObjectName) objectNameIterator.next();
                if (!registeredTargets.contains(target)) {
                    try {
                        gmbean.getServer().addNotificationListener(target, this, NotificationType.STATE_CHANGE_FILTER, null);
                    } catch (InstanceNotFoundException e) {
                        // the instance died before we could get going... not a big deal
                        break;
                    }

                    // if the bean is running add it to the runningTargets list
                    if (isRunning(target)) {
                        proxy.addTarget(target);
                    }
                }
            }
        }

        // set the proxy into the instance
        if (setInvoker != null && patterns.size() > 0) {
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(gmbean.getClassLoader());
                setInvoker.invoke(gmbean.getTarget(), new Object[]{proxy.getProxy()});
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }
    }

    public synchronized void offline() {
        try {
            gmbean.getServer().removeNotificationListener(JMXUtil.DELEGATE_NAME, this);
        } catch (InstanceNotFoundException ignore) {
            // we don't care... the mbean we were listening to disapeared
        } catch (ListenerNotFoundException ignore) {
            // we don't care... the mbean doesn't think we were listening
        }

        if (proxy == null) {
            //we weren't fully online
            return;
        }

        // get the targets from the proxy because we are listening to them
        Set registeredTargets = proxy.getTargets();

        // destroy the proxy
        proxy.destroy();
        proxy = null;

        // unregister for all notifications
        for (Iterator iterator = registeredTargets.iterator(); iterator.hasNext();) {
            ObjectName target = (ObjectName) iterator.next();
            try {
                gmbean.getServer().removeNotificationListener(target, this, NotificationType.STATE_CHANGE_FILTER, null);
            } catch (InstanceNotFoundException ignore) {
                // we don't care... the mbean we were listening to disapeared
            } catch (ListenerNotFoundException ignore) {
                // we don't care... the mbean doesn't think we were listening
            }
        }
    }


    public synchronized void start() throws WaitingException {
        if (!patterns.isEmpty()) {
            proxy.start();
        }
    }

    public synchronized void stop() {
        if (!patterns.isEmpty()) {
            proxy.stop();
        }
    }

    public synchronized void handleNotification(Notification notification, Object o) {
        String type = notification.getType();

        if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(type)) {
            ObjectName source = ((MBeanServerNotification) notification).getMBeanName();

            // if this is not a possible peer we are done
            if (!isPossiblePeer(source)) {
                return;
            }

            // register for state change notifications
            try {
                gmbean.getServer().addNotificationListener(source, this, NotificationType.STATE_CHANGE_FILTER, null);
            } catch (InstanceNotFoundException e) {
                // the instance died before we could get going... not a big deal
                return;
            }

            if (isRunning(source)) {
                proxy.addTarget(source);
            }
        } else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
            proxy.removeTarget(((MBeanServerNotification) notification).getMBeanName());
        } else if (NotificationType.STATE_RUNNING.equals(type)) {
            final ObjectName source = (ObjectName) notification.getSource();
            if (isPossiblePeer(source)) {
                proxy.addTarget(source);
            }
        } else if (NotificationType.STATE_STOPPED.equals(type) || NotificationType.STATE_FAILED.equals(type)) {
            proxy.removeTarget((ObjectName) notification.getSource());
        }
    }


    /**
     * Is the component in the Running state
     * @param objectName name of the component to check
     * @return true if the component is running; false otherwise
     */
    private boolean isRunning(ObjectName objectName) {
        try {
            final int state = ((Integer) gmbean.getServer().getAttribute(objectName, "state")).intValue();
            return state == State.RUNNING_INDEX;
        } catch (AttributeNotFoundException e) {
            // ok -- mbean is not a startable
            return true;
        } catch (InstanceNotFoundException e) {
            // mbean is no longer registerd
            return false;
        } catch (Exception e) {
            // problem getting the attribute, mbean has most likely failed
            return false;
        }
    }

    /**
     * Is the component a possible peer.  A component is a possible peer if
     * its name matched onee of the object name patterns we watch.
     * @param objectName name of the component to check
     * @return true if the component is a possible peer; false otherwise
     */
    private synchronized boolean isPossiblePeer(ObjectName objectName) {
        for (Iterator iterator = patterns.iterator(); iterator.hasNext();) {
            ObjectName pattern = (ObjectName) iterator.next();
            if (pattern.apply(objectName)) {
                return true;
            }
        }
        return false;
    }

    private static Method searchForSetter(GBeanMBean gMBean, GReferenceInfo referenceInfo) throws InvalidConfigurationException {
        if (referenceInfo.getSetterName() == null) {
            // no explicit name give so we must search for a name
            String setterName = "set" + referenceInfo.getName();
            Method[] methods = gMBean.getType().getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equalsIgnoreCase(method.getName())) {

                    return method;
                }
            }
        } else {
            // even though we have an exact name we need to search the methods becaus we don't know the parameter type
            Method[] methods = gMBean.getType().getMethods();
            String setterName = referenceInfo.getSetterName();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equals(method.getName())) {

                    return method;
                }
            }
        }
        throw new InvalidConfigurationException("Target does not have specified method:" +
                " name=" + referenceInfo.getName() +
                " targetClass=" + gMBean.getType().getName());
    }
}
