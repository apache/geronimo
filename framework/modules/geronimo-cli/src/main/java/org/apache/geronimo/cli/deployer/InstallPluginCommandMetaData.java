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
public class InstallPluginCommandMetaData extends BaseCommandMetaData  {
    public static final CommandMetaData META_DATA = new InstallPluginCommandMetaData();

    private InstallPluginCommandMetaData() {
        super("install-plugin", "3. Geronimo Plugins", "PluginFile",
                "Installs a Geronimo plugin you've exported from a Geronimo server " +
                "or downloaded from an external repository.  The file must be a " +
                "properly configured Geronimo CAR file.  This is used to add new " +
                "functionality to the Geronimo server.");
    }

    public CommandArgs parse(String[] newArgs) throws CLParserException {
        if (newArgs.length != 1) {
            throw new CLParserException("Must specify Plugin CAR file");
        }
        return new BaseCommandArgs(newArgs);
    }
    
}
