package org.apache.geronimo.j2ee.deployment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

import java.io.File;
import java.util.jar.JarFile;
import java.net.URL;
import java.net.URI;

/**
 * @version $Rev: 356097 $ $Date: 2005-12-11 17:29:03 -0800 (Sun, 11 Dec 2005) $
 */
public class UnavailableModuleBuilder implements ModuleBuilder {

    public Module createModule(File plan, JarFile moduleFile) throws DeploymentException {
    		return null; 
    }

    public Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, URI earConfigId, Object moduleContextInfo) throws DeploymentException {
    		return null;
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        throw new DeploymentException("Module type unavailable");
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
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
