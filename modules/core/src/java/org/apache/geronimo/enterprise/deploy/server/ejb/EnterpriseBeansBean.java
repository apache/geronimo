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
package org.apache.geronimo.enterprise.deploy.server.ejb;

import java.util.ArrayList;
import java.util.List;
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

/**
 * The DConfigBean corresponding to the EJB JAR XPath /ejb-jar/enterprise-beans
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/06 14:35:34 $
 */
public class EnterpriseBeansBean extends BaseDConfigBean {
    private static final Log log = LogFactory.getLog(EnterpriseBeansBean.class);
    final static String SESSION_XPATH = "session";
    final static String ENTITY_XPATH = "entity";
    final static String MESSAGE_DRIVEN_XPATH = "message-driven";
    private List sessions = new ArrayList();
    private List entities = new ArrayList();
    private List messageDrivens = new ArrayList();

    /**
     * This is present for JavaBeans compliance, but if it is used, the
     * DConfigBean won't be properly associated with a DDBean, so it
     * should be used for experimentation only.
     */
    public EnterpriseBeansBean() {
        super(null, null);
    }

    public EnterpriseBeansBean(DDBean ddBean, DConfigBeanLookup lookup) {
        super(ddBean, lookup);
    }

    public String[] getXpaths() {
        return new String[] {
            SESSION_XPATH,
            ENTITY_XPATH,
            MESSAGE_DRIVEN_XPATH,
        };
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        DConfigBean result = getDConfigBean(bean, true);
        if(result == null) {
            throw new ConfigurationException("No matching DConfigBean for DDBean "+bean.getXpath());
        }
        return result;
    }

    public void removeDConfigBean(DConfigBean bean) throws BeanNotFoundException {
        if(bean instanceof SessionBean) {
            if(!sessions.remove(bean)) {
                throw new BeanNotFoundException("Could not find session bean "+((SessionBean)bean).getEjbName()+" to remove");
            }
        } else if(bean instanceof EntityBean) {
            if(!entities.remove(bean)) {
                throw new BeanNotFoundException("Could not find entity bean "+((EntityBean)bean).getEjbName()+" to remove");
            }
        } else if(bean instanceof MessageDrivenBean) {
            if(!messageDrivens.remove(bean)) {
                throw new BeanNotFoundException("Could not find message-driven bean "+((MessageDrivenBean)bean).getEjbName()+" to remove");
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

    private DConfigBean getDConfigBean(DDBean bean, boolean create) {
        if(bean.getXpath().equals(SESSION_XPATH)) {
            for(Iterator it = sessions.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean session = new SessionBean(bean, lookup);
                sessions.add(session);
                return session;
            }
        } else if(bean.getXpath().equals(ENTITY_XPATH)) {
            for(Iterator it = entities.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean entity = new EntityBean(bean, lookup);
                entities.add(entity);
                return entity;
            }
        } else if(bean.getXpath().equals(MESSAGE_DRIVEN_XPATH)) {
            for(Iterator it = messageDrivens.iterator(); it.hasNext();) {
                DConfigBean dcb = (DConfigBean)it.next();
                if(dcb.getDDBean().equals(bean)) {
                    return dcb;
                }
            }
            if(create) {
                DConfigBean mdb = new MessageDrivenBean(bean, lookup);
                messageDrivens.add(mdb);
                return mdb;
            }
        }
        return null;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    List getSession() {
        return sessions;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    List getEntity() {
        return entities;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    List getMessageDriven() {
        return messageDrivens;
    }
}
