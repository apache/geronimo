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

import org.apache.geronimo.common.AbstractContainer;
import org.apache.geronimo.common.Component;
import org.apache.geronimo.common.Interceptor;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.State;

/**
 *
 *
 * @todo Currently this class implements the startRecursive method of 
 * the JSR77 lifecycle. This should be moved to the AbstractContainer class
 * @todo The stop method is implemented as stopRecursive, which should be moved
 * to an abstractContainer class
 * @version $Revision: 1.5 $ $Date: 2003/08/14 07:14:34 $
 */
public class ContainerImpl extends AbstractContainer
{
    private final Map plugins= new LinkedHashMap();
    private final Map pluginObjects= new LinkedHashMap();
    private final LinkedList interceptors= new LinkedList();
    // for efficency keep a reference to the first interceptor
    private Interceptor firstInterceptor;

    public InvocationResult invoke(Invocation invocation) throws Exception
    {
        return firstInterceptor.invoke(invocation);
    }

    public void addInterceptor(Interceptor interceptor)
    {
        if (firstInterceptor == null)
        {
            firstInterceptor= interceptor;
            interceptors.addLast(interceptor);
        }
        else
        {
            Interceptor lastInterceptor= (Interceptor)interceptors.getLast();
            lastInterceptor.setNext(interceptor);
            interceptors.addLast(interceptor);
        }
    }

    public void startRecursive() throws Exception
    {
        try
        {
            // start this component
            setState(State.STARTING);

            // Start all the components in forward insertion order
            for (Iterator iterator= pluginObjects.values().iterator(); iterator.hasNext();)
            {
                Object object= iterator.next();
                if (object instanceof Component)
                {
                    Component component= (Component)object;
                    component.startRecursive();
                }
            }

            // Start this component
            doStart();

            setState(State.RUNNING);
        }
        finally
        {
            if (getStateInstance() != State.RUNNING)
                setState(State.FAILED);
        }
    }

    public void doStart() throws Exception
    {
        // Start all the plugins in forward insertion order
        for (Iterator iterator= interceptors.iterator(); iterator.hasNext();)
        {
            Interceptor interceptor= (Interceptor)iterator.next();
            interceptor.start();
        }
    }

    public void stop()
    {
        try
        {
            setState(State.STOPPING);

            // @todo this is actually a stopRecursive, which is not supported
            // by JSR77
            // Stop all the plugins in reverse insertion order
            LinkedList list= new LinkedList();
            for (Iterator iterator= pluginObjects.values().iterator(); iterator.hasNext();)
            {
                Object object= iterator.next();
                if (object instanceof Component)
                {
                    list.addFirst(object);
                }
            }
            for (Iterator iterator= list.iterator(); iterator.hasNext();)
            {
                Component component= (Component)iterator.next();
                component.stop();
            }

            // Stop this component
            doStop();

            setState(State.STOPPED);
        }
        finally
        {
            if (getStateInstance() != State.STOPPED)
                setState(State.FAILED);
        }
    }

    public void doStop()
    {
        // Stop all the interceptors in reverse insertion order
        for (ListIterator iterator= interceptors.listIterator(interceptors.size()); iterator.hasPrevious();)
        {
            Interceptor interceptor= (Interceptor)iterator.previous();
            interceptor.stop();
        }
    }

    // @todo destroy not supported in JSR77 lifecycle, needs to be
    // integrated or removed.
    public void destroy()
    {
        plugins.clear();
        pluginObjects.clear();
    }

    public ObjectName getPlugin(String logicalPluginName)
    {
        return (ObjectName)plugins.get(logicalPluginName);
    }

    public void putPlugin(String logicalPluginName, ObjectName objectName)
    {
        State state= getStateInstance();
        if (state != State.STOPPED)
        {
            throw new IllegalStateException(
                "putPluginObject can only be called while in the stopped state: state=" + state);
        }
        plugins.put(logicalPluginName, objectName);
    }

    public Object getPluginObject(String logicalPluginName)
    {
        return pluginObjects.get(logicalPluginName);
    }

    public void putPluginObject(String logicalPluginName, Object plugin)
    {
        State state= getStateInstance();
        if (state != State.STOPPED)
        {
            throw new IllegalStateException(
                "putPluginObject can only be called while in the not-created or destroyed states: state=" + state);
        }
        pluginObjects.put(logicalPluginName, plugin);
    }
}
