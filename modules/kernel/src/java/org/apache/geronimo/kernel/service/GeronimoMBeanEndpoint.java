/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel.service;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.kernel.management.NotificationType;
import org.apache.geronimo.kernel.management.State;

import net.sf.cglib.reflect.FastMethod;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/11/11 16:39:58 $
 */
public class GeronimoMBeanEndpoint implements NotificationListener, GeronimoMBeanTarget {
    private static final Log log = LogFactory.getLog(GeronimoMBeanEndpoint.class);

    /**
     * Is this class still mutable from users.
     */
    private final boolean immutable;

    /**
     * Is this endpoint running.
     */
    private boolean running = false;

    /**
     * Name of this endpoint.
     */
    private String name;

    /**
     * A user displayable descrption of this endpoint.
     */
    private String description;

    /**
     * Type of this endpoint.
     */
    private String type;

    /**
     * Interface this Geronimo MBean uses to refer to the other.
     */
    private final Class iface;

    /**
     * The object name patters for object to communicate with
     */
    private Collection peers;

    /**
     * The connection to the other mbean
     */
    private Map connections;

    /**
     * The proxies for the connections
     */
    private Map proxies;

    /**
     * Is this endpoint single valued or multi (collection) valued.
     */
    private final boolean singleValued;

    /**
     * Is this endpoint required.  That means the bean can not start until there
     * is a match for the endpoint.  This is only valid for single valued endpoints.
     */
    private boolean required = false;

    /**
     * Logical name of the target.
     */
    private String targetName;

    /**
     * Name of the setter method.
     * The default is "set" + name.  In the case of a defualt value we do a caseless search for the name.
     */
    private String setterName;

    /**
     * The object on which the getter and setter will be invoked
     */
    private final Object target;

    /**
     * The method that will be called to set the attribute value.  If null, the value will only be
     * set into the cache.
     */
    private final FastMethod setterMethod;

    /**
     * Context back to the Geronimo MBean that owns this endpoint
     */
    private GeronimoMBeanContext context;

    /**
     * A reference to the dependency service.
     */
    private DependencyService2MBean dependency;

    public GeronimoMBeanEndpoint() {
        this(null, null, Collections.EMPTY_SET, false);
    }

    public GeronimoMBeanEndpoint(String name, String type) {
        this(name, type, Collections.EMPTY_SET, false);
    }

    public GeronimoMBeanEndpoint(String name, String type, ObjectName pattern) {
        this(name, type, Collections.singleton(pattern), false);
    }

    public GeronimoMBeanEndpoint(String name, String type, ObjectName pattern, boolean required) {
        this(name, type, Collections.singleton(pattern), required);
    }

    public GeronimoMBeanEndpoint(String name, String type, ObjectName pattern, boolean required, String target) {
        this(name, type, Collections.singleton(pattern), required, target);
    }

    public GeronimoMBeanEndpoint(String name, String type, Collection peers) {
        this(name, type, peers, false);
    }

    public GeronimoMBeanEndpoint(String name, String type, Collection peers, boolean required) {
        this(name, type, peers, required, null);
    }

    public GeronimoMBeanEndpoint(String name, String type, Collection peers, boolean required, String targetName) {
        this.name = name;
        this.type = type;
        this.peers = new HashSet(peers);
        this.required = required;
        this.targetName = targetName;

        iface = null;
        target = null;
        setterMethod = null;
        singleValued = false;
        immutable = false;
    }

