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

import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.AbstractHandlerContainer;
import org.mortbay.util.LazyList;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractImmutableHandler implements Handler /*extends AbstractHandlerContainer*/ {
    private Object _lock = new Object();
    protected LifeCycle.Listener[] _listeners;
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

    public void removeHandler(Handler handler) {
        if (next instanceof HandlerContainer) {
            ((HandlerContainer) next).removeHandler(handler);
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

    /**
     * this is basically the implementation from AbstractLifeCycle
     */
    public void start() throws Exception {
        synchronized (_lock)
        {
            try
            {
                if (isStarted() || isStarting())
                    return;
                setStarting();
                doStart();
                setStarted();
            }
            catch (Exception e)
            {
                setFailed(e);
                throw e;
            }
            catch (Error e)
            {
                setFailed(e);
                throw e;
            }
        }
    }

    public void stop() throws Exception {
        synchronized (_lock)
        {
            try
            {
                if (isStopping() || isStopped())
                    return;
                setStopping();
                doStop();
                setStopped();
            }
            catch (Exception e)
            {
                setFailed(e);
                throw e;
            }
            catch (Error e)
            {
                setFailed(e);
                throw e;
            }
        }
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
        return next.isFailed();
    }

    public void addLifeCycleListener(LifeCycle.Listener listener) {
        _listeners = (LifeCycle.Listener[])LazyList.addToArray(_listeners,listener,LifeCycle.Listener.class);
    }

    public void removeLifeCycleListener(LifeCycle.Listener listener) {
        LazyList.removeFromArray(_listeners,listener);
    }

    private void setStarted()
    {
        if (_listeners != null)
        {
            for (int i = 0; i < _listeners.length; i++)
            {
                _listeners[i].lifeCycleStarted(this);
            }
        }
    }

    private void setStarting()
    {
        if (_listeners != null)
        {
            for (int i = 0; i < _listeners.length; i++)
            {
                _listeners[i].lifeCycleStarting(this);
            }
        }
    }

    private void setStopping()
    {
        if (_listeners != null)
        {
            for (int i = 0; i < _listeners.length; i++)
            {
                _listeners[i].lifeCycleStopping(this);
            }
        }
    }

    private void setStopped()
    {
        if (_listeners != null)
        {
            for (int i = 0; i < _listeners.length; i++)
            {
                _listeners[i].lifeCycleStopped(this);
            }
        }
    }

    private void setFailed(Throwable error)
    {
        if (_listeners != null)
        {
            for (int i = 0; i < _listeners.length; i++)
            {
                _listeners[i].lifeCycleFailure(this,error);
            }
        }
    }

}
