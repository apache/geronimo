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

import java.net.InetAddress;

/**
 * @version $Rev$ $Date$
 */
public abstract class Authenticator {
    private InetAddress _host;
    private int _port;
    private String _prompt;
    private String _protocol;

    protected final String getDefaultUserName() {
        return System.getProperty("mail.user");
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return null;
    }

    protected final int getRequestingPort() {
        return _port;
    }

    protected final String getRequestingPrompt() {
        return _prompt;
    }

    protected final String getRequestingProtocol() {
        return _protocol;
    }

    protected final InetAddress getRequestingSite() {
        return _host;
    }
}
