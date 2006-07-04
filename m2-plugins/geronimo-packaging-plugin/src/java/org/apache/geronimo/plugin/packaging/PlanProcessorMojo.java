/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.plugin.packaging;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.model.Dependency;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * Add dependencies to a plan and process with velocity 
 * 
 * @goal dependencies
 * 
 * @version $Rev$ $Date$
 */
public class PlanProcessorMojo extends AbstractPackagingMojo {
	private static Log log = LogFactory.getLog(PlanProcessorMojo.class);

	private static final String IMPORT_PROPERTY = "geronimo.import";
	//private static final String INCLUDE_PROPERTY = "geronimo.include";
	private static final String DEPENDENCY_PROPERTY = "geronimo.dependency";
	private static final String KEEP_VERSION_PROPERTY = "geronimo.keep.version";
    private static final String REFERENCE_PROPERTY = "geronimo.reference";
    private static final String ENVIRONMENT_LOCAL_NAME = "environment";

	private static final QName ENVIRONMENT_QNAME = new QName(
			"http://geronimo.apache.org/xml/ns/deployment-1.1", "environment");
	/**
	 * POM
	 * @parameter expression="${project}"
	 * @readonly
	 * @required
     */
	private MavenProject project;
	/**
     * @parameter expression="${basedir}/src/plan"
     */
	private String sourceDir;
	/**
     * @parameter expression="${project.build.directory}/plan"
     */
	private String targetDir;
	/**
     * @parameter expression="plan.xml"
     */
	private String planFile;
	/**
     * @parameter expression="${project.build.directory}/plan/plan.xml"
     */
	private String targetFile;
    
	private VelocityContext context;
	
	//private List artifacts;
	
	// This is needed for ${pom.currentVersion} and will be removed when 
	// we move to a full m2 build
	private VelocityContext createContext() {
		VelocityContext context = new VelocityContext();
		Map pom = new HashMap();
		pom.put("groupId", project.getGroupId());
		pom.put( "artifactId", project.getArtifactId());
		pom.put("currentVersion", project.getVersion());
		context.put("pom", pom);
        // Load properties, It inherits them all!
        Properties props = project.getProperties();        
        for (Iterator iter = props.keySet().iterator(); iter.hasNext();) {
            String key = (String)iter.next();
            //System.out.println("**** key = " + key + "value = " + props.getProperty(key));
            context.put(key, props.getProperty(key));
        }
        
		return context;
	}

	public void execute() throws MojoExecutionException {

		context = createContext();
		try {
			executePlanProcessor();
		} catch (Exception e) {
			handleError(e);
		}
	}

