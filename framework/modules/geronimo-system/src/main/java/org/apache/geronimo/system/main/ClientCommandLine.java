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
package org.apache.geronimo.system.main;

import java.util.Collections;

import org.apache.geronimo.cli.CLParserException;
import org.apache.geronimo.cli.client.ClientCLParser;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.LifecycleException;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Revision$ $Date$
 */
public class ClientCommandLine extends CommandLine {

    /**
     * Command line entry point called by executable jar
     * @param args command line args
     */
    public static void main(String[] args) {
        ClientCLParser parser = new ClientCLParser(System.out);
        try {
            parser.parse(args);
        } catch (CLParserException e) {
            System.err.println(e.getMessage());
            parser.displayHelp();
            System.exit(1);
        }
        
        ClientCommandLine clientCommandLine = new ClientCommandLine();
        int exitCode = clientCommandLine.execute(parser);
        System.exit(exitCode);
    }

    public ClientCommandLine(Artifact configuration, String[] args) throws Exception {
        startClient(configuration, args);
    }
    
    protected ClientCommandLine() {
    }
    
    public int execute(ClientCLParser parser) {
        String applicationClientConfiguration = parser.getApplicationClientConfiguration();
        log.info("Client startup begun of configuration: " + applicationClientConfiguration);
        try {
            Artifact configuration = Artifact.create(applicationClientConfiguration);
            return startClient(configuration, parser.getApplicationClientArgs());
        } catch (Exception e) {
            ExceptionUtil.trimStackTrace(e);
            if (e instanceof LifecycleException && e.getCause() instanceof NoSuchConfigException) {
                log.error("The client is not found in server: " + ((NoSuchConfigException) e.getCause()).getConfigId());
            } else {
                log.error("Client failed with exception: ", e);
            }
            return 2;
        }
    }
    
    protected int startClient(Artifact configuration, String[] args) throws Exception {
        Jsr77Naming naming = new Jsr77Naming();
        //this kinda sucks, but resource adapter modules deployed on the client insist on having a
        //J2EEApplication name component
        AbstractName baseName = naming.createRootName(configuration, configuration.toString(), "J2EEApplication");
        AbstractNameQuery baseNameQuery = new AbstractNameQuery(baseName);
        invokeMainGBean(Collections.singletonList(configuration), baseNameQuery, "main", args);
        return 0;
    }
}
