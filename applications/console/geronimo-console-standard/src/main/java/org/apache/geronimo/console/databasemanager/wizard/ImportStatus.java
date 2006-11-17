/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.databasemanager.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.geronimo.converter.DatabaseConversionStatus;
import org.apache.geronimo.converter.AbstractDatabasePool;
import org.apache.geronimo.converter.JDBCPool;
import org.apache.geronimo.converter.XADatabasePool;

/**
 * Tracks the progress of a database pool import operation.
 *
 * @version $Rev$ $Date$
 */
public class ImportStatus implements Serializable {
    private DatabaseConversionStatus original;
    private int currentPool = -1;
    private PoolProgress[] pools;

    public ImportStatus(DatabaseConversionStatus original) {
        this.original = original;
        List list = new ArrayList();
        for (int i = 0; i < original.getNoTXPools().length; i++) {
            JDBCPool pool = original.getNoTXPools()[i];
            list.add(new PoolProgress(pool, PoolProgress.TYPE_NOTX));
        }
        for (int i = 0; i < original.getJdbcPools().length; i++) {
            JDBCPool pool = original.getJdbcPools()[i];
            list.add(new PoolProgress(pool, PoolProgress.TYPE_LOCAL));
        }
        for (int i = 0; i < original.getXaPools().length; i++) {
            XADatabasePool pool = original.getXaPools()[i];
            final PoolProgress progress = new PoolProgress(pool, PoolProgress.TYPE_XA);
            if(pool.getXaDataSourceClass().indexOf("apache.derby") < 0) {
                progress.setSkipped(true);
            }
            list.add(progress);
        }
        pools = (PoolProgress[]) list.toArray(new PoolProgress[list.size()]);
    }

    public boolean isFinished() {
        for (int i = 0; i < pools.length; i++) {
            PoolProgress pool = pools[i];
            if(!pool.isFinished() && !pool.isSkipped()) {
                return false;
            }
        }
        return true;
    }

    public void setCurrentPoolIndex(int currentPool) {
        this.currentPool = currentPool;
        getCurrentPool().setStarted(true);
    }

    public DatabaseConversionStatus getOriginal() {
        return original;
    }

    public int getCurrentPoolIndex() {
        return currentPool;
    }

    public PoolProgress getCurrentPool() {
        return currentPool > -1 ? pools[currentPool] : null;
    }

    public PoolProgress[] getPools() {
        return pools;
    }

    public int getPendingCount() {
        int count = 0;
        for (int i = 0; i < pools.length; i++) {
            PoolProgress pool = pools[i];
            if(!pool.isSkipped() && !pool.isStarted()) {
                ++count;
            }
        }
        return count;
    }

    public int getStartedCount() {
        int count = 0;
        for (int i = 0; i < pools.length; i++) {
            PoolProgress pool = pools[i];
            if(pool.isStarted() && !pool.isFinished() && !pool.isSkipped()) {
                ++count;
            }
        }
        return count;
    }

    public int getFinishedCount() {
        int count = 0;
        for (int i = 0; i < pools.length; i++) {
            PoolProgress pool = pools[i];
            if(!pool.isSkipped() && pool.isFinished()) {
                ++count;
            }
        }
        return count;
    }

    public int getSkippedCount() {
        int count = 0;
        for (int i = 0; i < pools.length; i++) {
            PoolProgress pool = pools[i];
            if(pool.isSkipped()) {
                ++count;
            }
        }
        return count;
    }

    public final static class PoolProgress implements Serializable {
        public final static String TYPE_NOTX = "NoTX";
        public final static String TYPE_LOCAL = "JDBC";
        public final static String TYPE_XA = "XA";

        private AbstractDatabasePool pool;
        private boolean started;
        private boolean finished;
        private boolean skipped;
        private String type;
        private String name; // Once in Geronimo
        private String configurationName; // Once in Geronimo

        public PoolProgress(AbstractDatabasePool pool, String type) {
            this.pool = pool;
            this.type = type;
        }

        public AbstractDatabasePool getPool() {
            return pool;
        }

        public boolean isStarted() {
            return started;
        }

        public void setStarted(boolean started) {
            this.started = started;
        }

        public boolean isFinished() {
            return finished;
        }

        public void setFinished(boolean finished) {
            this.finished = finished;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public void setSkipped(boolean skipped) {
            this.skipped = skipped;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getConfigurationName() {
            return configurationName;
        }

        public void setConfigurationName(String configurationName) {
            this.configurationName = configurationName;
        }

        public String getStatus() {
            return isSkipped() ? "Ignored" : isFinished() ? "Deployed as "+name : isStarted() ? "Started" : "Pending";
        }
    }
}
