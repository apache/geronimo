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
package org.apache.geronimo.transaction.wrapper.log;

import java.io.IOException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.log.HOWLLog;
import org.objectweb.howl.log.LogConfigurationException;

/**
 * @version $Rev$ $Date$
 */
public class HOWLLogGBean extends HOWLLog implements GBeanLifecycle {
    public HOWLLogGBean(String bufferClassName, int bufferSize, boolean checksumEnabled, boolean adler32Checksum, int flushSleepTimeMilliseconds, String logFileDir, String logFileExt, String logFileName, int maxBlocksPerFile, int maxBuffers, int maxLogFiles, int minBuffers, int threadsWaitingForceThreshold, XidFactory xidFactory, ServerInfo serverInfo) throws IOException, LogConfigurationException {
        super(bufferClassName, bufferSize, checksumEnabled, adler32Checksum, flushSleepTimeMilliseconds, logFileDir, logFileExt, logFileName, maxBlocksPerFile, maxBuffers, maxLogFiles, minBuffers, threadsWaitingForceThreshold, xidFactory, serverInfo.resolveServer("."));
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(HOWLLogGBean.class, NameFactory.TRANSACTION_LOG);
        infoFactory.addAttribute("bufferClassName", String.class, true);
        infoFactory.addAttribute("bufferSizeKBytes", Integer.TYPE, true);
        infoFactory.addAttribute("checksumEnabled", Boolean.TYPE, true);
        infoFactory.addAttribute("adler32Checksum", Boolean.TYPE, true);
        infoFactory.addAttribute("flushSleepTimeMilliseconds", Integer.TYPE, true);
        infoFactory.addAttribute("logFileDir", String.class, true);
        infoFactory.addAttribute("logFileExt", String.class, true);
        infoFactory.addAttribute("logFileName", String.class, true);
        infoFactory.addAttribute("maxBlocksPerFile", Integer.TYPE, true);
        infoFactory.addAttribute("maxBuffers", Integer.TYPE, true);
        infoFactory.addAttribute("maxLogFiles", Integer.TYPE, true);
        infoFactory.addAttribute("minBuffers", Integer.TYPE, true);
        infoFactory.addAttribute("threadsWaitingForceThreshold", Integer.TYPE, true);

        infoFactory.addReference("XidFactory", XidFactory.class, NameFactory.XID_FACTORY);
        infoFactory.addReference("ServerInfo", ServerInfo.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        infoFactory.addInterface(TransactionLog.class);

        infoFactory.setConstructor(new String[]{
                "bufferClassName",
                "bufferSizeKBytes",
                "checksumEnabled",
                "adler32ChecksumEnabled",
                "flushSleepTimeMilliseconds",
                "logFileDir",
                "logFileExt",
                "logFileName",
                "maxBlocksPerFile",
                "maxBuffers",
                "maxLogFiles",
                "minBuffers",
                "threadsWaitingForceThreshold",
                "XidFactory",
                "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return HOWLLogGBean.GBEAN_INFO;
    }

}
