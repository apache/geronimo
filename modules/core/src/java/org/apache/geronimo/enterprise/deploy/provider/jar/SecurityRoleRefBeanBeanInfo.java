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
public class SecurityRoleRefBeanBeanInfo extends SimpleBeanInfo {
    private static final Log log = LogFactory.getLog(SecurityRoleRefBeanBeanInfo.class);

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SecurityRoleRefBean.class);
        bd.setDisplayName("Security Role Reference");
        bd.setShortDescription("A reference from this EJB to a resource defined in the local server environment");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor name = new PropertyDescriptor("roleName", SecurityRoleRefBean.class, "getRoleName", null);
            name.setBound(true);
            name.setDisplayName("Role Name");
            name.setShortDescription("The name the EJB uses to refer to this security role");
            PropertyDescriptor link = new PropertyDescriptor("roleLink", SecurityRoleRefBean.class);
            link.setBound(true);
            link.setDisplayName("Role Link");
            link.setShortDescription("The name the EJB JAR uses to refer to this security role");
            return new PropertyDescriptor[]{
                name, link,
            };
        } catch(IntrospectionException e) {
            log.error("Error in BeanInfo", e);
            return null;
        }
    }
}
