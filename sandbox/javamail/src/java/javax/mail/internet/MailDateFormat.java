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

package javax.mail.internet;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Parses dates of the form
 *
 * <code>Wed, 02 Jan 2003 23:59:59 -0100 (GMT)</code>
 *
 * <code>EEE,  d MMM yyyy HH:mm:ss Z (z)</code>
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:09 $
 */
public class MailDateFormat extends SimpleDateFormat {
    static final MailDateFormat INSTANCE = new MailDateFormat(); // @todo jboynes: this does not seem to be used
    private static final String pattern = "EEE, d MMM yyyy HH:mm:ss Z (z)";

    public MailDateFormat() {
        super(pattern, Locale.US);
    }

    // @todo jboynes: are these commented out for a reason?
    //    public StringBuffer format(Date date, StringBuffer buffer, FieldPosition position) {
    //        return super.format(date,buffer,position);
    //    }
    //    public Date parse(String string, ParsePosition position) {
    //        return parse(string,position);
    //    }
    public void setCalendar(Calendar calendar) {
        throw new UnsupportedOperationException();
    }

    public void setNumberFormat(NumberFormat format) {
        throw new UnsupportedOperationException();
    }
}
