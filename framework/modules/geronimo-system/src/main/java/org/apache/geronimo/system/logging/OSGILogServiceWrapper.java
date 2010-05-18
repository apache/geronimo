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

package org.apache.geronimo.system.logging;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.logging.SystemLog;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public class OSGILogServiceWrapper implements SystemLog, GBeanLifecycle {

    private SystemLog wrappedSystemLog;

    private String filter;

    private BundleContext bundleContext;

    private ServiceReference serviceReference;

    public OSGILogServiceWrapper(@ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext, @ParamAttribute(name = "filter") String filter) {
        this.filter = filter;
        this.bundleContext = bundleContext;
    }

    @Override
    public String getConfigFileName() {
        return wrappedSystemLog.getConfigFileName();
    }

    @Override
    public String[] getLogFileNames() {
        return wrappedSystemLog.getLogFileNames();
    }

    @Override
    public SearchResults getMatchingItems(String logFile, Integer firstLine, Integer lastLine, String minLevel, String regex, int maxResults, boolean includeStackTraces) {
        return wrappedSystemLog.getMatchingItems(logFile, firstLine, lastLine, minLevel, regex, maxResults, includeStackTraces);
    }

    @Override
    public int getRefreshPeriodSeconds() {
        return wrappedSystemLog.getRefreshPeriodSeconds();
    }

    @Override
    public String getRootLoggerLevel() {
        return wrappedSystemLog.getRootLoggerLevel();
    }

    @Override
    public void setConfigFileName(String fileName) {
        wrappedSystemLog.setConfigFileName(fileName);
    }

    @Override
    public void setRefreshPeriodSeconds(int seconds) {
        wrappedSystemLog.setRefreshPeriodSeconds(seconds);
    }

    @Override
    public void setRootLoggerLevel(String level) {
        wrappedSystemLog.setRootLoggerLevel(level);
    }

    @Override
    public void doFail() {
        stop();
    }

    @Override
    public void doStart() throws Exception {
        ServiceReference[] serviceReferences = bundleContext.getServiceReferences(SystemLog.class.getName(), filter);
        if (serviceReferences != null && serviceReferences.length > 0) {
            serviceReference = serviceReferences[0];
            wrappedSystemLog = (SystemLog) bundleContext.getService(serviceReference);
        }
    }

    @Override
    public void doStop() throws Exception {
        stop();
    }

    private void stop() {
        if (serviceReference != null) {
            bundleContext.ungetService(serviceReference);
        }
    }
}
