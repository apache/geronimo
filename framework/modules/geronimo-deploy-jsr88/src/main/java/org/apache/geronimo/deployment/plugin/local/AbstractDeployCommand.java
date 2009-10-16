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
package org.apache.geronimo.deployment.plugin.local;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractDeployCommand extends CommandSupport {
    protected final Kernel kernel;
    private static final String[] DEPLOY_SIG = {boolean.class.getName(), File.class.getName(), File.class.getName(), String.class.getName()};
    protected final boolean spool;
    protected File moduleArchive;
    protected File deploymentPlan;
    protected final ModuleType moduleType;
    protected InputStream moduleStream;
    protected InputStream deploymentStream;
    protected AbstractName deployer;

    public AbstractDeployCommand(CommandType command, Kernel kernel, File moduleArchive, File deploymentPlan, ModuleType moduleType, InputStream moduleStream, InputStream deploymentStream, boolean spool) {
        super(command);
        this.kernel = kernel;
        this.moduleArchive = moduleArchive;
        this.deploymentPlan = deploymentPlan;
        this.moduleType = moduleType;
        this.moduleStream = moduleStream;
        this.deploymentStream = deploymentStream;
        this.spool = spool;
        deployer = getDeployerName();
    }

    private AbstractName getDeployerName() {
        Set deployers = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.deployment.Deployer"));
        if (deployers.isEmpty()) {
            fail("No Deployer GBean present in running Geronimo server. " +
                 "This usually indicates a serious problem with the configuration of " +
                 "your running Geronimo server.  If " +
                 "the deployer is present but not started, the workaround is to run " +
                 "a deploy command like 'start geronimo/geronimo-gbean-deployer/1.0/car'.  " +
                 "If the deployer service is not present at all (it was undeployed) then " +
                 "you need to either re-install Geronimo or get a deployment plan for the " +
                 "runtime deployer and distribute it while the server is not running and " +
                 "then start the server with a command like the above.  For help on this, " +
                 "write to user@geronimo.apache.org and include the contents of your " +
                 "var/config/config.xml file.");
            return null;
        }
        Iterator j = deployers.iterator();
        AbstractName deployer = (AbstractName) j.next();
        if (j.hasNext()) {
            fail("More than one deployer found");
            return null;
        }
        return deployer;

    }

    // be careful to clean up the temp file... we tell the vm to delete this on exit
    // but VMs can't be trusted to acutally delete the file
    // Copied from DeploymentUtil
    protected static File createTempFile(String extension) throws IOException {
        File tempFile = File.createTempFile("geronimo-deploymentUtil", extension == null? ".tmpdir": extension);
        tempFile.deleteOnExit();
        return tempFile;
    }

    protected void copyTo(File outfile, InputStream is) throws IOException {
        byte[] buffer = new byte[4096];
        int count;
        OutputStream os = new FileOutputStream(outfile);
        try {
            while ((count = is.read(buffer)) > 0) {
                os.write(buffer, 0, count);
            }
        } finally {
            os.close();
        }
    }

    public void doDeploy(Target target, boolean finished) throws Exception {
        File[] args = {moduleArchive, deploymentPlan};
        massageFileNames(args);
        Object deployParams[] = new Object[] {Boolean.valueOf(commandContext.isInPlace()), args[0], args[1], target.getName()};
        List objectNames = (List) kernel.invoke(deployer, "deploy", deployParams, DEPLOY_SIG);
        if (objectNames == null || objectNames.isEmpty()) {
            throw new DeploymentException("Server didn't deploy anything");
        }
        String parentName = (String) objectNames.get(0);
        String[] childIDs = new String[objectNames.size()-1];
        for (int j=0; j < childIDs.length; j++) {
            childIDs[j] = (String)objectNames.get(j+1);
        }

        TargetModuleIDImpl moduleID = new TargetModuleIDImpl(target, parentName, childIDs);
        if(isWebApp(kernel, parentName)) {
            moduleID.setType(ModuleType.WAR);
        }
        if(moduleID.getChildTargetModuleID() != null) {
            for (int i = 0; i < moduleID.getChildTargetModuleID().length; i++) {
                TargetModuleIDImpl id = (TargetModuleIDImpl) moduleID.getChildTargetModuleID()[i];
                if(isWebApp(kernel, id.getModuleID())) {
                    id.setType(ModuleType.WAR);
                }
            }
        }
        addModule(moduleID);
        if(finished) {
            addWebURLs(kernel);
            complete("Completed with id " + parentName);
        }
    }

    protected void massageFileNames(File[] inputs) {
    }

    public URL getRemoteDeployUploadURL() throws Exception {
       return new URL((String)kernel.getAttribute(deployer, "remoteDeployUploadURL"));
    }
}
