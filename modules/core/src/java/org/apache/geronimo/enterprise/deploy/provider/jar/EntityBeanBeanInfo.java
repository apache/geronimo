package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * BeanInfo descriptor for the EntityBean class
 *
 * @version $Revision: 1.1 $
 */
public class EntityBeanBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(EntityBean.class);
        bd.setDisplayName("Entity Bean");
        bd.setShortDescription("An Entity EJB");
        return bd;
    }
}