	public void executePlanProcessor() throws Exception, XmlException {
		try {
			if (project == null) {
				throw new RuntimeException("project not supplied");
			}
			if (targetDir == null) {
				throw new RuntimeException("No target directory supplied");
			}
			if (planFile == null) {
				throw new RuntimeException("No source plan supplied");
			}
			if (targetFile == null) {
				throw new RuntimeException("No target plan supplied");
			}

			File sourceD = new File(sourceDir);
			VelocityEngine velocity = new VelocityEngine();
			velocity.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH,
					sourceD.getAbsolutePath());
			velocity.init();
			Template template = velocity.getTemplate(planFile);
			StringWriter writer = new StringWriter();
			template.merge(context, writer);
			
			String plan = writer.toString();
			
			XmlObject doc = XmlObject.Factory.parse(plan);
			XmlCursor xmlCursor = doc.newCursor();
			LinkedHashSet dependencies = toDependencies();
            org.apache.geronimo.kernel.repository.Artifact configId = new org.apache.geronimo.kernel.repository.Artifact(
					project.getGroupId(), project.getArtifactId(), project.getVersion(), "car");
            try {

                mergeEnvironment(xmlCursor, configId, dependencies);
				File targetDir = new File(this.targetDir);
				if (targetDir.exists()) {
					if (!targetDir.isDirectory()) {
						throw new RuntimeException("TargetDir: "
								+ this.targetDir
								+ " exists and is not a directory");
					}
				} else {
					targetDir.mkdirs();
				}
				File output = new File(targetFile);
				XmlOptions xmlOptions = new XmlOptions();
				xmlOptions.setSavePrettyPrint();
				doc.save(output, xmlOptions);
			} finally {
				xmlCursor.dispose();
			}
		} catch (Exception e) {
			log.error(e.getClass().getName() + ": " + e.getMessage(), e);
			throw e;
		}
	}

    void mergeEnvironment(XmlCursor xmlCursor, org.apache.geronimo.kernel.repository.Artifact configId, LinkedHashSet dependencies) {
        xmlCursor.toFirstContentToken();
        xmlCursor.toFirstChild();
        xmlCursor.insertComment("Processed by geronimo m2 packaging plugin");
        QName childName = xmlCursor.getName();
        Environment oldEnvironment;
        if (childName != null && childName.getLocalPart().equals(ENVIRONMENT_LOCAL_NAME)) {
            convertElement(xmlCursor, ENVIRONMENT_QNAME.getNamespaceURI());
            XmlObject xmlObject = xmlCursor.getObject();
            EnvironmentType environmentType = (EnvironmentType) xmlObject.copy().changeType(EnvironmentType.type);
            oldEnvironment = EnvironmentBuilder.buildEnvironment(environmentType);
            xmlCursor.removeXml();
        } else {
            oldEnvironment = new Environment();
        }


        Environment newEnvironment = new Environment();
        newEnvironment.setConfigId(configId);
        newEnvironment.setDependencies(dependencies);

        EnvironmentBuilder.mergeEnvironments(oldEnvironment, newEnvironment);
        EnvironmentType environmentType = EnvironmentBuilder.buildEnvironmentType(oldEnvironment);

        xmlCursor.beginElement(ENVIRONMENT_QNAME);
        XmlCursor element = environmentType.newCursor();
        try {
            element.copyXmlContents(xmlCursor);
        } finally {
            element.dispose();
        }
    }

    private void convertElement(XmlCursor cursor, String namespace) {
        cursor.push();
        XmlCursor end = cursor.newCursor();
        try {
            end.toCursor(cursor);
            end.toEndToken();
            while (cursor.hasNextToken() && cursor.isLeftOf(end)) {
                if (cursor.isStart()) {
                    if (!namespace.equals(cursor.getName().getNamespaceURI())) {
                        cursor.setName(new QName(namespace, cursor.getName().getLocalPart()));
                    }
                }
                cursor.toNextToken();
            }
            cursor.pop();
        } finally {
            end.dispose();
        }
    }

    private LinkedHashSet toDependencies() {
    	List artifacts = project.getDependencies();
        LinkedHashSet dependencies = new LinkedHashSet();
        for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
            //Artifact artifact = (Artifact) iterator.next();
            Dependency dependency = (Dependency) iterator.next();
            //Dependency dependency = artifact.getDependency();
            org.apache.geronimo.kernel.repository.Dependency geronimoDependency = toGeronimoDependency(dependency);
            if (geronimoDependency != null) {
                dependencies.add(geronimoDependency);
            }
        }
        return dependencies;
    }

    private static org.apache.geronimo.kernel.repository.Dependency toGeronimoDependency(Dependency dependency) {    	
        org.apache.geronimo.kernel.repository.Artifact artifact = toGeronimoArtifact(dependency);
        String type = dependency.getType();
        String scope = dependency.getScope();
        String groupId = dependency.getGroupId();
        //!"org.apache.geronimo.specs".equals(groupId) jacc spec needed in plan.xml
        if ("jar".equalsIgnoreCase(type)&& !"junit".equals(groupId)) {
            if (dependency.getVersion() != null) {
                artifact = new org.apache.geronimo.kernel.repository.Artifact(artifact.getGroupId(), artifact.getArtifactId(), dependency.getVersion(), artifact.getType());
            }
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.CLASSES);
//        } else if ("true".equals(dependency.getProperty(REFERENCE_PROPERTY))) {
        } else if ("car".equalsIgnoreCase(type)&& ("runtime").equalsIgnoreCase(type)) {    
           return new org.apache.geronimo.kernel.repository.Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.SERVICES);
        } else if ("car".equalsIgnoreCase(type)&& ("compile".equalsIgnoreCase(scope))) {
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.CLASSES);  
        } else if ("car".equalsIgnoreCase(type)&& (scope == null)) { //parent
            return new org.apache.geronimo.kernel.repository.Dependency(artifact, org.apache.geronimo.kernel.repository.ImportType.ALL);
        
        } else {
            // not one of ours
            return null;
        }
    }

    private static org.apache.geronimo.kernel.repository.Artifact toGeronimoArtifact(Dependency dependency) {
        String groupId = dependency.getGroupId();
        String artifactId = dependency.getArtifactId();
        String version = null;
        String type = dependency.getType();
        return new org.apache.geronimo.kernel.repository.Artifact(groupId, artifactId, version, type);
    }

    interface Inserter {
        ArtifactType insert(EnvironmentType environmentType);
    }


}
