package org.apache.geronimo.web.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.apache.geronimo.web.AbstractWebApplication;
import org.apache.geronimo.web.AbstractWebContainer;
import org.apache.geronimo.web.WebContainer;
import org.mortbay.jetty.servlet.WebApplicationContext;


/**
 * JettyWebApplication.java
 *
 *
 * Created: Sun Sep 14 16:40:17 2003
 *
 * @version $Revision: 1.9 $ $Date: 2003/12/30 08:28:58 $
 */
public class JettyWebApplication extends AbstractWebApplication {

    private static String CONTAINER_NAME = "Jetty";

    private JettyWebApplicationContext jettyContext;
    private static final Log log = LogFactory.getLog(JettyWebApplication.class);
    private AbstractWebContainer webContainer;

    public JettyWebApplication() {
        super(new org.apache.geronimo.web.WebApplicationContext());
    }

    public JettyWebApplication(org.apache.geronimo.web.WebApplicationContext webApplicationContext) {
        super(webApplicationContext);
        URI uri = webApplicationContext.uri;

        if (uri == null) {
            jettyContext = new JettyWebApplicationContext();
        } else {
            jettyContext = new JettyWebApplicationContext(uri.toString());
        }
        //we could perhaps use geronimo classloading
        //jettyContext.setClassLoader(webApplicationContext.classLoader);
        jettyContext.setParentClassLoader(webApplicationContext.parentClassLoader);
        jettyContext.setContextPath(webApplicationContext.contextPath);
        jettyContext.setClassLoaderJava2Compliant(webApplicationContext.java2ClassLoadingCompliance);
        jettyContext.setComponentContext(webApplicationContext.context);

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

    public WebApplicationContext getJettyContext() {
        return jettyContext;
    }

    public void setWebContainer(WebContainer webContainer) {
        this.webContainer = (AbstractWebContainer) webContainer;
    }

    public WebContainer getWebContainer() {
        return webContainer;
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = AbstractWebApplication.getGeronimoMBeanInfo(CONTAINER_NAME);
        mbeanInfo.setTargetClass(JettyWebApplication.class.getName());
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getJettyContext", new GeronimoParameterInfo[] {}, GeronimoOperationInfo.INFO, "Retrieve the internal JettyContext"));
        return mbeanInfo;
    }
}
