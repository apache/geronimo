package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BeanInfo describing the ResourceEnvRefBean.
 *
 * @version $Revision: 1.1 $
 */
public class ResourceEnvRefBeanBeanInfo extends SimpleBeanInfo {
    private static final Log log = LogFactory.getLog(ResourceEnvRefBeanBeanInfo.class);

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ResourceEnvRefBean.class);
        bd.setDisplayName("Resource Env Reference");
        bd.setShortDescription("A reference from this EJB to a resource defined in the local server environment");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor name = new PropertyDescriptor("resourceEnvRefName", ResourceEnvRefBean.class, "getResourceEnvRefName", null);
            name.setBound(true);
            name.setDisplayName("Resource Env Ref Name");
            name.setShortDescription("The unique identifier for this Resource Env Reference");
            PropertyDescriptor jndi = new PropertyDescriptor("jndiName", ResourceEnvRefBean.class);
            jndi.setBound(true);
            jndi.setDisplayName("JNDI Name");
            jndi.setShortDescription("The JNDI location used to access the referred resource");
            return new PropertyDescriptor[]{
                name, jndi,
            };
        } catch(IntrospectionException e) {
            log.error("Error in BeanInfo", e);
            return null;
        }
    }
}
