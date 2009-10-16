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
public abstract class BaseCommandMetaData implements CommandMetaData  {
    private final String command;
    private final String group;
    private final String helpArgumentList;
    private final String helpText;

    protected BaseCommandMetaData(String command, String group, String helpArgumentList, String helpText) {
        if (null == command) {
            throw new IllegalArgumentException("command is required");
        } else if (null == group) {
            throw new IllegalArgumentException("group is required");
        } else if (null == helpArgumentList) {
            throw new IllegalArgumentException("helpArgumentList is required");
        } else if (null == helpText) {
            throw new IllegalArgumentException("helpText is required");
        }
        this.command = command;
        this.group = group;
        this.helpArgumentList = helpArgumentList;
        this.helpText = helpText;
    }

    public CommandArgs parse(String[] newArgs) throws CLParserException {
        return new BaseCommandArgs(newArgs);
    }
    
    public String getCommandName() {
        return command;
    }

    public String getHelpArgumentList() {
        return helpArgumentList;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getCommandGroup() {
        return group;
    }

}
