package org.apache.geronimo.deployment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;
import org.apache.geronimo.deployment.plugin.TargetModuleIDImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jeremy
 * Date: Jun 23, 2004
 * Time: 1:23:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteDeployer {
    public static void main(String[] args) throws Exception {
        File module = new File(args[0]);
        ProgressObject po;
        new DeploymentFactoryImpl();

        String uri = "deployer:geronimo:jmx:rmi://localhost/jndi/rmi:/JMXConnector";
        DeploymentManager manager = DeploymentFactoryManager.getInstance().getDeploymentManager(uri, "system", "manager");
        Target[] targets = manager.getTargets();
        TargetModuleID[] modules = manager.getAvailableModules(null, targets);
        List redeploy = new ArrayList();
        if (args.length > 1) {
            for (int i = 0; i < modules.length; i++) {
                TargetModuleIDImpl targetModuleID = (TargetModuleIDImpl)modules[i];
                if (targetModuleID.getModuleID().equals(args[1])) {
                    redeploy.add(targetModuleID);
                }
            }
        }
        if (redeploy.isEmpty()) {
            po = manager.distribute(targets, module, null);
        } else {
            TargetModuleID[] todo = (TargetModuleID[]) redeploy.toArray(new TargetModuleID[redeploy.size()]);
            po = manager.redeploy(todo, module, null);
        }
        while (po.getDeploymentStatus().isRunning()) {
            Thread.sleep(100);
        }
        System.out.println(po.getDeploymentStatus().getMessage());
        if (po.getDeploymentStatus().isCompleted()) {
            manager.start(po.getResultTargetModuleIDs());
        }

//        po = manager.stop(po.getResultTargetModuleIDs());
//        while (po.getDeploymentStatus().isRunning()) {
//            Thread.sleep(100);
//        }
//        System.out.println(po.getDeploymentStatus().getMessage());
//        po = manager.undeploy(po.getResultTargetModuleIDs());
//        while (po.getDeploymentStatus().isRunning()) {
//            Thread.sleep(100);
//        }
//        System.out.println(po.getDeploymentStatus().getMessage());
    }
}
