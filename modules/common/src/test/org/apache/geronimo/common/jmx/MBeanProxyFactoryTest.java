/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * Unit test for {@link org.apache.geronimo.kernel.jmx.MBeanProxyFactory} class.
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/25 09:57:04 $
 */
public class MBeanProxyFactoryTest extends TestCase {
    public void testNothing() {
    }
/*
    protected MBeanServer server;
    protected ObjectName target;
    protected MockObject targetObject;

    protected void setUp() throws Exception
    {
        server = MBeanServerFactory.createMBeanServer("geronimo.test");
        
        target = new ObjectName("geronimo.test:bean=test");
        targetObject = new MockObject();
        server.registerMBean(targetObject, target);
    }
    
    protected void tearDown() throws Exception
    {
        MBeanServerFactory.releaseMBeanServer(server);
        server = null;
    }

    public void testCreate() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);
    }

    public void testGetString() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);

        String value = bean.getString();
        assertEquals(targetObject.getString(), value);
    }

    public void testSetString() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);

        String value = "newvalue";
        bean.setString(value);
        assertEquals(value, targetObject.getString());
    }

    public void testOperation_Simple() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);

        String value = bean.doIt();
        assertEquals(targetObject.doIt(), value);
    }

    public void testOperation_PoorlyNamed() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);

        String value = bean.setPoorlyNameOperation();
        assertEquals(targetObject.setPoorlyNameOperation(), value);
    }

    public void testOperation_SameNameDiffArgs() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);

        String value = bean.someOperation();
        assertEquals(targetObject.someOperation(), value);

        value = bean.someOperation("foo");
        assertEquals(targetObject.someOperation("foo"), value);

        value = bean.someOperation(true);
        assertEquals(targetObject.someOperation(true), value);
    }

    public void testMBeanProxyContext() throws Exception
    {
        MockObjectMBean bean = (MockObjectMBean)
            MBeanProxyFactory.create(MockObjectMBean.class, server, target);
        assertNotNull(bean);

        assertTrue(bean instanceof MBeanProxyContext);

        MBeanProxyContext ctx = (MBeanProxyContext)bean;
        assertNotNull(ctx.getObjectName());
        assertEquals(target, ctx.getObjectName());
        assertNotNull(ctx.getMBeanServer());
    }
*/
}
