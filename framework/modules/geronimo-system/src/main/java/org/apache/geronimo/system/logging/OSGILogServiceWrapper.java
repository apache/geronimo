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

import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.apache.geronimo.logging.SystemLog;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class OSGILogServiceWrapper extends AbstractServiceWrapper<SystemLog> implements SystemLog {

    public OSGILogServiceWrapper(@ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle) {
        super(bundle, SystemLog.class);
    }

//    @Override
//    public String getConfigFileName() {
//        return get().getConfigFileName();
//    }

    @Override
    public String[] getLogFileNames() {
        return get().getLogFileNames();
    }

    @Override
    public SearchResults getMatchingItems(String logFile, Integer firstLine, Integer lastLine, String minLevel, String regex, int maxResults, boolean includeStackTraces) {
        return get().getMatchingItems(logFile, firstLine, lastLine, minLevel, regex, maxResults, includeStackTraces);
    }

//    @Override
//    public int getRefreshPeriodSeconds() {
//        return get().getRefreshPeriodSeconds();
//    }

    @Override
    public String getRootLoggerLevel() {
        return get().getRootLoggerLevel();
    }

//    @Override
//    public void setConfigFileName(String fileName) {
//        get().setConfigFileName(fileName);
//    }
//
//    @Override
//    public void setRefreshPeriodSeconds(int seconds) {
//        get().setRefreshPeriodSeconds(seconds);
//    }

    @Override
    public void setRootLoggerLevel(String level) {
        get().setRootLoggerLevel(level);
    }

}
