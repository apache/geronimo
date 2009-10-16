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


package org.apache.geronimo.mavenplugins.geronimo.server;

import java.io.File;

/**
 * @goal start-multi-server
 *
 * @version $Rev$ $Date$
 */
public class StartMultiServerMojo extends StartServerMojo{

    /**
     * @parameter
     */
    private int portOffset = 10;

    /**
     * @parameter
     * @required
     */
    private int count;

    /**
     * e.g start at server0, portOffset 0, or server1...
     * Default is 1 in order to allow room for a "controller" server with no port offset, by default.
     * @parameter
     */
    private int startAt = 1;

    /**
     * @parameter
     */
    private String relativePath = "server";

    /**
     *
     * @parameter
     */
    private String prefix = "org.apache.geronimo.config.substitution";

    @Override
    protected void doExecute() throws Exception {
        File base = installDirectory;
        String geronimoHomeSegment = geronimoHome.getName();
        String portOffsetName = prefix + ".PortOffset";
        String oldPortOffset = System.getProperty(portOffsetName);
        int oldPort = port;
        for (int i = startAt; i< count + startAt; i++) {
            String name = relativePath + i;
            installDirectory = new File(base, name);
            geronimoHome = new File(installDirectory, geronimoHomeSegment);
            logOutputDirectory = new File(installDirectory, "geronimo-logs");
            int actualPortOffset = this.portOffset * i;
            System.setProperty(portOffsetName, "" + actualPortOffset);
            port = oldPort + actualPortOffset;
            log.info("server: " + i + " actual port offset: "  + actualPortOffset + " port: " + port);
            super.doExecute();
        }
        if (oldPortOffset == null) {
            System.getProperties().remove(portOffsetName);
        } else {
            System.setProperty(portOffsetName, oldPortOffset);
        }
        port = oldPort;
    }

}
