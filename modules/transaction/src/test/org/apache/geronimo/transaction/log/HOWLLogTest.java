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

package org.apache.geronimo.transaction.log;

import java.io.File;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class HOWLLogTest extends AbstractLogTest {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    private static final String LOG_FILE_NAME = "howl_test";


    protected String getResultFileName() {
        return "howllog";
    }

    protected void closeTransactionLog(TransactionLog transactionLog) throws Exception {
        ((HOWLLog) transactionLog).doStop();
    }


    protected TransactionLog createTransactionLog() throws Exception {
        XidFactory xidFactory = new XidFactoryImpl();
        HOWLLog howlLog = new HOWLLog(
                "org.objectweb.howl.log.BlockLogBuffer", //                "bufferClassName",
                4, //                "bufferSizeKBytes",
                true, //                "checksumEnabled",
                20, //                "flushSleepTime",
                "txlog", //                "logFileDir",
                "log", //                "logFileExt",
                LOG_FILE_NAME, //                "logFileName",
                200, //                "maxBlocksPerFile",
                10, //                "maxBuffers",
                2, //                "maxLogFiles",
                2, //                "minBuffers",
                10,//                "threadsWaitingForceThreshold"});
                xidFactory,
                new BasicServerInfo(new File(basedir, "target").getAbsolutePath())
        );
        howlLog.doStart();
        return howlLog;
    }
    public static Test suite() {
        return new TestSetup(new TestSuite(HOWLLogTest.class)) {
            protected void setUp() throws Exception {
                File logFile = new File(basedir, "target/" + LOG_FILE_NAME + "_1.log");
                if (logFile.exists()) {
                    logFile.delete();
                }
                logFile = new File(basedir, "target/" + LOG_FILE_NAME + "_2.log");
                if (logFile.exists()) {
                    logFile.delete();
                }
            }
        };
    }

}
