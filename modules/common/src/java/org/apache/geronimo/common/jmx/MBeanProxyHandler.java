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

package org.apache.geronimo.common.jmx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import java.util.Map;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;

import org.apache.geronimo.common.NullArgumentException;

/**
 * This class handles invocations for MBean proxies.
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/30 20:38:46 $
 */
public class MBeanProxyHandler
    implements InvocationHandler, MBeanProxyContext
{
    protected final MBeanServer server;
    protected ObjectName target;
    protected Map attributeMap;
    protected Map taskCache;
    
    //
    // TODO: Replace cache map with a backing which will not eat memory
    //
    
    public MBeanProxyHandler(final MBeanServer server,
                             final ObjectName target)
    {
        if (server == null) {
            throw new NullArgumentException("server");
        }
        // target can be null
        
        this.server = server;
        this.target = target;
    }
    
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable
    {
        assert proxy != null;
        assert method != null;
        
        Class declaringClass = method.getDeclaringClass();
        
        // if the method belongs to MBeanProxyContext, then invoke locally
        if (declaringClass == MBeanProxyContext.class) {
            return method.invoke(this, args);
        }
        
        //
        // TODO: Handle DynamicMBean.class
        //
        
        try {
            return getTask(method, args).execute(proxy, method, args);
        }
        catch (Throwable t) {
            Throwable decoded = JMXExceptionDecoder.decode(t);
            
            // If it is a RuntimeException or Error just toss is
            if (decoded instanceof RuntimeException) {
                throw (RuntimeException)decoded;
            }
            if (decoded instanceof Error) {
                throw (Error)decoded;
            }
            
            // Attempt to throw a declared exception
            Class[] declared = method.getExceptionTypes();
            for (int i=0; i < declared.length; i++) {
                Class type = declared[i];
                if (type.isInstance(decoded)) {
                    throw decoded;
                }
            }
            
            // Else we don't have much choice, so...
            throw new MBeanProxyException(t);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////
    //                                Tasks                                //
    /////////////////////////////////////////////////////////////////////////
    
    protected interface Task
    {
        Object execute(Object proxy, Method method, Object[] args) throws Throwable;
    }
    
    protected class GetAttributeTask
        implements Task
    {
        private MBeanAttributeInfo info;
        
        public GetAttributeTask(MBeanAttributeInfo info)
        {
            this.info = info;
        }
        
        public Object execute(final Object proxy, final Method method, final Object[] args)
            throws Throwable
        {
            return server.getAttribute(
                getObjectName(), 
                info.getName()
            );
        }
    }
    
    protected class SetAttributeTask
        implements Task
    {
        private MBeanAttributeInfo info;
        
        public SetAttributeTask(MBeanAttributeInfo info)
        {
            this.info = info;
        }
        
        public Object execute(final Object proxy, final Method method, final Object[] args)
            throws Throwable
        {
            server.setAttribute(
                getObjectName(),
                new Attribute(info.getName(), args[0])
            );
            
            return null;
        }
    }

    protected class InvokeOperationTask
        implements Task
    {
        public Object execute(final Object proxy, final Method method, final Object[] args)
            throws Throwable
        {
            String[] signature = null;
            
            if (args != null) {
                signature = new String[args.length];
                Class[] types = method.getParameterTypes();
                
                for (int i=0; i<types.length; i++) {
                    signature[i] = types[i].getName();
                }
            }
            
            return server.invoke(
                getObjectName(),
                method.getName(),
                args,
                signature
            );
        }
    }
    
    protected Task createTask(final Method method, final Object[] args)
        throws Exception
    {
        assert method != null;
        
        if (taskCache == null) {
            // Allow sub-class overrides
            ObjectName target = getObjectName();
            
            // Get information about the target
            MBeanInfo info = server.getMBeanInfo(target);
            
            // Load up the attribute mapping
            attributeMap = new HashMap();
            MBeanAttributeInfo[] attributes = info.getAttributes();
            for (int i=0; i<attributes.length; i++) {
                attributeMap.put(attributes[i].getName(), attributes[i]);
            }
            
            // Initialize the task cache
            taskCache = new HashMap();
        }
        
        String methodName = method.getName();
        
        // Check for getters
        if (args == null) {
            if (methodName.startsWith("get")) {
                String attrName = methodName.substring(3, methodName.length());
                MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
                if (info != null) {
                    //
                    // TODO: Check that return value is what is expected
                    //
                    return new GetAttributeTask(info);
                }
            }
            else if (methodName.startsWith("is")) {
                String attrName = methodName.substring(2, methodName.length());
                MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
                if (info != null && info.isIs()) {
                    // is getters must return booleans
                    Class rtype = method.getReturnType();
                    if (rtype == Boolean.class || rtype == boolean.class) {
                        return new GetAttributeTask(info);
                    }
                }
            }
        }
        
        // Check for setters
        else if (args.length == 1 && methodName.startsWith("set")) {
            String attrName = methodName.substring(3, methodName.length());
            MBeanAttributeInfo info = (MBeanAttributeInfo)attributeMap.get(attrName);
            if (info != null) {
                // setters must return void
                Class rtype = method.getReturnType();
                if (rtype == void.class) {
                    return new SetAttributeTask(info);
                }
            }
        }
        
        // Else it will pass through as an attempted operation
        return new InvokeOperationTask();
    }
    
    protected Task getTask(final Method method, final Object[] args)
        throws Exception
    {
        // Key off of method name & arg count
        Object key = method.getName() + (args == null ? 0 : args.length);
        
        // Check if there is a cached task
        Task task = null;
        if (taskCache != null) {
            task = (Task)taskCache.get(key);
        }
        
        // If not create one and cache it
        if (task == null) {
            task = createTask(method, args);
            taskCache.put(key, task);
        }
        
        return task;
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                           MBeanProxyContext                         //
    /////////////////////////////////////////////////////////////////////////
    
    public ObjectName getObjectName()
    {
        return target;
    }
    
    public MBeanServer getMBeanServer()
    {
        return server;
    }
}
