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

import org.apache.geronimo.cli.CLParserException;

/**
 * @version $Rev$ $Date$
 */
public class UnlockKeystoreCommandMetaData extends BaseCommandMetaData {
    public static final CommandMetaData META_DATA = new UnlockKeystoreCommandMetaData();

    private UnlockKeystoreCommandMetaData() {
        super("unlock-keystore", "2. Other Commands", "[keyStoreName][AliasName1][AliasName2]....",
                "Command to unlock a keystore and its associated private key.\n" +
                "The usage of javax.net.ssl.keyStorePassword and javax.net.ssl.trustStorePassword " +
                "in a command line has been deprecated.\n"+
                "Use org.apache.geronimo.keyStoreTrustStorePasswordFile " +
                "property to specify the properties file containing password of keystore and its private keys.\n"+
                "Keystore password and associated private key password should be specified in " +
                "encrypted format in a properties file.\n"+
                "The private key password should follow the format AliasName1=<Encrypted_AliasName1_Password>\n"+
                "and keystorepassword should follow the format keyStorePassword=<Encrypted_KeyStore_Password>"
                );
    }

    public CommandArgs parse(String[] newArgs) throws CLParserException {
        if (newArgs.length == 0) {
            throw new CLParserException("Must specify a keystore name");
        }
        return new BaseCommandArgs(newArgs);
    }
}
