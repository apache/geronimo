/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.concurrent.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.geronimo.management.ManagedConstants;
import org.testng.annotations.Test;

public class ManagedThreadFactoryTest extends ConcurrentTest {
    
    public String getServletName() {
        return "ManagedThreadFactoryServlet";
    }
        
    private ObjectName getManagedThreadFactory() throws Exception {
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        
        String name = "DefaultManagedThreadFactory";
        
        Set<ObjectName> objectNameSet = 
            mbServerConn.queryNames(new ObjectName("*:j2eeType=ManagedThreadFactory,name=" + name + ",*"), null);
        assertEquals(1, objectNameSet.size());
        
        return objectNameSet.iterator().next();
    }
    
    @Test
    public void testBasicContextMigration() throws Exception {
        invokeTest("testBasicContextMigration"); 
    }
    
    @Test
    public void testSecurityContextMigration() throws Exception {
        invokeSecureTest("testSecurityContextMigration"); 
    }
    
    @Test
    public void testHungReleaseTaskNotification() throws Exception { 
        ObjectName threadFactory = getManagedThreadFactory();
                
        invokeTest("testLongTask");
                                
        String expectedTaskName = "TestRunnable long";
        String expectedTaskDesc = expectedTaskName;
        ObjectName taskThread = findTaskThread(threadFactory, expectedTaskName, expectedTaskDesc);    
        assertNotNull("Task thread not found", taskThread);
        
        // subscribe for a notifications
        TaskStateFilter filter = new TaskStateFilter();
        TestListener listener = new TestListener(filter);            
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        mbServerConn.addNotificationListener(taskThread, listener, null, null);
        
        Notification notification = null;
        
        // check for task hung notification
        notification = listener.getNotification(TIMEOUT);
        assertNotNull("Task hung notification", notification);        
        assertEquals(ManagedConstants.TASK_HUNG_STATE, notification.getType());
        checkNotificationUserData(taskThread, expectedTaskName, 
                                  expectedTaskDesc, notification.getUserData());
                
        // check for task released notification
        notification = listener.getNotification(TIMEOUT);
        assertNotNull("Task released notification", notification);        
        assertEquals(ManagedConstants.TASK_RELEASED_STATE, notification.getType());
        checkNotificationUserData(taskThread, expectedTaskName, 
                                  expectedTaskDesc, notification.getUserData());
    }
    
    @Test
    public void testHungCancelReleaseTaskNotification() throws Exception {  
        ObjectName threadFactory = getManagedThreadFactory();
                
        invokeTest("testLongTask");
                                       
        String expectedTaskName = "TestRunnable long";
        String expectedTaskDesc = expectedTaskName;
        ObjectName taskThread = findTaskThread(threadFactory, expectedTaskName, expectedTaskDesc);
        assertNotNull("Task thread not found", taskThread);
        
        // subscribe for a notifications
        TaskStateFilter filter = new TaskStateFilter();
        TestListener listener = new TestListener(filter);    
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        mbServerConn.addNotificationListener(taskThread, listener, null, null);
        
        Notification notification = null;
        
        // check for task hung notification
        notification = listener.getNotification(TIMEOUT);
        assertNotNull("Task hung notification", notification);        
        assertEquals(ManagedConstants.TASK_HUNG_STATE, notification.getType());
        checkNotificationUserData(taskThread, expectedTaskName, 
                                  expectedTaskDesc, notification.getUserData());
                
        Object result = mbServerConn.invoke(taskThread, "cancel", null, null);        
        assertEquals(Boolean.TRUE, result);
        
        // XXX: can released and cancelled notification come unordered?
        
        // check for task cancelled notification
        notification = listener.getNotification(TIMEOUT);
        assertNotNull("Task cancelled notification", notification);        
        assertEquals(ManagedConstants.TASK_CANCELLED_STATE, notification.getType());
        checkNotificationUserData(taskThread, expectedTaskName, 
                                  expectedTaskDesc, notification.getUserData());
        
        // check for task released notification
        notification = listener.getNotification(TIMEOUT);
        assertNotNull("Task released notification", notification);        
        assertEquals(ManagedConstants.TASK_RELEASED_STATE, notification.getType());
        checkNotificationUserData(taskThread, expectedTaskName, 
                                  expectedTaskDesc, notification.getUserData());
    }
    
