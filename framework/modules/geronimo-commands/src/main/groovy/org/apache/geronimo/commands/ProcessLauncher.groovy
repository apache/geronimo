/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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

package org.apache.geronimo.commands

import java.util.Timer

import org.apache.geronimo.gshell.common.StopWatch

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.geronimo.gshell.command.IO

/**
 * Helper to execute a process and perform some verification logic to determine if the process is up or not.
 *
 * @version $Rev$ $Date$
 */
class ProcessLauncher
{
    private Logger log = LoggerFactory.getLogger(this.class)
    
    IO io
    
    String name
    
    Closure process
    
    Closure verifier
    
    int verifyWaitDelay = 1000
    
    int timeout = -1
    
    boolean background = false
    
    def launch() {
        assert io
        assert process
        assert name
        
        Throwable error
        
        def runner = {
            try {
                process()
            }
            catch (Exception e) {
                error = e
            }
        }
        
        def t = new Thread(runner, "$name Runner")
        
        io.out.println("Launching ${name}...")
        io.flush()
        
        def watch = new StopWatch()
        watch.start()
        
        t.start()
        
        if (verifier) {
            def timer = new Timer("$name Timer", true)
            
            def timedOut = false
            
            def timeoutTask
            if (timeout > 0) {
                timeoutTask = timer.runAfter(timeout * 1000, {
                    timedOut = true
                })
            }
            
            def started = false
            
            log.debug("Waiting for ${name}...")
            
            while (!started) {
                if (timedOut) {
                    throw new Exception("Unable to verify if $name was started in the given time ($timeout seconds)")
                }
                
                if (error) {
                    throw new Exception("Failed to start: $name", error)
                }
                
                if (verifier()) {
                    started = true
                }
                else {
                    Thread.sleep(verifyWaitDelay)
                }
            }
            
            timeoutTask?.cancel()
        }
        
        io.out.println("$name started in $watch")
        io.flush()
        
        if (!background) {
            log.debug("Waiting for $name to shutdown...")
            
            t.join()
            
            log.debug("$name has shutdown")
        }
    }
}
