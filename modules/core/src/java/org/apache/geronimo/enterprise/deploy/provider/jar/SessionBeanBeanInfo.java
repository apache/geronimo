package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * BeanInfo descriptor for the SessionBean class
 *
 * @version $Revision: 1.1 $
 */
public class SessionBeanBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(SessionBean.class);
        bd.setDisplayName("Session Bean");
        bd.setShortDescription("A Session EJB");
        return bd;
    }
}
