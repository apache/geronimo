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
package org.apache.geronimo.connector.deployment.jsr88;

import javax.enterprise.deploy.model.DDBean;
import org.apache.geronimo.deployment.dconfigbean.XmlBeanSupport;
import org.apache.geronimo.xbeans.connector.GerConnectionmanagerType;
import org.apache.xmlbeans.SchemaTypeLoader;

/**
 * Represents connectiondefinition-instance/connectionmanager in the Geronimo
 * Connector deployment plan.
 *
 * @version $Rev$ $Date$
 */
public class ConnectionManager extends XmlBeanSupport {
    private DDBean outboundRA;
    private SinglePool singlePool;
    private PartitionedPool partitionedPool;

    public ConnectionManager() {
        super(null);
    }

    public ConnectionManager(DDBean outboundRA, GerConnectionmanagerType manager) {
        super(null);
        configure(outboundRA, manager);
    }

    void configure(DDBean outboundRA, GerConnectionmanagerType manager) {
        this.outboundRA = outboundRA;
        setXmlObject(manager);
        if(!manager.isSetNoTransaction() && !manager.isSetLocalTransaction() &&
                !manager.isSetXaTransaction()) {
            String[] test = outboundRA.getText("transaction-support");
            if(test.length > 0) {
                String tx = test[0];
                if(tx.equalsIgnoreCase("NoTransaction")) {
                    manager.addNewNoTransaction();
                } else if(tx.equalsIgnoreCase("LocalTransaction")) {
                    manager.addNewLocalTransaction();
                } else if(tx.equalsIgnoreCase("XATransaction")) {
                    manager.addNewXaTransaction();
                    manager.getXaTransaction().addNewTransactionCaching();
                }
            }
        }
        if(manager.isSetSinglePool()) {
            singlePool = new SinglePool(manager.getSinglePool());
        } else if(manager.isSetPartitionedPool()) {
            partitionedPool = new PartitionedPool(manager.getPartitionedPool());
        } else if(!manager.isSetNoPool()) {
            singlePool = new SinglePool(manager.addNewSinglePool());
        }
    }

    protected GerConnectionmanagerType getManager() {
        return (GerConnectionmanagerType) getXmlObject();
    }

    // ----------------------- JavaBean Properties for connection-manager ----------------------

    public boolean isContainerManagedSecurity() {
        return getManager().isSetContainerManagedSecurity();
    }

    public void setContainerManagedSecurity(boolean set) {
        if(set) {
            if(!isContainerManagedSecurity()) {
                getManager().addNewContainerManagedSecurity();
                pcs.firePropertyChange("containerManagedSecurity", !set, set);
            }
        } else {
            if(isContainerManagedSecurity()) {
                getManager().unsetContainerManagedSecurity();
                pcs.firePropertyChange("containerManagedSecurity", !set, set);
            }
        }
    }

    public boolean isTransactionNone() {
        return getManager().isSetNoTransaction();
    }

    public void setTransactionNone(boolean set) {
        if(set) {
            if(!isTransactionNone()) {
                getManager().addNewNoTransaction();
                pcs.firePropertyChange("transactionNone", !set, set);
            }
            if(isTransactionLocal()) setTransactionLocal(false);
            if(isTransactionXA()) setTransactionXA(false);
            if(isTransactionLog()) setTransactionLog(false);
        } else {
            if(isTransactionNone()) {
                getManager().unsetNoTransaction();
                pcs.firePropertyChange("transactionNone", !set, set);
            }
        }
    }

    public boolean isTransactionLocal() {
        return getManager().isSetLocalTransaction();
    }

    public void setTransactionLocal(boolean set) {
        if(set) {
            if(!isTransactionLocal()) {
                getManager().addNewLocalTransaction();
                pcs.firePropertyChange("transactionLocal", !set, set);
            }
            if(isTransactionNone()) setTransactionNone(false);
            if(isTransactionXA()) setTransactionXA(false);
            if(isTransactionLog()) setTransactionLog(false);
        } else {
            if(isTransactionLocal()) {
                getManager().unsetLocalTransaction();
                pcs.firePropertyChange("transactionLocal", !set, set);
            }
        }
    }

    public boolean isTransactionLog() {
        return getManager().isSetTransactionLog();
    }

    public void setTransactionLog(boolean set) {
        if(set) {
            if(!isTransactionLog()) {
                getManager().addNewTransactionLog();
                pcs.firePropertyChange("transactionLog", !set, set);
            }
            if(isTransactionNone()) setTransactionNone(false);
            if(isTransactionXA()) setTransactionXA(false);
            if(isTransactionLocal()) setTransactionLocal(false);
        } else {
            if(isTransactionLog()) {
                getManager().unsetTransactionLog();
                pcs.firePropertyChange("transactionLog", !set, set);
            }
        }
    }

    public boolean isTransactionXA() {
        return getManager().isSetXaTransaction();
    }

