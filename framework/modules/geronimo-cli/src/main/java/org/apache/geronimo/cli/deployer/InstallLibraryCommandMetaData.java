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
 * @version $Rev$ $Date$
 */
public class InstallLibraryCommandMetaData extends BaseCommandMetaData  {
    public static final CommandMetaData META_DATA = new InstallLibraryCommandMetaData();

    private InstallLibraryCommandMetaData() {
        super("install-library", "2. Other Commands", "[--groupId grp] LibraryFile",
                "Installs a library file into repository.\n"+
                "If a groupId is provided, the library file will be installed under that groupId. "+
                "Otherwise, default will be used. "+
                "The artifactId, version and type are computed from the library file name which should be in the form "+
                "<artifactId>-<version>.<type>, for e.g. mylib-1.0.jar.");
    }

    public CommandArgs parse(String[] newArgs) throws CLParserException {
        return new InstallLibraryCommandArgsImpl(newArgs);
    }
}
