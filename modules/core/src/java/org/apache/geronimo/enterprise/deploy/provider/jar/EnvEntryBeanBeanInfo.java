package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BeanInfo describing the SecurityRoleBean.
 *
 * @version $Revision: 1.1 $
 */
public class EnvEntryBeanBeanInfo extends SimpleBeanInfo {
    private static final Log log = LogFactory.getLog(EnvEntryBeanBeanInfo.class);

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(EnvEntryBean.class);
        bd.setDisplayName("Environment Entry");
        bd.setShortDescription("An environment entry for htis EJB");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor name = new PropertyDescriptor("envEntryName", EnvEntryBean.class, "getEnvEntryName", null);
            name.setBound(true);
            name.setDisplayName("Env Entry Name");
            name.setShortDescription("The name the EJB uses to refer to this environment entry");
            PropertyDescriptor value = new PropertyDescriptor("envEntryValue", EnvEntryBean.class);
            value.setBound(true);
            value.setDisplayName("Env Entry Value");
            value.setShortDescription("The value for this environment entry.  The type is defined in ejb-jar.xml");
            return new PropertyDescriptor[]{
                name, value,
            };
        } catch(IntrospectionException e) {
            log.error("Error in BeanInfo", e);
            return null;
        }
    }
}
