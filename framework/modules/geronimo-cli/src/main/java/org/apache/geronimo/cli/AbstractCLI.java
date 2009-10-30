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

import java.io.PrintStream;

import org.apache.geronimo.cli.CLParserException;
import org.apache.geronimo.main.Bootstrapper;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public abstract class AbstractCLI {

    private final String[] args;
    private final PrintStream errStream;
    
    protected AbstractCLI(String[] args, PrintStream errStream) {
        if (null == args) {
            throw new IllegalArgumentException("args is required");
        } else if (null == errStream) {
            throw new IllegalArgumentException("errStream is required");
        }
        this.args = args;
        this.errStream = errStream;
    }

    public int executeMain() {
        CLParser parser = getCLParser();
        try {
            parser.parse(args);
        } catch (CLParserException e) {
            errStream.println(e.getMessage());
            parser.displayHelp();
            return 1;
        }

        if (parser.isHelp()) {
            parser.displayHelp();
            return 0;
        }
        
        boolean executed = executeCommand(parser);
        if (executed) {
            return 0;
        }
        
        initializeLogging(parser);
        
        Bootstrapper boot = createBootstrapper();
        return boot.execute(parser);
    }

    protected Bootstrapper createBootstrapper() {
        return new Bootstrapper();
    }
    
    protected boolean executeCommand(CLParser parser) {
        return false;
    }

    protected void initializeLogging(final CLParser parser) {
        assert parser != null;
        
        String level = "WARN";
        
        if (parser.isVerboseInfo()) {
            level = "INFO";
        } else if (parser.isVerboseDebug()) {
            level = "DEBUG";
        } else if (parser.isVerboseTrace()) {
            level = "TRACE";
        }
        
        System.setProperty("org.apache.geronimo.log.ConsoleLogLevel", level);
    }

    protected abstract CLParser getCLParser();

}
