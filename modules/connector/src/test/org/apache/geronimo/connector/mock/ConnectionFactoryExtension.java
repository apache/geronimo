package org.apache.geronimo.connector.mock;

import javax.resource.cci.ConnectionFactory;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/12 17:58:45 $
 *
 * */
public interface ConnectionFactoryExtension extends ConnectionFactory{

    void doSomethingElse();
}
