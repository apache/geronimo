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

package javax.mail.search;

import java.io.IOException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Multipart;
import javax.mail.BodyPart;

/**
 * Term that matches on a message body. All {@link javax.mail.BodyPart parts} that have
 * a MIME type of "text/*" are searched.
 *
 * @version $Rev$ $Date$
 */
public final class BodyTerm extends StringTerm {
    public BodyTerm(String pattern) {
        super(pattern);
    }

    public boolean match(Message message) {
        try {
            return matchPart(message);
        } catch (IOException e) {
            return false;
        } catch (MessagingException e) {
            return false;
        }
    }

    private boolean matchPart(Part part) throws MessagingException, IOException {
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            int count = mp.getCount();
            for (int i=0; i < count; i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (matchPart(bp)) {
                    return true;
                }
            }
            return false;
        } else if (part.isMimeType("text/*")) {
            String content = (String) part.getContent();
            return super.match(content);
        } else {
            return false;
        }
    }
}