    private void checkNotificationUserData(ObjectName taskThread,
                                           String expectedTaskName,
                                           String expectedTaskDesc, 
                                           Object userData) {
        assertTrue(userData instanceof Properties);
        Properties props = (Properties) userData;
        String threadName = props.getProperty("managedthread");
        assertEquals(taskThread.toString(), threadName);
        
        assertTrue("managedthread.threadID", 
                   props.getProperty("managedthread.threadID") != null);
        assertTrue("managedthread.threadName", 
                   props.getProperty("managedthread.threadName") != null);
        assertTrue("managedthread.taskRunTime", 
                   props.getProperty("managedthread.taskRunTime") != null);
        
        assertEquals("managedthread.taskIdentityName", 
                     expectedTaskName,
                     props.getProperty("managedthread.taskIdentityName"));
        assertEquals("managedthread.taskIdentityDescription", 
                     expectedTaskDesc,
                     props.getProperty("managedthread.taskIdentityDescription"));
    }
    
    private ObjectName findTaskThread(ObjectName threadFactory,
                                      String expectedTaskName,
                                      String expectedTaskDescription) throws Exception {
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        long slept = 0;
        while (slept < TIMEOUT) {           
            String[] threadNames = (String[])mbServerConn.getAttribute(threadFactory, "threads");
            if (threadNames != null && threadNames.length > 0) {
                for (String threadName : threadNames) {
                    ObjectName threadObjectName = new ObjectName(threadName);
                    
                    String taskName = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityName");                   
                    String taskDesc = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityDescription");
                    
                    if (expectedTaskName.equals(taskName) && 
                        expectedTaskDescription.equals(taskDesc)) {
                        return threadObjectName;
                    }
                }
            } 
            Thread.sleep(1000 * 10);
            slept += 1000 * 10;
        }
        return null;
    }
        
    @Test
    public void testHungTaskCancel() throws Exception { 
        ObjectName threadFactory = getManagedThreadFactory();
                
        invokeTest("testLongTask");
                
        ObjectName hungTaskThread = findHungTaskThread(threadFactory, 
                                                       "TestRunnable long", 
                                                       "TestRunnable long");      
        assertNotNull("Hung task thread not found", hungTaskThread);
                               
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();        
        Object result = mbServerConn.invoke(hungTaskThread, "cancel", null, null);        
        assertEquals(Boolean.TRUE, result);

        // calling anything after cancel can generate an exception (attribute or mbean not found)
    }
    
    private ObjectName findHungTaskThread(ObjectName threadFactory,
                                          String expectedTaskName,
                                          String expectedTaskDescription) throws Exception {
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        long slept = 0;
        while (slept < TIMEOUT) {           
            String[] threadNames = (String[])mbServerConn.getAttribute(threadFactory, "threads");
            if (threadNames != null && threadNames.length > 0) {
                for (String threadName : threadNames) {
                    ObjectName threadObjectName = new ObjectName(threadName);
                    
                    String taskName = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityName");                   
                    String taskDesc = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityDescription");
                    Boolean taskHung = (Boolean)mbServerConn.getAttribute(threadObjectName, "taskHung");
                    
                    if (expectedTaskName.equals(taskName) && 
                        expectedTaskDescription.equals(taskDesc) &&
                        Boolean.TRUE.equals(taskHung)) {
                        return threadObjectName;
                    }
                }
            } 
            Thread.sleep(1000 * 10);
            slept += 1000 * 10;
        }
        return null;
    }
    
    @Test
    public void testNewThreadNotification() throws Exception {
        ObjectName threadFactory = getManagedThreadFactory();
        
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();               
        Object eventProvider = mbServerConn.getAttribute(threadFactory, "eventProvider");
        
        System.out.println(eventProvider);
        
        if (!Boolean.TRUE.equals(eventProvider)) {
            System.out.println("Test skipped as thread notifications are not supported");
            return;
        }
        
        // check for event types
        List<String> expectedEventTypes = new ArrayList<String>();
        expectedEventTypes.addAll(Arrays.asList(ManagedConstants.NEW_THREAD_EVENT));

        String[] eventTypes = (String[]) mbServerConn.getAttribute(threadFactory, "eventTypes");
        List<String> actualEventTypes = Arrays.asList(eventTypes);

        expectedEventTypes.removeAll(actualEventTypes);
        assertTrue("Expected eventTypes are missing", expectedEventTypes.isEmpty());
            
        // subscribe for a notification of a new thread
        NewThreadFilter filter = new NewThreadFilter();
        TestListener listener = new TestListener(filter);            
        mbServerConn.addNotificationListener(threadFactory, listener, null, null);
                   
        invokeTest("testStartRunnable");        
        testThreadNotification(threadFactory, 
                               listener, 
                               "TestRunnable", 
                               "TestRunnable",
                               "TestRunnable");
        
        listener.reset();
            
        invokeTest("testStartIdentifiableRunnable");        
        testThreadNotification(threadFactory, 
                               listener, 
                               "TestIdentifiableRunnable Name", 
                               "TestIdentifiableRunnable Description",
                               "TestIdentifiableRunnable Description CA");                                   
    }   
    
