package org.apache.geronimo.j2ee.deployment;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.jar.JarFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev: 356097 $ $Date: 2005-12-11 17:29:03 -0800 (Sun, 11 Dec 2005) $
 */
public class UnavailableModuleBuilder implements ModuleBuilder {

    public Module createModule(File plan, JarFile moduleFile, Naming naming) throws DeploymentException {
    		return null; 
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming) throws DeploymentException {
    		return null;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public String getSchemaNamespace() {
        return null;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(UnavailableModuleBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addInterface(ModuleBuilder.class);
	    GBEAN_INFO = infoBuilder.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
