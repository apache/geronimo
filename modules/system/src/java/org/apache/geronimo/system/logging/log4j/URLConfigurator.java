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

package org.apache.geronimo.system.logging.log4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Handles the details of configuring Log4j from a URL.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:30 $
 */
public class URLConfigurator
        implements Configurator {
    private static final Log log = LogFactory.getLog(URLConfigurator.class);

    public static void configure(final URL url) {
        new URLConfigurator().doConfigure(url, LogManager.getLoggerRepository());
    }

    private Configurator getConfigurator(final URL url) {
        String contentType = null;

        // Get the content type to see if it is XML or not
        URLConnection connection = null;
        try {
            connection = url.openConnection();
            contentType = connection.getContentType();
            if (log.isTraceEnabled()) {
                log.trace("Content type: " + contentType);
            }
        } catch (IOException e) {
            log.warn("Could not determine content type from URL; ignoring", e);
        }
        if (contentType != null) {
            if (contentType.toLowerCase().endsWith("/xml")) {
                return new DOMConfigurator();
            }
        }

        // Check thr file name
        String filename = url.getFile().toLowerCase();
        if (filename.endsWith(".xml")) {
            return new DOMConfigurator();
        } else if (filename.endsWith(".properties")) {
            return new PropertyConfigurator();
        }

        // Check for <?xml in content
        if (connection != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                try {
                    String head = reader.readLine();
                    if (head.startsWith("<?xml")) {
                        return new DOMConfigurator();
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                log.warn("Failed to check content header; ignoring", e);
            }
        }

        log.warn("Unable to determine content type, using property configurator");
        return new PropertyConfigurator();
    }

    public void doConfigure(final URL url, final LoggerRepository repo) {
        if (log.isDebugEnabled()) {
            log.debug("Configuring from URL: " + url);
        }

        // Get the config delegate and target repository to config with
        Configurator delegate = getConfigurator(url);

        if (log.isTraceEnabled()) {
            log.trace("Configuring Log4j using configurator: " +
                    delegate + ", repository: " + repo);
        }

        // Now actually configure Log4j
        delegate.doConfigure(url, repo);
    }
}
