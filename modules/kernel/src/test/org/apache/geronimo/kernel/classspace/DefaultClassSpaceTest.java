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
package org.apache.geronimo.kernel.classspace;

import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.loading.ClassLoaderRepository;

import org.apache.geronimo.kernel.service.GeronimoMBeanContext;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.1 $ $Date: 2003/10/28 23:43:22 $
 */
public class DefaultClassSpaceTest extends TestCase {
    private ClassLoader parentClassLoader = new URLClassLoader(new URL[0]);

    public void testParent() throws Exception {
        DefaultClassSpace classSpace = new DefaultClassSpace();
        classSpace.setMBeanContext(new GeronimoMBeanContext(new MockMBeanServer(), null, new ObjectName("mybeans", "type", "test")));

        ObjectName parent = new ObjectName("blah", "foo", "bar");
        classSpace.setParent(parent);
        assertEquals(parent, classSpace.getParent());
        assertEquals(parent, classSpace.getCurrentParent());

        // start and set a new parent
        classSpace.doStart();
        ObjectName newParent = new ObjectName("a", "b", "c");
        classSpace.setParent(newParent);

        // current parent should still be original parent
        assertEquals(parent, classSpace.getCurrentParent());

        // parent should be the new parent
        assertEquals(newParent, classSpace.getParent());

        // stop
        classSpace.doStop();

        // both should be the new parent
        assertEquals(newParent, classSpace.getParent());
        assertEquals(newParent, classSpace.getCurrentParent());

        // start
        classSpace.doStart();

        // both should still be the new parent
        assertEquals(newParent, classSpace.getParent());
        assertEquals(newParent, classSpace.getCurrentParent());

        classSpace.doStop();
    }

    public void testGetClassLoader() throws Exception {
        DefaultClassSpace classSpace = new DefaultClassSpace();
        classSpace.setMBeanContext(new GeronimoMBeanContext(new MockMBeanServer(), null, new ObjectName("mybeans", "type", "test")));
        classSpace.setParent(new ObjectName("blah", "foo", "bar"));

        // not started so class loader should be null
        assertNull(classSpace.getClassLoader());

        // start
        classSpace.doStart();

        // running so class loader should not be null
        ClassLoader loader = classSpace.getClassLoader();
        assertNotNull(classSpace.getClassLoader());

        // parent of the class loader should be parentClassLoader
        assertSame(parentClassLoader, loader.getParent());

        // stop
        classSpace.doStop();

        // not started so class loader should be null
        assertNull(classSpace.getClassLoader());

        // start
        classSpace.doStart();

        // running so class loader should not be null
        assertNotNull(classSpace.getClassLoader());

        // the class loader should have been recycled so we should get a new reference
        assertNotSame(loader, classSpace.getClassLoader());

        // parent of the new class loader should also be parentClassLaoder
        assertSame(parentClassLoader, classSpace.getClassLoader().getParent());

        // stop
        classSpace.doStop();
    }

