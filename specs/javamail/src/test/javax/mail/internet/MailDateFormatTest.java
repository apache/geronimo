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

package javax.mail.internet;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MailDateFormatTest extends TestCase {
    public void testMailDateFormat() throws ParseException {
        MailDateFormat mdf = new MailDateFormat();
        Date date = mdf.parse("Wed, 27 Aug 2003 13:43:38 +0100 (BST)");
        // don't we just love the Date class?
        Calendar cal = Calendar.getInstance(new SimpleTimeZone(+1 * 60 * 60 * 1000, "BST"), Locale.getDefault());
        cal.setTime(date);
        assertEquals(2003, cal.get(Calendar.YEAR));
        assertEquals(Calendar.AUGUST, cal.get(Calendar.MONTH));
        assertEquals(27, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(Calendar.WEDNESDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(43, cal.get(Calendar.MINUTE));
        assertEquals(38, cal.get(Calendar.SECOND));
    }
}
