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

import junit.framework.TestCase;

/**
 * Unit test for {@link Duration} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:27 $
 */
public class DurationTest
    extends TestCase
{
    public void testConstructorNull() {
        try {
            Duration d = new Duration(null);
            fail();
        } catch(NullPointerException ex){
            // success
        }
    }
    
    public void testConstructorPrimative() {
        Duration d = new Duration(1000L);
        assertEquals("1s", d.toString());
    }
    
    public void testConstructorNumber() {
        Duration d = new Duration(new Long(1000L));
        assertEquals("1s", d.toString());
    }
    
    public void testToStringMillisecond() {
        Duration d = new Duration(1L);
        assertEquals("1ms", d.toString());
    }
    
    public void testToStringMinute() {
        Duration d = new Duration(60L * 1000L);
        assertEquals("1m", d.toString());
    }
    
    public void testToStringHour() {
        Duration d = new Duration(60L * 60L * 1000L);
        assertEquals("1h", d.toString());
    }
    
    public void testToStringDay() {
        Duration d = new Duration(24L * 60L * 60L * 1000L);
        assertEquals("1d", d.toString());
    }
    
    public void testToStringWeek() {
        Duration d = new Duration(7L * 24L * 60L * 60L * 1000L);
        assertEquals("1w", d.toString());
    }
    
    public void testToStringOnes() {
        Duration d = new Duration(Duration.ONE_WEEK + Duration.ONE_DAY +
            Duration.ONE_HOUR + Duration.ONE_MINUTE + Duration.ONE_SECOND +
            Duration.ONE_MILLISECOND);
        assertEquals("1w:1d:1h:1m:1s:1ms", d.toString());
    }
}
