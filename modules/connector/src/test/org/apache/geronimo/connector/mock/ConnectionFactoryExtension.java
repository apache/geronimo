package org.apache.geronimo.connector.mock;

import javax.resource.cci.ConnectionFactory;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface ConnectionFactoryExtension extends ConnectionFactory{

    void doSomethingElse();
}
