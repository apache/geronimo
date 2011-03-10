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

package org.apache.geronimo.console.bundlemanager;

import org.osgi.framework.Bundle;

public enum BundleState {
    
    UNINSTALLED(Bundle.UNINSTALLED, "Uninstalled"), 
    INSTALLED(Bundle.INSTALLED, "Installed"), 
    RESOLVED(Bundle.RESOLVED, "Resolved"),
    STARTING(Bundle.STARTING, "Starting"), 
    STOPPING(Bundle.STOPPING, "Stopping"), 
    ACTIVE(Bundle.ACTIVE, "Active");
    
    private final int state;
    private final String name;

    BundleState(int state, String name) {
        this.state = state;
        this.name = name;
    }

    public int getState() {
        return state;
    }
    
    public String getName() {
        return name;
    }
    
    public String toString() {
        return name;
    }
    
    public boolean isRunning() {
        return state == Bundle.ACTIVE || state == Bundle.STARTING;
    }
    
    public boolean isStopped() {
        return state == Bundle.INSTALLED || state == Bundle.RESOLVED || state == Bundle.STOPPING;
    }
    
    public static BundleState getState(Bundle bundle) {
        int state = bundle.getState();
        switch (state) {
        case Bundle.UNINSTALLED:
            return BundleState.UNINSTALLED;
        case Bundle.INSTALLED:
            return BundleState.INSTALLED;
        case Bundle.RESOLVED:
            return BundleState.RESOLVED;
        case Bundle.STOPPING:
            return BundleState.STOPPING;
        case Bundle.STARTING:
            return BundleState.STARTING;            
        case Bundle.ACTIVE:
            return BundleState.ACTIVE;
        }
        throw new IllegalStateException("Unknown state: " + state);
    }
}
