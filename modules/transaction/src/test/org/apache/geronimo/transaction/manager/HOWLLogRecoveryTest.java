/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.transaction.manager;

import java.io.File;

import org.apache.geronimo.transaction.log.HOWLLog;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/20 18:45:52 $
 *
 * */
public class HOWLLogRecoveryTest extends AbstractRecoveryTest {

    public void xtest2Again() throws Exception {
        xtest2ResOnlineAfterRecoveryStart();
    }

    public void xtest3Again() throws Exception {
        xtest3ResOnlineAfterRecoveryStart();
    }

    protected void setUp() throws Exception {
//        HOWLLog howlLog = new HOWLLog(
//                "org.objectweb.howl.log.BlockLogBuffer", //                "bufferClassName",
//                4, //                "bufferSizeKBytes",
//                true, //                "checksumEnabled",
//                20, //                "flushSleepTime",
//                "target", //                "logFileDir",
//                "log", //                "logFileExt",
//                "howl_test_", //                "logFileName",
//                200, //                "maxBlocksPerFile",
//                10, //                "maxBuffers",                       log
//                2, //                "maxLogFiles",
//                2, //                "minBuffers",
//                10//                "threadsWaitingForceThreshold"});
//        );
//        howlLog.doStart();
//        txLog = howlLog;
    }

    protected void tearDown() throws Exception {
//        ((HOWLLog)txLog).doStop();
//        txLog = null;
    }

    protected void prepareForReplay() throws Exception {
        tearDown();
        setUp();
    }

    public static Test suite() {
        return new TestSetup(new TestSuite(HOWLLogRecoveryTest.class)) {
            protected void setUp() throws Exception {
                File logFile = new File("target/howl_1.log");
                if (logFile.exists()) {
                    logFile.delete();
                }
                logFile = new File("target/howl_2.log");
                if (logFile.exists()) {
                    logFile.delete();
                }
            }
        };
    }
}