    public void testDeployment() throws Exception {
        DefaultClassSpace classSpace = new DefaultClassSpace();
        classSpace.setMBeanContext(new GeronimoMBeanContext(new MockMBeanServer(), null, new ObjectName("mybeans", "type", "test")));

        // Deployment one
        ObjectName deploymentOne = new ObjectName("deployment", "number", "one");
        ArrayList deploymentOneURLs = new ArrayList(2);
        deploymentOneURLs.add(new URL("http://deploymentOne/urlOne"));
        deploymentOneURLs.add(new URL("http://deploymentOne/urlTwo"));
        HashMap deploymentOneMap = new HashMap();
        deploymentOneMap.put(deploymentOne,  deploymentOneURLs);

        // Deployment two
        ObjectName deploymentTwo = new ObjectName("deployment", "number", "two");
        ArrayList deploymentTwoURLs = new ArrayList(2);
        deploymentTwoURLs.add(new URL("http://deploymentTwo/urlOne"));
        deploymentTwoURLs.add(new URL("http://deploymentTwo/urlTwo"));
        HashMap deploymentTwoMap = new HashMap();
        deploymentTwoMap.put(deploymentTwo, deploymentTwoURLs);

        // Deployment one and two combined
        HashMap deploymentOneAndTwoMap = new HashMap();
        deploymentOneAndTwoMap.put(deploymentOne, deploymentOneURLs);
        deploymentOneAndTwoMap.put(deploymentTwo, deploymentTwoURLs);
        ArrayList deploymentOneAndTwoURLs = new ArrayList(4);
        deploymentOneAndTwoURLs.addAll(deploymentOneURLs);
        deploymentOneAndTwoURLs.addAll(deploymentTwoURLs);

        // List with deployment two before deployment one
        ArrayList deploymentTwoAndOneURLs = new ArrayList(4);
        deploymentTwoAndOneURLs.addAll(deploymentTwoURLs);
        deploymentTwoAndOneURLs.addAll(deploymentOneURLs);

        // add and verify deployment one
        classSpace.addDeployment(deploymentOne, deploymentOneURLs);
        assertEquals(deploymentOneMap, classSpace.getURLsByDeployment());
        assertEquals(deploymentOneMap, classSpace.getCurrentURLsByDeployment());
        assertEquals(deploymentOneURLs, classSpace.getURLs());
        assertEquals(deploymentOneURLs, classSpace.getCurrentURLs());

        // start
        classSpace.doStart();
        assertEquals(deploymentOneMap, classSpace.getURLsByDeployment());
        assertEquals(deploymentOneMap, classSpace.getCurrentURLsByDeployment());
        assertEquals(deploymentOneURLs, classSpace.getURLs());
        assertEquals(deploymentOneURLs, classSpace.getCurrentURLs());

        // add deployment two
        classSpace.addDeployment(deploymentTwo, deploymentTwoURLs);
        assertEquals(deploymentOneAndTwoMap, classSpace.getURLsByDeployment());
        assertEquals(deploymentOneAndTwoMap, classSpace.getCurrentURLsByDeployment());
        assertEquals(deploymentOneAndTwoURLs, classSpace.getURLs());
        assertEquals(deploymentOneAndTwoURLs, classSpace.getCurrentURLs());
        Iterator iterator = classSpace.getCurrentURLsByDeployment().keySet().iterator();
        assertEquals(deploymentOne,  iterator.next());
        assertEquals(deploymentTwo,  iterator.next());

        // remove deployment one (should not be removed from current)
        classSpace.dropDeployment(deploymentOne);
        assertEquals(deploymentTwoMap, classSpace.getURLsByDeployment());
        assertEquals(deploymentOneAndTwoMap, classSpace.getCurrentURLsByDeployment());
        assertEquals(deploymentTwoURLs, classSpace.getURLs());
        assertEquals(deploymentOneAndTwoURLs, classSpace.getCurrentURLs());

        // restart
        classSpace.doStop();
        classSpace.doStart();

        // only deployment two should remain
        assertEquals(deploymentTwoMap, classSpace.getURLsByDeployment());
        assertEquals(deploymentTwoMap, classSpace.getCurrentURLsByDeployment());
        assertEquals(deploymentTwoURLs, classSpace.getURLs());
        assertEquals(deploymentTwoURLs, classSpace.getCurrentURLs());

        // add deployment one back
        classSpace.addDeployment(deploymentOne, deploymentOneURLs);
        assertEquals(deploymentOneAndTwoMap, classSpace.getURLsByDeployment());
        assertEquals(deploymentOneAndTwoMap, classSpace.getCurrentURLsByDeployment());
        assertEquals(deploymentTwoAndOneURLs, classSpace.getURLs());
        assertEquals(deploymentTwoAndOneURLs, classSpace.getCurrentURLs());
        iterator = classSpace.getCurrentURLsByDeployment().keySet().iterator();
        assertEquals(deploymentTwo,  iterator.next());
        assertEquals(deploymentOne,  iterator.next());
    }

    private class MockMBeanServer implements MBeanServer {
        public Object invoke(ObjectName objectName, String methodName, Object[] args, String[] parameters)
                throws InstanceNotFoundException, MBeanException, ReflectionException {
            return parentClassLoader;
        }

        public void addNotificationListener(ObjectName observed, NotificationListener listener, NotificationFilter filter, Object handback)
                throws InstanceNotFoundException {
        }

