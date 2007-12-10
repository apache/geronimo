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
package org.apache.geronimo.commands;

import org.apache.geronimo.cli.deployer.InstallLibraryCommandArgs;

/**
 * @version $Rev: 597690 $ $Date: 2007-11-23 10:43:02 -0500 (Fri, 23 Nov 2007) $
 */
public class InstallLibraryCommandArgsImpl implements InstallLibraryCommandArgs {
   
    private String libraryFile;
    private String groupId;
    
    public InstallLibraryCommandArgsImpl(String libraryFile, String groupId) {
        this.libraryFile = libraryFile;
        this.groupId = groupId;
    }

    public String getGroupId() {
        return this.groupId;
    }
    
    public String[] getArgs() {
        return new String[] {this.libraryFile};
    }
    
}
