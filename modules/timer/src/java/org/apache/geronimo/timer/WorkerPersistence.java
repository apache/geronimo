package org.apache.geronimo.timer;

import java.util.Date;
import java.util.Collection;

import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.Playback;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/18 22:10:56 $
 *
 * */
public interface WorkerPersistence {

    void save(WorkInfo workInfo) throws PersistenceException;

    void cancel(long id) throws PersistenceException;

    void playback(String key, Playback playback) throws PersistenceException;

    void fixedRateWorkPerformed(long id) throws PersistenceException;

    void intervalWorkPerformed(long id, long period) throws PersistenceException;

    Collection getIdsByKey(String key) throws PersistenceException;
}