    private void testThreadNotification(ObjectName threadFactory, 
                                        TestListener listener,
                                        String expectedTaskName,
                                        String expectedTaskDesc,
                                        String expectedLocaleTaskDesc) throws Exception {        
        
        // wait for a notification
        Notification notification = listener.getNotification(TIMEOUT);
        assertTrue("new thread notification", notification != null);

        assertEquals(ManagedConstants.NEW_THREAD_EVENT, notification.getType());
        assertTrue(notification.getUserData() instanceof Properties);
        Properties props = (Properties) notification.getUserData();              
        String threadName = props.getProperty("managedthread");
        assertTrue("managedthread", threadName != null);

        // check if the thread is listed in the "threads" attribute
        List<String> expectedThreadNames = new ArrayList<String>();
        expectedThreadNames.addAll(Arrays.asList(threadName));

        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        String[] threadNames = (String[]) mbServerConn.getAttribute(threadFactory, "threads");
        List<String> actualThreadNames = Arrays.asList(threadNames);

        expectedThreadNames.removeAll(actualThreadNames);
        assertTrue("Expected thread is not in the thread list", expectedThreadNames.isEmpty());
        
        // wait for a bit, to make sure the task actually starts
        Thread.sleep(1000 * 5);
        
        ObjectName threadObjectName = new ObjectName(threadName);
        
        // check task name
        String taskName = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityName");
        assertEquals("taskIdentityName", expectedTaskName, taskName);
        
        // check task description
        String taskDesc = (String)mbServerConn.getAttribute(threadObjectName, "taskIdentityDescription");
        assertEquals("taskIdentityDescription", expectedTaskDesc, taskDesc);                           
    }
                    
    @Test
    public void testAttributes() throws Exception {
        ObjectName threadFactory = getManagedThreadFactory();

        invokeTest("testStartRunnable");
        checkThreadAttributes(threadFactory, 
                              "TestRunnable", 
                              "TestRunnable", 
                              "TestRunnable");
        
        invokeTest("testStartIdentifiableRunnable");        
        checkThreadAttributes(threadFactory, 
                              "TestIdentifiableRunnable Name", 
                              "TestIdentifiableRunnable Description",
                              "TestIdentifiableRunnable Description CA");  
    }
    
