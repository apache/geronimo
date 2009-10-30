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

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.service.startlevel.StartLevel;

public class StartLevelListener implements FrameworkListener {
    
    private Bootstrapper bootstrapper;
    private StartLevel startLevel;
    
    public StartLevelListener(Bootstrapper bootstrapper) {
        this.bootstrapper = bootstrapper;                      
    }
    
    public void start() {
        BundleContext context = bootstrapper.getBundleContext();
        startLevel = (StartLevel) context.getService(context.getServiceReference(StartLevel.class.getName()));
        
        context.addFrameworkListener(this);        
    }
    
    public void frameworkEvent(FrameworkEvent event) {
        if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
            bootstrapper.startLevelChanged(startLevel.getStartLevel());
        }
    }

}
