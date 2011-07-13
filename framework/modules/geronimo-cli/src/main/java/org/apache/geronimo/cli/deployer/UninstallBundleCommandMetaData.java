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

public class UninstallBundleCommandMetaData extends BaseCommandMetaData {
    public static final CommandMetaData META_DATA = new UninstallBundleCommandMetaData();

    private UninstallBundleCommandMetaData() {
        super("uninstall-bundle", 
                "2. Other Commands", 
                "bundleId",
                "Uninstall the bundle and erase its record if it appears in startup.properties."
                );
    }

    public CommandArgs parse(String[] newArgs) throws CLParserException {
        if (newArgs.length == 0) {
            throw new CLParserException("Must specify a bundle id");
        }
        return new BaseCommandArgs(newArgs);
    }
}