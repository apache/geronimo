package org.apache.geronimo.web.jetty;

import java.net.URI;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.web.AbstractWebApplication;
import org.apache.geronimo.web.WebContainer;
import org.apache.geronimo.naming.java.ReadOnlyContext;

import org.mortbay.jetty.servlet.WebApplicationContext;


/**
 * JettyWebApplication.java
 *
 *
 * Created: Sun Sep 14 16:40:17 2003
 *
 * @jmx:mbean extends="org.apache.geronimo.web.AbstractWebApplicationMBean
 * @version $Revision: 1.4 $ $Date: 2003/11/16 07:18:26 $
 */
public class JettyWebApplication extends AbstractWebApplication implements JettyWebApplicationMBean {
    private WebApplicationContext jettyContext;
    private ReadOnlyContext context;
    private final Log log = LogFactory.getLog(getClass());

    public JettyWebApplication() {
        super();
        jettyContext = new JettyWebApplicationContext();

        try {
            objectName = new ObjectName("jetty:role=WebApplication, instance=" + hashCode());
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    public JettyWebApplication(URI uri) {
        super(uri);
        jettyContext = new JettyWebApplicationContext(uri.toString());

        try {
            objectName = new ObjectName("jetty:role=WebApplication, uri=" + ObjectName.quote(uri.toString()));
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        return super.preRegister(server, objectName);
    }

    public void setParentClassLoader(ClassLoader loader) {
        jettyContext.setParentClassLoader(loader);
    }

    public ClassLoader getParentClassLoader() {
        return jettyContext.getParentClassLoader();
    }

    public void setContextPath(String path) {
        jettyContext.setContextPath(path);
    }

    public String getContextPath() {
        return jettyContext.getContextPath();
    }

    public String getDeploymentDescriptor() {
        //TODO
        return null;
    }

    public boolean getJava2ClassloadingCompliance() {
        return jettyContext.isClassLoaderJava2Compliant();
    }

    public void setJava2ClassloadingCompliance(boolean state) {
        jettyContext.setClassLoaderJava2Compliant(state);
    }

    WebApplicationContext getJettyContext() {
        return jettyContext;
    }

    public ReadOnlyContext getContext() {
        return context;
    }

    public void setContext(ReadOnlyContext context) {
        this.context = context;
    }

    public void doStart() throws Exception {
        super.doStart();
        String defaultDescriptor = null;
        URI defaultDescriptorURI = ((WebContainer) getContainer()).getDefaultWebXmlURI();
        if (defaultDescriptorURI != null)
            defaultDescriptor = defaultDescriptorURI.toString();
        jettyContext.setDefaultsDescriptor(defaultDescriptor);
        jettyContext.start();

        log.debug(jettyContext.getFileClassPath());
    }

    public void doStop() throws Exception {
        super.doStop();
        jettyContext.stop();
    }
}
