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

package org.apache.geronimo.kernel.jmx;

import java.net.URL;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ServiceNotFoundException;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;
import javax.management.loading.MLet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:01 $
 */
public class JMXKernel {
    private static final Log log = LogFactory.getLog(JMXKernel.class);
    private final MBeanServer server;

    public JMXKernel(String domainName) {
        server = MBeanServerFactory.createMBeanServer(domainName);
    }

    public void release() {
        MBeanServerFactory.releaseMBeanServer(server);
    }

    public MBeanServer getMBeanServer() {
        return server;
    }

    public String getMBeanServerId() {
        return getMBeanServerId(server);
    }

    public static  String getMBeanServerId(MBeanServer server) {
        try {
            return (String)server.getAttribute(ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate"), "MBeanServerId");
        } catch (MBeanException e) {
            log.info(e);
        } catch (AttributeNotFoundException e) {
            log.info(e);
        } catch (InstanceNotFoundException e) {
            log.info(e);
        } catch (ReflectionException e) {
            log.info(e);
        } catch (MalformedObjectNameException e) {
            log.info(e);
        }
        throw new RuntimeException("could not get the MBeanServerId");
    }

    public Set bootMLet(URL mletURL) throws ServiceNotFoundException {
        String urlString = mletURL.toString();
        log.info("Booting MLets from URL " + urlString);

        ObjectName objectName;
        try {
            objectName = new ObjectName("geronimo.boot:type=BootMLet,bootURL=" + ObjectName.quote(urlString));
        } catch (MalformedObjectNameException e) {
            IllegalArgumentException ex = new IllegalArgumentException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }


        MLet bootMLet = new MLet();
        try {
            server.registerMBean(bootMLet, objectName);
        } catch (Exception e) {
            // it should be impossible for this registration to fail
            IllegalStateException ex = new IllegalStateException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
        return bootMLet.getMBeansFromURL(mletURL);
    }

}
