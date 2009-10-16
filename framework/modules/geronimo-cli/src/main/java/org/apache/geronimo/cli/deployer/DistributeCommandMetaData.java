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
public class DistributeCommandMetaData extends BaseCommandMetaData  {
    public static final CommandMetaData META_DATA = new DistributeCommandMetaData();
    
    private DistributeCommandMetaData() {
        super("distribute", "2. Other Commands", "[--inPlace] [--targets target;target;...] [module] [plan]",
                "Processes a module and adds it to the server environment, but does "+
                "not start it or mark it to be started in the future. " +
                "Normally both a module and plan are passed to the deployer.  " +
                "Sometimes the module contains a plan, or requires no plan, in which case " +
                "the plan may be omitted.  Sometimes the plan references a module already " +
                "deployed in the Geronimo server environment, in which case a module does " +
                "not need to be provided.\n" +
                "If no targets are provided, the module is distributed to all available " +
                "targets.  Geronimo only provides one target (ever), so this is primarily " +
                "useful when using a different driver.\n" +
                "If inPlace is provided, the module is not copied to the configuration " +
                "store of the selected targets. The targets directly use the module.");
    }

    public CommandArgs parse(String[] args) throws CLParserException {
        return new DistributeCommandArgsImpl(args);
    }
    
}
