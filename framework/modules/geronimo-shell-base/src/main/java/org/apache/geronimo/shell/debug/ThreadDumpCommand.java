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

package org.apache.geronimo.shell.debug;

import java.io.File;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;

@Command(scope = "debug", name = "thread-dump", description = "Dump thread information")
public class ThreadDumpCommand extends OsgiCommandSupport {

    private static final String FORMAT = "yyyyMMdd.HHmmss.SSS";
    private static final SimpleDateFormat df = new SimpleDateFormat(FORMAT);
    
    @Option(name = "-d", aliases = { "--dir" }, description = "Write thread dump to a file in the specified directory")
    String dir = null;
    
    protected Object doExecute() throws Exception {

        ThreadMXBean mxbean = ManagementFactory.getThreadMXBean(); 
        ThreadInfo[] threads = mxbean.dumpAllThreads(true, true);
        
        String timestamp = df.format(new Date());
        
        File dumpFile = null;
        PrintWriter writer = null;
        if (dir != null) {
            dumpFile = new File(dir, "threaddump." + timestamp + ".txt");
            writer = new PrintWriter(dumpFile, "UTF-8");
        } else {
            writer = new PrintWriter(System.out);
        }
        
        writer.println("Thread Dump Timestamp: " + timestamp);
        writer.println();
        
        for (ThreadInfo thread : threads) {
            Utils.writeThreadInfo(thread, writer);
        }
        
        if (dir != null) {
            System.out.println("Thread dump written to " + dumpFile);
            writer.close();
        } else {
            writer.flush();
        }
        
        return null;
    }
       
}
