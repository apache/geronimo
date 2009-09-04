/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.cxf.tools;

import org.apache.geronimo.gshell.command.CommandContext;
import org.apache.geronimo.gshell.command.CommandSupport;
import org.apache.geronimo.gshell.command.annotation.CommandComponent;
import org.apache.geronimo.gshell.command.annotation.Requirement;
import org.apache.geronimo.gshell.shell.ShellInfo;

/**
 * GShell command for java2wsdl tool.
 */
@CommandComponent(id="geronimo-cxf-tools:java2ws", description="Generate JAX-WS artifacts from class")
public class Java2WSCommand extends CommandSupport {
    
    @Requirement
    ShellInfo shellInfo;
             
    @Override
    public Object execute(final CommandContext context, final Object... args) throws Exception {
        init(context);
        
        String[] arguments = toString(args);
        if (arguments.length >= 1 && arguments[0].equalsIgnoreCase("--help")) {
            arguments[0] = "-help";
        }
        return JAXWSToolsCLI.run(JAXWSToolsCLI.Command.JAVA2WS, 
                                 shellInfo.getHomeDir().getAbsolutePath(),
                                 arguments);
    }
    
    @Override
    protected Object doExecute() throws Exception { 
        return null;
    }
        
    private static String[] toString(Object [] args) {
        String [] a = new String[args.length];
        for (int i=0; i<a.length; i++) {
            a[i] = args[i].toString();
        }
        return a;
    }
        
}