    private void checkThreadAttributes(ObjectName threadFactory,
                                       String expectedTaskName,
                                       String expectedTaskDesc,
                                       String expectedLocaleTaskDesc) throws Exception {
        ObjectName threadName = findTaskThread(threadFactory,
                                               expectedTaskName, 
                                               expectedTaskDesc);
        assertNotNull("Task thread", threadName);        
        
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
                
        Object eventProvider = mbServerConn.getAttribute(threadName, "eventProvider");
        
        System.out.println(eventProvider);
        
        if (Boolean.TRUE.equals(eventProvider)) {
            // check for event types
            List<String> expectedEventTypes = new ArrayList<String>();
            expectedEventTypes.addAll(Arrays.asList(ManagedConstants.TASK_CANCELLED_STATE,
                                                    ManagedConstants.TASK_HUNG_STATE,
                                                    ManagedConstants.TASK_RELEASED_STATE));

            String[] eventTypes = (String[]) mbServerConn.getAttribute(threadName, "eventTypes");
            List<String> actualEventTypes = Arrays.asList(eventTypes);

            expectedEventTypes.removeAll(actualEventTypes);
            assertTrue("Expected eventTypes are missing", expectedEventTypes.isEmpty());
        }
                
        // check read-only attributes
        testBooleanAttribute(threadName, "taskCancelled");
        testBooleanAttribute(threadName, "taskHung");
        
        testLongAttribute(threadName, "taskRunTime");
        testLongAttribute(threadName, "threadID");
        
        testStringAttribute(threadName, "threadName");
        testStringAttribute(threadName, "taskIdentityName");
        testStringAttribute(threadName, "taskIdentityDescription");               
        
        // check read/write attributes        
        Long hungTaskThreshold = (Long)mbServerConn.getAttribute(threadName, "hungTaskThreshold");
        assertTrue(hungTaskThreshold != null);
        Long newHungTaskThreshold = new Long(hungTaskThreshold.longValue() + 1000 * 60 * 5);
        mbServerConn.setAttribute(threadName, new Attribute("hungTaskThreshold", newHungTaskThreshold));
        hungTaskThreshold = (Long)mbServerConn.getAttribute(threadName, "hungTaskThreshold");
        assertEquals(newHungTaskThreshold, hungTaskThreshold);
        
        // test getTaskIdentityDescription operation
        Locale locale = Locale.CANADA;
        Object localeTaskDesc = mbServerConn.invoke(threadName, 
                                                    "getTaskIdentityDescription", 
                                                    new String [] { locale.toString() }, 
                                                    new String [] { String.class.getName() });
        
        assertEquals("getTaskIdentityDescription", expectedLocaleTaskDesc, localeTaskDesc);  
    }
        
    private void testStringAttribute(ObjectName object, String attributeName) throws Exception {
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        String value = (String)mbServerConn.getAttribute(object, attributeName);
        System.out.println(attributeName + ": " + value);
        assertTrue(value != null);
        try {
            mbServerConn.setAttribute(object, new Attribute(attributeName, "foobar"));
            fail("setAttribute did not fail");
        } catch (Exception e) {
            // ignore
        }
    }
    
    private void testLongAttribute(ObjectName object, String attributeName) throws Exception {
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        Long value = (Long)mbServerConn.getAttribute(object, attributeName);
        System.out.println(attributeName + ": " + value);
        assertTrue(value != null);
        try {
            mbServerConn.setAttribute(object, new Attribute(attributeName, new Long(5)));
            fail("setAttribute did not fail");
        } catch (Exception e) {
            // ignore
        }
    }
    
    private void testBooleanAttribute(ObjectName object, String attributeName) throws Exception {
        MBeanServerConnection mbServerConn = this.jmxConnector.getMBeanServerConnection();
        Boolean value = (Boolean)mbServerConn.getAttribute(object, attributeName);
        System.out.println(attributeName + ": " + value);
        assertFalse(value.booleanValue());
        try {
            mbServerConn.setAttribute(object, new Attribute(attributeName, Boolean.TRUE));
            fail("setAttribute did not fail");
        } catch (Exception e) {
            // ignore
        }
    }
    
    public static class NewThreadFilter implements NotificationFilter {
        public boolean isNotificationEnabled(Notification notification) {
            return ManagedConstants.NEW_THREAD_EVENT.equals(notification.getType());
        }
    }
    
    public static class TaskStateFilter implements NotificationFilter {
        public boolean isNotificationEnabled(Notification notification) {
            return ManagedConstants.TASK_CANCELLED_STATE.equals(notification.getType()) ||
                   ManagedConstants.TASK_HUNG_STATE.equals(notification.getType()) ||
                   ManagedConstants.TASK_RELEASED_STATE.equals(notification.getType());
        }        
    }
    
    public static class TestListener implements NotificationListener {

        private LinkedList<Notification> notifications = new LinkedList<Notification>();
        private NotificationFilter filter;
        
        public TestListener(NotificationFilter filter) {
            reset();
            this.filter = filter;
        }
        
        public synchronized void handleNotification(Notification notification, Object handback) {
            System.out.println(notification);
            if (this.filter.isNotificationEnabled(notification)) {
                this.notifications.add(notification);            
                notify();
            }
        }
        
        public synchronized Notification getNotification(int timeout) throws InterruptedException {
            if (this.notifications.isEmpty()) {
                wait(timeout);
            }
            return (this.notifications.isEmpty()) ? null : this.notifications.removeFirst();
        }
               
        public void reset() {
            this.notifications.clear();
        }
    }
     
}
