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
package org.apache.geronimo.security.realm.providers;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;

/**
 * Tracks the number of recent login failures for each user, and starts
 * rejecting login attemps if the number of failures in a certain period for a
 * particular user gets too high.  The period, number of failures, and lockout
 * duration are configurable, but default to 5 failures in 5 minutes cause all
 * subsequent attemps to fail for 30 minutes.
 *
 * This module does not write any Principals into the Subject.
 *
 * To enable this login module, set your primary login module and any other
 * login modules to REQUIRED or OPTIONAL, and list this module in last place,
 * set to REQUISITE.
 *
 * The parameters used by this module are:
 * <ul>
 *   <li><b>failureCount</b> - The number of failures to allow before subsequent
 *                             login attempts automatically fail</li>
 *   <li><b>failurePeriodSecs</b> - The window of time the failures must occur
 *                                 in in order to cause the lockout</li>
 *   <li><b>lockoutDurationSecs</b> - The duration of a lockout caused by
 *                                    exceeding the failureCount in
 *                                    failurePeriodSecs.</li>
 * </ul>
 *
 * This login module does not check credentials so it should never be able to cause a login to succeed.
 * Therefore the lifecycle methods must return false to indicate success or throw a LoginException to indicate failure.
 *
 * @version $Rev$ $Date$
 */
public class RepeatedFailureLockoutLoginModule implements LoginModule {
    private static final Logger log = LoggerFactory.getLogger(RepeatedFailureLockoutLoginModule.class);
    
    public static final String FAILURE_COUNT_OPTION = "failureCount";
    public static final String FAILURE_PERIOD_OPTION = "failurePeriodSecs";
    public static final String LOCKOUT_DURATION_OPTION = "lockoutDurationSecs";
    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(FAILURE_COUNT_OPTION, FAILURE_PERIOD_OPTION, LOCKOUT_DURATION_OPTION));
    
    private static final HashMap<String, LoginHistory> userData = new HashMap<String, LoginHistory>();
    private CallbackHandler handler;
    private String username;
    private int failureCount = 5;
    private int failurePeriod = 5 * 60 * 1000;
    private int lockoutDuration = 30 * 60 * 1000;

    /**
     * Reads the configuration settings for this module.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        for(Object option: options.keySet()) {
            if(!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: "+option+". Not supported.");
            }
        }
        String fcString = (String) options.get(FAILURE_COUNT_OPTION);
        if(fcString != null) {
            fcString = fcString.trim();
            if(!fcString.equals("")) {
                failureCount = Integer.parseInt(fcString);
            }
        }
        String fpString = (String) options.get(FAILURE_PERIOD_OPTION);
        if(fpString != null) {
            fpString = fpString.trim();
            if(!fpString.equals("")) {
                failurePeriod = Integer.parseInt(fpString) * 1000;
            }
        }
        String ldString = (String) options.get(LOCKOUT_DURATION_OPTION);
        if(ldString != null) {
            ldString = ldString.trim();
            if(!ldString.equals("")) {
                lockoutDuration = Integer.parseInt(ldString) * 1000;
            }
        }
        handler = callbackHandler;
    }

    /**
     * Checks whether the user should be or has been locked out.
     */
    public boolean login() throws LoginException {
        NameCallback user = new NameCallback("User name:");
        Callback[] callbacks = new Callback[]{user};
        try {
            handler.handle(callbacks);
        } catch (Exception e) {
            throw (LoginException)new LoginException("Unable to process callback: "+e.getMessage()).initCause(e);
        }
        if(callbacks.length != 1) {
            throw new IllegalStateException("Number of callbacks changed by server!");
        }
        user = (NameCallback) callbacks[0];
        username = user.getName();
        if(username != null) {
            LoginHistory history;
            synchronized (userData) {
                history = userData.get(username);
            }
            if(history != null && !history.isLoginAllowed(lockoutDuration, failurePeriod, failureCount)) {
                username = null;
                throw new FailedLoginException("Maximum login failures exceeded; try again later");
            }
        }
        return false;
    }

    /**
     * This module does nothing if a login succeeds.
     */
    public boolean commit() throws LoginException {
        return false;
    }

    /**
     * Notes that (and when) a login failure occured, used to calculate
     * whether the user should be locked out.
     */
    public boolean abort() throws LoginException {
        if(username != null) { //work around initial "fake" login
            LoginHistory history;
            synchronized (userData) {
                history = userData.get(username);
                if(history == null) {
                    history = new LoginHistory(username);
                    userData.put(username, history);
                }
            }
            history.addFailure();
            username = null;
        }
        return false;
     }

    /**
     * This module does nothing on a logout.
     */
    public boolean logout() throws LoginException {
        username = null;
        return false;
    }

    /**
     * Tracks failure attempts for a user, and calculates lockout
     * status and expiry, etc.
     */
    private static class LoginHistory implements Serializable {
        private static final long serialVersionUID = 7792298296084531182L;

        private String user;
        private LinkedList<Long> data = new LinkedList<Long>();
        private long lockExpires = -1;

        public LoginHistory(String user) {
            this.user = user;
        }

        public String getUser() {
            return user;
        }

        /**
         * Cleans up the failure history and then calculates whether this user
         * is locked out or not.
         */
        public synchronized boolean isLoginAllowed(int lockoutLengthMillis, int failureAgeMillis, int maxFailures) {
            long now = System.currentTimeMillis();
            cleanup(now - failureAgeMillis);
            if(lockExpires > now) {
                return false;
            }
            if(data.size() >= maxFailures) {
                lockExpires = data.getLast() + lockoutLengthMillis;
                if(lockExpires > now) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Notes that a failure occured.
         */
        public synchronized void addFailure() {
            data.add(System.currentTimeMillis());
        }

        /**
         * Cleans up all failure records outside the window of time we care
         * about.
         */
        public synchronized void cleanup(long ignoreOlderThan) {
            for (Iterator it = data.iterator(); it.hasNext();) {
                Long time = (Long) it.next();
                if(time < ignoreOlderThan) {
                    it.remove();
                } else {
                    break;
                }
            }
        }
    }
}
