/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
package org.apache.geronimo.wink;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.ws.rs.WebApplicationException;

import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.wink.common.internal.i18n.Messages;
import org.apache.wink.server.internal.DeploymentConfiguration;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeronimoWinkDeloymentConfiguration extends DeploymentConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GeronimoWinkDeloymentConfiguration.class);

    private static final String ALTERNATIVE_SHORTCUTS = "META-INF/wink-alternate-shortcuts.properties";

    protected void initAlternateShortcutMap() {
        if (getAlternateShortcutMap() != null) {
            return;
        }
        Properties properties = new Properties();
        InputStream is = null;
        try {
            //Load default shortcut configuration file
            is = DeploymentConfiguration.class.getClassLoader().getResourceAsStream(ALTERNATIVE_SHORTCUTS);
            properties.load(is);
            IOUtils.close(is);
            //Load application customized configuration file
            Bundle bundle = BundleUtils.getContextBundle(true);
            if (bundle != null) {
                URL url = bundle.getEntry(ALTERNATIVE_SHORTCUTS);
                if (url != null) {
                    is = url.openStream();
                    properties.load(is);
                }
            }
        } catch (IOException e) {
            logger.error(Messages.getMessage("alternateShortcutMapLoadFailure"), e);
            throw new WebApplicationException(e);
        } finally {
            try {
                IOUtils.close(is);
            } catch (Exception e) {
            }
        }
        Map<String, String> alternateShortcutMap = new HashMap<String, String>();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            alternateShortcutMap.put((String) entry.getKey(), (String) entry.getValue());
        }
        setAlternateShortcutMap(alternateShortcutMap);
    }

}
