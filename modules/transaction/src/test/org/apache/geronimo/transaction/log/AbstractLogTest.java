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
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

import javax.transaction.xa.Xid;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.manager.TransactionLog;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/06 04:00:51 $
 *
 * */
public abstract class AbstractLogTest extends TestCase {
    private Object startBarrier = new Object();
    private Object stopBarrier = new Object();
    private int startedThreads = 0;
    private int stoppedThreads = 0;
    long totalDuration = 0;
    private Xid xid;
    final Object mutex = new Object();
    long totalXidCount = 0;
    private Writer resultsXML;
    private Writer resultsCSV;

    public void testTransactionLog() throws Exception {
        File resultFileXML = new File(getResultFileName() + ".xml");
        resultsXML = new FileWriter(resultFileXML);
        resultsXML.write("<log-test>\n");
        File resultFileCSV = new File(getResultFileName() + ".csv");
        resultsCSV = new FileWriter(resultFileCSV);
        resultsCSV.write("workerCount,xidCount,TotalXids,missingXids,DurationMilliseconds,XidsPerSecond,AverageForceTime,AverageBytesPerForce,AverageLatency\n");
        int xidCount = Integer.getInteger("xa.log.test.xid.count", 50).intValue();
        int minWorkerCount = Integer.getInteger("xa.log.test.worker.count.min", 20).intValue();
        int maxWorkerCount = Integer.getInteger("xa.log.test.worker.count.max", 40).intValue();
        int workerCountStep = Integer.getInteger("xa.log.test.worker.count.step", 20).intValue();
        int repCount = Integer.getInteger("xa.log.test.repetition.count", 1).intValue();
        long maxTime = Long.getLong("xa.log.test.max.time.seconds", 30).longValue() * 1000;
        int overtime = 0;
        try {
            for (int workers = minWorkerCount; workers <= maxWorkerCount; workers += workerCountStep) {
                for (int reps = 0; reps < repCount; reps++) {
                    if (testTransactionLog(workers, xidCount) > maxTime) {
                        overtime++;
                        if (overtime > 1) {
                            return;
                        }
                    }
                    resultsCSV.flush();
                    resultsXML.flush();
                }
            }
        } finally {
            resultsXML.write("</log-test>\n");
            resultsXML.flush();
            resultsXML.close();
            resultsCSV.flush();
            resultsCSV.close();
        }
    }

    protected abstract String getResultFileName();

    public long testTransactionLog(int workers, int xidCount) throws Exception {
        TransactionLog transactionLog = createTransactionLog();

        xid = new XidImpl2(new byte[Xid.MAXGTRIDSIZE]);

        long startTime = journalTest(transactionLog, workers, xidCount);

        long stopTime = System.currentTimeMillis();

        printSpeedReport(transactionLog, startTime, stopTime, workers, xidCount);
        closeTransactionLog(transactionLog);
        return stopTime - startTime;
    }

    protected abstract void closeTransactionLog(TransactionLog transactionLog) throws Exception ;

    protected abstract TransactionLog createTransactionLog() throws Exception;

    private long journalTest(final TransactionLog logger, final int workers, final int xidCount)
            throws Exception {
        totalXidCount = 0;
        startedThreads = 0;
        stoppedThreads = 0;
        totalDuration = 0;
        for (int i = 0; i < workers; i++) {
            new Thread() {
                public void run() {
                    long localXidCount = 0;
                    boolean exception = false;
                    long localDuration = 0;
                    try {
                        synchronized (startBarrier) {
                            ++startedThreads;
                            startBarrier.notifyAll();
                            while (startedThreads < (workers + 1)) startBarrier.wait();
                        }
                        long localStartTime = System.currentTimeMillis();

                        for (int i = 0; i < xidCount; i++) {
                            // journalize COMMITTING record
                            logger.prepare(xid);
                            //localXidCount++;

                            // journalize FORGET record
                            logger.commit(xid);
                            localXidCount++;
                        }
                        localDuration = System.currentTimeMillis() - localStartTime;
                    } catch (Exception e) {
                        System.err.println(Thread.currentThread().getName());
                        e.printStackTrace(System.err);
                        exception = true;
                    } finally {
                        synchronized (mutex) {
                            totalXidCount += localXidCount;
                            totalDuration += localDuration;
                        }
                        synchronized (stopBarrier) {
                            ++stoppedThreads;
                            stopBarrier.notifyAll();
                        }
                    }

                }
            }
                    .start();
        }

        // Wait for all the workers to be ready..
        long startTime = 0;
        synchronized (startBarrier) {
            while (startedThreads < workers) startBarrier.wait();
            ++startedThreads;
            startBarrier.notifyAll();
            startTime = System.currentTimeMillis();
        }

        // Wait for all the workers to finish.
        synchronized (stopBarrier) {
            while (stoppedThreads < workers) stopBarrier.wait();
        }

        return startTime;

    }

    void printSpeedReport(TransactionLog logger, long startTime, long stopTime, int workers, int xidCount) throws IOException {
        long mc = ((long) xidCount) * workers;
        long duration = (stopTime - startTime);
        long xidsPerSecond = (totalXidCount * 1000 / (duration));
        int averageForceTime = logger.getAverageForceTime();
        int averageBytesPerForce = logger.getAverageBytesPerForce();
        long averageLatency = totalDuration/totalXidCount;
        resultsXML.write("<run><workers>" + workers + "</workers><xids-per-thread>" + xidCount + "</xids-per-thread><expected-total-xids>" + mc + "</expected-total-xids><missing-xids>" + (mc - totalXidCount) + "</missing-xids><totalDuration-milliseconds>" + duration + "</totalDuration-milliseconds><xids-per-second>" + xidsPerSecond + "</xids-per-second></run>\n");
        resultsXML.write(logger.getXMLStats() + "\n");
        resultsCSV.write("" + workers + "," + xidCount + "," + mc + "," + (mc - totalXidCount) + "," + duration + "," + xidsPerSecond + "," + averageForceTime + "," + averageBytesPerForce  + "," + averageLatency + "\n");

    }
}
