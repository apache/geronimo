package org.apache.geronimo.web.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

import javax.naming.Context;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.deployment.model.geronimo.web.GeronimoWebAppDocument;
import org.apache.geronimo.deployment.model.web.WebApp;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.apache.geronimo.web.AbstractWebApplication;
import org.apache.geronimo.transaction.manager.UserTransactionImpl;
import org.mortbay.jetty.servlet.WebApplicationContext;


/**
 * JettyWebApplication.java
 *
 *
 * Created: Sun Sep 14 16:40:17 2003
 *
 * @version $Revision: 1.12 $ $Date: 2004/01/16 23:31:21 $
 */
public class JettyWebApplication extends AbstractWebApplication {

    private static final GBeanInfo GBEAN_INFO;

    private static final Log log = LogFactory.getLog(JettyWebApplication.class);

    private static String CONTAINER_NAME = "Jetty";

    private JettyWebApplicationContext jettyContext;

    public JettyWebApplication() {
        super(new org.apache.geronimo.web.WebApplicationContext());
    }

    public JettyWebApplication(URI uri, ClassLoader parentClassLoader, WebApp webApp, GeronimoWebAppDocument geronimoWebAppDocument, String contextPath,
                               Context context, boolean java2ClassLoadingCompliance, UserTransactionImpl userTransaction, TransactionManager transactionManager, TrackedConnectionAssociator trackedConnectionAssociator) {
        super(uri, parentClassLoader, webApp, geronimoWebAppDocument, contextPath, context,
                java2ClassLoadingCompliance, userTransaction, transactionManager, trackedConnectionAssociator);
        if (uri == null) {
            jettyContext = new JettyWebApplicationContext();
        } else {
            jettyContext = new JettyWebApplicationContext(uri.toString());
        }
        //we could perhaps use geronimo classloading
        //jettyContext.setClassLoader(classLoader);
        jettyContext.setParentClassLoader(parentClassLoader);
        jettyContext.setContextPath(contextPath);
        jettyContext.setClassLoaderJava2Compliant(java2ClassLoadingCompliance);
        jettyContext.setComponentContext(context);
    }

    /**
     *  @deprecated, remove when GBean -only
     */
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
            URL url = new URL(jettyContext.getDeploymentDescriptor());
            StringBuffer strbuff = new StringBuffer();
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            boolean more = true;
            while (more) {
                String line = reader.readLine();
                if (line == null)
                    more = false;
                else
                    strbuff.append(line);
            }

            deploymentDescriptorStr = strbuff.toString();

            return deploymentDescriptorStr;

        } catch (IOException e) {
            log.error(e);
            return null;
        } finally {
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

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Jetty Web Application", JettyWebApplication.class.getName(), AbstractWebApplication.getGBeanInfo());
        infoFactory.addOperation(new GOperationInfo("getJettyContext", Collections.EMPTY_LIST));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


    /**
     *  @deprecated, remove when GBean -only
     */
    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = AbstractWebApplication.getGeronimoMBeanInfo(CONTAINER_NAME);
        mbeanInfo.setTargetClass(JettyWebApplication.class.getName());
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getJettyContext", new GeronimoParameterInfo[]{}, GeronimoOperationInfo.INFO, "Retrieve the internal JettyContext"));
        return mbeanInfo;
    }
}
