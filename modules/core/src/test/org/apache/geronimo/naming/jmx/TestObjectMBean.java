package org.apache.geronimo.naming.jmx;

/**
 * 
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/13 22:22:31 $
 * 
 * */
public interface TestObjectMBean {
    Object getEJBHome();
    Object getEJBLocalHome();
    Object getConnectionFactory();
}
