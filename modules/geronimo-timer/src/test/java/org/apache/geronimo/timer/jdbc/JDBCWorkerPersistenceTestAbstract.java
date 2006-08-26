/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.timer.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Date;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.apache.geronimo.timer.Playback;
import org.apache.geronimo.timer.WorkInfo;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class JDBCWorkerPersistenceTestAbstract extends TestCase {


    private final String countSQL = "select count(*) from timertasks";

    private final String serverUniqueId = "TestServerUniqueID";
    private final String key = "test:service=Timer";
    private final Object userInfo = "test user info";
    private Object userId = null;

    private JDBCWorkerPersistence jdbcWorkerPersistence;
    protected DataSource datasource;
    protected boolean useSequence;


    private WorkInfo workInfo;
    protected Date time;
    protected Long period;

    protected void setUp() throws Exception {
        jdbcWorkerPersistence = new JDBCWorkerPersistence(serverUniqueId, datasource, useSequence);
        time = new Date(System.currentTimeMillis());
        period = new Long(1000);
        workInfo = new WorkInfo(key, userId, userInfo, time, period, true);
    }


    public void testSaveCancel() throws Exception {
        assertEquals(0, countRows());
        jdbcWorkerPersistence.save(workInfo);
        assertEquals(1, countRows());
        jdbcWorkerPersistence.cancel(workInfo.getId());
        assertEquals(0, countRows());
    }

    public void testSaveUpdate() throws Exception {
        assertEquals(0, countRows());
        long now = workInfo.getTime().getTime();
        jdbcWorkerPersistence.save(workInfo);
        assertEquals(1, countRows());
        jdbcWorkerPersistence.fixedRateWorkPerformed(workInfo.getId());
//        showRows();
        PlaybackImpl playback = new PlaybackImpl();
        jdbcWorkerPersistence.playback(key, playback);
        assertEquals(now + period.longValue(), playback.getTime().getTime());
        assertEquals(1, playback.getCount());
        long before = System.currentTimeMillis();
        jdbcWorkerPersistence.intervalWorkPerformed(workInfo.getId(), period.longValue());
        long after = System.currentTimeMillis();
        playback = new PlaybackImpl();
        jdbcWorkerPersistence.playback(key, playback);
        assertTrue(before + period.longValue() <= playback.getTime().getTime());
        assertTrue(after + period.longValue() >= playback.getTime().getTime());
        assertEquals(1, playback.getCount());
        jdbcWorkerPersistence.cancel(workInfo.getId());
        assertEquals(0, countRows());
    }

    public void testGetByKey() throws Exception {
        time = new Date(System.currentTimeMillis());
        period = new Long(1000);
        WorkInfo workInfo1 = new WorkInfo(key, new Long(1), userInfo, time, period, true);
        WorkInfo workInfo2 = new WorkInfo(key, new Long(2), userInfo, time, period, true);
        jdbcWorkerPersistence.save(workInfo1);
        jdbcWorkerPersistence.save(workInfo2);
        Collection idsAll = jdbcWorkerPersistence.getIdsByKey(key, null);
        assertEquals(2, idsAll.size());
        Collection ids1 = jdbcWorkerPersistence.getIdsByKey(key, new Long(1));
        assertEquals(1, ids1.size());
        Collection ids2 = jdbcWorkerPersistence.getIdsByKey(key, new Long(2));
        assertEquals(1, ids2.size());
    }



    private void showRows() throws Exception {
        Connection c = datasource.getConnection();
        try {
            PreparedStatement p = c.prepareStatement("select id, task from timertasks");
            try {
                ResultSet countRS = p.executeQuery();
                try {
                    while(countRS.next()) {
                        System.out.println("id: " + countRS.getLong(1) + " task: " + countRS.getString(2));
                    }
                } finally {
                    countRS.close();
                }
            } finally {
                p.close();
            }
        } finally {
            c.close();
        }
    }

    private int countRows() throws Exception {
        Connection c = datasource.getConnection();
        try {
            PreparedStatement p = c.prepareStatement(countSQL);
            try {
                ResultSet countRS = p.executeQuery();
                try {
                    countRS.next();
                    return countRS.getInt(1);
                } finally {
                    countRS.close();
                }
            } finally {
                p.close();
            }
        } finally {
            c.close();
        }
    }

    private static class PlaybackImpl implements Playback {

        private int count = 0;
        private Date time;

        public void schedule(WorkInfo workInfo) {
            count++;
            this.time = workInfo.getTime();
        }

        public int getCount() {
            return count;
        }

        public Date getTime() {
            return time;
        }

    }

}
