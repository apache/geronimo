package org.apache.geronimo.deployment;

import java.io.File;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;

import org.apache.geronimo.deployment.plugin.factories.DeploymentFactoryImpl;

/**
 * Created by IntelliJ IDEA.
 * User: jeremy
 * Date: Jun 23, 2004
 * Time: 1:23:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoteDeployer {
    public static void main(String[] args) throws Exception {
        new DeploymentFactoryImpl();
        String uri = "deployer:geronimo:jmx:rmi://localhost/jndi/rmi:/JMXConnector";
        DeploymentManager manager = DeploymentFactoryManager.getInstance().getDeploymentManager(uri, "system", "manager");
        Target[] targets = manager.getTargets();
        File module = new File(args[0]);
        ProgressObject po = manager.distribute(targets, module, null);
        while (po.getDeploymentStatus().isRunning()) {
            Thread.sleep(100);
        }
        System.out.println(po.getDeploymentStatus().getMessage());
        if (po.getDeploymentStatus().isCompleted()) {
            manager.start(po.getResultTargetModuleIDs());
        }
    }
}
