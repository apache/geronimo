/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.util.Map;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.security.auth.spi.LoginModule;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.Callback;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.security.realm.GenericSecurityRealm;

/**
 * Writes audit records to a file for all authentication activity.  Currently
 * doesn't perform too well; perhaps the file management should be centralized
 * and the IO objects kept open across many requests.  It would also be nice
 * to write in a more convenient XML format.
 *
 * This module does not write any Principals into the Subject.
 *
 * To enable this login module, set your primary login module to REQUIRED or
 * OPTIONAL, and list this module after it (with any setting).
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class FileAuditLoginModule implements LoginModule {
    public static final String LOG_FILE_OPTION = "file";
    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private File logFile;
    private CallbackHandler handler;
    private String username;

    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map sharedState, Map options) {
        String name = (String) options.get(LOG_FILE_OPTION);
        ServerInfo info = (ServerInfo) options.get(GenericSecurityRealm.SERVERINFO_LM_OPTION);
        logFile = info.resolve(name);
        handler = callbackHandler;
    }

    public boolean login() throws LoginException {
        NameCallback user = new NameCallback("User name:");
        Callback[] callbacks = new Callback[]{user};
        try {
            handler.handle(callbacks);
        } catch (Exception e) {
            throw new LoginException("Unable to process callback: "+e);
        }
        if(callbacks.length != 1) {
            throw new IllegalStateException("Number of callbacks changed by server!");
        }
        user = (NameCallback) callbacks[0];
        username = user.getName();
        writeToFile("Authentication attempt");

        return true;
    }

    private synchronized void writeToFile(String action) {
        Date date = new Date();
        try {
            FileOutputStream out = new FileOutputStream(logFile, true);
            FileChannel channel = out.getChannel();
            FileLock lock = channel.lock(0, Long.MAX_VALUE, false);
            PrintWriter writer = new PrintWriter(out, false);
            writer.println(DATE_FORMAT.format(date)+" - "+action+" - "+username);
            writer.flush();
            writer.close();
            lock.release();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to authentication log file", e);
        }
    }

    public boolean commit() throws LoginException {
        writeToFile("Authentication succeeded");
        return true;
    }

    public boolean abort() throws LoginException {
        if(username != null) { //work around initial "fake" login
            writeToFile("Authentication failed");
            username = null;
        }
        return true;
    }

    public boolean logout() throws LoginException {
        writeToFile("Explicit logout");
        username = null;
        return true;
    }
}
