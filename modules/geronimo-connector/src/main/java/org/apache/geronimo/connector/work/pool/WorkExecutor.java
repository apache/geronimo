package org.apache.geronimo.connector.work.pool;

import javax.resource.spi.work.WorkException;

import edu.emory.mathcs.backport.java.util.concurrent.Executor;

import org.apache.geronimo.connector.work.WorkerContext;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface WorkExecutor {

    /**
     * This method must be implemented by sub-classes in order to provide the
     * relevant synchronization policy. It is called by the executeWork template
     * method.
     *
     * @param work Work to be executed.
     *
     * @throws javax.resource.spi.work.WorkException Indicates that the work has failed.
     * @throws InterruptedException Indicates that the thread in charge of the
     * execution of the specified work has been interrupted.
     */
     void doExecute(WorkerContext work, Executor executor)
            throws WorkException, InterruptedException;


}
