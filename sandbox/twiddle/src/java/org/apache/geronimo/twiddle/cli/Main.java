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

package org.apache.geronimo.twiddle.cli;

import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.InputStream;

import java.net.URL;

import org.codehaus.classworlds.ClassWorld;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.geronimo.common.NullArgumentException;

import org.apache.geronimo.twiddle.Twiddle;
import org.apache.geronimo.twiddle.config.Configuration;
import org.apache.geronimo.twiddle.config.ConfigurationReader;
import org.apache.geronimo.twiddle.util.HelpFormatter;

/**
 * Command-line interface to <code>Twiddle</code>.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:17 $
 */
public class Main
{
    private String filename = "etc/twiddle.conf";
    private ClassWorld world;
    
    public Main(final ClassWorld world)
    {
        if (world == null) {
            throw new NullArgumentException("world");
        }
        
        this.world = world;
    }
    
    private String[] processCommandLine(final String[] args) throws Exception
    {
        assert args != null;
        
        // create the Options
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("help")
                                       .withDescription("Display this help message")
                                       .create('h'));
                                       
        options.addOption(OptionBuilder.withLongOpt("debug")
                                       .withDescription("Enable debug output")
                                       .create('d'));
                                       
        options.addOption(OptionBuilder.withLongOpt("trace")
                                       .withDescription("Enable trace output")
                                       .create('T'));
                                       
        options.addOption(OptionBuilder.withLongOpt("define")
                                       .withDescription("Define a system property")
                                       .hasArg()
                                       .create('D'));
                                       
        options.addOption(OptionBuilder.withLongOpt("file")
                                       .withDescription("Read a specific configuration file")
                                       .hasArg()
                                       .create('f'));
        
        // create the command line parser
        CommandLineParser parser = new PosixParser();
        
        // parse the command line arguments
        CommandLine line = parser.parse(options, args, true);
        
        // Display command-line help and exit
        if (line.hasOption('h')) {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out), true);
            HelpFormatter formatter = new HelpFormatter(out);
            
            formatter.print(
                Twiddle.getBanner(),
                "twiddle [options] (<command> [options] [arguments])*",
                options
            );
            
            System.exit(0);
        }
        
        // Set system properties
        if (line.hasOption('D')) {
            String[] values = line.getOptionValues('D');
            
            for (int i=0; i<values.length; i++) {
                String name, value;
                int j = values[i].indexOf("=");
                
                if (j == -1) {
                    name = values[i];
                    value = "true";
                }
                else {
                    name = values[i].substring(0, j);
                    value = values[i].substring(j + 1, values[i].length());
                }
                
                System.setProperty(name.trim(), value);
            }
        }
        
        if (line.hasOption('f')) {
            filename = line.getOptionValue('f');
        }
        
        //
        // TODO handle --trace and --debug flags
        //
        
        return line.getArgs();
    }
    
    public void boot(String[] args) throws Exception
    {
        if (args == null) {
            throw new NullArgumentException("args");
        }
        
        // Read property defaults
        InputStream input = getClass().getResourceAsStream("/twiddle.properties");
        if (input != null) {
            System.getProperties().load(input);
        }
        
        // Process command-line options
        args = processCommandLine(args);
        
        URL homeURL = Twiddle.getHomeURL();
        URL configURL = new URL(homeURL, filename);
        
        ConfigurationReader reader = new ConfigurationReader();
        Configuration config = reader.read(configURL);
        
        Twiddle twiddle = new Twiddle(world);
        twiddle.configure(config);
        
        if (args.length != 0) {
            int result = twiddle.execute(args);
            System.exit(result);
        }
        else {
            //
            // TODO: Start the interactive console
            //
            // twiddle.run();
            //
        }
    }
    
    public static void main(final String[] args, final ClassWorld world)
    {
        // args & world are checked for null by Main
        
        try {
            Main main = new Main(world);
            main.boot(args);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public static void main(final String[] args) throws Exception
    {
        // args are checked for null by Main
        
        ClassWorld world = new ClassWorld();
        main(args, world);
    }
}
