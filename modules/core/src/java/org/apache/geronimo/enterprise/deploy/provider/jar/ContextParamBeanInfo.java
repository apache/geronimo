package org.apache.geronimo.enterprise.deploy.provider.jar;

import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BeanInfo describing the ContextParam class
 *
 * @version $Revision: 1.1 $
 */
public class ContextParamBeanInfo extends SimpleBeanInfo {
    private static final Log log = LogFactory.getLog(ContextParamBeanInfo.class);

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(ContextParam.class);
        bd.setDisplayName("JNDI Context Parameter");
        bd.setShortDescription("One of several parameters used to initialize a JNDI InitialContext");
        return bd;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor name = new PropertyDescriptor("paramName", ContextParam.class);
            name.setBound(true);
            name.setDisplayName("Parameter Name");
            name.setShortDescription("The name of this context parameter (e.g. java.naming.provider.url)");
            PropertyDescriptor value = new PropertyDescriptor("paramValue", ContextParam.class);
            value.setBound(true);
            value.setDisplayName("Parameter Value");
            value.setShortDescription("The value to set for this context parameter");
            return new PropertyDescriptor[] {
                name, value,
            };
        } catch(IntrospectionException e) {
            log.error("Error in BeanInfo", e);
            return null;
        }
    }
}
