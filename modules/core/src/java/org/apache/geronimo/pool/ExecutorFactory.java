package org.apache.geronimo.pool;

import EDU.oswego.cs.dl.util.concurrent.Executor;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/08 22:09:39 $
 *
 * */
public interface ExecutorFactory {

    Executor getExecutor();

}
