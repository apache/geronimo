package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * BeanInfo class for the EjbRefBean
 *
 * @version $Revision: 1.1 $
 */
public class EjbRefBeanBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(EjbRefBean.class);
        bd.setDisplayName("EJB Reference");
        bd.setShortDescription("A reference from this EJB to a remote EJB (in the same JVM or elsewhere)");
        return bd;
    }
}
