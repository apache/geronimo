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
package org.apache.geronimo.main;

public class ScriptLaunchListener implements LaunchListener {

    private final String propertyPrefix;
    
    public ScriptLaunchListener(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }
    
    @Override
    public void starting() {
        String cmd = getOption("starting.cmd", null);
        if (cmd != null) {
            execute(cmd);
        }
    }

    @Override
    public void started() {
        String cmd = getOption("started.cmd", null);
        if (cmd != null) {
            execute(cmd);
        }
    }

    @Override
    public void stopped(int errorCode) {
        String cmd = getOption("stopped.cmd", null);
        if (cmd != null) {
            cmd = cmd + " " + errorCode;
            execute(cmd);
        }
    }
   
    private String getOption(String type, String defaultValue) {
        if (propertyPrefix == null) {
            return System.getProperty(type, defaultValue);
        } else {
            return System.getProperty(propertyPrefix + "." + type, defaultValue);
        }
    }
    
    private void execute(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            if (waitForCommand()) {
                process.waitFor();
            }
        } catch (Exception e) {
            System.err.println("Error executing command [" + cmd + "]");
        }       
    }
    
    private boolean waitForCommand() {
        String value = getOption("cmd.waitFor", "false");
        return Boolean.parseBoolean(value);
    }
}