        public void addNotificationListener(ObjectName observed, ObjectName listener, NotificationFilter filter, Object handback)
                throws InstanceNotFoundException {
        }

        public void removeNotificationListener(ObjectName observed, ObjectName listener)
                throws InstanceNotFoundException, ListenerNotFoundException {
        }

        public void removeNotificationListener(ObjectName observed, NotificationListener listener)
                throws InstanceNotFoundException, ListenerNotFoundException {
        }

        public void removeNotificationListener(ObjectName observed, ObjectName listener, NotificationFilter filter, Object handback)
                throws InstanceNotFoundException, ListenerNotFoundException {
        }

        public void removeNotificationListener(ObjectName observed, NotificationListener listener, NotificationFilter filter, Object handback)
                throws InstanceNotFoundException, ListenerNotFoundException {
        }

        public MBeanInfo getMBeanInfo(ObjectName objectName)
                throws InstanceNotFoundException, IntrospectionException, ReflectionException {
            return null;
        }

        public boolean isInstanceOf(ObjectName objectName, String className)
                throws InstanceNotFoundException {
            return false;
        }

        public String[] getDomains() {
            return new String[0];
        }

        public String getDefaultDomain() {
            return null;
        }

        public ObjectInstance createMBean(String className, ObjectName objectName)
                throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
            return null;
        }

        public ObjectInstance createMBean(String className, ObjectName objectName, ObjectName loaderName)
                throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
            return null;
        }

        public ObjectInstance createMBean(String className, ObjectName objectName, Object[] args, String[] parameters)
                throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException {
            return null;
        }

        public ObjectInstance createMBean(String className, ObjectName objectName, ObjectName loaderName, Object[] args, String[] parameters)
                throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException {
            return null;
        }

        public void unregisterMBean(ObjectName objectName)
                throws InstanceNotFoundException, MBeanRegistrationException {
        }

        public Object getAttribute(ObjectName objectName, String attribute)
                throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
            return null;
        }

        public void setAttribute(ObjectName objectName, Attribute attribute)
                throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        }

        public AttributeList getAttributes(ObjectName objectName, String[] attributes)
                throws InstanceNotFoundException, ReflectionException {
            return null;
        }

        public AttributeList setAttributes(ObjectName objectName, AttributeList attributes)
                throws InstanceNotFoundException, ReflectionException {
            return null;
        }

        public Integer getMBeanCount() {
            return null;
        }

        public boolean isRegistered(ObjectName objectname) {
            return false;
        }

        public ObjectInstance getObjectInstance(ObjectName objectName)
                throws InstanceNotFoundException {
            return null;
        }

        public Set queryMBeans(ObjectName patternName, QueryExp filter) {
            return null;
        }

        public Set queryNames(ObjectName patternName, QueryExp filter) {
            return null;
        }

        public Object instantiate(String className)
                throws ReflectionException, MBeanException {
            return null;
        }

        public Object instantiate(String className, ObjectName loaderName)
                throws ReflectionException, MBeanException, InstanceNotFoundException {
            return null;
        }

        public Object instantiate(String className, Object[] args, String[] parameters)
                throws ReflectionException, MBeanException {
            return null;
        }

        public Object instantiate(String className, ObjectName loaderName, Object[] args, String[] parameters)
                throws ReflectionException, MBeanException, InstanceNotFoundException {
            return null;
        }

        public ObjectInstance registerMBean(Object mbean, ObjectName objectName)
                throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
            return null;
        }

        public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] bytes)
                throws InstanceNotFoundException, OperationsException, ReflectionException {
            return null;
        }

        public ObjectInputStream deserialize(String className, byte[] bytes)
                throws OperationsException, ReflectionException {
            return null;
        }

        public ObjectInputStream deserialize(ObjectName objectName, byte[] bytes)
                throws InstanceNotFoundException, OperationsException {
            return null;
        }

        public ClassLoader getClassLoaderFor(ObjectName mbeanName)
                throws InstanceNotFoundException {
            return null;
        }

        public ClassLoader getClassLoader(ObjectName loaderName)
                throws InstanceNotFoundException {
            return null;
        }

        public ClassLoaderRepository getClassLoaderRepository() {
            return null;
        }
    }
}
