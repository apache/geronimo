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

package javax.mail.search;
import javax.mail.Message;
import javax.mail.MessagingException;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:09 $
 */
public final class HeaderTerm extends StringTerm {
    protected String headerName;
    public HeaderTerm(String header, String pattern) {
        super(pattern);
        this.headerName = header;
    }
    public boolean equals(Object other) {
        return super.equals(other)
            && ((HeaderTerm) other).headerName.equals(headerName);
    }
    public String getHeaderName() {
        return headerName;
    }
    public int hashCode() {
        return super.hashCode() + headerName.hashCode();
    }
    public boolean match(Message message) {
        try {
            String values[] = message.getHeader(headerName);
            if (values == null || values.length == 0) {
                return false;
            } else {
                boolean result = false;
                for (int i = 0; !result && i < values.length; i++) {
                    String value = values[i];
                    result = match(value);
                }
                return result;
            }
        } catch (MessagingException e) {
            return false;
        }
    }
}
