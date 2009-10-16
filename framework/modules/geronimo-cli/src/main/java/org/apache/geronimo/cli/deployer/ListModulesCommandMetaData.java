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
public class ListModulesCommandMetaData extends BaseCommandMetaData  {
    public static final CommandMetaData META_DATA = new ListModulesCommandMetaData();

    private ListModulesCommandMetaData() {
        super("list-modules", "2. Other Commands", "[--all|--started|--stopped] [target*]",
                "Lists the modules available on the specified targets.  If " +
                "--started or --stopped is specified, only started or stopped modules will " +
                "be listed; otherwise all modules will be listed.  If no targets are " +
                "specified, then modules on all targets will be listed; otherwise only " +
                "modules on the specified targets.");
    }

    public CommandArgs parse(String[] args) throws CLParserException {
        return new ListModulesCommandArgsImpl(args);
    }

}
