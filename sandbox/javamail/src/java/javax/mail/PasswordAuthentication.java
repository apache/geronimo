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

package javax.mail;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:27 $
 */
public final class PasswordAuthentication {
    private String _user;
    private String _password;
    public PasswordAuthentication(String user, String password) {
        _user = user;
        _password = password;
    }
    public String getUserName() {
        return _user;
    }
    public String getPassword() {
        return _password;
    }
}
