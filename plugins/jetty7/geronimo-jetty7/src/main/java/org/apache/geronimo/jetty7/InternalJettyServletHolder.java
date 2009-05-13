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


package org.apache.geronimo.jetty7;

import java.io.IOException;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;

import org.apache.geronimo.jetty7.handler.AbstractImmutableHandler;
import org.apache.geronimo.jetty7.handler.LifecycleCommand;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @version $Rev$ $Date$
 */
public class InternalJettyServletHolder extends ServletHolder {

    private static final ThreadLocal<String> currentServletName = new ThreadLocal<String>();

    private final AbstractImmutableHandler lifecycleChain;
    private final Subject runAsSubject;
    private final JettyServletRegistration servletRegistration;
    private boolean stopped;

    public InternalJettyServletHolder(AbstractImmutableHandler lifecycleChain, Subject runAsSubject, JettyServletRegistration servletRegistration) {
        this.lifecycleChain = lifecycleChain;
        this.runAsSubject = runAsSubject;
        this.servletRegistration = servletRegistration;
    }

    //TODO probably need to override init and destroy (?) to handle runAsSubject since we are not setting it in the superclass any more.

    /**
     * Service a request with this servlet.  Set the ThreadLocal to hold the
     * current JettyServletHolder.
     */
    public void handle(Request baseRequest, ServletRequest request, ServletResponse response)
            throws ServletException, UnavailableException, IOException {
        String oldServletName = getCurrentServletName();
        setCurrentServletName(getName());
        try {
            if (runAsSubject == null) {
                super.handle(baseRequest, request, response);
            } else {
                Callers oldCallers = ContextManager.pushNextCaller(runAsSubject);
                try {
                    super.handle(baseRequest, request, response);
                } finally {
                    ContextManager.popCallers(oldCallers);
                }
            }
        } finally {
            setCurrentServletName(oldServletName);
        }
    }


    public synchronized Object newInstance() throws InstantiationException, IllegalAccessException {
        return servletRegistration.newInstance(_className);
    }

    public void destroyInstance(Object o) throws Exception {
        if (!stopped) {
            super.destroyInstance(o);
            servletRegistration.destroyInstance(o);
        }
    }

    /**
     * Provide the thread's current JettyServletHolder
     *
     * @return the thread's current JettyServletHolder
     * TODO remove
     */
    static String getCurrentServletName() {
        return currentServletName.get();
    }

    static void setCurrentServletName(String servletName) {
        currentServletName.set(servletName);
    }

    public void doStart() throws Exception {
        LifecycleCommand lifecycleCommand = new StartCommand();
        lifecycleChain.lifecycleCommand(lifecycleCommand);
    }

    public void doStop() {
        LifecycleCommand lifecycleCommand = new StopCommand();
        try {
            lifecycleChain.lifecycleCommand(lifecycleCommand);
        } catch (Exception e) {
            //ignore????
        }
    }

    private void internalDoStart() throws Exception {
        super.doStart();
    }

    private void internalDoStop() {
        super.doStop();
        stopped = true;
    }

    public class StartCommand implements LifecycleCommand {

        public void lifecycleMethod() throws Exception {
            internalDoStart();
        }
    }

    public class StopCommand implements LifecycleCommand {

        public void lifecycleMethod() throws Exception {
            internalDoStop();
        }
    }

}
