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
package org.apache.geronimo.core.service;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.core.service.AbstractContainer;

/**
 * Base class for a Container that can accept invocations.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:25:19 $
 */
public class AbstractRPCContainer extends AbstractContainer implements RPCContainer {
    // @todo access to these objects must be synchronized
    private final Map plugins = new LinkedHashMap();
    private final Map pluginObjects = new LinkedHashMap();
    private final LinkedList interceptors = new LinkedList();
    private Interceptor firstInterceptor;

    public void postDeregister() {
        plugins.clear();
        pluginObjects.clear();
        interceptors.clear();
        firstInterceptor = null;
        super.postDeregister();
    }

    public final InvocationResult invoke(Invocation invocation) throws Throwable {
        if (getStateInstance() != State.RUNNING) {
            throw new IllegalStateException("invoke can only be called after the Container has started");
        }
        return firstInterceptor.invoke(invocation);
    }

    /**
     * Add a Component to this Container.
     *
     * @param component a <code>Component</code> value
     */
    public final void addComponent(Component component) {
        if (component == null) {
            return;
        }

        if (component instanceof Interceptor) {
            addInterceptor((Interceptor) component);
            return;
        }

        throw new IllegalStateException("Cannot add component of type " + component.getClass() + " to an RPCContainer");
    }

    /**
     * Add an Interceptor to the end of the Interceptor list.
     *
     * @param interceptor
     */
    public final void addInterceptor(Interceptor interceptor) {
        if (getStateInstance() == State.RUNNING) {
            throw new IllegalStateException("Interceptors cannot be added if the Container is running");
        }

        if (firstInterceptor == null) {
            firstInterceptor = interceptor;
            interceptors.addLast(interceptor);
        } else {
            Interceptor lastInterceptor = (Interceptor) interceptors.getLast();
            lastInterceptor.setNext(interceptor);
            interceptors.addLast(interceptor);
        }
    }

    /**
     * Clear the interceptor stack
     */
    public final void clearInterceptors() {
        if (getStateInstance() == State.RUNNING) {
            throw new IllegalStateException("Interceptors cannot be cleared is the Container is running");
        }
        interceptors.clear();
        firstInterceptor = null;
    }

    public final ObjectName getPlugin(String logicalPluginName) {
        return (ObjectName) plugins.get(logicalPluginName);
    }

    public final void putPlugin(String logicalPluginName, ObjectName objectName) {
        if (getStateInstance() != State.STOPPED) {
            throw new IllegalStateException(
                    "putPluginObject can only be called while in the stopped state: state="
                    + getState());
        }
        plugins.put(logicalPluginName, objectName);
    }

    /**
     * @deprecated
     * @see org.apache.geronimo.core.service.RPCContainer#getPluginObject(java.lang.String)
     */
    public final Object getPluginObject(String logicalPluginName) {
        return pluginObjects.get(logicalPluginName);
    }

    /**
     * @deprecated
     * @see org.apache.geronimo.core.service.RPCContainer#putPluginObject(java.lang.String, java.lang.Object)
     */
    public final void putPluginObject(String logicalPluginName, Object plugin) {
        if (getStateInstance() != State.STOPPED) {
            throw new IllegalStateException(
                    "putPluginObject can only be called while in the not-created or destroyed states: state="
                    + getState());
        }
        pluginObjects.put(logicalPluginName, plugin);
    }
}
