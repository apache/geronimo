/**
 *
 * Copyright 2005 The Apache Software Foundation
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

package org.apache.geronimo.connector.work;

import javax.resource.spi.work.WorkManager;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.context.TransactionContextManager;

/**
 * 
 * @version $Revision$
 */
public class GeronimoWorkManagerGBean extends GeronimoWorkManager implements GBeanLifecycle {

    public GeronimoWorkManagerGBean() {
    }

    public GeronimoWorkManagerGBean(int size, TransactionContextManager transactionContextManager) {
        super(size, transactionContextManager);
    }

    public GeronimoWorkManagerGBean(int syncSize, int startSize, int schedSize, TransactionContextManager transactionContextManager) {
        super(syncSize, startSize, schedSize, transactionContextManager);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(GeronimoWorkManagerGBean.class, NameFactory.JCA_WORK_MANAGER);
        infoFactory.addInterface(WorkManager.class);

        infoFactory.addAttribute("syncMaximumPoolSize", Integer.TYPE, true);
        infoFactory.addAttribute("startMaximumPoolSize", Integer.TYPE, true);
        infoFactory.addAttribute("scheduledMaximumPoolSize", Integer.TYPE, true);

        infoFactory.addOperation("getXATerminator");

        infoFactory.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);

        infoFactory.setConstructor(new String[]{
            "syncMaximumPoolSize",
            "startMaximumPoolSize",
            "scheduledMaximumPoolSize",
            "TransactionContextManager"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }



}
