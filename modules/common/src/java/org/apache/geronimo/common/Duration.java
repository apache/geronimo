/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.common;

import org.apache.geronimo.common.mutable.MuLong;

/**
 * An abstraction of the time during which something exists or lasts.
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:25 $
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
    
    public static final long ONE_MILLISECOND =    1L;
    public static final long ONE_SECOND =      1000L * ONE_MILLISECOND;
    public static final long ONE_MINUTE =        60L * ONE_SECOND;
    public static final long ONE_HOUR =          60L * ONE_MINUTE;
    public static final long ONE_DAY =           24L * ONE_HOUR;
    public static final long ONE_WEEK =           7L * ONE_DAY;
    public static final long ONE_MONTH =          4L * ONE_WEEK;
    public static final long ONE_YEAR =          12L * ONE_MONTH;
    
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
