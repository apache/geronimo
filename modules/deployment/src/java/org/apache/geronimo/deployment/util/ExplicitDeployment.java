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

package org.apache.geronimo.deployment.util;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.management.ObjectName;

import org.apache.geronimo.deployment.URLDeployer;
import org.apache.geronimo.deployment.BatchDeployerFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.kernel.KernelMBean;
import org.apache.geronimo.kernel.config.ConfigurationParent;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.deployment.util.URLType;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/01/25 21:07:03 $
 *
 * */
public class ExplicitDeployment implements GBean {

    private final static GBeanInfo GBEAN_INFO;

    private ConfigurationParent configurationParent;

    private URI configID;

    private URL packageURL;

    private BatchDeployerFactory batchDeployerFactory;

    private KernelMBean kernel;
    private ObjectName configName;

    public ExplicitDeployment(URI configID, URL packageURL, ConfigurationParent configurationParent, BatchDeployerFactory batchDeployerFactory, KernelMBean kernel) {
        this.configID = configID;
        this.packageURL = packageURL;
        this.configurationParent = configurationParent;
        this.batchDeployerFactory = batchDeployerFactory;
        this.kernel = kernel;
    }

    public ConfigurationParent getConfigurationParent() {
        return configurationParent;
    }

    public void setConfigurationParent(ConfigurationParent configurationParent) {
        this.configurationParent = configurationParent;
    }

    public URI getConfigID() {
        return configID;
    }

    public void setConfigID(URI configID) {
        this.configID = configID;
    }

    public URL getPackageURL() {
        return packageURL;
    }

    public void setPackageURL(URL packageURL) {
        this.packageURL = packageURL;
    }

    public BatchDeployerFactory getBatchDeployerFactory() {
        return batchDeployerFactory;
    }

    public void setBatchDeployerFactory(BatchDeployerFactory batchDeployerFactory) {
        this.batchDeployerFactory = batchDeployerFactory;
    }

    public KernelMBean getKernel() {
        return kernel;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        File workDir = batchDeployerFactory.createWorkDir();
        URLDeployer batchDeployer = batchDeployerFactory.getBatchDeployer(configurationParent, configID, workDir);
        batchDeployer.addSource(new URLInfo(packageURL, URLType.getType(packageURL)));
        batchDeployer.deploy();
        configName = kernel.load(batchDeployer.getConfiguration(), workDir.toURL());
        kernel.getMBeanServer().invoke(configName, "startRecursive", null, null);
    }

    public void doStop() {
        try {
            kernel.getMBeanServer().invoke(configName,  "stopRecursive", null, null);
        } catch (Exception e) {
            //log.info(e);
        }
        try {
            kernel.unload(configName);
        } catch (NoSuchConfigException e) {
            //log.info(e);
        }
        configName = null;
    }

    public void doFail() {
        if (configName != null) {
            doStop();
        }
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ExplicitDeployment.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("ConfigID", true));
        infoFactory.addAttribute(new GAttributeInfo("PackageURL", true));
        infoFactory.addReference(new GReferenceInfo("ConfigurationParent", ConfigurationParent.class.getName()));
        infoFactory.addReference(new GReferenceInfo("BatchDeployerFactory", BatchDeployerFactory.class.getName()));
        infoFactory.addReference(new GReferenceInfo("Kernel", KernelMBean.class.getName()));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[] {"ConfigID", "PackageURL", "ConfigurationParent", "BatchDeployerFactory", "Kernel"},
                new Class[] {URI.class, URL.class, ConfigurationParent.class, BatchDeployerFactory.class, KernelMBean.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
