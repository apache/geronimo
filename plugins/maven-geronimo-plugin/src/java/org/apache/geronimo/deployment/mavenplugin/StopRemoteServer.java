package org.apache.geronimo.deployment.mavenplugin;

import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

/**
 */
public class StopRemoteServer extends AbstractModuleCommand {

    private MBeanServerConnection mbServerConnection;
    private KernelMBean kernel;

    public void execute() throws Exception {
        String uri = getUri().substring(DeploymentFactoryImpl.URI_PREFIX.length());
        if (!uri.startsWith("jmx")) {
            throw new Exception("bad uri");
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
            kernel = (KernelMBean) MBeanProxyFactory.getProxy(KernelMBean.class, mbServerConnection, Kernel.KERNEL);
            kernel.shutdown();

        } finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }

    }

}
