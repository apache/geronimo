/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.connector.deployment.jsr88;

import org.apache.geronimo.xbeans.geronimo.GerPartitionedpoolType;
import org.apache.geronimo.xbeans.geronimo.GerSinglepoolType;

/**
 * Settings for connectionmanager/partitioned-pool
 *
 * @version $Rev$ $Date$
 */
public class PartitionedPool extends SinglePool {
    public PartitionedPool() {
    }

    public PartitionedPool(GerPartitionedpoolType pool) {
        super(pool);
    }

    protected GerPartitionedpoolType getPool() {
        return (GerPartitionedpoolType) getXmlObject();
    }

    protected void configure(GerSinglepoolType pool) {
        super.configure(pool);
        if(!isPartitionBySubject() && !isPartitionByRequest()) {
            setPartitionBySubject(true);
        }
    }

    public boolean isPartitionBySubject() {
        return getPool().isSetPartitionBySubject();
    }

    public void setPartitionBySubject(boolean set) {
        if(set) {
            if(!isPartitionBySubject()) {
                getPool().addNewPartitionBySubject();
                pcs.firePropertyChange("partitionBySubject", !set, set);
            }
        } else {
            if(isPartitionBySubject()) {
                getPool().unsetPartitionBySubject();
                pcs.firePropertyChange("partitionBySubject", !set, set);
            }
        }
    }

    public boolean isPartitionByRequest() {
        return getPool().isSetPartitionByConnectionrequestinfo();
    }

    public void setPartitionByRequest(boolean set) {
        if(set) {
            if(!isPartitionBySubject()) {
                getPool().addNewPartitionByConnectionrequestinfo();
                pcs.firePropertyChange("partitionByRequest", !set, set);
            }
        } else {
            if(isPartitionBySubject()) {
                getPool().unsetPartitionByConnectionrequestinfo();
                pcs.firePropertyChange("partitionByRequest", !set, set);
            }
        }
    }
}
