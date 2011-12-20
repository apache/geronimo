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

package org.apache.geronimo.jaxws.sun.tools;

import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * OSGi command for wsimport tool.
 *
 * @version $Rev: 595889 $ $Date: 2007-11-16 20:13:06 -0500 (Fri, 16 Nov 2007) $
 */
@Command(scope = "jaxws-sun-tools", name = "wsimport", description = "Generate JAX-WS artifacts from WSDL")
public class WsimportCommand extends OsgiCommandSupport {

    @Argument(index = 0, name = "arguments", description = "The list arguments for wsimport command", required = true, multiValued = true)
    private List<String> arguments;

    @Override
    protected Object doExecute() throws Exception {
        //TODO  should use io.out instead of System.out?
        return JAXWSToolsCLI.run(JAXWSToolsCLI.Command.WSIMPORT, System.getProperty("org.apache.geronimo.home.dir"), arguments.toArray(new String[arguments.size()]), System.out);
    }

}
