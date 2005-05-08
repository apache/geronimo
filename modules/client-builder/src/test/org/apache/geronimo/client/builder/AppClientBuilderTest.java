package org.apache.geronimo.client.builder;

import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;

/**
 */
public class AppClientBuilderTest extends TestCase {

    public void testAppClientGBean() throws Exception {
        Kernel kernel = KernelFactory.newInstance().createKernel("testDomain");
        kernel.boot();

        GBeanData gbeanData = new GBeanData(new ObjectName("testDomain:test=test"), AppClientModuleBuilder.GBEAN_INFO);
        kernel.loadGBean(gbeanData, AppClientModuleBuilder.class.getClassLoader());
    }
}
