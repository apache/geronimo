package org.apache.geronimo.jetty;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Host gbean for jetty containing an array of hosts and virtual hosts
 */
public class Host {

    private final String[] hosts;
    private final String[] virtualHosts;

    public Host() {
        hosts = null;
        virtualHosts = null;
    }

    public Host(String[] hosts, String[] virtualHosts) {
        this.hosts = hosts;
        this.virtualHosts = virtualHosts;
    }

    public String[] getHosts() {
        return hosts;
    }

    public String[] getVirtualHosts() {
        return virtualHosts;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(Host.class, "Host");
        infoBuilder.addAttribute("hosts", String[].class, true);
        infoBuilder.addAttribute("virtualHosts", String[].class, true);
        infoBuilder.setConstructor(new String[] {"hosts", "virtualHosts"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
