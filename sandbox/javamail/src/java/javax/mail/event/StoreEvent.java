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
import javax.mail.Store;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:27 $
 */
public class StoreEvent extends MailEvent {
    public static final int ALERT = 1;
    public static final int NOTICE = 2;
    protected int type;
    protected String message;
    public StoreEvent(Store store, int type, String message) {
        super(store);
        this.type = type;
        this.message = message;
    }
    public int getMessageType() {
        return type;
    }
    public String getMessage() {
        return message;
    }
    public void dispatch(Object listener) {
        ((StoreListener) listener).notification(this);
    }
}
