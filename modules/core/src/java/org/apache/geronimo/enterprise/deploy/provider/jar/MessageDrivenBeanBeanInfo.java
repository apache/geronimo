package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * BeanInfo descriptor for the MessageDrivenBean class
 *
 * @version $Revision: 1.1 $
 */
public class MessageDrivenBeanBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(MessageDrivenBean.class);
        bd.setDisplayName("Message-Driven Bean");
        bd.setShortDescription("A Message-Driven EJB");
        return bd;
    }
}
