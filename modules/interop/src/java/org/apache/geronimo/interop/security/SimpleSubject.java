/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.security;

public class SimpleSubject {
    // -----------------------------------------------------------------------
    // public data
    // -----------------------------------------------------------------------

    public static final int FLAG_SESSION_MANAGER = 1;

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static ThreadLocal _current = new ThreadLocal();

    private String _username;

    private String _password;

    private int _flags;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public static SimpleSubject getCurrent() {
        return (SimpleSubject) _current.get();
    }

    public static void setCurrent(SimpleSubject subject) {
        _current.set(subject);
    }

    public SimpleSubject(String username, String password) {
        _username = username;
        _password = password;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public int getFlags() {
        return _flags;
    }

    public void setFlags(int flags) {
        _flags = flags;
    }
}
