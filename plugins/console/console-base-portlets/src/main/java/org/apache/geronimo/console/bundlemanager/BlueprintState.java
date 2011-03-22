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

import org.osgi.service.blueprint.container.BlueprintEvent;

public enum BlueprintState {
    
    CREATING(BlueprintEvent.CREATING, "Creating"), 
    CREATED(BlueprintEvent.CREATED, "Created"), 
    WAITING(BlueprintEvent.WAITING, "Waiting"),     //service reference is blocking because of unsatisfied mandatory dependencies
    GRACE_PERIOD(BlueprintEvent.GRACE_PERIOD, "Grace_Period"),  //can not get referenced service
    DESTROYING(BlueprintEvent.DESTROYING, "Destroying"), 
    DESTROYED(BlueprintEvent.DESTROYED, "Destroyed"),
    FAILURE(BlueprintEvent.FAILURE, "Failure");
    
    private final int state;
    private final String name;

    BlueprintState(int state, String name) {
        this.state = state;
        this.name = name;
    }

    public int getState() {
        return state;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public static BlueprintState getState(int state) {
        switch (state) {
        case BlueprintEvent.CREATING:
            return BlueprintState.CREATING;
        case BlueprintEvent.CREATED:
            return BlueprintState.CREATED;
        case BlueprintEvent.WAITING:
            return BlueprintState.WAITING;
        case BlueprintEvent.GRACE_PERIOD:
            return BlueprintState.GRACE_PERIOD;
        case BlueprintEvent.DESTROYING:
            return BlueprintState.DESTROYING;            
        case BlueprintEvent.DESTROYED:
            return BlueprintState.DESTROYED;
        case BlueprintEvent.FAILURE:
            return BlueprintState.FAILURE;
        }
        throw new IllegalStateException("Unknown blueprint state: " + state);
    }
}
