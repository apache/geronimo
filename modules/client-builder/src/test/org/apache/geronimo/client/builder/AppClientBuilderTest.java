package org.apache.geronimo.client.builder;

import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 */
public class AppClientBuilderTest extends TestCase {

    public void testAppClientGBean() throws Exception {
        Kernel kernel = new Kernel("testDomain");
        kernel.boot();
        GBeanMBean gbean = new GBeanMBean(AppClientModuleBuilder.class.getName(), AppClientModuleBuilder.class.getClassLoader());
        kernel.loadGBean(new ObjectName("testDomain:test=test"), gbean);

    }
}
