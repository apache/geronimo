package org.apache.geronimo.timer;

import java.util.Date;

import org.apache.geronimo.timer.ExecutorTask;


/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/18 22:10:56 $
 *
 * */
public interface ExecutorTaskFactory {
    //TODO make the WorkerPersistence smarter so the oneTime and atFixedRate parameters are not needed.
    // This could be done by eg. using a stored procedure for update/delete.
    ExecutorTask createExecutorTask(Runnable userTask, WorkInfo workInfo, ThreadPooledTimer threadPooledTimer);
}
