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

import java.io.IOException;

import javax.transaction.xa.Xid;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.objectweb.howl.log.LogClosedException;
import org.objectweb.howl.log.LogFileOverflowException;
import org.objectweb.howl.log.Logger;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/19 17:17:13 $
 *
 * */
public class HOWLLogTest extends AbstractLogTest {


    protected String getResultFileName() {
        return "howllog";
    }

    protected void closeTransactionLog(TransactionLog transactionLog) throws Exception {
        ((HOWLLog) transactionLog).doStop();
    }


    protected TransactionLog createTransactionLog() throws Exception {
        HOWLLog howlLog = new HOWLLog(
                "org.objectweb.howl.log.BlockLogBuffer", //                "bufferClassName",
                4, //                "bufferSizeKBytes",
                true, //                "checksumEnabled",
                20, //                "flushSleepTime",
                "target", //                "logFileDir",
                "log", //                "logFileExt",
                "howl_test_", //                "logFileName",
                200, //                "maxBlocksPerFile",
                10, //                "maxBuffers",
                2, //                "maxLogFiles",
                2, //                "minBuffers",
                10//                "threadsWaitingForceThreshold"});
        );
        howlLog.doStart();
        return howlLog;
    }

}