    /**
     * Creates an immutable copy of the source GeronimoAttributeInfo.
     * @param source the source GeronimoAttributeInfo to copy
     * @param parent the GeronimoMBeanInfo that will contain this attribute
     */
    GeronimoMBeanEndpoint(GeronimoMBeanEndpoint source, GeronimoMBeanInfo parent) {
        immutable = true;

        //
        // Required
        //

        // name
        if (source.name == null) {
            throw new IllegalArgumentException("Source must have a name");
        }
        name = source.name;

        // peers
        if (source.peers.isEmpty()) {
            throw new IllegalArgumentException("Source must have at lease one peer specified");
        }
        peers = new HashSet(source.peers);

        // interface type
        if (source.type == null) {
            throw new IllegalArgumentException("Source must have a type specified");
        }
        type = source.type;
        try {
            iface = Thread.currentThread().getContextClassLoader().loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Interface class could not be loaded: type=" + type);
        }
//        if (!iface.isInterface()) {
//            throw new IllegalArgumentException("Interface class is not an interface: type=" + type);
//        }

        // required
        required = source.required;


        //
        // Optional
        //

        // setterName
        if (source.setterName != null) {
            setterName = source.setterName;
        }

        name = source.name;
        description = source.description;

        //
        // Optional (derived)
        //

        // target
        if (source.target != null) {
            targetName = source.targetName;
            target = source.target;
        } else if (source.targetName == null) {
            targetName = GeronimoMBeanInfo.DEFAULT_TARGET_NAME;
            target = parent.getTarget();
        } else {
            targetName = source.targetName;
            target = parent.getTarget(targetName);
            if (target == null) {
                throw new IllegalArgumentException("Target not found: targetName=" + targetName);
            }
        }

        // setter proxy
        Method[] methods = target.getClass().getMethods();
        Method setterJavaMethod = null;
        if (setterName == null) {
            setterName = "set" + name;
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 && setterName.equalsIgnoreCase(method.getName())) {
                    setterJavaMethod = method;
                    setterName = method.getName();
                    break;
                }
            }
        } else {
            // even though we have an exact name we need to search the methods because we don't know the parameter type
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 && setterName.equals(method.getName())) {
                    setterJavaMethod = method;
                    break;
                }
            }
        }

        if (setterJavaMethod == null) {
            throw new IllegalArgumentException("Setter method not found on target:" +
                    " setterName=" + setterName +
                    " targetClass=" + target.getClass().getName());
        }

        if (Collection.class == setterJavaMethod.getParameterTypes()[0]) {
            singleValued = false;
        } else if (setterJavaMethod.getParameterTypes()[0].isAssignableFrom(iface)) {
            singleValued = true;
        } else {
            throw new IllegalArgumentException("Setter parameter must be Collection or " + type);
        }

        setterMethod = parent.getTargetFastClass(targetName).getMethod(setterJavaMethod);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.type = type;
    }

    public Collection getPeers() {
        return peers;
    }

    public void setPeers(Collection peers) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.peers = new HashSet(peers);
    }

    public void addPeer(ObjectName peer) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        peers.add(peer);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.required = required;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.targetName = targetName;
    }

    public String getSetterName() {
        return setterName;
    }

    public void setSetterName(String setterName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.setterName = setterName;
    }

    public synchronized void setMBeanContext(GeronimoMBeanContext context) {
        if (context != null) {
            this.context = context;

            MBeanServer server = context.getServer();
            try {
                // get a proxy to the dependency service
                dependency = (DependencyService2MBean) MBeanProxyFactory.getProxy(
                        DependencyService2MBean.class,
                        server,
                        new ObjectName("geronimo.boot:role=DependencyService2"));

                // register for mbean registration notifications
                NotificationFilterSupport mbeanServerFilter = new NotificationFilterSupport();
                mbeanServerFilter.enableType(MBeanServerNotification.REGISTRATION_NOTIFICATION);
                mbeanServerFilter.enableType(MBeanServerNotification.UNREGISTRATION_NOTIFICATION);
                context.getServer().addNotificationListener(JMXUtil.DELEGATE_NAME, this, mbeanServerFilter, null);
            } catch (JMException e) {
                // this will never happen... all of the above is well formed
                throw new AssertionError(e);
            }

            // setup the inital connections
            connections = new HashMap();
            proxies = new HashMap();
            for (Iterator targetIterator = peers.iterator(); targetIterator.hasNext();) {
                ObjectName target = (ObjectName) targetIterator.next();
                Set names = server.queryNames(target, null);
                for (Iterator objectNameIterator = names.iterator(); objectNameIterator.hasNext();) {
                    ObjectName peer = (ObjectName) objectNameIterator.next();
                    // if we haven't seen this one before
                    if (!connections.containsKey(peer)) {
                        // register for state change notifications
                        try {
                            server.addNotificationListener(peer, this, NotificationType.STATE_CHANGE_FILTER, null);
                        } catch (InstanceNotFoundException e) {
                            // the instance died before we could get going... not a big deal
                            break;
                        }

                        // if the bean is running add a connection
                        if (isRunning(peer)) {
                            addConnection(peer);
                        }
                    }
                }
            }
        } else {
            MBeanServer server = this.context.getServer();
            ObjectName objectName = this.context.getObjectName();
            this.context = null;

            // we are ending so we need to remove all holds
            if (required) {
                dependency.removeStartHolds(objectName, peers);
            }

            // close all the connections
            for (Iterator iterator = connections.values().iterator(); iterator.hasNext();) {
                GeronimoMBeanEndpointConnection connection = (GeronimoMBeanEndpointConnection) iterator.next();
                try {
                    server.removeNotificationListener(connection.getObjectName(), this);
                } catch (JMException e) {
                    // no big deal.. just being a good citizen
                }
                if (required) {
                    dependency.removeDependency(objectName, connection.getObjectName());
                }
                connection.invalidate();
            }
            connections.clear();
            connections = null;
            if (proxies != null) {
                proxies.clear();
                proxies = null;
            }
            dependency = null;
            try {
                server.removeNotificationListener(JMXUtil.DELEGATE_NAME, this);
            } catch (JMException ignore) {
                // not important... just means the server is not valid or we were never registered
            }
        }
    }

    public synchronized boolean canStart() {
        if (running) {
            return false;
        }
        if (required && connections.size() != 1) {
            return false;
        }
        if (singleValued && connections.size() > 1) {
            return false;
        }
        return true;
    }

    public synchronized void doStart() {
        if (running) {
            throw new IllegalStateException("Endpoint is already running");
        }

        proxies = new HashMap();

        // Do we have enough connections?
        if (!canStart()) {
            context.fail();
            return;
        }
        running = true;

        // open all the connections
        for (Iterator iterator = connections.values().iterator(); iterator.hasNext();) {
            GeronimoMBeanEndpointConnection connection = (GeronimoMBeanEndpointConnection) iterator.next();
            connection.open();
            proxies.put(connection.getObjectName(), connection.getProxy());
        }

        // set the collection or instance proxy into the target
        if (singleValued) {
            if (connections.isEmpty()) {
                setEndpointProxy(null);
            } else {
                // we must block all other mbeans with matching name from starting
                dependency.addStartHolds(context.getObjectName(), peers);

                // set the connection into the target
                GeronimoMBeanEndpointConnection connection = (GeronimoMBeanEndpointConnection) connections.values().iterator().next();
                setEndpointProxy(connection.getProxy());
            }
        } else {
            setEndpointProxy(Collections.unmodifiableCollection(proxies.values()));
        }
    }

    public boolean canStop() {
        return true;
    }

    public synchronized void doStop() {
        if (!running) {
            return;
        }

        running = false;

        // set null into target
        setEndpointProxy(null);

        // close all the connections
        for (Iterator iterator = connections.values().iterator(); iterator.hasNext();) {
            GeronimoMBeanEndpointConnection connection = (GeronimoMBeanEndpointConnection) iterator.next();
            connection.close();
        }
        proxies.clear();
        proxies = null;

        // we must block all other mbeans with matching name from starting
        dependency.removeStartHolds(context.getObjectName(), peers);
    }

    public void doFail() {
        doStop();
    }

    private synchronized void setEndpointProxy(Object proxy) {
        try {
            setterMethod.invoke(target, new Object[]{proxy});
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            throw new AssertionError(t);
        }
    }

    public synchronized void handleNotification(Notification notification, Object o) {
        MBeanServer server = context.getServer();
        String type = notification.getType();

        if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(type)) {
            ObjectName source = ((MBeanServerNotification) notification).getMBeanName();

            // if this is not a possible peer we are done
            if (!isPossiblePeer(source)) {
                return;
            }

            // register for state change notifications
            try {
                server.addNotificationListener(source, this, NotificationType.STATE_CHANGE_FILTER, null);
            } catch (InstanceNotFoundException e) {
                // the instance died before we could get going... not a big deal
                return;
            }

            if (isRunning(source)) {
                addConnection(source);
            }
        } else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
            final ObjectName source = ((MBeanServerNotification) notification).getMBeanName();
            if (connections.containsKey(source)) {
                removeConnection(source);
            }
        } else if (NotificationType.STATE_RUNNING.equals(type)) {
            final ObjectName source = (ObjectName) notification.getSource();
            if (isPossiblePeer(source)) {
                addConnection(source);
            }
        } else if (NotificationType.STATE_STOPPED.equals(type)) {
            final ObjectName source = (ObjectName) notification.getSource();
            if (connections.containsKey(source)) {
                removeConnection(source);
            }
        } else if (NotificationType.STATE_FAILED.equals(type)) {
            final ObjectName source = (ObjectName) notification.getSource();
            if (connections.containsKey(source)) {
                if (running && required) {
                    context.fail();
                } else {
                    removeConnection(source);
                }
            }
        }
    }

    /**
     * Adds a connection to a peer.  This does not actively open a connection, only creates one.
     * If we end up with only one connectiona and are required, a dependency will be declared, and
     * the component will attempt to fully start.  On the other hand, if we are required and we now
     * have two connections, we remove the depenency to the first we are in an ambiguous state and
     * can no longer start.
     *
     * @param peer the name of the component to create a connection to
     */
    private synchronized void addConnection(ObjectName peer) {
        if (connections.containsKey(peer)) {
            // we already have an connection to this peer
            return;
        }

        // if required update the dependencies
        if (required) {
            if (connections.size() == 1) {
                // there is now more then one possible parent so we need to remove our dependency on the current one
                GeronimoMBeanEndpointConnection connection =
                        (GeronimoMBeanEndpointConnection) connections.values().iterator().next();
                dependency.removeDependency(context.getObjectName(), connection.getObjectName());
            } else if (connections.isEmpty()) {
                dependency.addDependency(context.getObjectName(), peer);
            }
        }

        // create a connection
        final GeronimoMBeanEndpointConnection connection = new GeronimoMBeanEndpointConnection(iface, context.getServer(), peer);
        connections.put(peer, connection);

        // update running state
        if (required) {
            if (running) {
                // this is bad... someone else started runnign that matches our hold list
                context.fail();
                return;
            }

            // if we have a single connection we are ready to start
            if (connections.size() == 1) {
                try {
                    context.start();
                } catch (Exception e) {
                    log.warn("A problem occured while attemping to start", e);
                }
            }
        } else if (running) {
            // this is a new connection in a running endpoint, open it
            connection.open();
            proxies.put(connection.getObjectName(), connection.getProxy());

            if (singleValued) {
                setEndpointProxy(connection.getProxy());
            }
        }
    }

    /**
     * Removes a connection to a component.  If the connection is open it will be closed and any
     * proxies to the connection will be dropped.  If we are required and after the removal we have
     * only a single connection, the component will attempt to fully start.
     * @param peer
     */
    private synchronized void removeConnection(ObjectName peer) {
        if (connections.containsKey(peer)) {
            // we already have an connection to this peer
            return;
        }

        GeronimoMBeanEndpointConnection connection =
                (GeronimoMBeanEndpointConnection) connections.remove(peer);

        if (required) {
            // Update the dependencies
            dependency.removeDependency(context.getObjectName(), peer);
            if (connections.size() == 1) {
                GeronimoMBeanEndpointConnection parent =
                        (GeronimoMBeanEndpointConnection) connections.values().iterator().next();
                dependency.addDependency(context.getObjectName(), parent.getObjectName());
            }

            if (running) {
                // this is bad... mbean we were connected to stopped
                context.fail();
                return;
            }

            // if we are left with a singled connection we are ready to start
            if (connections.size() == 1) {
                try {
                    context.start();
                } catch (Exception e) {
                    log.warn("A problem occured while attemping to start", e);
                }
            }
        }
        if (running) {
            // connection died... clean up
            if (singleValued) {
                setEndpointProxy(null);
            }

            proxies.remove(connection.getObjectName());
            connection.close();
        }
    }

    /**
     * Is the component in the Running state
     * @param objectName name of the component to check
     * @return true if the component is running; false otherwise
     */
    private boolean isRunning(ObjectName objectName) {
        try {
            final int state = ((Integer) context.getServer().getAttribute(objectName, "state")).intValue();
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
        for (Iterator iterator = peers.iterator(); iterator.hasNext();) {
            ObjectName pattern = (ObjectName) iterator.next();
            if (pattern.apply(objectName)) {
                return true;
            }
        }
        return false;
    }
}