    public void setTransactionXA(boolean set) {
        if(set) {
            if(!isTransactionXA()) {
                getManager().addNewXaTransaction();
                pcs.firePropertyChange("transactionXA", !set, set);
            }
            if(isTransactionNone()) setTransactionNone(false);
            if(isTransactionLog()) setTransactionLog(false);
            if(isTransactionLocal()) setTransactionLocal(false);
        } else {
            if(isTransactionXA()) {
                boolean oldTX = isTransactionXACachingTransaction();
                boolean oldThread = isTransactionXACachingThread();
                getManager().unsetXaTransaction();
                pcs.firePropertyChange("transactionXA", !set, set);
                if(oldTX) {
                    pcs.firePropertyChange("transactionXACachingTransaction", true, false);
                }
                if(oldThread) {
                    pcs.firePropertyChange("transactionXACachingThread", true, false);
                }
            }
        }
    }

    public boolean isTransactionXACachingTransaction() {
        return isTransactionXA() && getManager().getXaTransaction().isSetTransactionCaching();
    }

    public void setTransactionXACachingTransaction(boolean set) {
        if(set) {
            setTransactionXA(true);
            if(!getManager().getXaTransaction().isSetTransactionCaching()) {
                getManager().getXaTransaction().addNewTransactionCaching();
                pcs.firePropertyChange("transactionXACachingTransaction", !set, set);
            }
        } else {
            if(isTransactionXA() && getManager().getXaTransaction().isSetTransactionCaching()) {
                getManager().getXaTransaction().unsetTransactionCaching();
                pcs.firePropertyChange("transactionXACachingTransaction", !set, set);
            }
        }
    }

    public boolean isTransactionXACachingThread() {
        return isTransactionXA() && getManager().getXaTransaction().isSetThreadCaching();
    }

    public void setTransactionXACachingThread(boolean set) {
        if(set) {
            setTransactionXA(true);
            if(!getManager().getXaTransaction().isSetThreadCaching()) {
                getManager().getXaTransaction().addNewThreadCaching();
                pcs.firePropertyChange("transactionXACachingThread", !set, set);
            }
        } else {
            if(isTransactionXA() && getManager().getXaTransaction().isSetThreadCaching()) {
                getManager().getXaTransaction().unsetThreadCaching();
                pcs.firePropertyChange("transactionXACachingThread", !set, set);
            }
        }
    }

    public boolean isPoolNone() {
        return getManager().isSetNoPool();
    }

    public void setPoolNone(boolean set) {
        if(set) {
            if(!getManager().isSetNoPool()) {
                getManager().addNewNoPool();
                pcs.firePropertyChange("poolNone", !set, set);
            }
            if(getPoolSingle() != null) setPoolSingle(null);
            if(getPoolPartitioned() != null) setPoolPartitioned(null);
        } else {
            if(getManager().isSetNoPool()) {
                getManager().unsetNoPool();
                pcs.firePropertyChange("poolNone", !set, set);
            }
            if(getPoolSingle() == null && getPoolPartitioned() == null) {
                setPoolSingle(new SinglePool());
            }
        }
    }

    public SinglePool getPoolSingle() {
        return singlePool;
    }

    public void setPoolSingle(SinglePool pool) {
        SinglePool old = getPoolSingle();
        if(pool != null) {
            singlePool = pool;
            if(!getManager().isSetSinglePool()) {
                getManager().addNewSinglePool();
            }
            singlePool.configure(getManager().getSinglePool());
            pcs.firePropertyChange("poolSingle", old, pool);
            if(isPoolNone()) setPoolNone(false);
            if(getPoolPartitioned() != null) setPoolPartitioned(null);
        } else {
            if(getManager().isSetSinglePool()) {
                getManager().unsetSinglePool();
                pcs.firePropertyChange("poolSingle", old, pool);
            }
        }
    }

    public PartitionedPool getPoolPartitioned() {
        return partitionedPool;
    }

    public void setPoolPartitioned(PartitionedPool pool) {
        PartitionedPool old = getPoolPartitioned();
        if(pool != null) {
            partitionedPool = pool;
            if(!getManager().isSetPartitionedPool()) {
                getManager().addNewPartitionedPool();
            }
            partitionedPool.configure(getManager().getPartitionedPool());
            pcs.firePropertyChange("poolPartitioned", old, pool);
            if(isPoolNone()) setPoolNone(false);
            if(getPoolSingle() != null) setPoolSingle(null);
        } else {
            if(getManager().isSetPartitionedPool()) {
                getManager().unsetPartitionedPool();
                pcs.firePropertyChange("poolPartitioned", old, pool);
            }
        }
    }

    // ----------------------- End of JavaBean Properties ----------------------

    protected SchemaTypeLoader getSchemaTypeLoader() {
        return Connector15DCBRoot.SCHEMA_TYPE_LOADER;
    }
}
