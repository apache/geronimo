/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.car;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.namespace.QName;
import org.apache.geronimo.deployment.service.plan.ArtifactType;
import org.apache.geronimo.deployment.service.plan.EnvironmentType;
import org.apache.geronimo.deployment.service.plan.JaxbUtil;
import org.apache.geronimo.deployment.service.plan.ModuleType;
import org.apache.geronimo.deployment.service.plan.ObjectFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

//
// TODO: Rename to PreparePlanMojo
//

/**
 * Add module id and dependencies to a plan and process with velocity
 *
 * @version $Rev$ $Date$
 * @goal prepare-plan
 * @requiresDependencyResolution runtime
 */
public class PlanProcessorMojo
        extends AbstractCarMojo {
    private static final String ENVIRONMENT_LOCAL_NAME = "environment";

    private static final QName ENVIRONMENT_QNAME = new QName("http://geronimo.apache.org/xml/ns/deployment-1.2", "environment");

    /**
     * Location of unproccesed plan, normally missing moduleId and dependencies.
     *
     * @parameter expression="${basedir}/src/main/plan/plan.xml"
     * @required
     */
    protected File sourceFile = null;

    /**
     * Directory to put the processed plan in.
     *
     * @parameter expression="${project.build.directory}/work"
     * @required
     */
    protected File targetDir = null;

    /**
     * XXX
     *
     * @parameter expression="${project.build.directory}/work/plan.xml"
     * @required
     */
    protected File targetFile = null;

    /**
     * we copy the plan here for filtering, then add env stuff.
     *
     * @parameter expression="${project.build.directory}/work/filteredplan.xml"
     * @required
     */
    protected File filteredPlanFile;

    /**
     * whether this is a boot bundle (starts kernel, and the config) or a normal plugin bundle.
     * @parameter
     */
    private boolean boot;


    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!sourceFile.exists()) {
            getLog().info("No plan found, plugin will have no classloader");
            return;
        }
        try {
            filter(sourceFile, filteredPlanFile);

            InputStream in = new FileInputStream(filteredPlanFile);
            ModuleType moduleType;
            try {
                moduleType = JaxbUtil.unmarshalModule(in, false);
            } finally {
                in.close();
            }
            EnvironmentType environmentType = moduleType.getEnvironment();
            if (environmentType == null) {
                environmentType = new ObjectFactory().createEnvironmentType();
                moduleType.setEnvironment(environmentType);
            }
            ArtifactType artifactType = new ObjectFactory().createArtifactType();
            artifactType.setGroupId(project.getGroupId());
            artifactType.setArtifactId(project.getArtifactId());
            artifactType.setVersion(project.getVersion());
            artifactType.setType("car");
            environmentType.setModuleId(artifactType);



                if (targetDir.exists()) {
                    if (!targetDir.isDirectory()) {
                        throw new RuntimeException("TargetDir: " + this.targetDir + " exists and is not a directory");
                    }
                } else {
                    targetDir.mkdirs();
                }

                Writer out = new FileWriter(targetFile);
            try {
                JaxbUtil.marshal(ModuleType.class, moduleType, out);
            } finally {
                out.close();
            }

            if (getLog() != null) {
                    getLog().info("Generated: " + targetFile);
                }
        } catch (Exception e) {
            throw new MojoExecutionException("Could not process plan", e);
        }
    }

}
