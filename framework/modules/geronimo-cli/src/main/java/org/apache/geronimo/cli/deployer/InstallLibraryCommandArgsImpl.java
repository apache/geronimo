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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.geronimo.cli.CLParserException;

/**
 * @version $Rev$ $Date$
 */
public class InstallLibraryCommandArgsImpl implements InstallLibraryCommandArgs {
    private final static String ARGUMENT_GROUP_ID_SHORTFORM = "g";
    private final static String ARGUMENT_GROUP_ID = "groupId";

    protected final Options options;
    protected CommandLine commandLine;

    public InstallLibraryCommandArgsImpl(String[] args) throws CLParserException {
        options = new Options();
        addGroupId();
        
        CommandLineParser parser = new GnuParser();
        try {
            commandLine = parser.parse(options, args, true);
        } catch (ParseException e) {
            throw new CLParserException(e.getMessage(), e);
        }
        
        if (0 == commandLine.getArgs().length) {
            throw new CLParserException("Must specify a LibraryFile.");
        } else if (1 < commandLine.getArgs().length) {
            throw new CLParserException("Too many arguments.");
        }
    }

    protected void addGroupId() {
        OptionBuilder optionBuilder = OptionBuilder.hasArg().withArgName(ARGUMENT_GROUP_ID);
        optionBuilder = optionBuilder.withLongOpt(ARGUMENT_GROUP_ID);
        optionBuilder = optionBuilder
                .withDescription("If a groupId is provided, the library file will be installed under that groupId. "+
                        "Otherwise, default will be used.");
        Option option = optionBuilder.create(ARGUMENT_GROUP_ID_SHORTFORM);
        options.addOption(option);
    }

    public String getGroupId() {
        return commandLine.getOptionValue(ARGUMENT_GROUP_ID_SHORTFORM);
    }
    
    public String[] getArgs() {
        return commandLine.getArgs();
    }
    
}
