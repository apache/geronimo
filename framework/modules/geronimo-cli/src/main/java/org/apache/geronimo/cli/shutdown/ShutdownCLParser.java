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
package org.apache.geronimo.cli.shutdown;

import java.io.OutputStream;

import org.apache.geronimo.cli.BaseCLParser;
import org.apache.geronimo.cli.PrintHelper;

/**
 * @version $Rev$ $Date$
 */
public class ShutdownCLParser extends BaseCLParser {
    
    private final static String ARGUMENT_HOST_SHORTFORM = "host";
    
    private final static String ARGUMENT_PORT_SHORTFORM = "port";
    private final static String ARGUMENT_PORT = "port";
    
    private final static String ARGUMENT_USER_SHORTFORM = "u";
    private final static String ARGUMENT_USER = "user";

    private final static String ARGUMENT_PASSWORD_SHORTFORM = "p";
    private final static String ARGUMENT_PASSWORD = "password";
    
    private final static String ARGUMENT_SECURE_SHORTFORM = "secure";
    private final static String ARGUMENT_SECURE = "secure";
    
    public ShutdownCLParser(OutputStream out) {
        super(out);
        
        addHost();
        addPort();
        addUser();
        addPassword();
        addSecure();
    }

    public String getHost() {
        return commandLine.getOptionValue(ARGUMENT_HOST_SHORTFORM);
    }
    
    public Integer getPort() {
        String port = commandLine.getOptionValue(ARGUMENT_PORT_SHORTFORM);
        if (null == port) {
            return null;
        }
        return new Integer(port);
    }
    
    public String getUser() {
        return commandLine.getOptionValue(ARGUMENT_USER_SHORTFORM);
    }
    
    public String getPassword() {
        return commandLine.getOptionValue(ARGUMENT_PASSWORD_SHORTFORM);
    }
    
    public boolean isSecure() {
        return commandLine.hasOption(ARGUMENT_SECURE_SHORTFORM);
    }    
    
    public void displayHelp() {
        PrintHelper printHelper = new PrintHelper(System.out);
        printHelper.printHelp("java -jar bin/shutdown.jar $options",
                "\nThe following options are available:",
                options,
                "\n",
                true);
    }

    protected void addSecure() {
        options.addOption(ARGUMENT_SECURE_SHORTFORM,
                ARGUMENT_SECURE,
                false,
                "Use secure channel to communicate with the server.  Unsecured channel is used by default.");
    }
    
    protected void addPassword() {
        addOptionWithParam(ARGUMENT_PASSWORD,
                ARGUMENT_PASSWORD_SHORTFORM,
                "password",
                "Specifies a password to use to authenticate to the server.");
    }

    protected void addUser() {
        addOptionWithParam(ARGUMENT_USER,
                ARGUMENT_USER_SHORTFORM,
                "username",
                "Specifies a username to use to authenticate to the server.");
    }
    
    protected void addPort() {
        addOptionWithParam(ARGUMENT_PORT,
                ARGUMENT_PORT_SHORTFORM,
                "port",
                "The RMI port of a Geronimo server to stop.");
    }

    protected void addHost() {
        addOptionWithParam(ARGUMENT_HOST_SHORTFORM,
                ARGUMENT_HOST_SHORTFORM,
                "hostname",
                "The host name of a Geronimo server to stop.");                        
    }
    
}
