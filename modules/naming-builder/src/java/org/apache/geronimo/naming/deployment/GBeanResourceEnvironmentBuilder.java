package org.apache.geronimo.naming.deployment;

import java.util.Set;

import org.apache.geronimo.gbean.GBeanData;

/**
 */
public class GBeanResourceEnvironmentBuilder implements ResourceEnvironmentBuilder {

    private final GBeanData gbean;

    public GBeanResourceEnvironmentBuilder(GBeanData gbean) {
        this.gbean = gbean;
    }

    public Set getUnshareableResources() {
        return (Set) gbean.getAttribute("unshareableResources");
    }

    public void setUnshareableResources(Set unshareableResources) {
        gbean.setAttribute("unshareableResources", unshareableResources);
    }

    public Set getApplicationManagedSecurityResources() {
        return (Set) gbean.getAttribute("applicationManagedSecurityResources");
    }

    public void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources) {
        gbean.setAttribute("applicationManagedSecurityResources", applicationManagedSecurityResources);
    }
}
