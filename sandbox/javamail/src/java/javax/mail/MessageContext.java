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

package javax.mail;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:07 $
 */
public class MessageContext {
    private Part _part;
    public MessageContext(Part part) {
        _part = part;
    }
    public Message getMessage() {
        return getMessageFrom(getPart());
    }
    private Message getMessageFrom(Part part) {
        if (part instanceof Message) {
            return (Message)part;
        } else if (part instanceof BodyPart) {
            Part parent = ((Multipart)part).getParent();
            return getMessageFrom(parent);
        } else if (part instanceof Multipart) {
            Part parent = ((Multipart)part).getParent();
            return getMessageFrom(parent);
        } else {
            return null;
        }
    }
    public Part getPart() {
        return _part;
    }
    public Session getSession() {
        Message message = getMessage();
        if (message == null) {
            return null;
        } else {
            return message.session;
        }
    }
}
