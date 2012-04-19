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

package org.apache.geronimo.shell.debug;

import java.io.PrintWriter;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;

public class Utils {

    public static void writeThreadInfo(ThreadInfo info, PrintWriter w) {
        w.print("\"" + info.getThreadName() + "\" " + info.getThreadId() + " " + info.getThreadState());
        if (info.isSuspended()) {
            w.print(" (suspended)");
        }
        w.println();

        MonitorInfo[] lockedMonitors = info.getLockedMonitors();
        StackTraceElement[] stackTrace = info.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            w.println("\tat " + ste.toString());

            if (i == 0 && info.getLockInfo() != null) {
                Thread.State ts = info.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        w.print("\t - blocked on " + info.getLockInfo());
                        writeLockOwnerInfo(info, w);
                        break;
                    case WAITING:
                        w.print("\t - waiting on " + info.getLockInfo());
                        writeLockOwnerInfo(info, w);
                        break;
                    case TIMED_WAITING:
                        w.println("\t - waiting on " + info.getLockInfo());
                        writeLockOwnerInfo(info, w);
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : lockedMonitors) {
                if (mi.getLockedStackDepth() == i) {
                    w.println("\t - locked " + mi);
                }
            }
        }
        
        w.println();
    }
    
    private static void writeLockOwnerInfo(ThreadInfo info, PrintWriter writer) {
        if (info.getLockOwnerName() != null) {
            writer.print(" owned by \"" + info.getLockOwnerName() + "\" id=" + info.getLockOwnerId());
        }
        writer.println();
    }
}
