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

    public Message.RecipientType getRecipientType() {
        return type;
    }

    public boolean match(Message message) {
        try {
            Address from[] = message.getRecipients(type);
            for (int i = 0; i < from.length; i++) {
                Address address = from[i];
                if (match(address)) {
                    return true;
                }
            }
            return false;
        } catch (MessagingException e) {
            return false;
        }
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof RecipientTerm == false) return false;

        final RecipientTerm recipientTerm = (RecipientTerm) other;
        return address.equals(recipientTerm.address) && type == recipientTerm.type;
    }

    public int hashCode() {
        return address.hashCode();
    }
}
