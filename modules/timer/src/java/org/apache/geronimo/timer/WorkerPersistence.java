package org.apache.geronimo.timer;

import java.util.Collection;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface WorkerPersistence {

    void save(WorkInfo workInfo) throws PersistenceException;

    void cancel(long id) throws PersistenceException;

    void playback(String key, Playback playback) throws PersistenceException;

    void fixedRateWorkPerformed(long id) throws PersistenceException;

    void intervalWorkPerformed(long id, long period) throws PersistenceException;

    Collection getIdsByKey(String key, Object userId) throws PersistenceException;
}
