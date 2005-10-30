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
package org.apache.geronimo.system.main;

import java.net.URI;
import java.util.Collections;
import javax.management.ObjectName;

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
        if(args.length == 0) {
            System.out.println();
            System.out.println("ERROR: No arguments");
            showHelp();
            System.exit(1);
        } else if(args[0].equals("--help") || args[0].equals("-h") || args[0].equals("/?")) {
            showHelp();
            System.exit(0);
        }
        try {
            URI configuration = new URI(args[0]);
            String[] clientArgs = new String[args.length -1];
            System.arraycopy(args, 1, clientArgs, 0, clientArgs.length);
            new ClientCommandLine(configuration, clientArgs);

            log.info("Client shutdown completed");
        } catch (Exception e) {
            ExceptionUtil.trimStackTrace(e);
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }
    }

    private static void showHelp() {
        System.out.println();
        System.out.println("syntax:   java -jar bin/client.jar config-name [app arg] [app arg] ...");
        System.out.println();
        System.out.println("The first argument should identify the Geronimo configuration that");
        System.out.println("contains the application client you want to run.");
        System.out.println();
        System.out.println("The rest of the arguments will be passed as arguments to the");
        System.out.println("application client when it is started.");
        System.out.println();
    }


    public ClientCommandLine(URI configuration, String[] args) throws Exception {
        invokeMainGBean(Collections.singletonList(configuration), new ObjectName("geronimo.client:type=ClientContainer"), "main", args);
    }
}
