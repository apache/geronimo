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

package org.apache.geronimo.common.task;

import java.io.File;

import java.net.URL;

import java.util.Timer;

import junit.framework.TestCase;

import org.apache.geronimo.common.NullArgumentException;

import org.apache.geronimo.common.net.protocol.Protocols;
import org.apache.geronimo.common.net.protocol.URLStreamHandlerFactory;

/**
 * Unit test for {@link URLMonitorTask} class.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:05 $
 */
public class URLMonitorTaskTest
    extends TestCase
{
    static {
        //
        // Have to install factory to make sure that our file handler is used
        // and not Sun's
        //
        Protocols.prependHandlerPackage("org.apache.geronimo.common.net.protocol");
        URLStreamHandlerFactory factory = new URLStreamHandlerFactory();
        URL.setURLStreamHandlerFactory(factory);
    }
    
    private File file;
    private URL fileURL;

    protected void setUp() throws Exception {
        file = File.createTempFile("URLMonitor", ".tmp");
        fileURL = file.toURI().toURL();
    }
    
    protected void tearDown() throws Exception {
        file.delete();
    }
    
    public void testConstructorWithNull() {
        try {
            new URLMonitorTask(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }
    }

    public void testGetURL() {
        URLMonitorTask urlMonitorTask = new URLMonitorTask(fileURL);
        assertEquals(fileURL, urlMonitorTask.getURL());
    }

    public void testAddRemoveListeners() {
        URLMonitorTask urlMonitorTask = new URLMonitorTask(fileURL);

        try {
            urlMonitorTask.addListener(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }

        try {
            urlMonitorTask.removeThrowableListener(null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException ignore) {
        }
    }

    public void testEventFiring() throws Exception
    {
        URLMonitorTask urlMonitorTask = new URLMonitorTask(fileURL);
        MockURLMonitorTaskListener mockListener = new MockURLMonitorTaskListener();
        urlMonitorTask.addListener(mockListener);
        assertEquals(1, urlMonitorTask.listeners.size());
        
        // duplicate entry.lets verify the fireCount later,
        // to verify whether this has been added again
        urlMonitorTask.addListener(mockListener);
        assertEquals(1, urlMonitorTask.listeners.size());
        
        // Schedule the timer now
        Timer timer = new Timer();
	
	// Use a value >=0 to avoid race conditions
        timer.schedule(urlMonitorTask, 20);
        
        // Change the last modified and get the last modified from the file,
        // since it might be truncated while setting
        long mtime = System.currentTimeMillis();
        file.setLastModified(mtime);
        long lastModifiedTime = file.lastModified();
        // mtime is truncated so this may fail
        // assertEquals(mtime, lastModifiedTime);
        
        // Wait until the event is fired
        while (mockListener.getFireCount() == 0) {
            Thread.sleep(50);
        }
        timer.cancel();
        assertEquals(1, mockListener.getFireCount());
        
        URLMonitorTask.Event event = mockListener.getEvent();
        
        assertEquals(lastModifiedTime, event.getLastModified());
        assertEquals(lastModifiedTime, event.getURLMonitorTask().getLastChanged());
        
        assertEquals(fileURL, event.getURL());
        assertEquals(fileURL, event.getURLMonitorTask().getURL());
        
        urlMonitorTask.removeThrowableListener(mockListener);
        assertEquals(0, urlMonitorTask.listeners.size());
    }
}
