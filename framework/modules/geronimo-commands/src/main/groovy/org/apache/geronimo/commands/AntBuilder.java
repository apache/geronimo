/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.commands;

import java.io.PrintStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;

import org.slf4j.Logger;

import org.apache.geronimo.gshell.command.IO;

/**
 * Custom Ant builder to setup the desired output formatting.
 *
 * @version $Rev$ $Date$
 */
public class AntBuilder
    extends groovy.util.AntBuilder
{
    public AntBuilder(final Logger log, final IO io) {
        super(createProject(log, io));
    }

    protected static Project createProject(final Logger log, final IO io) {
        Project project = new Project();
        project.addBuildListener(new OutputAdapter(log, io));
        project.init();
        project.getBaseDir();

        return project;
    }
    
    private static class OutputAdapter
        extends DefaultLogger
    {
        private Logger log;
        
        private IO io;

        public OutputAdapter(final Logger log, final IO io) {
            assert log != null;
            assert io != null;
            
            this.log = log;
            this.io = io;
            
            setOutputPrintStream(new PrintStream(io.outputStream, true));
            setErrorPrintStream(new PrintStream(io.errorStream, true));
            setEmacsMode(true);
            
            String level = System.getProperty("gshell.log.console.level");
            if ("DEBUG".equals(level)) {
                setMessageOutputLevel(Project.MSG_DEBUG);
            }
            else {
                setMessageOutputLevel(Project.MSG_INFO);
            }
        }

        protected void printMessage(final String message, final PrintStream stream, final int priority) {
            assert message != null;
            assert stream != null;
            
            switch (priority) {
                case Project.MSG_ERR:
                    log.error(message);
                    break;

                case Project.MSG_WARN:
                    log.warn(message);
                    break;

                case Project.MSG_INFO:
                    stream.println(message);
                    break;

                case Project.MSG_VERBOSE:
                case Project.MSG_DEBUG:
                    log.debug(message);
                    break;
                
                default:
                    // Should never happen
                    throw new Error("Invalid priority: " + priority);
            }
        }
    }
}
