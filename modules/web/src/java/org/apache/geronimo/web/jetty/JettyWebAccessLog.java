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


package org.apache.geronimo.web.jetty;

import java.io.File;
import java.lang.reflect.Constructor;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.web.AbstractWebAccessLog;
import org.mortbay.http.NCSARequestLog;
import org.mortbay.http.RequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.util.LifeCycle;

/* -------------------------------------------------------------------------------------- */
/**
 * JettyWebAccessLog
 * 
 * @jmx:mbean extends="org.apache.geronimo.web.AbstractWebAccessLogMBean
 * @version $Revision: 1.1 $ $Date: 2003/11/20 09:10:17 $
 */
public class JettyWebAccessLog extends AbstractWebAccessLog implements JettyWebAccessLogMBean
{
    private final static Log log = LogFactory.getLog(JettyWebAccessLog.class);
    private final static Class[] defaultConstructorSignature = new Class[]{};
    private final static Object[] defaultConstructorArgs = new Object[]{};
    private Server jetty = null;
    private RequestLog jettyLog = null;
    private boolean buffering = false;
    
    public JettyWebAccessLog ()
    {
        super ();

        try
         {
             objectName = new ObjectName ("jetty:role=WebAccessLog, instance="+hashCode());
         }
         catch (Exception e)
         {
             log.warn (e.getMessage());
         }
    }
    
 

    /* -------------------------------------------------------------------------------------- */
    /* Override AbstractWebAccessLog to disable
     * @param state
     * @see org.apache.geronimo.web.WebAccessLog#setResolveHostNames(boolean)
     */
    public void setResolveHostNames(boolean state)
    {
        throw new UnsupportedOperationException ("Host name DNS resolution is not supported for Jetty access log");
    }
    
    
    /* -------------------------------------------------------------------------------------- */
    /** Enable/disable buffering of logs. 
     *  Buffering improves performance.
     *  This is a Jetty-specific configuration
     * @param state true enables buffering, false disables it
     */
    public void setBuffering (boolean state)
    {
        buffering = state;
    }
    
    /* -------------------------------------------------------------------------------------- */
    /** Get buffering setting
     * This is a Jetty-specific configuration
     * @return
     */
    public boolean getBuffering ()
    {
        return buffering;
    }
    
    

    public ObjectName preRegister (MBeanServer server,  ObjectName objectName) throws Exception 
    {       
        return super.preRegister (server,objectName); 
    }
    

    /**
     * Set the parent Container for this  component
     *
     * @param container a <code>Container</code> value
     */
    public void setContainer (Container container)
    {
        super.setContainer(container);

        //get the Jetty instance
        if (! (container instanceof JettyWebContainer))
            throw new IllegalStateException ("Non Jetty container set as parent on JettyWebAccessLog");

        jetty = ((JettyWebContainer)container).getJettyServer();
    }


    public void doStart() throws Exception
   {
       try
       {
            if (getContainer() == null)
                  throw new IllegalStateException ("Container not set on JettyWebAccessLog");
           if (getLogImpl()!= null) 
           {
               Class logImplClass = Thread.currentThread().getContextClassLoader().loadClass(getLogImpl());
               //get the default constructor, if it has one
               Constructor constructor = logImplClass.getConstructor (defaultConstructorSignature);
               jettyLog = (RequestLog)constructor.newInstance (defaultConstructorArgs);
               log.warn ("RequestLog does not support rich configuration");
               
               jetty.setRequestLog (jettyLog);
               jettyLog.start();

               return;
           } 
           
           log.info("Using org.mortbay.http.NCSARequestLog as log impl");
           jettyLog = new NCSARequestLog();
           NCSARequestLog ncsaLog = (NCSARequestLog)jettyLog;
           
           // set up the configuration of the access log
           ncsaLog.setBuffered(getBuffering());
           ncsaLog.setAppend (getAppend());
           ncsaLog.setRetainDays(getLogRetentionDays());
           
           if (getLogDateFormat() == null)
           {
               log.warn ("No date format set, using Jetty default: "+ncsaLog.getLogDateFormat());
           }
           else
               ncsaLog.setLogDateFormat (getLogDateFormat());
           
           if (getLogPattern() == null)
           {
               log.warn ("No log pattern set, using Jetty default: NCSA common");
           }
           else if (getLogPattern().equals(NCSA_EXTENDED_PATTERN))
               ncsaLog.setExtended (true);
           else if (!getLogPattern().equals(NCSA_COMMON_PATTERN))
           {
               log.warn ("Jetty access log impl does not support custom patterns. Falling back to Jetty default: NCSA common");
           }
           
           if (logLocation == null)
               throw new IllegalStateException ("No log location specified");
           
           
           File logDir = new File (logLocation.normalize());
           
           //create the directory if necessary
           if (!logDir.exists())
              logDir.mkdir();
           
           //create the filename
           String filename = (getLogPrefix() == null? "":getLogPrefix());           
           if (getLogRolloverIntervalHrs() > 0)
           {
               log.warn ("Jetty access log does not support rollover intervals. Falling back to Jetty default: nightly ");
               //rollover of log files is requested, so format the Jetty log file name to enable it
               filename = filename + "yyyy_mm_dd";
           }
           else
               filename = filename+"access";
               
           filename =filename + (getLogSuffix() == null?"":getLogSuffix())+".log";
           ncsaLog.setFilename(logDir.getCanonicalPath()+File.separator+filename);
           
           jetty.setRequestLog(ncsaLog);
         
           ncsaLog.start();
           super.doStart();
       }
       catch (Exception e)
       {
           log.error (e);
           throw e;
       }
   }
   
   public void doStop () throws Exception 
   {
       if (jettyLog instanceof NCSARequestLog)
           jettyLog.stop();
   } 
  

    public RequestLog getJettyLog ()
   {
       return jettyLog;
   }

}
