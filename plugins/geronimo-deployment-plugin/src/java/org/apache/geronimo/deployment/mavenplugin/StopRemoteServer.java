package org.apache.geronimo.deployment.mavenplugin;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.KernelDelegate;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class StopRemoteServer extends AbstractModuleCommand {

    private MBeanServerConnection mbServerConnection;
    private Kernel kernel;

    public void execute() throws Exception {
        String uri = getUri();
        if (!uri.startsWith("jmx")) {
            throw new Exception("Bad JMX URI ("+uri+")");
        }

        Map environment = new HashMap();
        String[] credentials = new String[]{getUsername(), getPassword()};
        environment.put(JMXConnector.CREDENTIALS, credentials);

        JMXServiceURL address = new JMXServiceURL("service:" + uri);
        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            JMXConnector jmxConnector = JMXConnectorFactory.connect(address, environment);
            mbServerConnection = jmxConnector.getMBeanServerConnection();
            kernel = new KernelDelegate(mbServerConnection);
            kernel.shutdown();

        } finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }

    }

}
