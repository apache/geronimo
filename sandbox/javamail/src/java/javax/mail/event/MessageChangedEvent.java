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

package javax.mail.event;
import javax.mail.Message;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:08 $
 */
public class MessageChangedEvent extends MailEvent {
    public static final int ENVELOPE_CHANGED = 2;
    public static final int FLAGS_CHANGED = 1;
    protected transient Message msg;
    protected int type;
    public MessageChangedEvent(Object source, int type, Message message) {
        super(source);
        msg = message;
        this.type = type;
        if (type != ENVELOPE_CHANGED && type != FLAGS_CHANGED) {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }
    public void dispatch(Object listener) {
        // assume that it is the right listener type
        MessageChangedListener l = (MessageChangedListener) listener;
        l.messageChanged(this);
    }
    public Message getMessage() {
        return msg;
    }
    public int getMessageChangeType() {
        return type;
    }
}
