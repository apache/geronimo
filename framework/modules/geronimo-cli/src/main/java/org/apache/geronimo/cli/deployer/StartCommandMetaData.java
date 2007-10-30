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
 * @version $Rev: 515007 $ $Date: 2007-03-06 18:26:41 +1100 (Tue, 06 Mar 2007) $
 */
public class StartCommandMetaData extends BaseCommandMetaData  {
    public static final CommandMetaData META_DATA = new StartCommandMetaData();
    
    private StartCommandMetaData() {
        super("start", "1. Common Commands", "[ModuleID|TargetModuleID]+",
                "Accepts the configId of a module, or the fully-qualified " +
                "TargetModuleID identifying both the module and the server or cluster it's " +
                "on, and starts that module.  The module should be available to the server " +
                "but not currently running.  If multiple modules are specified, they will " +
                "all be started.\n" +
                "If the server is not running, the module will be marked to start " +
                "next time the server is started.");
    }

    public CommandArgs parse(String[] newArgs) throws CLParserException {
        if (newArgs.length == 0) {
            throw new CLParserException("Must specify a module or target module to start");
        }
        return super.parse(newArgs);
    }

}
