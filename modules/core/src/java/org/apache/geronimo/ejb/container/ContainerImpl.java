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
package org.apache.geronimo.ejb.container;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import javax.management.ObjectName;

import org.apache.geronimo.common.AbstractComponent;
import org.apache.geronimo.common.Component;
import org.apache.geronimo.common.Interceptor;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.State;
import org.apache.geronimo.common.Container;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public class ContainerImpl extends AbstractComponent implements Container {
    private final Map plugins = new LinkedHashMap();
    private final Map pluginObjects = new LinkedHashMap();
    private final LinkedList interceptors = new LinkedList();
    // for efficency keep a reference to the first interceptor
    private Interceptor firstInterceptor;

    public InvocationResult invoke(Invocation invocation) throws Exception {
        return firstInterceptor.invoke(invocation);
    }

    public void addInterceptor(Interceptor interceptor) {
        if (firstInterceptor == null) {
            firstInterceptor = interceptor;
            interceptors.addLast(interceptor);
        } else {
            Interceptor lastInterceptor = (Interceptor) interceptors.getLast();
            lastInterceptor.setNext(interceptor);
            interceptors.addLast(interceptor);
        }
    }

    public void create() throws Exception {
        super.create();
        // Create all the interceptors in forward insertion order
        for (Iterator iterator = pluginObjects.values().iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof Component) {
                Component component = (Component) object;
                component.setContainer(this);
                component.create();
            }
        }

        // Create all the plugins in forward insertion order
        for (Iterator iterator = interceptors.iterator(); iterator.hasNext();) {
            Interceptor interceptor = (Interceptor) iterator.next();
            interceptor.setContainer(this);
            interceptor.create();
        }
    }

    public void start() throws Exception {
        super.start();
        // Start all the interceptors in forward insertion order
        for (Iterator iterator = pluginObjects.values().iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof Component) {
                Component component = (Component) object;
                component.start();
            }
        }

        // Start all the plugins in forward insertion order
        for (Iterator iterator = interceptors.iterator(); iterator.hasNext();) {
            Interceptor interceptor = (Interceptor) iterator.next();
            interceptor.start();
        }
    }

    public void stop() {
        // Stop all the interceptors in reverse insertion order
        for (ListIterator iterator = interceptors.listIterator(interceptors.size()); iterator.hasPrevious();) {
            Interceptor interceptor = (Interceptor) iterator.previous();
            interceptor.stop();
        }

        // Stop all the plugins in reverse insertion order
        LinkedList list = new LinkedList();
        for (Iterator iterator = pluginObjects.values().iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof Component) {
                list.addFirst(object);
            }
        }
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Component component = (Component) iterator.next();
            component.stop();
        }

        super.stop();
    }

    public void destroy() {
        // Destroy all the interceptors in reverse insertion order
        for (ListIterator iterator = interceptors.listIterator(interceptors.size()); iterator.hasPrevious();) {
            Interceptor interceptor = (Interceptor) iterator.previous();
            interceptor.destroy();
            interceptor.setContainer(null);
        }

        // Destroy all the plugins in reverse insertion order
        LinkedList list = new LinkedList();
        for (Iterator iterator = pluginObjects.values().iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof Component) {
                list.addFirst(object);
            }
        }
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Component component = (Component) iterator.next();
            component.destroy();
            component.setContainer(null);
        }

        plugins.clear();
        pluginObjects.clear();
        super.destroy();
    }

    public ObjectName getPlugin(String logicalPluginName) {
        return (ObjectName) plugins.get(logicalPluginName);
    }

    public void putPlugin(String logicalPluginName, ObjectName objectName) {
        State state = getState();
        if (state != State.NOT_CREATED && state != State.DESTROYED) {
            throw new IllegalStateException("putPluginObject can only be called while in the not-created or destroyed states: state=" + state);
        }
        if (state == State.NOT_CREATED && objectName == null) {
            throw new IllegalArgumentException("Container has not been created; objectName must be NOT null.");
        }
        if (state == State.DESTROYED && objectName != null) {
            throw new IllegalArgumentException("Container has been destroyed; objectName must be null.");
        }

        plugins.put(logicalPluginName, objectName);
    }

    public Object getPluginObject(String logicalPluginName) {
        return pluginObjects.get(logicalPluginName);
    }

    public void putPluginObject(String logicalPluginName, Object plugin) {
        State state = getState();
        if (state != State.NOT_CREATED && state != State.DESTROYED) {
            throw new IllegalStateException("putPluginObject can only be called while in the not-created or destroyed states: state=" + state);
        }
        if (state == State.NOT_CREATED && plugin == null) {
            throw new IllegalArgumentException("Container has not been created; plugin must be NOT null.");
        }
        if (state == State.DESTROYED && plugin != null) {
            throw new IllegalArgumentException("Container has been destroyed; plugin must be null.");
        }

        pluginObjects.put(logicalPluginName, plugin);
    }
}

