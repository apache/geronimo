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
 * @version $Revision: 1.2 $ $Date: 2003/09/01 15:22:24 $
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
        timer.schedule(urlMonitorTask, 0);
        
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
