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

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Formats ths date as specified by
 * draft-ietf-drums-msg-fmt-08 dated January 26, 2000
 * which supercedes RFC822.
 * <p/>
 * <p/>
 * The format used is <code>EEE, d MMM yyyy HH:mm:ss Z</code> and
 * locale is always US-ASCII.
 *
 * @version $Rev$ $Date$
 */
public class MailDateFormat extends SimpleDateFormat {
    public MailDateFormat() {
        super("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
    }

    public StringBuffer format(Date date, StringBuffer buffer, FieldPosition position) {
        return super.format(date, buffer, position);
    }

    public Date parse(String string, ParsePosition position) {
        return super.parse(string, position);
    }

    /**
     * The calendar cannot be set
     * @param calendar
     * @throws UnsupportedOperationException
     */
    public void setCalendar(Calendar calendar) {
        throw new UnsupportedOperationException();
    }

    /**
     * The format cannot be set
     * @param format
     * @throws UnsupportedOperationException
     */
    public void setNumberFormat(NumberFormat format) {
        throw new UnsupportedOperationException();
    }
}
