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

import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;



/* -------------------------------------------------------------------------------------- */
/**
 * AbstractWebAccessLog
 *
 * @version $Revision: 1.2 $ $Date: 2003/12/30 08:28:57 $
 */
public abstract class AbstractWebAccessLog implements WebAccessLog {

    protected String logImpl;
    protected URI logLocation;
    protected String logPattern;
    protected String logSuffix;
    protected String logPrefix;
    protected String logDateFormat;
    protected int rolloverHrs =0;
    protected boolean resolutionEnabled = false;
    protected boolean appendEnabled = true;
    protected int logRetentionDays;




    /* -------------------------------------------------------------------------------------- */
    /*
     * @param uri
     * @see org.apache.geronimo.web.WebAccessLog#setLogLocation(java.net.URI)
     */
    public void setLogLocation(URI uri)
    {
         logLocation = uri;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getLogLocation()
     */
    public URI getLogLocation()
    {
        return logLocation;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param pattern
     * @see org.apache.geronimo.web.WebAccessLog#setLogPattern(java.lang.String)
     */
    public void setLogPattern(String pattern)
    {
        if (pattern.equalsIgnoreCase (NCSA_COMMON_NAME))
            logPattern = NCSA_COMMON_PATTERN;
         else if (pattern.equalsIgnoreCase (NCSA_EXTENDED_NAME))
             logPattern = NCSA_EXTENDED_PATTERN;
         else
             logPattern = pattern;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getLogPattern()
     */
    public String getLogPattern()
    {
         return logPattern;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param days
     * @see org.apache.geronimo.web.WebAccessLog#setLogRetentionDays(int)
     */
    public void setLogRetentionDays(int days)
    {
         logRetentionDays = days;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getLogRetentionDays()
     */
    public int getLogRetentionDays()
    {
         return logRetentionDays;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param state
     * @see org.apache.geronimo.web.WebAccessLog#setLogRollover(boolean)
     */
    public void setLogRolloverIntervalHrs(int hrs)
    {
       rolloverHrs = hrs;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getLogRollover()
     */
    public int getLogRolloverIntervalHrs()
    {
        return rolloverHrs;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param prefix
     * @see org.apache.geronimo.web.WebAccessLog#setLogPrefix(java.lang.String)
     */
    public void setLogPrefix(String prefix)
    {
        logPrefix = prefix;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getLogPrefix()
     */
    public String getLogPrefix()
    {
        return logPrefix;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param suffix
     * @see org.apache.geronimo.web.WebAccessLog#setLogSuffix(java.lang.String)
     */
    public void setLogSuffix(String suffix)
    {
        logSuffix = suffix;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getLogSuffix()
     */
    public String getLogSuffix()
    {
        return logSuffix;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param dateFormat
     * @see org.apache.geronimo.web.WebAccessLog#setLogDateFormat(java.lang.String)
     */
    public void setLogDateFormat(String dateFormat)
    {
        logDateFormat = dateFormat;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getLogDateFormat()
     */
    public String getLogDateFormat()
    {
        return logDateFormat;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param state
     * @see org.apache.geronimo.web.WebAccessLog#setResolveHostNames(boolean)
     */
    public void setResolveHostNames(boolean state)
    {
        resolutionEnabled = state;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getResolveHostNames()
     */
    public boolean getResolveHostNames()
    {
        return resolutionEnabled;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getAppend()
     */
    public boolean getAppend()
    {
        return appendEnabled;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param state
     * @see org.apache.geronimo.web.WebAccessLog#setAppend(boolean)
     */
    public void setAppend(boolean state)
    {
         appendEnabled = state;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @return
     * @see org.apache.geronimo.web.WebAccessLog#getLogImplementationClass()
     */
    public String getLogImplementationClass()
    {
        return logImpl;
    }

    /* -------------------------------------------------------------------------------------- */
    /*
     * @param classname
     * @see org.apache.geronimo.web.WebAccessLog#setLogImplementationClass(java.lang.String)
     */
    public void setLogImplementationClass(String classname)
    {
        logImpl = classname;
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LogImplementationClass", true, true, "class of log implementation. I think this is speculative"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LogLocation", true, true, "URI indicating where to put the log"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LogPattern", true, true, "NCSA log pattern spec"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LogRetentionDays", true, true, "Number of days to retain logs"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LogRolloverIntervalHrs", true, true, "Hours between log rollovers"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LogPrefix", true, true, "file name prefix for log files"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LogSuffix", true, true, "file name suffix for log files"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LogDateFormat", true, true, "Date format to use in logs, following java.text.DateFormat (??)"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ResolveHostNames", true, true, "Should host names be resolved"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Append", true, true, "Should logs be appended or overwritten (? rolled over)"));
        return mbeanInfo;
    }

}
