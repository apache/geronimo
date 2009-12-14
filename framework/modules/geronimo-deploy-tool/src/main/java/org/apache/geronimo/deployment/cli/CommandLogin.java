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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.geronimo.cli.deployer.CommandArgs;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.crypto.EncryptionManager;

/**
 * The CLI deployer logic to start.
 *
 * @version $Rev$ $Date$
 */
public class CommandLogin extends AbstractCommand {

    public void execute(ConsoleReader consoleReader, ServerConnection connection, CommandArgs commandArgs) throws DeploymentException {
        try {
            File authFile = new File(System.getProperty("user.home"), ".geronimo-deployer");
            if(!authFile.exists()) {
                if(!authFile.createNewFile()) {
                    throw new DeploymentException("Unable to create "+authFile.getAbsolutePath()+" to hold saved logins");
                }
            }
            if(!authFile.canRead() || !authFile.canWrite()) {
                throw new DeploymentException("Saved login file "+authFile.getAbsolutePath()+" is not readable or not writable");
            }
            Properties props = new Properties();
            InputStream authIn = new BufferedInputStream(new FileInputStream(authFile));
            props.load(authIn);
            authIn.close();
            props.setProperty("login."+connection.getServerURI(), EncryptionManager.encrypt(connection.getAuthentication()));
            OutputStream save = new BufferedOutputStream(new FileOutputStream(authFile));
            props.store(save, "Saved authentication information to connect to Geronimo servers");
            save.flush();
            save.close();
            consoleReader.printString(DeployUtils.reformat("Saved login for: "+connection.getServerURI(), 4, 72));
            consoleReader.printNewline();
        } catch (IOException e) {
            throw new DeploymentException("Unable to save authentication to login file", e);
        }
    }
}
