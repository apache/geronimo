package org.apache.geronimo.web.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.web.AbstractWebApplication;
import org.apache.geronimo.web.WebContainer;
import org.mortbay.jetty.servlet.WebApplicationContext;


/**
 * JettyWebApplication.java
 *
 *
 * Created: Sun Sep 14 16:40:17 2003
 *
 * @jmx:mbean extends="org.apache.geronimo.web.AbstractWebApplicationMBean
 * @version $Revision: 1.7 $ $Date: 2003/11/25 13:51:30 $
 */
public class JettyWebApplication extends AbstractWebApplication implements JettyWebApplicationMBean {
    private JettyWebApplicationContext jettyContext;
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
        jettyContext.setServer(server);
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

   
    /* Hacky implementation of getting the web.xm as a String.
     * This should be handled by converting pojo->xml->string
     * @return
     * @see org.apache.geronimo.kernel.management.J2EEDeployedObject#getDeploymentDescriptor()
     */
    public String getDeploymentDescriptor() {
        if (deploymentDescriptorStr != null)
            return deploymentDescriptorStr;
            
        BufferedReader reader = null;
        try {
            URL url = new URL (jettyContext.getDeploymentDescriptor());
            StringBuffer strbuff = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader (url.openStream()));
            boolean more = true;
            while (more) {
                String line = reader.readLine ();
                if (line == null)
                    more = false;
                else
                    strbuff.append (line);
            }
            
            deploymentDescriptorStr = strbuff.toString();

            return deploymentDescriptorStr;
            
        } catch (IOException e) {
            log.error (e);
            return null;
        }
        finally {
            try {
                reader.close();
            } catch (IOException e) {
                log.warn("Error closing web.xml reader", e);
            }
            
        }    
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

    public Context getComponentContext() {
        return jettyContext.getComponentContext();
    }

    public void setComponentContext(Context context) {
        jettyContext.setComponentContext(context);
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
