package org.apache.geronimo.timer;




/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface ExecutorTaskFactory {

    ExecutorTask createExecutorTask(Runnable userTask, WorkInfo workInfo, ThreadPooledTimer threadPooledTimer);

}
