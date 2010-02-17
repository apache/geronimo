/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.osgi.web.extender;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.system.main.ServerStatus;

/**
 * A GBean that tracks server status and starts rfc66 extender after all
 * plugins have been started.
 */
@GBean
public class ServerStatusTracker implements ServerStatus {

    private WebContainerExtender extender;
    private boolean serverStarted;
    
    public ServerStatusTracker(@ParamReference(name = "extender") WebContainerExtender extender) {
        this.extender = extender;
        this.serverStarted = false;
    }
    
    public boolean isServerStarted() {
        return serverStarted;
    }

    public void setServerStarted(boolean started) {
        serverStarted = started;
        if (started) {
            try {
                extender.start();
            } catch (Throwable e) {
                extender.stop();
            }
        } else {
            extender.stop();
        }
    }

}
