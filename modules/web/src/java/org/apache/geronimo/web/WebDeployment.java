package org.apache.geronimo.web;



import java.net.URL;
import javax.management.ObjectName;
import org.apache.geronimo.kernel.deployment.DeploymentInfo;


/**
 * WebDeploymentInfo.java
 *
 * This class is registered with the mbean server as
 * the parent of a deployment unit (a web app).
 * @jmx:mbean
 *      extends="org.apache.geronimo.kernel.deployment.DeploymentInfoMBean"
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/28 22:30:58 $
 * 
 */
public class WebDeployment extends DeploymentInfo implements WebDeploymentMBean
{



    public WebDeployment (ObjectName name, ObjectName parent, URL url) 
    {
        super (name, parent, url);
    } 
    

    
}
