package org.apache.geronimo.kernel;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/17 00:14:22 $
 *
 * */
public interface MockEndpoint {

    String doSomething(String name);

    int getMutableInt();

    void setMutableInt(int mutableInt);

}
