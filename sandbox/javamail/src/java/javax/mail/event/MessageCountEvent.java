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
import javax.mail.Folder;
import javax.mail.Message;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:08 $
 */
public class MessageCountEvent extends MailEvent {
    public static final int ADDED = 1;
    public static final int REMOVED = 2;
    protected transient Message msgs[];
    protected boolean removed;
    protected int type;
    public MessageCountEvent(
        Folder folder,
        int type,
        boolean removed,
        Message messages[]) {
        super(folder);
        msgs = messages;
        this.type = type;
        this.removed = removed;
    }
    public void dispatch(Object listener) {
        // assume that it is the right listener type
        MessageCountListener l = (MessageCountListener) listener;
        if (type == ADDED) {
            l.messagesAdded(this);
        } else if (type == REMOVED) {
            l.messagesRemoved(this);
        } else {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }
    public Message[] getMessages() {
        return msgs;
    }
    public int getType() {
        return type;
    }
    public boolean isRemoved() {
        return removed;
    }
}
