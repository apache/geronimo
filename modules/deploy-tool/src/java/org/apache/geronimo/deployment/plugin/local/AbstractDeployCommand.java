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
package org.apache.geronimo.deployment.plugin.local;

import java.util.Set;
import java.util.Iterator;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import javax.enterprise.deploy.shared.CommandType;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.KernelMBean;

/**
 * @version $Rev:  $ $Date:  $
 */
public abstract class AbstractDeployCommand extends CommandSupport {
    private final static String DEPLOYER_NAME = "*:name=Deployer,j2eeType=Deployer,*";

    protected final KernelMBean kernel;

    public AbstractDeployCommand(CommandType command, KernelMBean kernel) {
        super(command);
        this.kernel = kernel;
    }

    protected ObjectName getDeployerName() {
        Set deployers = kernel.listGBeans(JMXUtil.getObjectName(DEPLOYER_NAME));
        if (deployers.isEmpty()) {
            fail("No deployer present in kernel");
            return null;
        }
        Iterator j = deployers.iterator();
        ObjectName deployer = (ObjectName) j.next();
        if (j.hasNext()) {
            fail("More than one deployer found");
            return null;
        }
        return deployer;

    }

    protected void copyTo(File outfile, InputStream is) throws IOException {
        byte[] buffer = new byte[4096];
        int count;
        OutputStream os = new FileOutputStream(outfile);
        try {
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
        } finally {
            os.close();
        }
    }
}
