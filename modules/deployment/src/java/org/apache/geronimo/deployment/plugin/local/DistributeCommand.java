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
package org.apache.geronimo.deployment.plugin.local;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.jar.JarOutputStream;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.spi.TargetModuleID;

import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ModuleDeployer;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationParent;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 19:58:16 $
 */
public class DistributeCommand extends CommandSupport {
    private final ConfigurationParent parent;
    private final Kernel kernel;
    private final DeploymentModule module;

    public DistributeCommand(ConfigurationParent parent, Kernel kernel, DeploymentModule module) {
        super(CommandType.DISTRIBUTE);
        this.parent = parent;
        this.kernel = kernel;
        this.module = module;
    }

    public void run() {

        File configFile = null;
        File workDir = null;
        try {
            // create some working space
            configFile = File.createTempFile("deploy", ".car");
            workDir = File.createTempFile("deploy", "");
            workDir.delete();
            workDir.mkdir();

            // convert the module to a Configuration
            TargetModuleID targetID = module.getModuleID();
            URI moduleID = URI.create(targetID.getModuleID());
            ModuleDeployer deployer = new ModuleDeployer(parent, moduleID, workDir);
            deployer.addModule(module);
            deployer.deploy();

            // Save the Configuration into a CAR
            FileOutputStream os = new FileOutputStream(configFile);
            try {
                JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(os));
                deployer.saveConfiguration(jos);
            } finally {
                os.close();
            }

            // install in our local server
            kernel.install(configFile.toURL());

            // load configuration
            kernel.load(moduleID);

            complete();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (workDir != null) {
                FileUtil.recursiveDelete(workDir);
            }
            if (configFile != null) {
                configFile.delete();
            }
        }
    }
}
