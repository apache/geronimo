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
package org.apache.geronimo.deployment.service;

import java.beans.PropertyEditor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.JarInputStream;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.ReferencesType;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/19 01:51:44 $
 */
public class ServiceConfigBuilder implements ConfigurationBuilder {
    private final Repository repository;
    private final Kernel kernel;

    public ServiceConfigBuilder(Repository repository, Kernel kernel) {
        this.repository = repository;
        this.kernel = kernel;
    }

    public boolean canConfigure(XmlObject plan) {
        return plan instanceof ConfigurationDocument;
    }

    public XmlObject getDeploymentPlan(URL module) {
        return null;
    }

    public void buildConfiguration(File outfile, JarInputStream module, XmlObject plan, boolean install) throws IOException, DeploymentException {
        ConfigurationType configType = ((ConfigurationDocument) plan).getConfiguration();
        URI configID;
        try {
            configID = new URI(configType.getConfigId());
        } catch (URISyntaxException e) {
            throw new DeploymentException("Invalid configId " + configType.getConfigId(), e);
        }
        URI parentID;
        if (configType.isSetParentId()) {
            try {
                parentID = new URI(configType.getParentId());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid parentId " + configType.getParentId(), e);
            }
        } else {
            parentID = null;
        }

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
        // @todo support attributes in plan to make CARfile executable

        FileOutputStream fos = new FileOutputStream(outfile);
        try {
            JarOutputStream os = new JarOutputStream(new BufferedOutputStream(fos), manifest);
            DeploymentContext context = null;
            try {
                context = new DeploymentContext(os, configID, parentID, kernel);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
            }
            addIncludes(context, configType);
            addDependencies(context, configType.getDependencyArray());
            ClassLoader cl = context.getClassLoader(repository);
            addGBeans(context, configType, cl);
            context.close();
            os.flush();
        } finally {
            fos.close();
        }

        try {
            if (install) {
                kernel.install(outfile.toURL());
            }
        } catch (InvalidConfigException e) {
            // unlikely as we just built this
            throw new DeploymentException(e);
        }
    }

    private void addIncludes(DeploymentContext context, ConfigurationType configType) throws DeploymentException {
        DependencyType[] includes = configType.getIncludeArray();
        for (int i = 0; i < includes.length; i++) {
            DependencyType include = includes[i];
            URI uri = getDependencyURI(include);
            String name = uri.toString();
            int idx = name.lastIndexOf('/');
            if (idx != -1) {
                name = name.substring(idx + 1);
            }
            URI path;
            try {
                path = new URI(name);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to generate path for include: " + uri, e);
            }
            try {
                URL url = repository.getURL(uri);
                context.addInclude(path, url);
            } catch (IOException e) {
                throw new DeploymentException("Unable to add include: " + uri, e);
            }
        }
    }

    private void addDependencies(DeploymentContext context, DependencyType[] deps) throws DeploymentException {
        for (int i = 0; i < deps.length; i++) {
            URI dependencyURI = getDependencyURI(deps[i]);
            context.addDependency(dependencyURI);

            URL url;
            try {
                url = repository.getURL(dependencyURI);
            } catch (MalformedURLException e) {
                throw new DeploymentException("Unable to get URL for dependency " + dependencyURI, e);
            }
            ClassLoader depCL = new URLClassLoader(new URL[]{url}, ClassLoader.getSystemClassLoader());
            InputStream is = depCL.getResourceAsStream("META-INF/geronimo-service.xml");
            if (is != null) {
                // it has a geronimo-service.xml file
                ServiceDocument serviceDoc = null;
                try {
                    serviceDoc = ServiceDocument.Factory.parse(is);
                } catch (org.apache.xmlbeans.XmlException e) {
                    throw new DeploymentException("Invalid geronimo-service.xml file in " + url, e);
                } catch (IOException e) {
                    throw new DeploymentException("Unable to parse geronimo-service.xml file in " + url, e);
                }
                DependencyType[] dependencyDeps = serviceDoc.getService().getDependencyArray();
                if (dependencyDeps != null) {
                    addDependencies(context, dependencyDeps);
                }
            }
        }
    }

    private URI getDependencyURI(DependencyType dep) throws DeploymentException {
        URI uri;
        if (dep.isSetUri()) {
            try {
                uri = new URI(dep.getUri());
            } catch (URISyntaxException e) {
                throw new DeploymentException("Invalid dependency URI " + dep.getUri(), e);
            }
        } else {
            // @todo support more than just jars
            String id = dep.getGroupId() + "/jars/" + dep.getArtifactId() + '-' + dep.getVersion() + ".jar";
            try {
                uri = new URI(id);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Unable to construct URI for groupId=" + dep.getGroupId() + ", artifactId=" + dep.getArtifactId() + ", version=" + dep.getVersion(), e);
            }
        }
        return uri;
    }

    private void addGBeans(DeploymentContext context, ConfigurationType configType, ClassLoader cl) throws DeploymentException {
        GbeanType[] gbeans = configType.getGbeanArray();
        for (int i = 0; i < gbeans.length; i++) {
            GbeanType gbean = gbeans[i];
            ObjectName name;
            try {
                name = new ObjectName(gbean.getName());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Invalid ObjectName " + gbean.getName(), e);
            }
            String className = gbean.getClass1();
            GBeanMBean mbean;
            try {
                mbean = new GBeanMBean(className, cl);
            } catch (Exception e) {
                throw new DeploymentException("Unable to create GBean from class " + className, e);
            }

            // set up attributes
            AttributeType[] attrs = gbean.getAttributeArray();
            for (int j = 0; j < attrs.length; j++) {
                AttributeType attr = attrs[j];
                String attrName = attr.getName();
                String type = attr.getType();
                Object value = attr.getStringValue();
                try {
                    // @todo we should not need all of common just for this
                    PropertyEditor editor = PropertyEditors.findEditor(type);
                    if (editor != null) {
                        editor.setAsText((String) value);
                        value = editor.getValue();
                    }
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException("Unable to find PropertyEditor for " + type, e);
                }
                try {
                    mbean.setAttribute(attrName, value);
                } catch (Exception e) {
                    throw new DeploymentException("Unable to set attribute " + attrName, e);
                }
            }

            // set up all single pattern references
            ReferenceType[] refs = gbean.getReferenceArray();
            for (int j = 0; j < refs.length; j++) {
                ReferenceType ref = refs[j];
                String refName = ref.getName();
                String pattern = ref.getStringValue();
                try {
                    mbean.setReferencePatterns(refName, Collections.singleton(new ObjectName(pattern)));
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Invalid pattern for reference " + refName, e);
                }
            }

            // set up app multi-patterned references
            ReferencesType[] refs2 = gbean.getReferencesArray();
            for (int j = 0; j < refs2.length; j++) {
                ReferencesType type = refs2[j];
                String refName = type.getName();
                String[] patterns = type.getPatternArray();
                Set patternNames = new HashSet(patterns.length);
                for (int k = 0; k < patterns.length; k++) {
                    try {
                        patternNames.add(new ObjectName(patterns[k]));
                    } catch (MalformedObjectNameException e) {
                        throw new DeploymentException("Invalid pattern for reference " + refName + " : " + patterns[k], e);
                    }
                }
                mbean.setReferencePatterns(refName, patternNames);
            }

            context.addGBean(name, mbean);
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
