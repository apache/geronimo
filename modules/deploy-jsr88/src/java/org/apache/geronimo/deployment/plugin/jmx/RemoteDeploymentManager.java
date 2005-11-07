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
package org.apache.geronimo.deployment.plugin.jmx;

import java.io.IOException;
import javax.management.remote.JMXConnector;
import javax.management.MBeanServerConnection;
import org.apache.geronimo.kernel.jmx.KernelDelegate;

/**
 * Connects to a Kernel in a remote VM.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class RemoteDeploymentManager extends JMXDeploymentManager {
    private JMXConnector jmxConnector;
    private MBeanServerConnection mbServerConnection;

    public RemoteDeploymentManager(JMXConnector jmxConnector) throws IOException {
        this.jmxConnector = jmxConnector;
        mbServerConnection = jmxConnector.getMBeanServerConnection();
        initialize(new KernelDelegate(mbServerConnection));
    }

    public void release() {
        super.release();
        try {
            jmxConnector.close();
            jmxConnector = null;
        } catch (IOException e) {
            throw (IllegalStateException) new IllegalStateException("Unable to close connection").initCause(e);
        } finally {
            mbServerConnection = null;
        }
    }
}
