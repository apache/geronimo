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
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.transaction.manager.LogException;
import org.apache.geronimo.transaction.manager.Recovery;
import org.apache.geronimo.transaction.manager.TransactionBranchInfo;
import org.apache.geronimo.transaction.manager.TransactionBranchInfoImpl;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.objectweb.howl.log.Configuration;
import org.objectweb.howl.log.InvalidLogKeyException;
import org.objectweb.howl.log.LogClosedException;
import org.objectweb.howl.log.LogConfigurationException;
import org.objectweb.howl.log.LogFileOverflowException;
import org.objectweb.howl.log.LogRecord;
import org.objectweb.howl.log.LogRecordSizeException;
import org.objectweb.howl.log.LogRecordType;
import org.objectweb.howl.log.Logger;
import org.objectweb.howl.log.ReplayListener;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class HOWLLog implements TransactionLog, GBeanLifecycle {
    static final byte PREPARE = 1;
    static final byte COMMIT = 2;
    static final byte ROLLBACK = 3;

    static final String[] TYPE_NAMES = {null, "PREPARE", "COMMIT", "ROLLBACK"};

    private static final Log log = LogFactory.getLog(HOWLLog.class);

    private final ServerInfo serverInfo;
    private String logFileDir;

    private final Logger logger;
    private final Configuration configuration = new Configuration();
    private boolean started = false;

    private final SortedSet marks = new TreeSet();

    public HOWLLog(
            String bufferClassName,
            int bufferSize,
            boolean checksumEnabled,
            int flushSleepTimeMilliseconds,
            String logFileDir,
            String logFileExt,
            String logFileName,
            int maxBlocksPerFile,
            int maxBuffers,
            int maxLogFiles,
            int minBuffers,
            int threadsWaitingForceThreshold,
            ServerInfo serverInfo
            ) throws IOException {
        this.serverInfo = serverInfo;
        setBufferClassName(bufferClassName);
        setBufferSizeKBytes(bufferSize);
        setChecksumEnabled(checksumEnabled);
        setFlushSleepTimeMilliseconds(flushSleepTimeMilliseconds);
        //setLogFileDir(logFileDir);
        this.logFileDir = logFileDir;
        setLogFileExt(logFileExt);
        setLogFileName(logFileName);
        setMaxBlocksPerFile(maxBlocksPerFile);
        setMaxBuffers(maxBuffers);
        setMaxLogFiles(maxLogFiles);
        setMinBuffers(minBuffers);
        setThreadsWaitingForceThreshold(threadsWaitingForceThreshold);
        this.logger = new Logger(configuration);
    }

    public String getLogFileDir() {
        return logFileDir;
    }

    public void setLogFileDir(String logDir) {
        this.logFileDir = logDir;
        if (started) {
            configuration.setLogFileDir(serverInfo.resolvePath(logDir));
        }
    }

    public String getLogFileExt() {
        return configuration.getLogFileExt();
    }

    public void setLogFileExt(String logFileExt) {
        configuration.setLogFileExt(logFileExt);
    }

    public String getLogFileName() {
        return configuration.getLogFileName();
    }

    public void setLogFileName(String logFileName) {
        configuration.setLogFileName(logFileName);
    }

    public boolean isChecksumEnabled() {
        return configuration.isChecksumEnabled();
    }

    public void setChecksumEnabled(boolean checksumOption) {
        configuration.setChecksumEnabled(checksumOption);
    }

    public int getBufferSizeKBytes() {
        return configuration.getBufferSize();
    }

    public void setBufferSizeKBytes(int bufferSize) {
        configuration.setBufferSize(bufferSize);
    }

    public String getBufferClassName() {
        return configuration.getBufferClassName();
    }

    public void setBufferClassName(String bufferClassName) {
        configuration.setBufferClassName(bufferClassName);
    }

    public int getMaxBuffers() {
        return configuration.getMaxBuffers();
    }

    public void setMaxBuffers(int maxBuffers) {
        configuration.setMaxBuffers(maxBuffers);
    }

    public int getMinBuffers() {
        return configuration.getMinBuffers();
    }

    public void setMinBuffers(int minBuffers) {
        configuration.setMinBuffers(minBuffers);
    }

    public int getFlushSleepTimeMilliseconds() {
        return configuration.getFlushSleepTime();
    }

    public void setFlushSleepTimeMilliseconds(int flushSleepTime) {
        configuration.setFlushSleepTime(flushSleepTime);
    }

    public int getThreadsWaitingForceThreshold() {
        return configuration.getThreadsWaitingForceThreshold();
    }

    public void setThreadsWaitingForceThreshold(int threadsWaitingForceThreshold) {
        configuration.setThreadsWaitingForceThreshold(threadsWaitingForceThreshold == -1 ? Integer.MAX_VALUE : threadsWaitingForceThreshold);
    }

    public int getMaxBlocksPerFile() {
        return configuration.getMaxBlocksPerFile();
    }

    public void setMaxBlocksPerFile(int maxBlocksPerFile) {
        configuration.setMaxBlocksPerFile(maxBlocksPerFile == -1 ? Integer.MAX_VALUE : maxBlocksPerFile);
    }

    public int getMaxLogFiles() {
        return configuration.getMaxLogFiles();
    }

    public void setMaxLogFiles(int maxLogFiles) {
        configuration.setMaxLogFiles(maxLogFiles);
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void doStart() throws WaitingException, Exception {
        started = true;
        setLogFileDir(logFileDir);

        logger.open();
    }

    public void doStop() throws WaitingException, Exception {
        started = false;
        logger.close();
    }

    public void doFail() {
    }

    public void begin(Xid xid) throws LogException {
    }

    public long prepare(Xid xid, List branches) throws LogException {
        int branchCount = branches.size();
        byte[][] data = new byte[4 + 2 * branchCount][];
        data[0] = new byte[]{PREPARE};
        data[1] = intToBytes(xid.getFormatId());
        data[2] = xid.getGlobalTransactionId();
        data[3] = xid.getBranchQualifier();
        int i = 4;
        for (Iterator iterator = branches.iterator(); iterator.hasNext();) {
            TransactionBranchInfo transactionBranchInfo = (TransactionBranchInfo) iterator.next();
            data[i++] = transactionBranchInfo.getBranchXid().getBranchQualifier();
            data[i++] = transactionBranchInfo.getResourceName().getBytes();
        }
        try {
            long logMark = logger.put(data, true);
            addMark(logMark);
            return logMark;
        } catch (LogClosedException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (LogRecordSizeException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (LogFileOverflowException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (InterruptedException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (IOException e) {
            throw new LogException(e);
        }
    }

    private void addMark(long logMark) {
        synchronized (marks) {
            marks.add(new Long(logMark));
        }
    }

    public void commit(Xid xid, long logMark) throws LogException {
        byte[][] data = new byte[4][];
        data[0] = new byte[]{COMMIT};
        data[1] = intToBytes(xid.getFormatId());
        data[2] = xid.getGlobalTransactionId();
        data[3] = xid.getBranchQualifier();
        try {
            logger.put(data, false);
            boolean doMark = removeMark(logMark);
            if (doMark) {
                logger.mark(logMark);
            }
        } catch (LogClosedException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (LogRecordSizeException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (LogFileOverflowException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (InterruptedException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (IOException e) {
            throw new LogException(e);
        } catch (InvalidLogKeyException e) {
            throw new LogException(e);
        }
    }

    public void rollback(Xid xid, long logMark) throws LogException {
        byte[][] data = new byte[4][];
        data[0] = new byte[]{ROLLBACK};
        data[1] = intToBytes(xid.getFormatId());
        data[2] = xid.getGlobalTransactionId();
        data[3] = xid.getBranchQualifier();
        try {
            logger.put(data, false);
            boolean doMark = removeMark(logMark);
            if (doMark) {
                logger.mark(logMark);
            }
        } catch (LogClosedException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (LogRecordSizeException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (LogFileOverflowException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (InterruptedException e) {
            throw (IllegalStateException) new IllegalStateException().initCause(e);
        } catch (IOException e) {
            throw new LogException(e);
        } catch (InvalidLogKeyException e) {
            throw new LogException(e);
        }
    }

    private boolean removeMark(long logMark) {
        boolean doMark = false;
        Long mark = new Long(logMark);
        synchronized (marks) {
            doMark = (mark.equals(marks.first()));
            marks.remove(mark);
        }
        return doMark;
    }

    public Collection recover(XidFactory xidFactory) throws LogException {
        log.info("Initiating transaction manager recovery");
        Map recovered = new HashMap();
        ReplayListener replayListener = new GeronimoReplayListener(xidFactory, recovered);
        try {
            logger.replay(replayListener);
        } catch (LogConfigurationException e) {
            throw new LogException(e);
        }
        log.info("In doubt transactions recovered from log");
        return recovered.values();
    }

    public String getXMLStats() {
        return logger.getStats();
    }

    public int getAverageForceTime() {
        return 0;//logger.getAverageForceTime();
    }

    public int getAverageBytesPerForce() {
        return 0;//logger.getAverageBytesPerForce();
    }

    private byte[] intToBytes(int formatId) {
        byte[] buffer = new byte[4];
        buffer[0] = (byte) (formatId >> 24);
        buffer[1] = (byte) (formatId >> 16);
        buffer[2] = (byte) (formatId >> 8);
        buffer[3] = (byte) (formatId >> 0);
        return buffer;
    }

    private class GeronimoReplayListener implements ReplayListener {

        private final XidFactory xidFactory;
        private final Map recoveredTx;

        public GeronimoReplayListener(XidFactory xidFactory, Map recoveredTx) {
            this.xidFactory = xidFactory;
            this.recoveredTx = recoveredTx;
        }

        public void onRecord(LogRecord lr) {
            short recordType = lr.type;
            long logMark = lr.key;
            if (recordType == LogRecordType.USER) {
                ByteBuffer raw = lr.dataBuffer;
                if (raw.remaining() == 0) {
                    log.warn("Received empty log record of user type!");
                    return;
                }
                //type (PREPARE etc)
                short size = raw.getShort();
                assert size == 1;
                byte type = raw.get();
                //format id integer
                size = raw.getShort();
                assert size == 4;
                int formatId = raw.getInt();
                //global id
                int globalIdLength = raw.getShort();
                byte[] globalId = new byte[globalIdLength];
                raw.get(globalId);
                //branch qualifier for master xid
                int branchIdLength = raw.getShort();
                byte[] branchId = new byte[branchIdLength];
                raw.get(branchId);
                Xid masterXid = xidFactory.recover(formatId, globalId, branchId);
                if (type == PREPARE) {
                    Recovery.XidBranchesPair xidBranchesPair = new Recovery.XidBranchesPair(masterXid, logMark);
                    recoveredTx.put(masterXid, xidBranchesPair);
                    addMark(logMark);
//                log.info("recovered prepare record for master xid: " + masterXid);
                    while (raw.hasRemaining()) {
                        int branchBranchIdLength = raw.getShort();
                        byte[] branchBranchId = new byte[branchBranchIdLength];
                        raw.get(branchBranchId);
                        Xid branchXid = xidFactory.recover(formatId, globalId, branchBranchId);
                        int nameLength = raw.getShort();
                        byte[] nameBytes = new byte[nameLength];
                        raw.get(nameBytes);
                        String name = new String(nameBytes);
                        TransactionBranchInfoImpl branchInfo = new TransactionBranchInfoImpl(branchXid, name);
                        xidBranchesPair.addBranch(branchInfo);
//                    log.info("recovered branch for resource manager, branchId " + name + ", " + branchXid);
                    }
                } else if (type == COMMIT || type == ROLLBACK) {
                    recoveredTx.remove(masterXid);
                    removeMark(logMark);
//                log.info("Recovered " + TYPE_NAMES[type] + " for xid: " + masterXid + " and branches: " + o);
                } else {
                    log.error("Unknown recovery record received, type byte: " + type + ", buffer: " + raw);
                }
            } else if (recordType == LogRecordType.MARKKEY) {

            } else if (recordType != LogRecordType.END_OF_LOG) {
                log.warn("Received unexpected log record: " + lr);
            }
        }

        public void onError(org.objectweb.howl.log.LogException exception) {
            log.error("Error during recovery: ", exception);
        }

        public LogRecord getLogRecord() {
            //TODO justify this size estimate
            return new LogRecord(10 * 2 * Xid.MAXBQUALSIZE);
        }

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(HOWLLog.class);
        infoFactory.addAttribute("bufferClassName", String.class, true);
        infoFactory.addAttribute("bufferSizeKBytes", Integer.TYPE, true);
        infoFactory.addAttribute("checksumEnabled", Boolean.TYPE, true);
        infoFactory.addAttribute("flushSleepTimeMilliseconds", Integer.TYPE, true);
        infoFactory.addAttribute("logFileDir", String.class, true);
        infoFactory.addAttribute("logFileExt", String.class, true);
        infoFactory.addAttribute("logFileName", String.class, true);
        infoFactory.addAttribute("maxBlocksPerFile", Integer.TYPE, true);
        infoFactory.addAttribute("maxBuffers", Integer.TYPE, true);
        infoFactory.addAttribute("maxLogFiles", Integer.TYPE, true);
        infoFactory.addAttribute("minBuffers", Integer.TYPE, true);
        infoFactory.addAttribute("threadsWaitingForceThreshold", Integer.TYPE, true);

        infoFactory.addReference("serverInfo", ServerInfo.class);

        infoFactory.addInterface(TransactionLog.class);

        infoFactory.setConstructor(new String[]{
            "bufferClassName",
            "bufferSizeKBytes",
            "checksumEnabled",
            "flushSleepTimeMilliseconds",
            "logFileDir",
            "logFileExt",
            "logFileName",
            "maxBlocksPerFile",
            "maxBuffers",
            "maxLogFiles",
            "minBuffers",
            "threadsWaitingForceThreshold",
            "serverInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
