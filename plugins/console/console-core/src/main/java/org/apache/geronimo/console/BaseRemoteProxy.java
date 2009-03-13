/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;

import org.apache.geronimo.console.i18n.ConsoleResourceRegistry;
import org.apache.geronimo.console.util.PortletManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRemoteProxy {
    private static final Logger log = LoggerFactory.getLogger(BaseRemoteProxy.class);
    private static ConsoleResourceRegistry resourceRegistry;

    static {
        try {
            resourceRegistry = (ConsoleResourceRegistry) PortletManager.getKernel().getGBean(ConsoleResourceRegistry.class);
        } catch (Exception e) {
            log.error("Cannot get the console resource registery service", e);
        }
    }   
    
    public final String getLocalizedString(HttpServletRequest request, String bundleName, String key, Object... vars) {
        String value = resourceRegistry.handleGetObject(bundleName, request.getLocale(), key);
        if (null == value || 0 == value.length()) return key;     
        return MessageFormat.format(value, vars);
    }
    
}
