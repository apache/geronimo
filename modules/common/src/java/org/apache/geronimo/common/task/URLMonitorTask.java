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

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.NullArgumentException;

/**
 * A timer task to monitor a URL.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:04 $
 */
public class URLMonitorTask
    extends TimerTask
{
    private Log log = LogFactory.getLog(URLMonitorTask.class);
    
    protected URL url;
    
    private long lastChanged = -1;
    
    protected List listeners = Collections.synchronizedList(new ArrayList());
    
    public URLMonitorTask(final URL url)
    {
        if (url == null) {
            throw new NullArgumentException("url");
        }
        
        this.url = url;
    }
    
    public URL getURL()
    {
        return url;
    }
    
    public long getLastChanged()
    {
        return lastChanged;
    }
    
    public void run()
    {
        log.trace("Checking if URL has changed");
        
        boolean trace = log.isTraceEnabled();
        
        try {
            URLConnection connection = url.openConnection();
            if (trace) {
                log.trace("Connection: " + connection);
            }
            
            long lastModified = connection.getLastModified();
            if (trace) {
                log.trace("Last modified: " + lastModified + ", last changed: " + lastChanged);
            }
            
            if (lastChanged < lastModified) {
                fireURLChanged(new Event(this, lastModified));
                lastChanged = lastModified;
            }
        }
        catch (Exception e) {
            log.warn("Failed to check URL: " + url, e);
        }
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                            Listener Support                         //
    /////////////////////////////////////////////////////////////////////////
    
    public static class Event
        extends EventObject
    {
        private URLMonitorTask task;
        private long lastModified;
        
        Event(final URLMonitorTask task, final long lastModified)
        {
            super(task);
            
            assert task != null;
            
            this.task = task;
            this.lastModified = lastModified;
        }
        
        public URLMonitorTask getURLMonitorTask()
        {
            return task;
        }
        
        public URL getURL()
        {
            return getURLMonitorTask().getURL();
        }
        
        public long getLastModified()
        {
            return lastModified;
        }
    }
    
    public static interface Listener
    {
        void doURLChanged(Event event);
    }
    
    public void addListener(final Listener listener) {
        if (listener == null) {
            throw new NullArgumentException("listener");
        }
        
        // only add the listener if it isn't already in the list
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeThrowableListener(final Listener listener) {
        if (listener == null) {
            throw new NullArgumentException("listener");
        }
        
        listeners.remove(listener);
    }
    
    protected void fireURLChanged(final Event event) {
        assert event != null;
        
        Object[] list = listeners.toArray();
        
        for (int i=0; i<list.length; i++) {
            Listener listener = (Listener)list[i];
            assert listener != null;
            
            listener.doURLChanged(event);
        }
    }
}
