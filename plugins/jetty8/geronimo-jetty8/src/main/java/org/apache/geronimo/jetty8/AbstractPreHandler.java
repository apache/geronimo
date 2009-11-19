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

package org.apache.geronimo.jetty8;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractPreHandler implements PreHandler {
    protected Handler next;
    
    public final void setNextHandler(Handler next) {
        this.next = next;
    }
    
    public final Server getServer() {
        throw new UnsupportedOperationException();
    }

    public final boolean isFailed() {
        throw new UnsupportedOperationException();
    }

    public void addLifeCycleListener(LifeCycle.Listener listener) {
    }

    public void removeLifeCycleListener(LifeCycle.Listener listener) {
    }

    public final boolean isRunning() {
        throw new UnsupportedOperationException();
    }

    public final boolean isStarted() {
        throw new UnsupportedOperationException();
    }

    public final boolean isStarting() {
        throw new UnsupportedOperationException();
    }

    public final boolean isStopping() {
        throw new UnsupportedOperationException();
    }

    public boolean isStopped() {
        throw new UnsupportedOperationException();
    }

    public final void setServer(Server server) {
        throw new UnsupportedOperationException();
    }

    public final void start() throws Exception {
        throw new UnsupportedOperationException();
    }

    public final void stop() throws Exception {
        throw new UnsupportedOperationException();
    }

    public void destroy() {
    }

}
