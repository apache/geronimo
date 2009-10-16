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
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.geronimo.cli.CLParserException;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class ListModulesCommandArgsImpl implements ListModulesCommandArgs {
    private final static String ARGUMENT_ALL_SHORTFORM = "a";
    private final static String ARGUMENT_ALL = "all";

    private final static String ARGUMENT_STARTED_SHORTFORM = "s";
    private final static String ARGUMENT_STARTED = "started";
    
    private final static String ARGUMENT_STOPPED_SHORTFORM = "t";
    private final static String ARGUMENT_STOPPED = "stopped";
    
    protected final Options options;
    protected CommandLine commandLine;

    public ListModulesCommandArgsImpl(String[] args) throws CLParserException {
        options = new Options();
        addState();
        
        CommandLineParser parser = new GnuParser();
        try {
            commandLine = parser.parse(options, args, true);
        } catch (ParseException e) {
            throw new CLParserException(e.getMessage(), e);
        }
    }

    public boolean isAll() {
        return commandLine.hasOption(ARGUMENT_ALL_SHORTFORM);
    }

    public boolean isStarted() {
        return commandLine.hasOption(ARGUMENT_STARTED_SHORTFORM);
    }
    
    public boolean isStopped() {
        return commandLine.hasOption(ARGUMENT_STOPPED_SHORTFORM);
    }
    
    public String[] getArgs() {
        return commandLine.getArgs();
    }

    protected void addState() {
        OptionGroup optionGroup = new OptionGroup();

        Option option = new Option(ARGUMENT_ALL_SHORTFORM,
                ARGUMENT_ALL,
                false,
                "All modules will be listed.");
        optionGroup.addOption(option);

        option = new Option(ARGUMENT_STARTED_SHORTFORM,
                ARGUMENT_STARTED,
                false,
                "Only started modules will be listed.");
        optionGroup.addOption(option);

        option = new Option(ARGUMENT_STOPPED_SHORTFORM,
                ARGUMENT_STOPPED,
                false,
                "Only stopped modules will be listed.");
        optionGroup.addOption(option);

        options.addOptionGroup(optionGroup);
    }

}
