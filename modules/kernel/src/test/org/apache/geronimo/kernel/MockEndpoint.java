package org.apache.geronimo.kernel;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/22 18:34:13 $
 *
 * */
public interface MockEndpoint {

    String endpointDoSomething(String name);

    int getMutableInt();

    void doSetMutableInt(int mutableInt);

    void setMutableInt(int mutableInt);

}
