package org.apache.geronimo.timer;




/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/20 23:36:53 $
 *
 * */
public interface ExecutorTaskFactory {

    ExecutorTask createExecutorTask(Runnable userTask, WorkInfo workInfo, ThreadPooledTimer threadPooledTimer);

}
