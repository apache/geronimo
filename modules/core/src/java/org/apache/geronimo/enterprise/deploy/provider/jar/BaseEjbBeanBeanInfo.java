package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BeanInfo describing the BaseEjbBean class
 *
 * @version $Revision: 1.1 $
 */
public class BaseEjbBeanBeanInfo extends SimpleBeanInfo {
    private static final Log log = LogFactory.getLog(BaseEjbBeanBeanInfo.class);

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(BaseEjbBean.class);
        bd.setDisplayName("Enterprise JavaBean");
        bd.setShortDescription("An EJB defined in the EJB JAR");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor name = new PropertyDescriptor("ejbName", BaseEjbBean.class, "getEjbName", null);
            name.setBound(true);
            name.setDisplayName("EJB Name");
            name.setShortDescription("The unique name for this EJB");
            PropertyDescriptor jndi = new PropertyDescriptor("jndiName", BaseEjbBean.class);
            jndi.setBound(true);
            jndi.setDisplayName("JNDI Name");
            jndi.setShortDescription("The JNDI location where this EJB should be bound");
            return new PropertyDescriptor[] {
                name, jndi,
            };
        } catch(IntrospectionException e) {
            log.error("Error in BeanInfo", e);
            return null;
        }
    }
}
