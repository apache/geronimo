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

package org.apache.geronimo.common.jmx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.collections.ReferenceMap;
import org.apache.geronimo.common.NullArgumentException;

/**
 * This class handles invocations for MBean proxies.
 *
 * @todo remove this as noone is using it other then a test case... all uses have been swiched back to
 * abstract MBeanProxyHandler which is used by both the MBeanProxyFactory and RelationshipMBeanProxyFactory
 * also this class does lazy loading which is definately not what we want.
 *
 * @version $Revision: 1.8 $ $Date: 2004/03/10 09:58:26 $
 */
public class MBeanProxyHandler
    implements InvocationHandler, MBeanProxyContext
{
    protected final MBeanServer server;
    protected ObjectName target;
    protected Map attributeMap;
    protected Map taskCache;
    
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
        
        // Lazy init the attribute map
        if (attributeMap == null) {
            // Allow sub-class overrides
            ObjectName target = getObjectName();
            
            // Get information about the target
            MBeanInfo info = server.getMBeanInfo(target);
            
            // Load up the attribute mapping
            attributeMap = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT);
            MBeanAttributeInfo[] attributes = info.getAttributes();
            for (int i=0; i<attributes.length; i++) {
                attributeMap.put(attributes[i].getName(), attributes[i]);
            }
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
        // Lazy init the cache
        if (taskCache == null) {
            taskCache = new ReferenceMap(ReferenceMap.SOFT, ReferenceMap.SOFT);
        }
        
        // Check if there is a cached task
        Task task = (Task)taskCache.get(method);
        
        // If not create one and cache it
        if (task == null) {
            task = createTask(method, args);
            taskCache.put(method, task);
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
