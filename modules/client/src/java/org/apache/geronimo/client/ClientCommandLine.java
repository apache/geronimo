/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.client;

import java.util.Collections;
import java.net.URI;
import javax.management.ObjectName;

import org.apache.geronimo.system.main.CommandLine;

/**
 * @version $Revision$ $Date$
 */
public class ClientCommandLine extends CommandLine {
    /**
     * Command line entry point called by executable jar
     * @param args command line args
     */
    public static void main(String[] args) {
        log.info("Client startup begun");
        try {
            URI configuration = new URI(args[0]);
            String[] clientArgs = new String[args.length -1];
            System.arraycopy(args, 1, clientArgs, 0, clientArgs.length);
            new ClientCommandLine(configuration, clientArgs);

            log.info("Client shutdown completed");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }
    }


    public ClientCommandLine(URI configuration, String[] args) throws Exception {
        super(Collections.singletonList(configuration), new ObjectName("geronimo.client:type=ClientContainer"), "main", args);
    }
}
