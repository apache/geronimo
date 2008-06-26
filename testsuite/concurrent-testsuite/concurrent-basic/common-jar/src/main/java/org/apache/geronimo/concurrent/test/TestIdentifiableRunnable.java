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
package org.apache.geronimo.concurrent.test;

import java.util.Locale;

import javax.util.concurrent.Identifiable;

public class TestIdentifiableRunnable implements Runnable, Identifiable {
    
    long delay;
    
    public TestIdentifiableRunnable(long delay) {
        this.delay = delay;
    }
    
    public void run() {
        try {
            Thread.sleep(this.delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String toString() {
        return "TestIdentifiableRunnable";
    }
    
    public String getIdentityDescription(Locale locale) {
        if (Locale.CANADA.equals(locale)) {
            return "TestIdentifiableRunnable Description CA";
        } else {
            return "TestIdentifiableRunnable Description";
        }
    }
    
    public String getIdentityName() {
        return "TestIdentifiableRunnable Name";
    }
    
}