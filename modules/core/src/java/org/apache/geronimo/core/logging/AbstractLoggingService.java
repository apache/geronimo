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

package org.apache.geronimo.core.logging;

import java.net.URL;

import java.util.Timer;

import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.common.task.URLMonitorTask;

import org.apache.geronimo.management.AbstractManagedObject;

/**
 * An abstract logging service.
 *
 * <p>Sub-classes only need to provide a {@link #configure(URL)}.
 *
 * @version $Revision: 1.2 $ $Date: 2003/09/01 19:34:14 $
 */
public abstract class AbstractLoggingService
    extends AbstractManagedObject
    implements LoggingService
{
    /** The default refresh period (60 seconds) */
    public static final int DEFAULT_REFRESH_PERIOD = 60;
    
    /** The URL to the configuration file. */
    protected URL configURL;
    
    /** The time (in seconds) between checking for new config. */
    protected int refreshPeriod;
    
    /** The URL watch timer (in daemon mode). */
    protected Timer timer = new Timer(true);
    
    /** A monitor to check when the config URL changes. */
    protected URLMonitorTask monitor;
    
    /**
     * Initialize <code>AbstractLoggingService</code>.
     *
     * @param url       The configuration URL.
     * @param period    The refresh period (in seconds).
     */
    protected AbstractLoggingService(final URL url, final int period)
    {
        setRefreshPeriod(period);
        setConfigurationURL(url);
    }
    
    /**
     * Initialize <code>AbstractLoggingService</code> using the default
     * refresh period.
     *
     * @param url   The configuration URL.
     */
    protected AbstractLoggingService(final URL url)
    {
        this(url, DEFAULT_REFRESH_PERIOD);
    }
    
    public int getRefreshPeriod()
    {
        return refreshPeriod;
    }
    
    public void setRefreshPeriod(final int period)
    {
        if (period < 1) {
            throw new IllegalArgumentException("Refresh period must be > 0");
        }
        
        this.refreshPeriod = period;
    }
    
    public URL getConfigurationURL()
    {
        return configURL;
    }
    
    public void setConfigurationURL(final URL url)
    {
        if (url == null) {
            throw new NullArgumentException("url");
        }
        
        this.configURL = url;
    }
    
    public void reconfigure()
    {
        configure(configURL);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    //                         AbstractManagedObject                         //
    ///////////////////////////////////////////////////////////////////////////
    
    protected void doStart() throws Exception
    {
        monitor = new URLMonitorTask(configURL);
        monitor.addListener(new URLMonitorTask.Listener() {
            public void doURLChanged(final URLMonitorTask.Event event) {
                configure(event.getURL());
            }
        });
        monitor.run();
        timer.schedule(monitor, 1000 * refreshPeriod, 1000 * refreshPeriod);
    }
    
    protected void doStop() throws Exception
    {
        monitor.cancel();
        monitor = null;
    }
}
