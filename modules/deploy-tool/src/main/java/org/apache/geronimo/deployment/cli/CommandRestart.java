package org.apache.geronimo.deployment.cli;

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import java.io.PrintWriter;

/**
 * The CLI deployer logic to restart.
 */
public class CommandRestart extends CommandStart {
    public CommandRestart() {
        super("restart", "1. Common Commands", "[ModuleID|TargetModuleID]+",
                "Accepts the configId of a module, or the fully-qualified " +
                "TargetModuleID identifying both the module and the server or cluster it's " +
                "on, and restarts that module.  The module should be available to the server " +
                "and running. If multiple modules are specified, they will all be restarted.\n");
    }

    protected ProgressObject runCommand(PrintWriter out, DeploymentManager mgr, TargetModuleID[] ids) {
        ProgressObject po = mgr.stop(ids);
        waitForProgress(out, po);
        if(po.getDeploymentStatus().isCompleted()) {
            po = mgr.start(ids);
            waitForProgress(out, po);
        }
        return po;
    }

    protected String getAction() {
        return "Restarted";
    }
}
