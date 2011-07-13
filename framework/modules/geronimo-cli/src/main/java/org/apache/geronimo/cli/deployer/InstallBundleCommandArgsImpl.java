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

public class InstallBundleCommandArgsImpl implements InstallBundleCommandArgs {

    private final static String ARGUMENT_GROUP_ID_SHORTFORM = "g";
    private final static String ARGUMENT_GROUP_ID = "groupId";
    
    private final static String ARGUMENT_START_SHORTFORM = "s";
    private final static String ARGUMENT_START = "start";
        
    private final static String ARGUMENT_START_LEVEL_SHORTFORM = "l";
    private final static String ARGUMENT_START_LEVEL = "startLevel";
    
    protected final Options options;
    protected CommandLine commandLine;

    public InstallBundleCommandArgsImpl(String[] args) throws CLParserException {
        options = new Options();
        addGroupId();
        addStart();
        addStartLevel();
        
        CommandLineParser parser = new GnuParser();
        try {
            commandLine = parser.parse(options, args, true);
        } catch (ParseException e) {
            throw new CLParserException(e.getMessage(), e);
        }
        
        if (0 == commandLine.getArgs().length) {
            throw new CLParserException("Must specify a bundle file");
        }
    }
    
    protected void addStart() {
        options.addOption(ARGUMENT_START_SHORTFORM,
                ARGUMENT_START,
                false,
                "If start is provided, the bundle will be automatically started after recorded in Geronimo.");
    }
    
    protected void addStartLevel() {
        OptionBuilder optionBuilder = OptionBuilder.hasArg().withArgName("startLevel");
        optionBuilder = optionBuilder.withLongOpt(ARGUMENT_START_LEVEL);
        optionBuilder = optionBuilder.withDescription("If no start level are provided, will use the framework's initial bundle start level");
        Option option = optionBuilder.create(ARGUMENT_START_LEVEL_SHORTFORM);
        options.addOption(option);
    }
    
    protected void addGroupId() {
        OptionBuilder optionBuilder = OptionBuilder.hasArg().withArgName("groupId");
        optionBuilder = optionBuilder.withLongOpt(ARGUMENT_GROUP_ID);
        optionBuilder = optionBuilder.withDescription("If gourpId is not provided, will use \"default\" as its group id.");
        Option option = optionBuilder.create(ARGUMENT_GROUP_ID_SHORTFORM);
        options.addOption(option);
    }

    @Override
    public boolean isStart() {
        return commandLine.hasOption(ARGUMENT_START_SHORTFORM);
    }
    
    @Override
    public int getStartLevel(){
        String startLevelStr = commandLine.getOptionValue(ARGUMENT_START_LEVEL_SHORTFORM);
        if (null == startLevelStr) {
            return -1;
        }
        return Integer.valueOf(startLevelStr);
    }
    
    @Override
    public String getGroupId(){
        String groupId = commandLine.getOptionValue(ARGUMENT_GROUP_ID_SHORTFORM);
        return groupId;
    }
    
    @Override
    public String[] getArgs() {
        return commandLine.getArgs();
    }

}
