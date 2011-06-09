/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb.deployment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ClassLoadingRule;
import org.apache.geronimo.kernel.repository.ClassLoadingRules;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbEjbJarDocument;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.oejb2.ArtifactType;
import org.apache.openejb.jee.oejb2.DependencyType;
import org.apache.openejb.jee.oejb2.EnvironmentType;
import org.apache.openejb.jee.oejb2.GeronimoEjbJarType;
import org.apache.openejb.jee.oejb2.ImportType;
import org.apache.xmlbeans.XmlObject;

public final class XmlUtil {
    public static final QName OPENEJBJAR_QNAME = OpenejbEjbJarDocument.type.getDocumentElementName();
    private static final QName CMP_VERSION = new QName(SchemaConversionUtils.J2EE_NAMESPACE, "cmp-version");

    private XmlUtil() {
    }

    public static <T> String marshal(T object) throws DeploymentException {
        try {
            Class type = object.getClass();

            if (object instanceof JAXBElement) {
                JAXBElement element = (JAXBElement) object;
                type = element.getValue().getClass();
            }

            JAXBContext ctx = JAXBContext.newInstance(type);
            Marshaller marshaller = ctx.createMarshaller();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(object, baos);

            String xml = new String(baos.toByteArray());
            return xml;
        } catch (JAXBException e) {
            throw new DeploymentException(e);
        }
    }


    public static OpenejbGeronimoEjbJarType convertToXmlbeans(GeronimoEjbJarType geronimoEjbJarType) throws DeploymentException {
        //
        // it would be nice if Jaxb had a way to convert the object to a
        // sax reader that could be fed directly into xmlbeans
        //
        JAXBElement root = new JAXBElement(new QName("http://geronimo.apache.org/xml/ns/j2ee/ejb/openejb-2.0","ejb-jar"), GeronimoEjbJarType.class, geronimoEjbJarType);

        // marshal to xml

        String xml = marshal(root);
        try {
            XmlObject xmlObject = XmlBeansUtil.parse(xml);
            OpenejbGeronimoEjbJarType geronimoOpenejb = (OpenejbGeronimoEjbJarType) SchemaConversionUtils.fixGeronimoSchema(xmlObject, OPENEJBJAR_QNAME, OpenejbGeronimoEjbJarType.type);
            return geronimoOpenejb;
        } catch (Throwable e) {
            String filePath = "<error: could not be written>";
            FileOutputStream out = null;
            try {
                File tempFile = File.createTempFile("openejb-jar-", ".xml");
                tempFile.deleteOnExit();
                out = new FileOutputStream(tempFile);
                out.write(xml.getBytes());
                filePath = tempFile.getAbsolutePath();
            } catch (Exception notImportant) {
            } finally {
                IOUtils.close(out);
            }
            throw new DeploymentException("Error parsing geronimo-openejb.xml with xmlbeans.  For debug purposes, XML content written to: " + filePath, e);
        }
    }

    public static Environment buildEnvironment(EnvironmentType environmentType, Environment defaultEnvironment) {
        Environment environment = new Environment();
        if (environmentType != null) {
            if (environmentType.getModuleId() != null) {
                environment.setConfigId(toArtifact(environmentType.getModuleId(), null));
            }

            if (environmentType.getDependencies() != null) {
                for (DependencyType dependencyType : environmentType.getDependencies().getDependency()) {
                    Dependency dependency = toDependency(dependencyType);
                    environment.addDependency(dependency);
                }
            }
            
            environment.setBundleActivator(environmentType.getBundleActivator());
            environment.addToBundleClassPath(environmentType.getBundleClassPath());
            environment.addRequireBundles(environmentType.getRequireBundle());
            environment.addExportPackages(environmentType.getExportPackage());
            environment.addImportPackages(environmentType.getImportPackage());
            environment.addDynamicImportPackages(environmentType.getDynamicImportPackage());


            environment.setSuppressDefaultEnvironment(environmentType.isSuppressDefaultEnvironment());

            ClassLoadingRules classLoadingRules = environment.getClassLoadingRules();
            classLoadingRules.setInverseClassLoading(environmentType.isInverseClassloading());

            if (environmentType.getHiddenClasses() != null) {
                ClassLoadingRule hiddenRule = classLoadingRules.getHiddenRule();
                List<String> filter = environmentType.getHiddenClasses().getFilter();
                hiddenRule.setClassPrefixes(new HashSet<String>(filter));
            }

            if (environmentType.getNonOverridableClasses() != null) {
                ClassLoadingRule nonOverrideableRule = classLoadingRules.getNonOverrideableRule();
                List<String> filter = environmentType.getNonOverridableClasses().getFilter();
                nonOverrideableRule.setClassPrefixes(new HashSet<String>(filter));
            }
        }
        if (!environment.isSuppressDefaultEnvironment()) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        }

        return environment;
    }

    private static Dependency toDependency(DependencyType dependencyType) {
        Artifact artifact = toArtifact(dependencyType, null);
        if (ImportType.CLASSES.equals(dependencyType.getImport())) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.CLASSES);
        } else if (ImportType.SERVICES.equals(dependencyType.getImport())) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.SERVICES);
        } else if (dependencyType.getImport() == null) {
            return new Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.ALL);
        } else {
            throw new IllegalArgumentException("Unknown import type: " + dependencyType.getImport());
        }
    }

    private static Artifact toArtifact(ArtifactType artifactType, String defaultType) {
        String groupId = artifactType.getGroupId();
        String type = artifactType.getType();
        if (type == null) type = defaultType;
        String artifactId = artifactType.getArtifactId();
        String version = artifactType.getVersion();
        return new Artifact(groupId, artifactId, version, type);
    }

    public static GeronimoEjbJarType createDefaultPlan(String name, EjbJar ejbJar) {
        String id = ejbJar.getId();
        if (id == null) {
            id = name;
            if (id.endsWith(".jar")) {
                id = id.substring(0, id.length() - 4);
            }
            if (id.endsWith("/")) {
                id = id.substring(0, id.length() - 1);
            }
        }


        ArtifactType artifactType = new ArtifactType();
        artifactType.setArtifactId(id);

        EnvironmentType environmentType = new EnvironmentType();
        environmentType.setModuleId(artifactType);

        GeronimoEjbJarType geronimoEjbJarType = new GeronimoEjbJarType();
        geronimoEjbJarType.setEnvironment(environmentType);

        return geronimoEjbJarType;
    }

}
