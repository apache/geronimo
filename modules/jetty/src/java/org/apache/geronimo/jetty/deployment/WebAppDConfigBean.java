/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.jetty.deployment;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.naming.deployment.ENCHelper;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.geronimo.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.GerMessageDestinationRefType;
import org.apache.geronimo.xbeans.geronimo.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.GerResourceRefType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.9 $ $Date: 2004/02/18 20:58:43 $
 */
public class WebAppDConfigBean extends DConfigBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();

    private ENCHelper encHelper;

    WebAppDConfigBean(DDBean ddBean, JettyWebAppType webApp) {
        super(ddBean, webApp, SCHEMA_TYPE_LOADER);
        if (webApp.getContextRoot() == null) {
            webApp.addNewContextRoot();
        }
        encHelper = new ENCHelper(ddBean, new ENCHelper.XmlEnvRefs() {
            public GerEjbRefType[] getEjbRefs() {
                return getWebApp().getEjbRefArray();
            }

            public GerEjbRefType addNewEjbRef() {
                return getWebApp().addNewEjbRef();
            }

            public void removeEjbRef(int i) {
                getWebApp().removeEjbRef(i);
            }

            public GerEjbLocalRefType[] getEjbLocalRefs() {
                return getWebApp().getEjbLocalRefArray();
            }

            public GerEjbLocalRefType addNewEjbLocalRef() {
                return getWebApp().addNewEjbLocalRef();
            }

            public void removeEjbLocalRef(int i) {
                getWebApp().removeEjbLocalRef(i);
            }

            public GerMessageDestinationRefType[] getMessageDestinationRefs() {
                return getWebApp().getMessageDestinationRefArray();
            }

            public GerMessageDestinationRefType addNewMessageDestinationRef() {
                return getWebApp().addNewMessageDestinationRef();
            }

            public void removeMessageDestinationRef(int i) {
                getWebApp().removeMessageDestinationRef(i);
            }

            public GerResourceEnvRefType[] getResourceEnvRefs() {
                return getWebApp().getResourceEnvRefArray();
            }

            public GerResourceEnvRefType addNewResourceEnvRef() {
                return getWebApp().addNewResourceEnvRef();
            }

            public void removeResourceEnvRef(int i) {
                getWebApp().removeResourceEnvRef(i);
            }

            public GerResourceRefType[] getResourceRefs() {
                return getWebApp().getResourceRefArray();
            }

            public GerResourceRefType addNewResourceRef() {
                return getWebApp().addNewResourceRef();
            }

            public void removeResourceRef(int i) {
                getWebApp().removeResourceRef(i);
            }

        });
    }

    JettyWebAppType getWebApp() {
        return (JettyWebAppType)getXmlObject();
    }

    public String getContextRoot() {
        return getWebApp().getContextRoot().getStringValue();
    }

    public void setContextRoot(String contextRoot) {
        pcs.firePropertyChange("contextRoot", getContextRoot(), contextRoot);
        getWebApp().getContextRoot().setStringValue(contextRoot);
    }

    /** getContextPriorityClassLoader.
     * @return True if this context should give web application class in preference over the containers
     * classes, as per the servlet specification recommendations.
     */
    public boolean getContextPriorityClassLoader() {
        return getWebApp().getContextPriorityClassloader();
    }

    /** setContextPriorityClassLoader.
     * @param contextPriority True if this context should give web application class in preference over the containers
     * classes, as per the servlet specification recommendations.
     */
    public void setContextPriorityClassLoader(boolean contextPriority) {
        pcs.firePropertyChange("contextPriorityClassLoader", getContextPriorityClassLoader(), contextPriority);
        getWebApp().setContextPriorityClassloader(contextPriority);
    }

    public DConfigBean getDConfigBean(DDBean ddBean) throws ConfigurationException {
        return encHelper.getDConfigBean(ddBean);
    }

    public void removeDConfigBean(DConfigBean dcBean) throws BeanNotFoundException {
        //encHelper.removeDConfigBean(dcBean);
    }

    public String[] getXpaths() {
        return ENCHelper.ENC_XPATHS;
    }

}
