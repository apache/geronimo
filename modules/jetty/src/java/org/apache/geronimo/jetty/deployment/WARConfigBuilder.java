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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.GBeanBuilder;
import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyAttributeType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyGbeanType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyReferenceType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyReferencesType;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.jetty.JettyWebAppType;
import org.apache.geronimo.xbeans.j2ee.WebAppDocument;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/20 07:19:13 $
 */
public class WARConfigBuilder implements ConfigurationBuilder {
    private final Repository repository;
    private final Kernel kernel;

    public WARConfigBuilder(Kernel kernel, Repository repository) {
        this.kernel = kernel;
        this.repository = repository;
    }

    public boolean canConfigure(XmlObject plan) {
        return plan instanceof JettyWebAppDocument || plan instanceof WebAppDocument;
    }

    public SchemaTypeLoader[] getTypeLoaders() {
        return new SchemaTypeLoader[]{XmlBeans.getContextTypeLoader()};
    }

    public XmlObject getDeploymentPlan(URL module) {
        try {
            XmlObject plan = getPlan(new URL(module, "WEB-INF/geronimo-jetty.xml"), JettyWebAppDocument.type);
// todo needs generic web XMLBeans
//            if (plan == null) {
//                plan = getPlan(new URL(module, "WEB-INF/geronimo-web.xml"));
//            }
// todo should be able to deploy a naked WAR
//            if (plan == null) {
//                plan = getPlan(new URL(module, "WEB-INF/web.xml"), WebAppDocument.type);
//            }
            return plan;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private XmlObject getPlan(URL planURL, SchemaType type) {
        InputStream is;
        try {
            is = planURL.openStream();
            try {
                return XmlBeans.getContextTypeLoader().parse(is, type, null);
            } finally {
                is.close();
            }
        } catch (IOException e) {
            return null;
        } catch (XmlException e) {
            return null;
        }
    }

    public void buildConfiguration(File outfile, JarInputStream module, XmlObject plan) throws IOException, DeploymentException {
        JettyWebAppType jettyWebApp = ((JettyWebAppDocument) plan).getWebApp();
        URI configID;
        try {
            configID = new URI(jettyWebApp.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + jettyWebApp.getConfigId(), e);
        }
        URI parentID;
        if (jettyWebApp.isSetParentId()) {
            try {
                parentID = new URI(jettyWebApp.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + jettyWebApp.getParentId(), e);
            }
        } else {
            parentID = null;
        }

        FileOutputStream fos = new FileOutputStream(outfile);
        try {
            JarOutputStream os = new JarOutputStream(new BufferedOutputStream(fos));
            DeploymentContext context = null;
            try {
                context = new DeploymentContext(os, configID, parentID, kernel);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }
//            addIncludes(context, configType);
//            addDependencies(context, configType.getDependencyArray());
            ClassLoader cl = context.getClassLoader(repository);
            addGBeans(context, jettyWebApp, cl);
            context.close();
            os.flush();
        } finally {
            fos.close();
        }
    }

    private void addGBeans(DeploymentContext context, JettyWebAppType webApp, ClassLoader cl) throws DeploymentException {
        JettyGbeanType[] gbeans = webApp.getGbeanArray();
        for (int i = 0; i < gbeans.length; i++) {
            JettyGbeanType gbean = gbeans[i];
            GBeanBuilder builder = new GBeanBuilder(gbean.getName(), cl, gbean.getClass1());

            // set up attributes
            JettyAttributeType[] attrs = gbean.getAttributeArray();
            for (int j = 0; j < attrs.length; j++) {
                JettyAttributeType attr = attrs[j];
                builder.setAttribute(attr.getName(), attr.getType(), attr.getStringValue());
            }

            // set up all single pattern references
            JettyReferenceType[] refs = gbean.getReferenceArray();
            for (int j = 0; j < refs.length; j++) {
                JettyReferenceType ref = refs[j];
                builder.setReference(ref.getName(), ref.getStringValue());
            }

            // set up app multi-patterned references
            JettyReferencesType[] refs2 = gbean.getReferencesArray();
            for (int j = 0; j < refs2.length; j++) {
                JettyReferencesType type = refs2[j];
                builder.setReference(type.getName(), type.getPatternArray());
            }

            context.addGBean(builder.getName(), builder.getGBean());
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ServiceConfigBuilder.class);
        infoFactory.addInterface(ConfigurationBuilder.class);
        infoFactory.addReference(new GReferenceInfo("Repository", Repository.class));
        infoFactory.addReference(new GReferenceInfo("Kernel", Kernel.class));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"Repository", "Kernel"},
                new Class[]{Repository.class, Kernel.class}
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
