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

package org.apache.geronimo.transaction.wrapper.manager;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.management.j2ee.statistics.Stats;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAException;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.management.stats.JTAStatsImpl;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Used to provide the GBean metadata for the GeronimoTransactionManager class
 *
 * @version $Rev$ $Date$
 */
@GBean(j2eeType=NameFactory.JTA_RESOURCE)
public class GeronimoTransactionManagerGBean extends GeronimoTransactionManager implements StatisticsProvider, GBeanLifecycle {

    private final BundleContext bundleContext;
    private final JTAStatsImpl stats;
    private ServiceRegistration serviceRegistration;

    /**
     * TODO NOTE!!! this should be called in an unspecified transaction context, but we cannot enforce this restriction!
     */
    public GeronimoTransactionManagerGBean(@ParamAttribute(name="defaultTransactionTimeoutSeconds") int defaultTransactionTimeoutSeconds,
                                           @ParamReference(name="XidFactory", namingType=NameFactory.XID_FACTORY) XidFactory xidFactory, 
                                           @ParamReference(name="TransactionLog", namingType=NameFactory.TRANSACTION_LOG) TransactionLog transactionLog,
                                           @ParamSpecial(type=SpecialAttributeType.bundleContext) BundleContext bundleContext) throws XAException {                                           
        super(defaultTransactionTimeoutSeconds == 0 ? DEFAULT_TIMEOUT : defaultTransactionTimeoutSeconds, 
              xidFactory,
              transactionLog);
        
        this.stats = new JTAStatsImpl();
        this.bundleContext = bundleContext;
        
        stats.setStartTime();
    }

    public void resetStats() {
        stats.setStartTime();
        super.resetStatistics();
    }

    public Stats getStats() {
        try {
            stats.getActiveCountImpl().setCount(super.getActiveCount());
        } catch(Exception e) {

        }
        stats.getCommittedCountImpl().setCount(super.getTotalCommits());
        stats.getRolledbackCountImpl().setCount(super.getTotalRollbacks());
        stats.setLastSampleTime();
        return stats;
    }

    public boolean isStateManageable() {
        return false;
    }

    public boolean isStatisticsProvider() {
        return true;
    }

    public boolean isEventProvider() {
        return false;
    }

    public void doFail() {
    }

    public void doStart() throws Exception {
        List<String> clazzes = new ArrayList<String>();
        clazzes.add(TransactionManager.class.getName());
        clazzes.add(TransactionSynchronizationRegistry.class.getName());
        clazzes.add(UserTransaction.class.getName());
        clazzes.add(RecoverableTransactionManager.class.getName());
        serviceRegistration = bundleContext.registerService(clazzes.toArray(new String[clazzes.size()]), this, new Hashtable<String, Object>());
    }

    public void doStop() throws Exception {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
    
}
