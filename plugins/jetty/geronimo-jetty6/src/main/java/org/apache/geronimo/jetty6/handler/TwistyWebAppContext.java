/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.jetty6.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.util.LazyList;


/**
 * @version $Rev$ $Date$
 */
public class TwistyWebAppContext extends WebAppContext {

    private Handler handler;


    public TwistyWebAppContext(SecurityHandler securityHandler, SessionHandler sessionHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
        super(securityHandler, sessionHandler, servletHandler, errorHandler);
    }

    public void setTwistyHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler newTwistyHandler() {
        return new TwistyHandler();
    }

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        handler.handle(target, request, response, dispatch);
    }

    private class TwistyHandler implements Handler {
        private Object _lock = new Object();
        protected LifeCycle.Listener[] _listeners;

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
            TwistyWebAppContext.super.handle(target, request, response, dispatch);
        }

        public void setServer(Server server) {
             TwistyWebAppContext.super.setServer(server);
        }

        public Server getServer() {
            return TwistyWebAppContext.super.getServer();
        }

        public void destroy() {
            TwistyWebAppContext.super.destroy();
        }

        /*
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
                    TwistyWebAppContext.super.start();
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
                    TwistyWebAppContext.super.stop();
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
            return TwistyWebAppContext.super.isRunning();
        }

        public boolean isStarted() {
            return TwistyWebAppContext.super.isStarted();
        }

        public boolean isStarting() {
            return TwistyWebAppContext.super.isStarting();
        }

        public boolean isStopping() {
            return TwistyWebAppContext.super.isStopping();
        }

        public boolean isStopped() {
            return TwistyWebAppContext.super.isStopped();
        }

        public boolean isFailed() {
            return TwistyWebAppContext.super.isFailed();
        }

        public void addLifeCycleListener(LifeCycle.Listener listener)
        {
            _listeners = (LifeCycle.Listener[])LazyList.addToArray(_listeners,listener,LifeCycle.Listener.class);
        }

        public void removeLifeCycleListener(LifeCycle.Listener listener)
        {
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
}
