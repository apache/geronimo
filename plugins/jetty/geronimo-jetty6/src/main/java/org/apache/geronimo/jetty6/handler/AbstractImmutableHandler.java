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


package org.apache.geronimo.jetty6.handler;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.AbstractHandlerContainer;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractImmutableHandler implements Handler /*extends AbstractHandlerContainer*/ {
    protected final Handler next;

    protected AbstractImmutableHandler(Handler next) {
        this.next = next;
    }

    protected void doStart() throws Exception {
        next.start();
    }

    protected void doStop() throws Exception {
        next.stop();
    }

    public void setServer(Server server) {
//        super.setServer(server);
        next.setServer(server);
    }

    public Server getServer() {
        return next.getServer();
    }

    public void destroy() {
        next.destroy();
    }

    public void lifecycleCommand(LifecycleCommand lifecycleCommand) throws Exception {
        if (next instanceof AbstractImmutableHandler) {
            ((AbstractImmutableHandler) next).lifecycleCommand(lifecycleCommand);
        } else {
            lifecycleCommand.lifecycleMethod();
        }
    }


    public void addHandler(Handler handler) {
        if (next instanceof HandlerContainer) {
            ((HandlerContainer) next).addHandler(handler);
        } else {
            throw new RuntimeException("geronimo HandlerContainers are immutable");
        }
    }

    /**
     * this is basically the implementation from HandlerWrapper.
     * @param list partial list of handlers matching byClass (may be null)
     * @param byClass class of the handlers we want
     * @return extended list of handlers matching byClass
     */
    protected Object expandChildren(Object list, Class byClass)
    {
//        return expandHandler(next, list, byClass);
        return null;
    }

    public void start() throws Exception {
        next.start();
    }

    public void stop() throws Exception {
        next.stop();
    }

    public boolean isRunning() {
        return next.isRunning();
    }

    public boolean isStarted() {
        return next.isStarted();
    }

    public boolean isStarting() {
        return next.isStarting();
    }

    public boolean isStopping() {
        return next.isStopping();
    }

    public boolean isStopped() {
        return next.isStopped();
    }

    public boolean isFailed() {
        return false;
    }
}
