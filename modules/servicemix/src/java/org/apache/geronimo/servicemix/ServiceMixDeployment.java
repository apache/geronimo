package org.apache.geronimo.servicemix;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

public class ServiceMixDeployment implements GBeanLifecycle {

    public static final GBeanInfo GBEAN_INFO;
    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ServiceMixDeployment.class);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);
        infoBuilder.setConstructor(new String[] { "classLoader", "configurationBaseUrl" });
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private final ClassLoader classLoader;
    private final URL configurationBaseUrl;
    private Object context;

    public ServiceMixDeployment(ClassLoader classLoader, URL configurationBaseUrl) {
        this.classLoader = classLoader;
        this.configurationBaseUrl=configurationBaseUrl;
    }

    synchronized public void doStart() throws Exception {
        if (context != null)
            throw new IllegalStateException("Already started.");
        System.out.println("starting..");
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            // We load dynamically so that servicemix and spring do not have to be in the system classpath.
            Class clazz = classLoader.loadClass("org.springframework.context.support.ClassPathXmlApplicationContext");
            Constructor constructor = clazz.getConstructor(new Class[]{String.class});
            context = constructor.newInstance(new Object[]{"META-INF/jbi.xml"});
            Method method = clazz.getMethod("getBean", new Class[]{String.class});
            method.invoke(context, new String[]{"jbi"});
        } finally {
            Thread.currentThread().setContextClassLoader(null);
        }
    }

    synchronized public void doStop() throws Exception {
        if (context == null)
            throw new IllegalStateException("Already stopped.");

        Method method = context.getClass().getMethod("close", null);
        method.invoke(context, null);
        context = null;
    }

    public void doFail() {
    }

}
