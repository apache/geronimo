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
package org.apache.geronimo.cli;

import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class BaseCLParser implements CLParser {
    private final static String ARGUMENT_HELP_SHORTFORM = "h";
    private final static String ARGUMENT_HELP = "help";

    private final static String ARGUMENT_VERBOSE_INFO_SHORTFORM = "v";
    private final static String ARGUMENT_VERBOSE_INFO = "verbose";
    
    private final static String ARGUMENT_VERBOSE_DEBUG_SHORTFORM = "vv";
    private final static String ARGUMENT_VERBOSE_DEBUG = "veryverbose";
    
    private final static String ARGUMENT_VERBOSE_TRACE_SHORTFORM = "vvv";
    private final static String ARGUMENT_VERBOSE_TRACE = "veryveryverbose";
    
    protected final OutputStream out;
    protected final Options options;
    protected CommandLine commandLine;
    
    public BaseCLParser(OutputStream out) {
        if (null == out) {
            throw new IllegalArgumentException("out is required");
        }
        this.out = out;
        options = new Options();
        
        addVerboseOptions();
        addHelp();
    }

    public void parse(String[] args) throws CLParserException {
        CommandLineParser parser = new GnuParser();
        try {
            commandLine = parser.parse(options, args, true);
        } catch (ParseException e) {
            throw new CLParserException(e);
        }
        
        validateOptions();
        validateRemainingArgs();
    }

    public boolean isHelp() {
        return commandLine.hasOption(ARGUMENT_HELP_SHORTFORM);
    }

    public boolean isVerboseInfo() {
        return commandLine.hasOption(ARGUMENT_VERBOSE_INFO_SHORTFORM);
    }
    
    public boolean isVerboseDebug() {
        return commandLine.hasOption(ARGUMENT_VERBOSE_DEBUG_SHORTFORM);
    }

    public boolean isVerboseTrace() {
        return commandLine.hasOption(ARGUMENT_VERBOSE_TRACE_SHORTFORM);
    }
    
    public String[] getArgs() {
        return commandLine.getArgs();
    }

    public void displayHelp() {
        throw new UnsupportedOperationException();
    }

    protected void displayHelp(String[] args) {
        throw new UnsupportedOperationException();
    }

    protected void validateOptions() throws CLParserException {
    }

    protected void validateRemainingArgs() throws CLParserException {
    }
    
    protected void addHelp() {
        options.addOption(ARGUMENT_HELP_SHORTFORM, ARGUMENT_HELP, false, "Display this help.");
    }

    protected void addVerboseOptions() {
        OptionGroup optionGroup = new OptionGroup();

        Option option = new Option(ARGUMENT_VERBOSE_INFO_SHORTFORM,
                ARGUMENT_VERBOSE_INFO,
                false,
                "Reduces the console log level to INFO, resulting in more console output than is normally present.");
        optionGroup.addOption(option);

        option = new Option(ARGUMENT_VERBOSE_DEBUG_SHORTFORM,
                ARGUMENT_VERBOSE_DEBUG,
                false,
                "Reduces the console log level to DEBUG, resulting in still more console output.");
        optionGroup.addOption(option);

        option = new Option(ARGUMENT_VERBOSE_TRACE_SHORTFORM,
                ARGUMENT_VERBOSE_TRACE,
                false,
                "Reduces the console log level to TRACE, resulting in still more console output.");
        optionGroup.addOption(option);

        options.addOptionGroup(optionGroup);
    }

    protected void addOptionWithParam(String longOption, String shortOption, String argName, String desc) {
        OptionBuilder optionBuilder = OptionBuilder.hasArg().withArgName(argName);
        optionBuilder = optionBuilder.withLongOpt(longOption);
        optionBuilder = optionBuilder.withDescription(desc);
        Option option = optionBuilder.create(shortOption);
        options.addOption(option);
    }
}
