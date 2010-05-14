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
 * @version $Rev: $ $Date: $
 */
public class EncryptCommandMetaData extends BaseCommandMetaData  {
    public static final CommandMetaData META_DATA = new EncryptCommandMetaData();
    
    private EncryptCommandMetaData() {
        super("encrypt", "2. Other Commands", "[--offline] string",
                "Encrypt a string for use in deployment plan.\n" +
                "If you want to use a running server to do the encryption, so " +
                "that you can use the encryption setting of that server, " +
                "make sure the server is running and specify the general " +
                "options to connect to it.\n "+
                "If you want to use the common simple encryption, use the "+
                " --offline option. No running server is required in this case.");
    }

}
