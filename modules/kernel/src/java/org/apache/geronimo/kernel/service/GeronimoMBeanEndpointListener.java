package org.apache.geronimo.kernel.service;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/30 08:25:32 $
 *
 * */
public interface GeronimoMBeanEndpointListener {

    void setTarget(Object target);

    void endpointAdded(Object endpoint);

    void endpointRemoved(Object endpoint);

}
