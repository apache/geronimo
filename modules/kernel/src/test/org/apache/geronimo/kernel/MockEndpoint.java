package org.apache.geronimo.kernel;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/17 00:32:10 $
 *
 * */
public interface MockEndpoint {

    String doSomething(String name);

    int getMutableInt();

    void doSetMutableInt(int mutableInt);

    void setMutableInt(int mutableInt);

}
