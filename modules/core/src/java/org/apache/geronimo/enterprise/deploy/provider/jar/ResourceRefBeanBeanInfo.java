package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BeanInfo describing the ResourceRefBean.
 *
 * @version $Revision: 1.1 $
 */
public class ResourceRefBeanBeanInfo extends SimpleBeanInfo {
    private static final Log log = LogFactory.getLog(ResourceRefBeanBeanInfo.class);

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ResourceRefBean.class);
        bd.setDisplayName("Resource Reference");
        bd.setShortDescription("A reference from this EJB to a resource defined in the server or elsewhere");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor name = new PropertyDescriptor("resRefName", ResourceRefBean.class, "getResRefName", null);
            name.setBound(true);
            name.setDisplayName("Resource Ref Name");
            name.setShortDescription("The unique identifier for this Resource Reference");
            PropertyDescriptor jndi = new PropertyDescriptor("jndiName", ResourceRefBean.class);
            jndi.setBound(true);
            jndi.setDisplayName("JNDI Name");
            jndi.setShortDescription("The JNDI location used to access the referred resource");
            IndexedPropertyDescriptor params = new IndexedPropertyDescriptor("contextParam", ResourceRefBean.class);
            params.setBound(true);
            params.setDisplayName("JNDI Context Parameters");
            params.setShortDescription("JNDI context parameters, used if the resource is in a different server, etc.");
            return new PropertyDescriptor[]{
                name, jndi, params
            };
        } catch(IntrospectionException e) {
            log.error("Error in BeanInfo", e);
            return null;
        }
    }
}
