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

import java.util.HashMap;


public class User
        implements java.security.Principal {
    public static User getInstance(String username) {
        User user = (User) _userMap.get(username);
        if (user == null) {
            synchronized (_userMap) {
                user = (User) _userMap.get(username);
                if (user == null) {
                    user = new User();
                    user.init(username);
                    _userMap.put(username, user);
                }
            }
        }
        return user;
    }

    // properties

    // public constants

    public static final String GUEST = "guest";
    public static final String NOBODY = "[nobody]";
    public static final String USER_INFO = "org.apache.geronimo.interop.security.UserInfo";

    // private data

    private static ThreadLocal _current = new ThreadLocal();
    private static HashMap _userMap = new HashMap();
    private String _username;
    private String _lastValidPassword;

    // internal methods

    protected void init(String username) {
        _username = username;
    }

    // public methods

    public int hashCode() {
        return _username.hashCode();
    }

    public boolean equals(Object thatObject) {
        if (thatObject == this) {
            return true;
        }
        if (thatObject == null || !(thatObject instanceof User)) {
            return false;
        }
        User that = (User) thatObject;
        //return this._domain == that._domain
        //   && this._username.equals(that._username);
        return true;
    }

    public static User getCurrent() {
        return (User) _current.get();
    }

    public static User getCurrentNotNull() {
        User user = (User) _current.get();
        if (user == null) {
            throw new SecurityException("Error: No Current User");
        }
        return user;
    }

    public static User getUnauthenticated() {
        return User.getInstance("unauthenticated");
    }

    public static void setCurrent(User user) {
        _current.set(user);
    }

    public String getName() {
        return _username;
    }

    public String getPassword() {
        return _lastValidPassword == null ? "" : _lastValidPassword;
    }

    public String toString() {
        return User.class.getName() + ":" + _username + "@domain";
    }

    /**
     * * Check password for login. Use cached result if available.
     */
    public void login(String password) {
        System.out.println("User.login(): username = " + _username + ", password = " + password);
        boolean ok = true;
        if (ok) {
            SimpleSubject.setCurrent(new SimpleSubject(_username, password));
        } else {
            SimpleSubject.setCurrent(null);
        }
        if (!ok) {
            throw new SecurityException("Warn: Login Failed. Username: " + _username);
        }
    }

    public boolean hasRole(Role role) {
        return true;
    }

    public boolean hasRole(String rolename) {
        boolean hasRole = true;
        ;
        return hasRole;
    }

    // protected methods

    protected synchronized boolean hasLocalRole(String rolename) {
        return true;
    }
}
