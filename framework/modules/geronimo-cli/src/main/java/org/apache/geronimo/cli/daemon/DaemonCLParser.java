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
package org.apache.geronimo.cli.daemon;

import java.io.OutputStream;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.geronimo.cli.BaseCLParser;
import org.apache.geronimo.cli.PrintHelper;


/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class DaemonCLParser extends BaseCLParser {
    private final static String ARGUMENT_NO_PROGRESS_SHORTFORM = "q";
    private final static String ARGUMENT_NO_PROGRESS = "quiet";
    
    private final static String ARGUMENT_LONG_PROGRESS_SHORTFORM = "l";
    private final static String ARGUMENT_LONG_PROGRESS = "long";
    
    private final static String ARGUMENT_MODULE_OVERRIDE_SHORTFORM = "o";
    private final static String ARGUMENT_MODULE_OVERRIDE = "override";

    private final static String ARGUMENT_SECURE_SHORTFORM = "s";
    private final static String ARGUMENT_SECURE = "secure";
    
    private final static String ARGUMENT_CLEAN_CACHE_SHORTFORM = "c";
    private final static String ARGUMENT_CLEAN_CACHE = "clean";    
    
    public DaemonCLParser(OutputStream out) {
        super(out);
        addProgressOptions();
        addOverride();
        addSecure();
        addCleanCache();
    }
    
    public boolean isCleanCache() {
        return commandLine.hasOption(ARGUMENT_CLEAN_CACHE_SHORTFORM);
    }       

    public boolean isSecure() {
        return commandLine.hasOption(ARGUMENT_SECURE_SHORTFORM);
    }
    
    public boolean isNoProgress() {
        return commandLine.hasOption(ARGUMENT_NO_PROGRESS_SHORTFORM);
    }

    public boolean isLongProgress() {
        return commandLine.hasOption(ARGUMENT_LONG_PROGRESS_SHORTFORM);
    }

    public String[] getOverride() {
        return commandLine.getOptionValues(ARGUMENT_MODULE_OVERRIDE_SHORTFORM);
    }
    
    public void displayHelp() {
        PrintHelper printHelper = new PrintHelper(out);
        printHelper.printHelp("java -jar bin/server.jar $options",
                "\nThe following options are available:",
                options,
                "\nIn addition you may specify a replacement for var/config/config.xml by setting the property "
                        + "-Dorg.apache.geronimo.config.file=var/config/<my-config.xml>. "
                        + "This is resolved relative to the geronimo base directory.\n",
                true);
    }
    
    protected void addOverride() {
        OptionBuilder optionBuilder = OptionBuilder.hasArgs().withArgName("moduleId ...");
        optionBuilder = optionBuilder.withLongOpt(ARGUMENT_MODULE_OVERRIDE);
        optionBuilder = optionBuilder.withDescription("USE WITH CAUTION!  Overrides the modules in "
                + "var/config/config.xml such that only the modules listed on "
                + "the command line will be started.  Note that many J2EE "
                + "features depend on certain modules being started, so you "
                + "should be very careful what you omit.  Any arguments after "
                + "this are assumed to be module names.");
        Option option = optionBuilder.create(ARGUMENT_MODULE_OVERRIDE_SHORTFORM);
        options.addOption(option);
    }

    protected void addSecure() {
        options.addOption(ARGUMENT_SECURE_SHORTFORM, ARGUMENT_SECURE, false,
                "Use secure channel to communicate with the server.  Unsecured channel is used by default.");
    }
    
    protected void addCleanCache() {
        options.addOption(ARGUMENT_CLEAN_CACHE_SHORTFORM,
                ARGUMENT_CLEAN_CACHE,
                false,
                "Delete cache folder. This is typically used when developer wants to pick up artifacts modification in repository");
    }    
    
    protected void addProgressOptions() {
        OptionGroup optionGroup = new OptionGroup();

        Option option = new Option(ARGUMENT_NO_PROGRESS_SHORTFORM,
                ARGUMENT_NO_PROGRESS,
                false,
                "Suppress the normal startup progress bar. This is typically "
                        + "used when redirecting console output to a file, or starting "
                        + "the server from an IDE or other tool.");
        optionGroup.addOption(option);

        option = new Option(ARGUMENT_LONG_PROGRESS_SHORTFORM,
                ARGUMENT_LONG_PROGRESS,
                false,
                "Write startup progress to the console in a format that is "
                        + "suitable for redirecting console output to a file, or starting "
                        + "the server from an IDE or other tool (doesn't use linefeeds to "
                        + "update the progress information that is used by default if you " + "don't specify "
                        + ARGUMENT_NO_PROGRESS + " or " + ARGUMENT_LONG_PROGRESS + ").");
        optionGroup.addOption(option);

        options.addOptionGroup(optionGroup);
    }

}
