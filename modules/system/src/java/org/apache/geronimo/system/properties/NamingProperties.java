package org.apache.geronimo.system.properties;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/** java.naming.factory.initial=com.sun.jndi.rmi.registry.RegistryContextFactory
java.naming.factory.url.pkgs=org.apache.geronimo.naming
java.naming.provider.url=rmi://localhost:1099

 */
public class NamingProperties {

    static final String JAVA_NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    static final String JAVA_NAMING_FACTORY_URL_PKGS = "java.naming.factory.url.pkgs";
    static final String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";

    public NamingProperties(String namingFactoryInitial, String namingFactoryUrlPkgs, String namingProviderUrl) {
        setNamingFactoryInitial(namingFactoryInitial);
        setNamingFactoryUrlPkgs(namingFactoryUrlPkgs);
        setNamingProviderUrl(namingProviderUrl);
    }

    public String getNamingFactoryInitial() {
        return System.getProperty(JAVA_NAMING_FACTORY_INITIAL);
    }

    public void setNamingFactoryInitial(String namingFactoryInitial) {
        System.setProperty(JAVA_NAMING_FACTORY_INITIAL, namingFactoryInitial);
    }

    public String getNamingFactoryUrlPkgs() {
        return System.getProperty(JAVA_NAMING_FACTORY_URL_PKGS);
    }

    public void setNamingFactoryUrlPkgs(String namingFactoryUrlPkgs) {
        System.setProperty(JAVA_NAMING_FACTORY_URL_PKGS, namingFactoryUrlPkgs);
    }

    public String getNamingProviderUrl() {
        return System.getProperty(JAVA_NAMING_PROVIDER_URL);
    }

    public void setNamingProviderUrl(String namingProviderUrl) {
        System.setProperty(JAVA_NAMING_PROVIDER_URL, namingProviderUrl);
    }

    public static final GBeanInfo gbeanInfo;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(NamingProperties.class);
        infoFactory.addAttribute("namingFactoryInitial", String.class, true);
        infoFactory.addAttribute("namingFactoryUrlPkgs", String.class, true);
        infoFactory.addAttribute("namingProviderUrl", String.class, true);

        infoFactory.setConstructor(new String[] {"namingFactoryInitial", "namingFactoryUrlPkgs", "namingProviderUrl"});

        gbeanInfo = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return gbeanInfo;
    }
}
