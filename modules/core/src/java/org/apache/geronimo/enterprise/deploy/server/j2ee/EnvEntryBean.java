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
package org.apache.geronimo.enterprise.deploy.server.j2ee;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import org.apache.geronimo.enterprise.deploy.server.BaseDConfigBean;
import org.apache.geronimo.enterprise.deploy.server.DConfigBeanLookup;

/**
 * The DConfigBean representing /ejb-jar/enterprise-beans/.../env-entry
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/06 14:35:34 $
 */
public class EnvEntryBean extends BaseDConfigBean {
    public final static String ENV_ENTRY_NAME_XPATH = "env-entry-name";
    public final static String ENV_ENTRY_VALUE_XPATH = "env-entry-value";
    private String envEntryName;
    private String envEntryValue;

    /**
     * This is present for JavaBeans compliance, but if it is used, the
     * DConfigBean won't be properly associated with a DDBean, so it
     * should be used for experimentation only.
     */
    public EnvEntryBean() {
        super(null, null);
        envEntryName = "";
        envEntryValue = "";
    }

    public EnvEntryBean(DDBean ddBean, DConfigBeanLookup lookup) {
        super(ddBean, lookup);
        envEntryName = ddBean.getText(ENV_ENTRY_NAME_XPATH)[0];
        ddBean.addXpathListener(ENV_ENTRY_NAME_XPATH, new XpathListener() {
            public void fireXpathEvent(XpathEvent xpe) {
                if(xpe.isChangeEvent() || xpe.isAddEvent()) {
                    setEnvEntryName(xpe.getBean().getText());
                } else if(xpe.isRemoveEvent()) {
                    setEnvEntryName(null);
                }
            }
        });
        envEntryValue = ddBean.getText(ENV_ENTRY_VALUE_XPATH)[0];
        ddBean.addXpathListener(ENV_ENTRY_VALUE_XPATH, new XpathListener() {
            public void fireXpathEvent(XpathEvent xpe) {
                if(xpe.isChangeEvent() || xpe.isAddEvent()) {
                    setEnvEntryValue(xpe.getBean().getText());
                } else if(xpe.isRemoveEvent()) {
                    setEnvEntryValue(null);
                }
            }
        });
    }

    public String[] getXpaths() {
        return new String[0];
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        throw new ConfigurationException("This DConfigBean does not support children.");
    }

    public void removeDConfigBean(DConfigBean bean) throws BeanNotFoundException {
        throw new BeanNotFoundException("This DConfigBean does not support children.");
    }

    public void notifyDDChange(XpathEvent event) {
    }

    public String getEnvEntryName() {
        return envEntryName;
    }

    public void setEnvEntryName(String envEntryName) {
        String old = this.envEntryName;
        this.envEntryName = envEntryName;
        pcs.firePropertyChange("envEntryName", old, this.envEntryName);
    }

    public String getEnvEntryValue() {
        return envEntryValue;
    }

    public void setEnvEntryValue(String envEntryValue) {
        String old = this.envEntryValue;
        this.envEntryValue = envEntryValue;
        pcs.firePropertyChange("envEntryValue", old, this.envEntryValue);
    }

    public String toString() {
        return envEntryName;
    }
}
