package org.apache.geronimo.timer;

import java.util.Date;
import java.util.Collection;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.geronimo.timer.PersistenceException;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/20 23:36:53 $
 *
 * */
public interface PersistentTimer {
    WorkInfo schedule(UserTaskFactory userTaskFactory, String key, Object userId, Object userInfo, long delay) throws PersistenceException, RollbackException, SystemException;

    WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userId, Object userInfo, Date time) throws PersistenceException, RollbackException, SystemException;

    WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userInfo, long delay, long period, Object userId) throws PersistenceException, RollbackException, SystemException;

    WorkInfo schedule(String key, UserTaskFactory userTaskFactory, Object userId, Object userInfo, Date firstTime, long period) throws PersistenceException, RollbackException, SystemException;

    WorkInfo scheduleAtFixedRate(String key, UserTaskFactory userTaskFactory, Object userId, Object userInfo, long delay, long period) throws PersistenceException, RollbackException, SystemException;

    WorkInfo scheduleAtFixedRate(String key, UserTaskFactory userTaskFactory, Object userId, Object userInfo, Date firstTime, long period) throws PersistenceException, RollbackException, SystemException;

    Collection playback(String key, UserTaskFactory userTaskFactory) throws PersistenceException;

    Collection getIdsByKey(String key, Object userId) throws PersistenceException;

    WorkInfo getWorkInfo(Long id);

    void cancelTimerTasks(Collection ids);
}
