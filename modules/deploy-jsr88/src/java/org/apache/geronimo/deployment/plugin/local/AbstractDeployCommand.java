/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.deployment.plugin.local;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.Target;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @version $Rev: 190584 $ $Date$
 */
public abstract class AbstractDeployCommand extends CommandSupport {
    private final static String DEPLOYER_NAME = "*:name=Deployer,j2eeType=Deployer,*";

    protected final Kernel kernel;
    private static final String[] DEPLOY_SIG = {File.class.getName(), File.class.getName()};
    protected final boolean spool;
    protected File moduleArchive;
    protected File deploymentPlan;
    protected InputStream moduleStream;
    protected InputStream deploymentStream;
    protected ObjectName deployer;

    public AbstractDeployCommand(CommandType command, Kernel kernel, File moduleArchive, File deploymentPlan, InputStream moduleStream, InputStream deploymentStream, boolean spool) {
        super(command);
        this.kernel = kernel;
        this.moduleArchive = moduleArchive;
        this.deploymentPlan = deploymentPlan;
        this.moduleStream = moduleStream;
        this.deploymentStream = deploymentStream;
        this.spool = spool;
        deployer = getDeployerName();
    }

    private ObjectName getDeployerName() {
        Set deployers = kernel.listGBeans(JMXUtil.getObjectName(DEPLOYER_NAME));
        if (deployers.isEmpty()) {
            fail("No Deployer GBean present in running Geronimo server. " +
                 "This usually indicates a serious problem with the configuration of " +
                 "your running Geronimo server.  If " +
                 "the deployer is present but not started, the workaround is to run " +
                 "a deploy command like 'start geronimo/runtime-deployer/1.0/car'.  " +
                 "If the deployer service is not present at all (it was undeployed) then " +
                 "you need to either re-install Geronimo or get a deployment plan for the " +
                 "runtime deployer and distribute it while the server is not running and " +
                 "then start the server with a command like the above.  For help on this, " +
                 "write to user@geronimo.apache.org and include the contents of your " +
                 "config-store/index.properties and var/config/config.xml files.");
            return null;
        }
        Iterator j = deployers.iterator();
        ObjectName deployer = (ObjectName) j.next();
        if (j.hasNext()) {
            fail("More than one deployer found");
            return null;
        }
        return deployer;

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

    protected void doDeploy(Target target, boolean finished) throws Exception {
        File[] args = {moduleArchive, deploymentPlan};
        massageFileNames(args);
        List objectNames = (List) kernel.invoke(deployer, "deploy", args, DEPLOY_SIG);
        if (objectNames == null || objectNames.isEmpty()) {
            throw new DeploymentException("Server didn't deploy anything");
        }
        String parentName = (String) objectNames.get(0);
        String[] childIDs = new String[objectNames.size()-1];
        for (int j=0; j < childIDs.length; j++) {
            childIDs[j] = (String)objectNames.get(j+1);
        }

        TargetModuleIDImpl moduleID = new TargetModuleIDImpl(target, parentName.toString(), childIDs);
        if(isWebApp(kernel, parentName.toString())) {
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
