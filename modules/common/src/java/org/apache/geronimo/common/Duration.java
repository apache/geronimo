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

package org.apache.geronimo.common;

import org.apache.geronimo.common.mutable.MuLong;

/**
 * An abstraction of the time during which something exists or lasts.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/24 20:51:22 $
 */
public class Duration
   extends MuLong
{
    /**
     * Default constructor.
     */
    public Duration()
    {
        super(0);
    }
    
    /**
     * Construct a duration.
     *
     * @param time   The time value for the duration.
     */
    public Duration(final long time)
    {
        super(time);
    }
    
    /**
     * Construct a duration.
     *
     * @param time   The time value for the duration.
     */
    public Duration(final Number time)
    {
        super(time.longValue());
    }
    
    public static final long ONE_YEAR = 2903040000L;
    public static final long ONE_MONTH = 241920000;
    public static final long ONE_WEEK = 60480000;
    public static final long ONE_DAY = 8640000;
    public static final long ONE_HOUR = 3600000;
    public static final long ONE_MINUTE = 600000;
    public static final long ONE_SECOND = 1000;
    public static final long ONE_MILLISECOND = 1;
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        
        //
        // jason: Bah... must be a better way
        //
        
        long y = value / ONE_YEAR;
        long mo = (value - (y * ONE_YEAR)) / ONE_MONTH;
        long w = (value - (y * ONE_YEAR) - (mo * ONE_MONTH)) / ONE_WEEK;
        long d = (value - (y * ONE_YEAR) - (mo * ONE_MONTH) - (w * ONE_WEEK)) / ONE_DAY;
        long h = (value - (y * ONE_YEAR) - (mo * ONE_MONTH) - (w * ONE_WEEK) - (d * ONE_DAY)) / ONE_HOUR;
        long m = (value - (y * ONE_YEAR) - (mo * ONE_MONTH) - (w * ONE_WEEK) - (d * ONE_DAY) - (h * ONE_HOUR)) / ONE_MINUTE;
        long s = (value - (y * ONE_YEAR) - (mo * ONE_MONTH) - (w * ONE_WEEK) - (d * ONE_DAY) - (h * ONE_HOUR) - (m * ONE_MINUTE)) / ONE_SECOND;
        long ms =  (value - (y * ONE_YEAR) - (mo * ONE_MONTH) - (w * ONE_WEEK) - (d * ONE_DAY) - (h * ONE_HOUR) - (m * ONE_MINUTE) - (s * ONE_SECOND));
        
        char spacer = ':';
        
        if (y != 0) {
            buff.append(y).append("y");
        }
        if (mo != 0) {
            if (buff.length() != 0) buff.append(spacer);
            buff.append(mo).append("mo");
        }
        if (w != 0) {
            if (buff.length() != 0) buff.append(spacer);
            buff.append(w).append("w");
        }
        if (d != 0) {
            if (buff.length() != 0) buff.append(spacer);
            buff.append(d).append("d");
        }
        if (h != 0) {
            if (buff.length() != 0) buff.append(spacer);
            buff.append(h).append("h");
        }
        if (m != 0) {
            if (buff.length() != 0) buff.append(spacer);
            buff.append(m).append("m");
        }
        if (s != 0) {
            if (buff.length() != 0) buff.append(spacer);
            buff.append(s).append("s");
        }
        if (ms != 0) {
            if (buff.length() != 0) buff.append(spacer);
            buff.append(ms).append("ms");
        }
        
        return buff.toString();
    }
    
    public static Duration parseDuration(final String text)
    {
        // for now...
        return new Duration(Long.parseLong(text));      
    }
    
    public static class PropertyEditor
        extends java.beans.PropertyEditorSupport
    {
        public void setAsText(final String text)
        {
            setValue(parseDuration(text));
        }
    }
}
