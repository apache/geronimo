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

package javax.mail.event;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Transport;

/**
 * @version $Rev$ $Date$
 */
public class TransportEvent extends MailEvent {
    public static final int MESSAGE_DELIVERED = 1;
    public static final int MESSAGE_NOT_DELIVERED = 2;
    public static final int MESSAGE_PARTIALLY_DELIVERED = 3;
    protected int type;
    protected transient Address[] validSent;
    protected transient Address[] validUnsent;
    protected transient Address[] invalid;
    protected transient Message msg;

    public TransportEvent(Transport transport,
                          int type,
                          Address[] validSent,
                          Address[] validUnsent,
                          Address[] invalid,
                          Message message) {
        super(transport);
        this.type = type;
        this.validSent = validSent;
        this.validUnsent = validUnsent;
        this.invalid = invalid;
        if (type != MESSAGE_DELIVERED
                && type != MESSAGE_NOT_DELIVERED
                && type != MESSAGE_PARTIALLY_DELIVERED) {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    public Address[] getValidSentAddresses() {
        return validSent;
    }

    public Address[] getValidUnsentAddresses() {
        return validUnsent;
    }

    public Address[] getInvalidAddresses() {
        return invalid;
    }

    public Message getMessage() {
        return msg;
    }

    public int getType() {
        return type;
    }

    public void dispatch(Object listener) {
        // assume that it is the right listener type
        TransportListener l = (TransportListener) listener;
        if (type == MESSAGE_DELIVERED) {
            l.messageDelivered(this);
        } else if (type == MESSAGE_NOT_DELIVERED) {
            l.messageNotDelivered(this);
        } else if (type == MESSAGE_PARTIALLY_DELIVERED) {
            l.messagePartiallyDelivered(this);
        } else {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }
}
