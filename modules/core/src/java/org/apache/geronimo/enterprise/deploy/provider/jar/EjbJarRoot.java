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
package org.apache.geronimo.enterprise.deploy.provider.jar;

import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.DDBeanRoot;

/**
 * The DConfigBeanRoot for the top level of the EJB JAR DD ("/").
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/27 10:33:34 $
 */
public class EjbJarRoot extends BaseDConfigBean implements DConfigBeanRoot {
    final static String EJB_JAR_XPATH = "ejb-jar";
    private EjbJarBean ejbJar;

    /**
     * This is present for JavaBeans compliance, but if it is used, the
     * DConfigBean won't be properly associated with a DDBean, so it
     * should be used for experimentation only.
     */
    public EjbJarRoot() {
        super(null);
    }

    public EjbJarRoot(DDBeanRoot ddBean) {
        super(ddBean);
    }

    /**
     * Gets the Standard DD XPaths of interest to this DConfigBean.
     */
    public String[] getXpaths() {
        return new String[] {
            EJB_JAR_XPATH,
        };
    }

    /**
     * Gets a DConfigBean corresponding to an interesting DDBean.  The only
     * interesting DDBean is for /ejb-jar/enterprise-beans (that's the only
     * one returned by getXpaths), so others will result in an exception.
     */
    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        if(getXpath(bean).equals(EJB_JAR_XPATH)) {
            if(ejbJar == null || !ejbJar.getDDBean().equals(bean)) {
                setEjbJar(new EjbJarBean(bean));
            }
            return ejbJar;
        } else {
            throw new ConfigurationException("Unable to generate a DConfigBean for EJB JAR DD XPath "+bean.getXpath());
        }
    }

    public void removeDConfigBean(DConfigBean bean) throws BeanNotFoundException {
        if(bean == ejbJar) {
            setEjbJar(null);
        } else {
            throw new BeanNotFoundException("No such child bean found");
        }
    }

    public void notifyDDChange(XpathEvent event) {
        if(getXpath(event.getBean()).equals(EJB_JAR_XPATH)) {
            if(event.isRemoveEvent()) {
                setEjbJar(null);
            } else if(event.isAddEvent()) {
                setEjbJar(new EjbJarBean(event.getBean()));
            }
        }
    }

    /**
     * Return a DConfigBean for a deployment descriptor that is not the module's
     * primary deployment descriptor.  Currently only EJB JAR DDs are handled.
     *
     * @return A DConfigBean if the DDBeanRoot represented the EJB DD, and null
     *         otherwise (web services, etc.).
     */
    public DConfigBean getDConfigBean(DDBeanRoot ddBeanRoot) {
        if(ddBeanRoot.getFilename().equals("META-INF/ejb-jar.xml")) { // Should not be called for this, but just to be safe...
            return this;
        } else { //todo: handle Web Services DDs
            return null;
        }
    }

    private void setEjbJar(EjbJarBean bean) {
        ejbJar = bean;
    }

    /**
     * Used by other classes in this package to store and restore.  A JSR-88
     * tool implementation should get the child DConfigBeans by calling
     * getDConfigBean for each of the DDBeans.
     */
    EjbJarBean getEjbJar() {
        return ejbJar;
    }
}
