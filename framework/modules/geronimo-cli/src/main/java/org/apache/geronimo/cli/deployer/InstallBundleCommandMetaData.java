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

public class InstallBundleCommandMetaData extends BaseCommandMetaData {
    public static final CommandMetaData META_DATA = new InstallBundleCommandMetaData();

    private InstallBundleCommandMetaData() {
        super("install-bundle", 
                "2. Other Commands", 
                "[--groupId groupId] [--startLevel number] [--start] bundleFile",
                "Install and record an OSGi bundle file in Geronimo so that it can be automatically started " + 
                "even after you cleaned the cache directory of the OSGi framework.\n" +
                "If the file is a normal jar file, then will automatically try convert the jar file to an OSGi bundle.\n" +
                "The Artifact will be calculated based on the rules which same with the install-library command, that is:\n" +
                "(1) a file with filename in the form <artifact>-<version>.<type>, for e.g. mylib-1.0.jar;\n"+
                "(2) or if it is an OSGi bundle, will use its Bundle-SymbolicName and Bundle-Version."
                );
    }
    
    @Override
    public CommandArgs parse(String[] newArgs) throws CLParserException {
        return new InstallBundleCommandArgsImpl(newArgs);
    }
}
