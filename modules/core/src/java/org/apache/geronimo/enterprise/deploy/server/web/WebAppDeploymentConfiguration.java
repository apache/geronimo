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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.Reader;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.DDBeanRoot;
import org.w3c.dom.Document;
import org.apache.geronimo.enterprise.deploy.server.ejb.EjbConverter;
import org.apache.geronimo.enterprise.deploy.server.ejb.EjbJarRoot;
import org.apache.geronimo.enterprise.deploy.server.DConfigBeanLookup;
import org.apache.geronimo.xml.deployment.GeronimoEjbJarLoader;
import org.apache.geronimo.xml.deployment.LoaderUtil;
import org.apache.geronimo.xml.deployment.GeronimoEjbJarStorer;
import org.apache.geronimo.xml.deployment.GeronimoWebAppLoader;
import org.apache.geronimo.deployment.model.geronimo.ejb.GeronimoEjbJarDocument;
import org.apache.geronimo.deployment.model.geronimo.web.GeronimoWebAppDocument;
import org.xml.sax.SAXException;

/**
 * The Geronimo implementation of the JSR-88 DeploymentConfiguration.  This is what
 * knows how to load and save server-specific deployment information, and to
 * generate a default set based on the J2EE deployment descriptors.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/07 17:16:36 $
 */
public class WebAppDeploymentConfiguration implements DeploymentConfiguration {
    private DeployableObject webDD;
    private WebAppRoot geronimoDD;
    private DConfigBeanLookup lookup;

    public WebAppDeploymentConfiguration(DeployableObject webDD, WebAppRoot geronimoDD, DConfigBeanLookup lookup) {
        this.webDD = webDD;
        this.geronimoDD = geronimoDD;
        this.lookup = lookup;
    }

    public DeployableObject getDeployableObject() {
        return webDD;
    }

    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot bean) throws ConfigurationException {
        if(bean.equals(webDD.getDDBeanRoot())) {
            return geronimoDD;
        } else {
            throw new ConfigurationException("This DeploymentConfiguration does not handle the DDBeanRoot "+bean);
        }
    }

    public void removeDConfigBean(DConfigBeanRoot bean) throws BeanNotFoundException {
        geronimoDD = null;
    }

    public DConfigBeanRoot restoreDConfigBean(InputStream inputArchive, DDBeanRoot bean) throws ConfigurationException {
        if(!bean.equals(webDD.getDDBeanRoot())) {
            throw new ConfigurationException("This DeploymentConfiguration does not handle the DDBeanRoot "+bean);
        }
        restore(inputArchive);
        return geronimoDD;
    }

    public void saveDConfigBean(OutputStream outputArchive, DConfigBeanRoot bean) throws ConfigurationException {
        if(!bean.equals(geronimoDD)) {
            throw new ConfigurationException("This DeploymentConfiguration does not handle the DDBeanRoot "+bean);
        }
        save(outputArchive);
    }

    public void restore(InputStream inputArchive) throws ConfigurationException {
        Reader reader = new InputStreamReader(inputArchive);
        Document doc = null;
        try {
            doc = LoaderUtil.parseXML(reader);
        } catch (SAXException e) {
            throw new ConfigurationException("Invalid deployment descriptor", e);
        } catch (IOException e) {
            throw new ConfigurationException("Error reading deployment descriptor", e);
        }
        GeronimoWebAppDocument parsed = GeronimoWebAppLoader.load(doc);
        geronimoDD = WebConverter.loadDConfigBeans(parsed.getWebApp(), webDD.getDDBeanRoot(), lookup);
    }

    public void save(OutputStream outputArchive) throws ConfigurationException {
        GeronimoWebAppDocument doc = new GeronimoWebAppDocument();
        doc.setWebApp(WebConverter.storeDConfigBeans(geronimoDD));
        throw new UnsupportedOperationException("Not yet implemented");
//        try {
//            GeronimoWebAppStorer.store(doc, new OutputStreamWriter(outputArchive));
//        } catch(IOException e) {
//            throw new ConfigurationException("Unable to store Geronimo-specific EJB deployment information", e);
//        }
    }
}
