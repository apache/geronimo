/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.twiddle.commands;

import java.io.PrintWriter;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.geronimo.common.NullArgumentException;
import org.apache.geronimo.twiddle.command.Command;
import org.apache.geronimo.twiddle.command.CommandException;
import org.apache.geronimo.twiddle.command.CommandInfo;
import org.apache.geronimo.twiddle.command.AbstractCommand;
import org.apache.geronimo.twiddle.util.HelpFormatter;

/**
 * Dumps system properties.
 *
 * @version <code>$Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $</code>
 */
public class DumpPropertiesCommand
    extends AbstractCommand
{
    public int execute(String[] args) throws Exception
    {
        if (args == null) {
            throw new NullArgumentException("args");
        }
        
        // Get our output writer
        PrintWriter out = getWriter();
        
        // Create the Options
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help")
                                       .withDescription("Display this help message")
                                       .create('h'));
        
        // Create the command line parser
        CommandLineParser parser = new PosixParser();
        
        // Carse the command line arguments
        CommandLine line = parser.parse(options, args);
        
        // Display help
        if (line.hasOption('h')) {
            CommandInfo info = getCommandInfo();
            if (info.hasDescription()) {
                out.println(info.getDescription());
                out.println();
            }
            
            HelpFormatter formatter = new HelpFormatter(out);
            formatter.print(info.getName() + " [options] [prefix]", options);
            
            return Command.SUCCESS;
        }
        
        String[] unused = line.getArgs();
        String prefix = null;
        if (unused != null && unused.length != 0) {
            if (unused.length == 1) {
                prefix = unused[0];
            }
            else {
                throw new CommandException("Too many arguments");
            }
        }
        log.debug("Using prefix: " + prefix);
        
        java.util.Iterator iter = System.getProperties().keySet().iterator();
        while (iter.hasNext()) {
            String name = (String)iter.next();
            if (prefix == null || name.startsWith(prefix)) {
                out.print(name + "=");
                out.println(System.getProperty(name));
            }
        }
        
        // Will never reach here
        assert false;
        
        return Command.FAILURE;
    }
}
