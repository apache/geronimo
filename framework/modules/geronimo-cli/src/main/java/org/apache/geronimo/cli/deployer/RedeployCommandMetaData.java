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
public class RedeployCommandMetaData extends BaseCommandMetaData  {
    public static final CommandMetaData META_DATA = new RedeployCommandMetaData();

    private RedeployCommandMetaData() {
        super("redeploy", "1. Common Commands", "[--targets target] [module] [plan] [ModuleID|TargetModuleID+]",
                "A shortcut to undeploy a module from one or more servers, then " +
                "deploy a new version.  This is not a smooth cutover -- some client " +
                "requests may be rejected while the redeploy takes place.\n" +
                "Normally both a module and plan are passed to the deployer. " +
                "Sometimes the module contains a plan, or requires no plan, in which case " +
                "the plan may be omitted.  Sometimes the plan references a module already " +
                "deployed in the Geronimo server environment, in which case a module does " +
                "not need to be provided.\n" +
                "If more than one TargetModuleID is provided, all TargetModuleIDs " +
                "must refer to the same module (just running on different targets).\n" +
                "Regardless of whether the old module was running or not, the new " +
                "module will be started.\n" +
                "If no ModuleID or TargetModuleID is specified, and you're deploying to "+
                "Geronimo, the deployer will attempt to guess the correct ModuleID for "+
                "you based on the module and/or plan you provided.\n"+
                "Note: To specify a TargetModuleID, use the form TargetName|ModuleName" +
                "Use --targets option only for clustering redeployment. " +
                "For clustering redeployment you can find the target with deploy list-targets command. " +
                "Copy the one with the name as MasterConfigurationStore and use it as a target variable.");
    }
    
    public CommandArgs parse(String[] newArgs) throws CLParserException {
        if (newArgs.length == 0) {
            throw new CLParserException("Must specify a module or plan (or both) and optionally module IDs to replace");
        }
        return super.parse(newArgs);
    }

}
