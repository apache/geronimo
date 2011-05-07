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
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.plan.ArtifactType;
import org.apache.geronimo.deployment.service.plan.EnvironmentType;
import org.apache.geronimo.j2ee.deployment.model.app.ApplicationType;
import org.apache.geronimo.openejb.deployment.model.GeronimoEjbJarType;
import org.apache.openejb.jee.EjbJar;

public final class XmlUtil {
//    public static final QName OPENEJBJAR_QNAME = OpenejbEjbJarDocument.type.getDocumentElementName();

    private XmlUtil() {
    }

    public static final XMLInputFactory XMLINPUT_FACTORY = XMLInputFactory.newInstance();
    private static  JAXBContext GERONIMO_OPENEJB_CONTEXT;
    static {
        try {
            GERONIMO_OPENEJB_CONTEXT = JAXBContext.newInstance(GeronimoEjbJarType.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static GeronimoEjbJarType unmarshalGeronimoEjb(InputStream in, boolean validate) throws XMLStreamException, JAXBException {
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        Unmarshaller unmarshaller = GERONIMO_OPENEJB_CONTEXT.createUnmarshaller();
        JAXBElement<GeronimoEjbJarType> element = unmarshaller.unmarshal(xmlStream, GeronimoEjbJarType.class);
        GeronimoEjbJarType applicationType = element.getValue();
        return applicationType;
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



//    public static Environment buildEnvironment(EnvironmentType environmentType, Environment defaultEnvironment) {
//        Environment environment = new Environment();
//        if (environmentType != null) {
//            if (environmentType.getModuleId() != null) {
//                environment.setConfigId(toArtifact(environmentType.getModuleId(), null));
//            }
//
//            if (environmentType.getDependencies() != null) {
//                for (DependencyType dependencyType : environmentType.getDependencies().getDependency()) {
//                    Dependency dependency = toDependency(dependencyType);
//                    environment.addDependency(dependency);
//                }
//            }
//
//            environment.setSuppressDefaultEnvironment(environmentType.isSuppressDefaultEnvironment());
//
//            ClassLoadingRules classLoadingRules = environment.getClassLoadingRules();
//            classLoadingRules.setInverseClassLoading(environmentType.isInverseClassloading());
//
//            if (environmentType.getHiddenClasses() != null) {
//                ClassLoadingRule hiddenRule = classLoadingRules.getHiddenRule();
//                List<String> filter = environmentType.getHiddenClasses().getFilter();
//                hiddenRule.setClassPrefixes(new HashSet<String>(filter));
//            }
//
//            if (environmentType.getNonOverridableClasses() != null) {
//                ClassLoadingRule nonOverrideableRule = classLoadingRules.getNonOverrideableRule();
//                List<String> filter = environmentType.getNonOverridableClasses().getFilter();
//                nonOverrideableRule.setClassPrefixes(new HashSet<String>(filter));
//            }
//        }
//        if (!environment.isSuppressDefaultEnvironment()) {
//            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
//        }
//
//        return environment;
//    }


//    private static Artifact toArtifact(ArtifactType artifactType, String defaultType) {
//        String groupId = artifactType.getGroupId();
//        String type = artifactType.getType();
//        if (type == null) type = defaultType;
//        String artifactId = artifactType.getArtifactId();
//        String version = artifactType.getVersion();
//        return new Artifact(groupId, artifactId, version, type);
//    }

    public static GeronimoEjbJarType createDefaultPlan(String name, String id) {
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
