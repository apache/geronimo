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
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class DistributeCommandArgsImpl implements DistributeCommandArgs {
    private final static String ARGUMENT_IN_PLACE_SHORTFORM = "i";
    private final static String ARGUMENT_IN_PLACE = "inPlace";

    private final static String ARGUMENT_TARGETS_SHORTFORM = "t";
    private final static String ARGUMENT_TARGETS = "targets";
    
    protected final Options options;
    protected CommandLine commandLine;

    public DistributeCommandArgsImpl(String[] args) throws CLParserException {
        options = new Options();
        addInPlace();
        addTargets();
        
        CommandLineParser parser = new GnuParser();
        try {
            commandLine = parser.parse(options, args, true);
        } catch (ParseException e) {
            throw new CLParserException(e.getMessage(), e);
        }
        
        if (0 == commandLine.getArgs().length) {
            throw new CLParserException("Must specify a module or plan (or both)");
        } else if (2 < commandLine.getArgs().length) {
            throw new CLParserException("Too many arguments");
        }
    }

    protected void addTargets() {
        OptionBuilder optionBuilder = OptionBuilder.hasArg().withArgName("targets");
        optionBuilder = optionBuilder.withLongOpt(ARGUMENT_TARGETS);
        optionBuilder = optionBuilder
                .withDescription("If no targets are provided, the module is distributed to all available "
                        + "targets. Geronimo only provides one target (ever), so this is primarily "
                        + "useful when using a different driver.\n");
        Option option = optionBuilder.create(ARGUMENT_TARGETS_SHORTFORM);
        options.addOption(option);
    }

    protected void addInPlace() {
        options.addOption(ARGUMENT_IN_PLACE_SHORTFORM,
                ARGUMENT_IN_PLACE,
                false,
                "If inPlace is provided, the module is not copied to the configuration "
                        + "store of the selected targets. The targets directly use the module.");
    }

    public String[] getTargets() {
        String targets = commandLine.getOptionValue(ARGUMENT_TARGETS_SHORTFORM);
        if (null == targets) {
            return new String[0];
        }
        return targets.split(";");
    }
    
    public boolean isInPlace() {
        return commandLine.hasOption(ARGUMENT_IN_PLACE_SHORTFORM);
    }
    
    public String[] getArgs() {
        return commandLine.getArgs();
    }
    
}
