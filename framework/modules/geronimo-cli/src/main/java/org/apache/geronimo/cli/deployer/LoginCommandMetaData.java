/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.cli.deployer;


/**
 * @version $Rev: 515007 $ $Date: 2007-03-06 18:26:41 +1100 (Tue, 06 Mar 2007) $
 */
public class LoginCommandMetaData extends BaseCommandMetaData  {
    public static final CommandMetaData META_DATA = new LoginCommandMetaData();
    
    private LoginCommandMetaData() {
        super("login", "1. Common Commands", "",
                "Saves the username and password for this connection to the "+
                "file .geronimo-deployer in the current user's home directory.  " +
                "Future connections to the same server will try to use this "+
                "saved authentication information instead of prompting where " +
                "possible.  This information is saved separately per connection " +
                "URL, so you can specify --url or --host and/or --port on the command " +
                "line to save a login to a different server.\n" +
                "WARNING: while the login information is not saved in " +
                "clear text, it is not secure either.  If you want to " +
                "save the authentication securely, you should change the " +
                ".geronimo-deployer file in your home directory so that nobody " +
                "else can read or write it.");
    }

}
