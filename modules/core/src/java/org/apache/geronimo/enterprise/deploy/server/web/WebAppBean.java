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
package org.apache.geronimo.enterprise.deploy.server.web;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.enterprise.deploy.server.BaseDConfigBean;
import org.apache.geronimo.enterprise.deploy.server.DConfigBeanLookup;
import org.apache.geronimo.enterprise.deploy.server.j2ee.JNDIRefs;
import org.apache.geronimo.enterprise.deploy.server.j2ee.EjbRefBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.EjbLocalRefBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.ResourceRefBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.ResourceEnvRefBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.EnvEntryBean;
import org.apache.geronimo.enterprise.deploy.server.j2ee.MessageDestinationRefBean;

/**
 * The DConfigBean representing /web-app
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/07 17:16:36 $
 */
public class WebAppBean extends BaseDConfigBean implements JNDIRefs {
    private static final Log log = LogFactory.getLog(WebAppBean.class);
    private List envEntries = new ArrayList();
    private List ejbRefs = new ArrayList();
    private List ejbLocalRefs = new ArrayList();
    private List resourceRefs = new ArrayList();
    private List resourceEnvRefs = new ArrayList();
    private List messageRefs = new ArrayList();

    /**
     * This is present for JavaBeans compliance, but if it is used, the
     * DConfigBean won't be properly associated with a DDBean, so it
     * should be used for experimentation only.
     */
    public WebAppBean() {
        super(null, null);
    }

    public WebAppBean(DDBean ddBean, DConfigBeanLookup lookup) {
        super(ddBean, lookup);
    }

    public String[] getXpaths() {
        return new String[] {
            ENV_ENTRY_XPATH,
            EJB_REF_XPATH,
            EJB_LOCAL_REF_XPATH,
            RESOURCE_REF_XPATH,
            RESOURCE_ENV_REF_XPATH,
            MESSAGE_DESTINATION_REF_XPATH,
        };
    }

    /**
     * Gets a DConfigBean corresponding to an interesting DDBean.  The only
     * interesting DDBean is for "enterprise-beans" (that's the only
     * one returned by getXpaths), so others will result in an exception.
     */
    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        DConfigBean result = getDConfigBean(bean, true);
        if(result == null) {
            throw new ConfigurationException("No matching DConfigBean for DDBean "+bean.getXpath());
        }
        return result;
    }

    public void removeDConfigBean(DConfigBean bean) throws BeanNotFoundException {
        if(bean instanceof EjbRefBean) {
            if(!ejbRefs.remove(bean)) {
                throw new BeanNotFoundException("Could not find EJB Reference "+bean+" to remove");
            }
        } else if(bean instanceof EjbLocalRefBean) {
            if(!ejbLocalRefs.remove(bean)) {
                throw new BeanNotFoundException("Could not find EJB Local Reference "+bean+" to remove");
            }
        } else if(bean instanceof ResourceRefBean) {
            if(!resourceRefs.remove(bean)) {
                throw new BeanNotFoundException("Could not find Resource Reference "+bean+" to remove");
            }
        } else if(bean instanceof ResourceEnvRefBean) {
            if(!resourceEnvRefs.remove(bean)) {
                throw new BeanNotFoundException("Could not find Resource Env Reference "+bean+" to remove");
            }
        } else if(bean instanceof EnvEntryBean) {
            if(!envEntries.remove(bean)) {
                throw new BeanNotFoundException("Could not find Env Entry "+bean+" to remove");
            }
        } else if(bean instanceof MessageDestinationRefBean) {
            if(!messageRefs.remove(bean)) {
                throw new BeanNotFoundException("Could not find Message Destination Ref "+bean+" to remove");
            }
        } else {
            throw new BeanNotFoundException("No such child bean "+bean.getClass().getName());
        }
    }

    public void notifyDDChange(XpathEvent event) {
        try {
            if(event.isAddEvent()) {
                getDConfigBean(event.getBean());
            } else if(event.isRemoveEvent()) {
                DConfigBean bean = getDConfigBean(event.getBean(), false);
                if(bean != null) {
                    removeDConfigBean(bean);
                }
            }
        } catch(ConfigurationException e) {
            log.error("Unable to handle XPathEvent", e);
        } catch(BeanNotFoundException e) {
            log.error("Unable to handle XPathEvent", e);
        }
    }

    protected DConfigBean getDConfigBean(DDBean bean, boolean create) {
        if(bean.getXpath().equals(EJB_REF_XPATH)) {
            for(Iterator it = ejbRefs.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean ref = new EjbRefBean(bean, lookup);
                ejbRefs.add(ref);
                return ref;
            }
        } else if(bean.getXpath().equals(EJB_LOCAL_REF_XPATH)) {
            for(Iterator it = ejbLocalRefs.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean ref = new EjbLocalRefBean(bean, lookup);
                ejbLocalRefs.add(ref);
                return ref;
            }
        } else if(bean.getXpath().equals(RESOURCE_REF_XPATH)) {
            for(Iterator it = resourceRefs.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean ref = new ResourceRefBean(bean, lookup);
                resourceRefs.add(ref);
                return ref;
            }
        } else if(bean.getXpath().equals(RESOURCE_ENV_REF_XPATH)) {
            for(Iterator it = resourceEnvRefs.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean ref = new ResourceEnvRefBean(bean, lookup);
                resourceEnvRefs.add(ref);
                return ref;
            }
        } else if(bean.getXpath().equals(ENV_ENTRY_XPATH)) {
            for(Iterator it = envEntries.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean entry = new EnvEntryBean(bean, lookup);
                envEntries.add(entry);
                return entry;
            }
        } else if(bean.getXpath().equals(MESSAGE_DESTINATION_REF_XPATH)) {
            for(Iterator it = messageRefs.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean ref = new MessageDestinationRefBean(bean, lookup);
                messageRefs.add(ref);
                return ref;
            }
        }
        return null;
    }

    public String toString() {
        return "WebApp";
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    public List getEnvEntry() {
        return envEntries;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    public List getEjbRef() {
        return ejbRefs;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    public List getEjbLocalRef() {
        return ejbLocalRefs;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    public List getResourceRef() {
        return resourceRefs;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    public List getResourceEnvRef() {
        return resourceEnvRefs;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    public List getMessageDestinationRef() {
        return messageRefs;
    }
}
