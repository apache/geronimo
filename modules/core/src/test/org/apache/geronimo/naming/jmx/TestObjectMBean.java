package org.apache.geronimo.naming.jmx;

/**
 * 
 *
 * @version $VERSION$ Nov 12, 2003$
 * 
 * */
public interface TestObjectMBean {
    Object getEJBHome();
    Object getEJBLocalHome();
    Object getConnectionFactory();
}
