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

package org.apache.geronimo.deployment.cli;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.util.EncryptionManagerWrapperGBean; 

/**
 * The CLI command to encrypt a given string.
 *
 * @version $Rev:  $ $Date:  $
 */
public class CommandEncrypt extends AbstractCommand {

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        try {
            if (commandArgs.getArgs().length == 0) {
                consoleReader.printString(DeployUtils.reformat("Please enter the string to be encrypted after the encrypt command.", 4, 72));
                return;
            }
            if (commandArgs.getArgs().length > 1) {
                consoleReader.printString(DeployUtils.reformat("Too many parameters. Only the first string will be encrypted.", 4, 72));
            }
            
            consoleReader.printString(DeployUtils.reformat("String to encrypt: "+commandArgs.getArgs()[0], 4, 72));
            if (!isOffline(connection)) {
                // Online encryption            
                Kernel k = getKernel(connection);
                Object ret = k.invoke(EncryptionManagerWrapperGBean.class, "encrypt", new Object[] {commandArgs.getArgs()[0]}, new String[] {"java.io.Serializable"});
                consoleReader.printString(DeployUtils.reformat("Online encryption result: "+ret, 4, 72));
            } else {
                // Offline encryption
                Object ret = EncryptionManager.encrypt(commandArgs.getArgs()[0]);
                consoleReader.printString(DeployUtils.reformat("Offline encryption result: "+ret, 4, 72));
            }
            consoleReader.printNewline();
        } catch (Exception e) {
            throw new DeploymentException("Unable to reach the server to do the encryption.", e);
        }
    }
}
