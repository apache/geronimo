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
package org.apache.geronimo.deployment.plugin;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.DDBeanRoot;

import org.apache.xmlbeans.XmlException;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/06 08:55:04 $
 */
public abstract class DeploymentConfigurationSupport implements DeploymentConfiguration {
    private final DeployableObject deployable;

    protected DConfigBeanRootSupport dConfigRoot;

    public DeploymentConfigurationSupport(DeployableObject deployable, DConfigBeanRootSupport dConfigRoot) {
        this.deployable = deployable;
        this.dConfigRoot = dConfigRoot;
    }

    public DeployableObject getDeployableObject() {
        return deployable;
    }

    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot bean) throws ConfigurationException {
        if (getDeployableObject().getDDBeanRoot().equals(bean)) {
            return dConfigRoot;
        }
        return null;
    }

    public void removeDConfigBean(DConfigBeanRoot bean) throws BeanNotFoundException {
    }

    public void save(OutputStream outputArchive) throws ConfigurationException {
        try {
            dConfigRoot.toXML(outputArchive);
            outputArchive.flush();
        } catch (IOException e) {
            throw (ConfigurationException) new ConfigurationException("Unable to save configuration").initCause(e);
        }
    }

    public void restore(InputStream inputArchive) throws ConfigurationException {
        try {
            dConfigRoot.fromXML(inputArchive);
        } catch (IOException e) {
            throw (ConfigurationException) new ConfigurationException("Error reading configuration input").initCause(e);
        } catch (XmlException e) {
            throw (ConfigurationException) new ConfigurationException("Error parsing configuration input").initCause(e);
        }
    }

    public void saveDConfigBean(OutputStream outputArchive, DConfigBeanRoot bean) throws ConfigurationException {
        try {
            ((DConfigBeanRootSupport)bean).toXML(outputArchive);
            outputArchive.flush();
        } catch (IOException e) {
            throw (ConfigurationException) new ConfigurationException("Unable to save configuration").initCause(e);
        }
    }

    //todo figure out how to implement this.
    public DConfigBeanRoot restoreDConfigBean(InputStream inputArchive, DDBeanRoot bean) throws ConfigurationException {
        return null;
    }
}
