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

import java.lang.reflect.Method;

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * Unit test for {@link MBeanProxyHandler} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/01 19:18:47 $
 */
public class MBeanProxyHandlerTest
    extends TestCase
{
    protected MBeanServer server;
    protected ObjectName target;
    protected MockObject targetObject;
    protected MyMBeanProxyHandler handler;
    
    protected void setUp() throws Exception
    {
        server = MBeanServerFactory.createMBeanServer("geronimo.test");
        
        target = new ObjectName("geronimo.test:bean=test");
        targetObject = new MockObject();
        server.registerMBean(targetObject, target);
        
        handler = new MyMBeanProxyHandler(server, target);
    }
    
    protected void tearDown() throws Exception
    {
        MBeanServerFactory.releaseMBeanServer(server);
        handler = null;
        server = null;
    }
    
    public void testHandlerCreateTask() throws Exception
    {
        Class type = MockObjectMBean.class;
        Method method = type.getMethod("someOperation", new Class[0]);
        Object[] args = new Object[0];
        
        Object task = handler.createTask(method, args);
        assertNotNull(task);
    }
    
    public void testHandlerGetTask() throws Exception
    {
        Class type = MockObjectMBean.class;
        Method method = type.getMethod("someOperation", new Class[0]);
        Object[] args = new Object[0];
        Object task1 = handler.getTask(method, args);
        assertNotNull(task1);
        
        Map taskCache = handler.getTaskCache();
        assertNotNull(taskCache);
        assertEquals(1, taskCache.size());
        
        Object task2 = handler.getTask(method, args);
        assertNotNull(task2);
        assertEquals(task1, task2);
        assertEquals(1, taskCache.size());
    }
    
    //
    // Test MBeanProxyHandler to get access to protected bits
    //
    
    protected class MyMBeanProxyHandler
        extends MBeanProxyHandler
    {
        public MyMBeanProxyHandler(final MBeanServer server,
                                   final ObjectName target)
        {
            super(server, target);
        }
        
        public Map getTaskCache()
        {
            return taskCache;
        }
        
        public Map getAttributeMap()
        {
            return attributeMap;
        }
        
        public Task createTask(final Method method, final Object[] args)
            throws Exception
        {
            return super.createTask(method, args);
        }
        
        public Task getTask(final Method method, final Object[] args)
            throws Exception
        {
            return super.getTask(method, args);
        }
    }
}
