package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.IndexedPropertyDescriptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BeanInfo for the EjbLocalRefBean.
 *
 * @version $Revision: 1.1 $
 */
public class EjbLocalRefBeanBeanInfo extends SimpleBeanInfo {
    private static final Log log = LogFactory.getLog(EjbLocalRefBeanBeanInfo.class);

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(EjbLocalRefBean.class);
        bd.setDisplayName("EJB Local Reference");
        bd.setShortDescription("A reference from this EJB to another local EJB (in the same JVM)");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor name = new PropertyDescriptor("ejbRefName", EjbLocalRefBean.class, "getEjbRefName", null);
            name.setBound(true);
            name.setDisplayName("EJB Ref Name");
            name.setShortDescription("The unique identifier for this EJB Reference");
            PropertyDescriptor jndi = new PropertyDescriptor("jndiName", EjbLocalRefBean.class);
            jndi.setBound(true);
            jndi.setDisplayName("JNDI Name");
            jndi.setShortDescription("The JNDI location used to access the referred EJB");
            IndexedPropertyDescriptor params = new IndexedPropertyDescriptor("contextParam", EjbLocalRefBean.class);
            params.setBound(true);
            params.setDisplayName("JNDI Context Parameters");
            params.setShortDescription("JNDI context parameters to access an EJB in a different application");
            return new PropertyDescriptor[]{
                name, jndi, params
            };
        } catch(IntrospectionException e) {
            log.error("Error in BeanInfo", e);
            return null;
        }
    }
}
