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
package org.apache.geronimo.cli.client;

import java.io.OutputStream;

import org.apache.geronimo.cli.BaseCLParser;
import org.apache.geronimo.cli.CLParserException;
import org.apache.geronimo.cli.PrintHelper;


/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class ClientCLParser extends BaseCLParser {
    
    public ClientCLParser(OutputStream out) {
        super(out);
    }

    public String getApplicationClientConfiguration() {
        String[] args = commandLine.getArgs();
        return args[0];
    }
    
    public String[] getApplicationClientArgs() {
        String[] args = commandLine.getArgs();
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }
    
    @Override
    protected void validateRemainingArgs() throws CLParserException {
        if (0 == commandLine.getArgs().length) {
            throw new CLParserException("No configuration provided");
        }
    }

    public void displayHelp() {
        PrintHelper printHelper = new PrintHelper(System.out);
        printHelper.printHelp("java -jar bin/client.jar $options config-name [app_arg ...]",
                "\nThe following options are available:",
                options,
                "\nThe first argument should identify the Geronimo configuration that "
                        + "contains the application client you want to run." + "\n"
                        + "The rest of the arguments will be passed as arguments to the "
                        + "application client when it is started.",
                true);
    }

}
