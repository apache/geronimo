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

import java.lang.reflect.Method;

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * Unit test for {@link MBeanProxyHandler} class.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:27 $
 */
public class MBeanProxyHandlerTest extends TestCase {
    public void testNothing() {
    }
/*
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
*/
}
