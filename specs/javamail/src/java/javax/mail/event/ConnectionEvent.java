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

/**
 * @version $Rev$ $Date$
 */
public class ConnectionEvent extends MailEvent {
    public static final int CLOSED = 3;
    public static final int DISCONNECTED = 2;
    public static final int OPENED = 1;
    protected int type;

    public ConnectionEvent(Object source, int type) {
        super(source);
        this.type = type;
        if (type != DISCONNECTED && type != OPENED && type != CLOSED) {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    public void dispatch(Object listener) {
        // assume that it is the right listener type
        ConnectionListener l = (ConnectionListener) listener;
        if (type == OPENED) {
            l.opened(this);
        } else if (type == DISCONNECTED) {
            l.disconnected(this);
        } else if (type == CLOSED) {
            l.closed(this);
        } else {
            throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    public int getType() {
        return type;
    }
}
