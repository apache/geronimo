/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.testsupport.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.mavenplugins.geronimo.ServerProxy;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.taskdefs.condition.Os;

public class CommandTestSupport {
        
    public static final String GSH = "gsh";
    public static final String DEPLOY = "deploy";
    
    protected static final long timeout = 30000;    
    protected static String geronimoHome;
    
    static {
        geronimoHome = getGeronimoHome();
    }
        
    private static String getGeronimoHome() {
        ServerProxy server = null;
        try {
            server = new ServerProxy("localhost", 1099, "system", "manager");           
        } catch (Exception e) {
            throw new RuntimeException("Unable to setup ServerProxy", e);
        }
        
        String home = server.getGeronimoHome();
        Throwable exception = server.getLastError();
        
        server.closeConnection();
        
        if (exception != null) {
            throw new RuntimeException("Failed to get Geronimo home", exception);
        } else {
            return home;
        }
    }
        
    public CommandTestSupport() {
    }
    
    public void execute(String command, String[] args, InputStream in, OutputStream out) throws Exception {
        execute(command, (args == null) ? null : Arrays.asList(args), in, out);
    }
        
    public void execute(String command, List<String> args, InputStream in, OutputStream out) throws Exception {
        List<String> cmdLine = new ArrayList<String>();
        if (isWindows()) {
            cmdLine.add("cmd.exe");
            cmdLine.add("/c");
        }
        cmdLine.add(resolveCommandForOS(command));
        // add command-specific arguments
        cmdLine.addAll(getCommandArguments(command));
        // add user arguments
        if (args != null) {
            cmdLine.addAll(args);
        }
        
        ExecuteWatchdog watchdog = new ExecuteWatchdog( timeout );
        ExecuteStreamHandler streamHandler = new PumpStreamHandler( out, out, in );
        Execute exec = new Execute( streamHandler, watchdog );
        exec.setCommandline( cmdLine.toArray(new String[] {}) );
        List<String> env = getCommandEnvironment(command);
        if (!env.isEmpty()) {
            exec.setEnvironment(env.toArray(new String[] {}) );
        }
        exec.execute();
    }
    
    protected List<String> getCommandArguments(String command) {
        if (GSH.equals(command)) {
            return Arrays.asList("-T", "false");
        } else {
            return Collections.emptyList();
        }
    }
        
    protected List<String> getCommandEnvironment(String command) {
        if (DEPLOY.equals(command)) {
            //this makes the output can be captured in Linux
            return Arrays.asList("JAVA_OPTS=-Djline.terminal=jline.UnsupportedTerminal");
        } else {
            return Collections.emptyList();
        }
    }

    protected String resolveCommandForOS(String command) {
    	String filename = "";
        if (isWindows()) {
            filename = geronimoHome + "/bin/" + command + ".bat";            
        } else {
        	try {
        		File cmdfile1 = new File(geronimoHome + "/bin/" + command);
        		File cmdfile2 = new File(geronimoHome + "/bin/" + command + ".sh");
        		if(cmdfile1.exists()) { 
        			filename = geronimoHome + "/bin/" + command;
        		} else if (cmdfile2.exists()) {
        			filename = geronimoHome + "/bin/" + command + ".sh";
        		}
        	} catch(Exception e) { 
        		
        	}            
        }    
        return filename;
    }
        
    public boolean isWindows() {
        return Os.isFamily("windows");
    }
    
}
