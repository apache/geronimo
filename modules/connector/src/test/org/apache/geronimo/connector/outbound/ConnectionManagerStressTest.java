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

package org.apache.geronimo.connector.outbound;

import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.UnspecifiedTransactionContext;
import org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl.DefaultComponentContext;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/04/22 17:06:30 $
 *
 * */
public class ConnectionManagerStressTest extends ConnectionManagerTestUtils {

    private static final Log log = LogFactory.getLog(ConnectionManagerStressTest.class);

    protected int repeatCount = 100;
    protected int threadCount = 50;
    private Object startBarrier = new Object();
    private Object stopBarrier = new Object();
    private int startedThreads = 0;
    private int stoppedThreads = 0;
    private long totalDuration = 0;
    private Object mutex = new Object();

    private Exception e = null;

    public void testNoTransactionCallOneThread() throws Throwable {
        TransactionContext.setContext(new UnspecifiedTransactionContext());
        for (int i = 0; i < repeatCount; i++) {
            defaultComponentInterceptor.invoke(defaultComponentContext);
        }
    }

    public void testNoTransactionCallMultiThread() throws Throwable {
        startedThreads = 0;
        stoppedThreads = 0;
        for (int t = 0; t < threadCount; t++) {
            new Thread() {
                public void run() {
                    TransactionContext.setContext(new UnspecifiedTransactionContext());
                    long localStartTime = 0;
                    try {
                        synchronized (startBarrier) {
                            ++startedThreads;
                            startBarrier.notifyAll();
                            while (startedThreads < (threadCount + 1)) {
                                startBarrier.wait();
                            }
                        }
                        localStartTime = System.currentTimeMillis();
                        for (int i = 0; i < repeatCount; i++) {
                            try {
                                defaultComponentInterceptor.invoke(new DefaultComponentContext());
                            } catch (Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        log.info(e);
                        ConnectionManagerStressTest.this.e = e;
                    } finally {
                        synchronized (stopBarrier) {
                            ++stoppedThreads;
                            stopBarrier.notifyAll();
                        }
                        long localDuration = System.currentTimeMillis() - localStartTime;
                        synchronized (mutex) {
                             totalDuration += localDuration;
                        }
                    }
                }
            }.start();
        }
        // Wait for all the workers to be ready..
        long startTime = 0;
        synchronized (startBarrier) {
            while (startedThreads < threadCount) startBarrier.wait();
            ++startedThreads;
            startBarrier.notifyAll();
            startTime = System.currentTimeMillis();
        }

        // Wait for all the workers to finish.
        synchronized (stopBarrier) {
            while (stoppedThreads < threadCount) stopBarrier.wait();
        }
        long duration = System.currentTimeMillis() - startTime;
        log.info("no tx run, thread count: " + threadCount + ", connection count: " + repeatCount + ", duration: " + duration + ", total duration: " + totalDuration + ", ms per cx request: " + (totalDuration/(threadCount * repeatCount)));
        //return startTime;
        if (e != null) {
            throw e;
        }
    }
}
