package org.apache.geronimo.timer;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/18 22:10:56 $
 *
 * */
public interface UserTaskFactory {

    Runnable newTask(long id);

}
