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
package org.apache.geronimo.mail;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;


/**
 * @version $Rev: $ $Date: $
 */
public class TestStore extends Store {

    public TestStore(Session session, URLName urlName) {
        super(session, urlName);
    }

    public Folder getDefaultFolder() throws MessagingException {
        return null;
    }

    public Folder getFolder(String s) throws MessagingException {
        return null;
    }

    public Folder getFolder(URLName urlName) throws MessagingException {
        return null;
    }
}
