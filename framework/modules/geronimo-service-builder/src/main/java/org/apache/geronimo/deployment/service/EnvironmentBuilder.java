/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.deployment.service;

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.geronimo.deployment.service.plan.ArtifactType;
import org.apache.geronimo.deployment.service.plan.EnvironmentType;
import org.apache.geronimo.deployment.service.plan.ObjectFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.w3c.dom.Element;

/**
 * @version $Rev$ $Date$
 */
public class EnvironmentBuilder extends PropertyEditorSupport /*implements XmlAttributeBuilder*/ {
//    private final static QName QNAME = EnvironmentDocument.type.getDocumentElementName();
//    private final static String NAMESPACE = QNAME.getNamespaceURI();

    public static Environment buildEnvironment(EnvironmentType environmentType) {
        Environment environment = new Environment();
        if (environmentType != null) {
                environment.setConfigId(toArtifact(environmentType.getModuleId(), null));
            if (environmentType.getInstructions() != null) {
                for (Element any: environmentType.getInstructions().getAny()) {
                    String name = any.getLocalName();
                    String value = any.getTextContent();

                    //From felix MavenBundlePlugin  BundlePlugin.transformDirectives
                    if ( name.startsWith( "_" ) )
                    {
                        name = "-" + name.substring( 1 );
                    }

                    if ( null == value )
                    {
                        value = "";
                    }
                    else
                    {
                        value = value.replaceAll( "\\p{Blank}*[\r\n]\\p{Blank}*", "" );
                    }

                    environment.getProperties().setProperty(name, value);
                }
            }
        }

        return environment;
    }

    public static void mergeEnvironments(Environment environment, Environment additionalEnvironment) {
        if (additionalEnvironment != null) {
            //TODO merge configIds??
            if (environment.getConfigId() == null) {
                environment.setConfigId(additionalEnvironment.getConfigId());
            }
            environment.addToBundleClassPath(additionalEnvironment.getBundleClassPath());
            environment.addImportPackages(additionalEnvironment.getImportPackages());
            environment.addExportPackages(additionalEnvironment.getExportPackages());
            environment.addRequireBundles(additionalEnvironment.getRequireBundles());
            environment.addDynamicImportPackages(additionalEnvironment.getDynamicImportPackages());
            if (environment.getBundleActivator() == null && additionalEnvironment.getBundleActivator() != null) {
                environment.setBundleActivator(additionalEnvironment.getBundleActivator());
            }
            
        }
    }

    public static Environment buildEnvironment(EnvironmentType environmentType, Environment defaultEnvironment) {
        Environment environment = buildEnvironment(environmentType);
        return environment;
    }

//    public static EnvironmentType buildEnvironmentType(Environment environment) {
//        EnvironmentType environmentType = new ObjectFactory().createEnvironmentType();
//        if (environment.getConfigId() != null) {
//            ArtifactType configId = toArtifactType(environment.getConfigId());
//            environmentType.setModuleId(configId);
//        }
//
//            environmentType.setBundleActivator(environment.getBundleActivator());
//            environmentType.getBundleClassPath().addAll(environment.getBundleClassPath());
//            environmentType.getImportPackage().addAll(environment.getImportPackages());
//            environmentType.getExportPackage().addAll(environment.getExportPackages());
//            environmentType.getRequireBundle().addAll(environment.getRequireBundles());
//            environmentType.getDynamicImportPackage().addAll(environment.getDynamicImportPackages());
//                return environmentType;
//    }

    private static ArtifactType toArtifactType(Artifact artifact) {
        ArtifactType artifactType = new ObjectFactory().createArtifactType();
        fillArtifactType(artifact, artifactType);
        return artifactType;
    }

    private static void fillArtifactType(Artifact artifact, ArtifactType artifactType) {
        if (artifact.getGroupId() != null) {
            artifactType.setGroupId(artifact.getGroupId());
        }
        if (artifact.getArtifactId() != null) {
            artifactType.setArtifactId(artifact.getArtifactId());
        }
        if (artifact.getVersion() != null) {
            artifactType.setVersion(artifact.getVersion().toString());
        }
        if (artifact.getType() != null) {
            artifactType.setType(artifact.getType());
        }
    }

    //package level for testing
    static LinkedHashSet toArtifacts(ArtifactType[] artifactTypes) {
        LinkedHashSet artifacts = new LinkedHashSet();
        for (int i = 0; i < artifactTypes.length; i++) {
            ArtifactType artifactType = artifactTypes[i];
            Artifact artifact = toArtifact(artifactType, "jar");
            artifacts.add(artifact);
        }
        return artifacts;
    }

    private static Artifact toArtifact(ArtifactType artifactType, String defaultType) {
        String groupId = trim(artifactType.getGroupId());
        String type = trim(artifactType.getType());
        String artifactId = trim(artifactType.getArtifactId());
        String version = trim(artifactType.getVersion());
        return new Artifact(groupId, artifactId, version, type);
    }

    private static String trim(String string) {
        if (string == null || string.length() == 0) {
        return null;
        }
        return string.trim();
    }


    public String getNamespace() {
        return null;//NAMESPACE;
    }

//    public Object getValue(XmlObject xmlObject, XmlObject enclosing, String type, Bundle bundle) throws DeploymentException {
//
//        EnvironmentType environmentType;
//        if (xmlObject instanceof EnvironmentType) {
//            environmentType = (EnvironmentType) xmlObject;
//        } else {
//            environmentType = (EnvironmentType) xmlObject.copy().changeType(EnvironmentType.type);
//        }
//        try {
//            XmlOptions xmlOptions = new XmlOptions();
//            xmlOptions.setLoadLineNumbers();
//            Collection errors = new ArrayList();
//            xmlOptions.setErrorListener(errors);
//            if (!environmentType.validate(xmlOptions)) {
//                throw new XmlException("Invalid deployment descriptor: " + errors + "\nDescriptor: " + environmentType.toString(), null, errors);
//            }
//        } catch (XmlException e) {
//            throw new DeploymentException(e);
//        }
//
//        return buildEnvironment(environmentType);
//    }

//    public String getAsText() {
//        Environment environment = (Environment) getValue();
//        EnvironmentType environmentType = buildEnvironmentType(environment);
//        XmlOptions xmlOptions = new XmlOptions();
//        xmlOptions.setSaveSyntheticDocumentElement(QNAME);
//        xmlOptions.setSavePrettyPrint();
//        return environmentType.xmlText(xmlOptions);
//    }

//    public void setAsText(String text) {
//        try {
//            EnvironmentDocument environmentDocument = EnvironmentDocument.Factory.parse(text);
//            EnvironmentType environmentType = environmentDocument.getEnvironment();
//            Environment environment = (Environment) getValue(environmentType, null, null, null);
//            setValue(environment);
//
//        } catch (XmlException e) {
//            throw new PropertyEditorException(e);
//        } catch (DeploymentException e) {
//            throw new PropertyEditorException(e);
//        }
//    }


    //This is added by hand to the xmlAttributeBuilders since it is needed to bootstrap the ServiceConfigBuilder.
}
