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

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * @version $Rev$ $Date$
 */
public final class RecipientTerm extends AddressTerm {
    protected Message.RecipientType type;

    public RecipientTerm(Message.RecipientType type, Address address) {
        super(address);
        this.type = type;
    }

    public boolean equals(Object other) {
        return super.equals(other) && ((RecipientTerm) other).type.equals(type);
    }

    public Message.RecipientType getRecipientType() {
        return type;
    }

    public int hashCode() {
        return super.hashCode() + type.hashCode();
    }

    public boolean match(Message message) {
        try {
            Address from[] = message.getRecipients(type);
            boolean result = false;
            for (int i = 0; !result && i < from.length; i++) {
                result = match(from[i]);
            }
            return result;
        } catch (MessagingException e) {
            return false;
        }
    }
}
