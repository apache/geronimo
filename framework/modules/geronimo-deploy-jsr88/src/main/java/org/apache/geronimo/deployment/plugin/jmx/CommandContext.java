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
package org.apache.geronimo.deployment.plugin.jmx;


/**
 * @version $Rev$ $Date$
 */
public class CommandContext {
    private boolean logErrors;
    private boolean verbose;
    private String username;
    private String password;
    private boolean inPlace;

    public CommandContext(boolean logErrors,
            boolean verbose,
            String username,
            String password,
            boolean inPlace) {
        this.logErrors = logErrors;
        this.verbose = verbose;
        this.username = username;
        this.password = password;
        this.inPlace = inPlace;
    }

    public CommandContext(CommandContext prototype) {
        this.logErrors = prototype.logErrors;
        this.verbose = prototype.verbose;
        this.username = prototype.username;
        this.password = prototype.password;
        this.inPlace = prototype.inPlace;
    }

    public boolean isLogErrors() {
        return logErrors;
    }

    public void setLogErrors(boolean logErrors) {
        this.logErrors = logErrors;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isInPlace() {
        return inPlace;
    }

    public void setInPlace(boolean inPlace) {
        this.inPlace = inPlace;
    }
}