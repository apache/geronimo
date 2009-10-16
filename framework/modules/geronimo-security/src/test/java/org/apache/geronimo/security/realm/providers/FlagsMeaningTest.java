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


package org.apache.geronimo.security.realm.providers;

import java.util.Map;
import java.util.Collections;

import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class FlagsMeaningTest extends TestCase {
    private static final Map<String, Object> noOptions = Collections.emptyMap();

    public void testSufficientExceptionTrue() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(ExceptionLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, noOptions),
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }

    public void testSufficientFalseTrue() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(FalseLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, noOptions),
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }

    public void testSufficientExceptionRequiredTrue() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(ExceptionLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, noOptions),
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }

    public void testOptionalExceptionTrue() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(ExceptionLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, noOptions),
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }

    public void testOptionalTrueException() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, noOptions),
                new AppConfigurationEntry(ExceptionLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }

    public void testRequiredExceptionTrue() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(ExceptionLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        try {
            lc.login();
            fail("login exception expected");
        } catch (LoginException e) {
        }
    }

    public void testRequisiteExceptionTrue() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(ExceptionLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, noOptions),
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        try {
            lc.login();
            fail("login exception expected");
        } catch (LoginException e) {
        }
    }

    public void testRequisiteTrueException() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, noOptions),
                new AppConfigurationEntry(ExceptionLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        try {
            lc.login();
            fail("login exception expected");
        } catch (LoginException e) {
        }
    }

    public void testRequiredTrueException() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
                new AppConfigurationEntry(ExceptionLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        try {
            lc.login();
            fail("login exception expected");
        } catch (LoginException e) {
        }
    }

    public void testRequiredTrueFalse() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
                new AppConfigurationEntry(FalseLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }

    public void testRequiredFalseTrue() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(FalseLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }

    public void testRequisiteTrueFalse() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, noOptions),
                new AppConfigurationEntry(FalseLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }
    
    public void testRequisiteFalseTrue() throws LoginException {
        Configuration conf = new FixedConfiguration(new AppConfigurationEntry[] {
                new AppConfigurationEntry(FalseLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, noOptions),
                new AppConfigurationEntry(TrueLM.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUISITE, noOptions),
        });
        LoginContext lc = new LoginContext("foo", null, null, conf);
        lc.login();
    }

    private static class FixedConfiguration extends Configuration {

        private final AppConfigurationEntry[] entries;

        private FixedConfiguration(AppConfigurationEntry[] entries) {
            this.entries = entries;
        }

        public AppConfigurationEntry[] getAppConfigurationEntry(String s) {
            return entries;
        }

        public void refresh() {
        }
    }

    public static class FalseLM implements LoginModule {

        public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> stringMap, Map<String, ?> stringMap1) {
        }

        public boolean login() throws LoginException {
            return false;
        }

        public boolean commit() throws LoginException {
            return true;
        }

        public boolean abort() throws LoginException {
            return true;
        }

        public boolean logout() throws LoginException {
            return true;
        }
    }

    public static class TrueLM implements LoginModule {
        public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> stringMap, Map<String, ?> stringMap1) {
        }

        public boolean login() throws LoginException {
            return true;
        }

        public boolean commit() throws LoginException {
            return true;
        }

        public boolean abort() throws LoginException {
            return true;
        }

        public boolean logout() throws LoginException {
            return true;
        }

    }

    public static class ExceptionLM implements LoginModule {

        public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> stringMap, Map<String, ?> stringMap1) {
        }

        public boolean login() throws LoginException {
            throw new LoginException();
        }

        public boolean commit() throws LoginException {
            return false;
        }

        public boolean abort() throws LoginException {
            return false;
        }

        public boolean logout() throws LoginException {
            return false;
        }
    }
}
