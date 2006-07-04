/**
 *
 * Copyright 2004-2006 The Apache Software Foundation
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

package org.apache.geronimo.plugins.deployment;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.KernelDelegate;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * @goal stopRemoteServer
 *
 * @version $Rev:$ $Date:$
 */
public class StopRemoteServerMojo extends AbstractModuleMojo {

    
    private MBeanServerConnection mbServerConnection;
    private Kernel kernel;

    private PrintStream logStream = System.out;
    
    private PrintStream resultStream;
    
    private final String goalName = "Stop Remote Server";

    public void execute() throws MojoExecutionException {        
        resultStream = getResultsStream();        
        logStream = getLogStream(goalName);
        
        try {
            stopRemoteServer();
        }
        catch (Exception e) {
            logResults(resultStream, goalName, "fail");
            handleError(e, logStream);
            return;
        }
        logResults(resultStream, goalName, "success"); 
    }

    /**
     * 
     */
    private void stopRemoteServer() throws Exception {
        String uri = getUri();        
        if (!uri.startsWith("jmx")) {            
                throw new Exception("Bad JMX URI ("+uri+")");            
        }

        Map environment = new HashMap();
        String[] credentials = new String[]{getUsername(), getPassword()};
        environment.put(JMXConnector.CREDENTIALS, credentials);

        JMXServiceURL address = null;
        JMXConnector jmxConnector;
        try {
            address = new JMXServiceURL("service:" + uri);
        }
        catch (Exception e) {
            throw e;            
        }
        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            jmxConnector = JMXConnectorFactory.connect(address, environment);
            mbServerConnection = jmxConnector.getMBeanServerConnection();
            kernel = new KernelDelegate(mbServerConnection);
            kernel.shutdown();
        }
        catch (Exception e) {
            throw e;            
        } 
        finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }
    }

}
