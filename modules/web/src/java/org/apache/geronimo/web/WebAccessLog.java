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

package org.apache.geronimo.web;

import java.net.URI;
import org.apache.geronimo.core.service.ManagedComponent;

/* -------------------------------------------------------------------------------------- */
/**
 * WebAccessLog
 * 
 * 
 * @version $Revision: 1.6 $ $Date: 2003/11/20 09:10:17 $
 */
/**
 * WebAccessLog
 *
 * Log for web hits.
 * 
 * @version $Revision: 1.6 $ $Date: 2003/11/20 09:10:17 $
 */
public interface WebAccessLog extends ManagedComponent {
    public static final String NCSA_COMMON_PATTERN = "%h %l %u %t \"%r\" %>s %b";
    public static final String NCSA_EXTENDED_PATTERN = "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-agent}i\"";
    
    public static final String NCSA_COMMON_NAME = "common";
    public static final String NCSA_EXTENDED_NAME = "extended";
    
    
    /* -------------------------------------------------------------------------------------- */
    /** Set the name of a log implementation class.
     * This is useful for containers that have a number of different
     * log implementations that may be plugged in.
     * @param classname fully qualified classname
     */
    public void setLogImpl (String classname);
    
    /* -------------------------------------------------------------------------------------- */
    /** Get the name of the log implementation class.
     * @return
     */
    public String getLogImpl ();
    
    /* -------------------------------------------------------------------------------------- */
    /**Set directory where access log logs will be written
     * @param uri
     */
    public void setLogLocation(URI uri);

    /* -------------------------------------------------------------------------------------- */
    /** Get location as uri of access log directory
     * @return
     */
    public URI getLogLocation();

    /* -------------------------------------------------------------------------------------- */
    /** Set the log format in accordance with NCSA spec
     * @param pattern
     */
    public void setLogPattern (String pattern);
    
    /* -------------------------------------------------------------------------------------- */
    /** Get the NCSA style log format pattern
     * @return
     */
    public String getLogPattern ();
   
    /* -------------------------------------------------------------------------------------- */
    /** Configure the number of days before old logs are deleted.
     * @param days
     */
    public void setLogRetentionDays (int days);
    
    /* -------------------------------------------------------------------------------------- */
    /** Get the number of days before old logs are deleted
     * @return
     */
    public int getLogRetentionDays ();

    /* -------------------------------------------------------------------------------------- */
    /** Number of hrs between log rotations
     *  eg 1 = 1hr, 24=1day, 168=1week
     * @param 0 is no rotation, otherwise it is hrs between rotations
     */
    public void setLogRolloverIntervalHrs (int hrs);
    
    /* -------------------------------------------------------------------------------------- */
    /** Number of hrs between log rotations
     * @return
     */
    public int getLogRolloverIntervalHrs ();
    
    /* -------------------------------------------------------------------------------------- */
    /** Prefix to prepend to all access log filenames
     * @param prefix
     */
    public void setLogPrefix (String prefix);
    
    /* -------------------------------------------------------------------------------------- */
    /** Get the log filename prefix
     * @return the prefix, or null if not set
     */
    public String getLogPrefix ();
    
    
    /* -------------------------------------------------------------------------------------- */
    /** Suffix to append to all access log filenames
     * @param suffix
     */
    public void setLogSuffix (String suffix);
    
    /* -------------------------------------------------------------------------------------- */
    /** Get the log filename suffix
     * @return the suffix, or null if not set
     */
    public String getLogSuffix ();
    
    /* -------------------------------------------------------------------------------------- */
    /** Set the format for the date in the log
     * @param dateFormat 
     * @see java.util.DateFormat
     */
    public void setLogDateFormat (String dateFormat);
    
    /* -------------------------------------------------------------------------------------- */
    /** Get the date format string
     * @return 
     */
    public String getLogDateFormat ();
  
    /* -------------------------------------------------------------------------------------- */
    /** Configure DNS resolution of host addresses in the log
     * @param state
     */
    public void setResolveHostNames (boolean state);
    
    /* -------------------------------------------------------------------------------------- */
    /** Get state of DNS hostname resolution for the log
     * @return true if DNS resolution is enabled, false otherwise
     */
    public boolean getResolveHostNames ();
    
    /* -------------------------------------------------------------------------------------- */
    /** Configure if logs will be appended or created afresh
     * @param state true if appending is enabled, false otherwise
     */
    public void setAppend (boolean state);
    
    /* -------------------------------------------------------------------------------------- */
    /** Get whether logs will be appended
     * @return true if appending is enabled, false otherwise
     */
    public boolean getAppend ();

}
