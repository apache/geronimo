package org.apache.geronimo.timer;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface UserTaskFactory {

    Runnable newTask(long id);

}
