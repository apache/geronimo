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

package org.apache.geronimo.system.logging.log4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.Configurator;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Handles the details of configuring Log4j from a URL.
 *
 * @version $Rev$ $Date$
 */
public class URLConfigurator implements Configurator {
    private static final Logger log = LoggerFactory.getLogger(URLConfigurator.class);

    public static void configure(final URL url) {
        try {
            new URLConfigurator().doConfigure(url, LogManager.getLoggerRepository());
        } catch (Throwable e) {
            log.info("could not configure log4j", e);
        }
    }

    private Configurator getConfigurator(final URL url) throws FileNotFoundException {
        String contentType = null;

        // Get the content type to see if it is XML or not
        URLConnection connection = null;
        try {
            connection = url.openConnection();
            contentType = connection.getContentType();
            if (log.isTraceEnabled()) {
                log.trace("Content type: " + contentType);
            }
        } catch (FileNotFoundException e) {
            throw e;
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
        try {
            if (log.isDebugEnabled()) {
                log.debug("Configuring from URL: " + url);
            }

            // Get the config delegate and target repository to config with
            Configurator delegate = null;
            try {
                delegate = getConfigurator(url);
            } catch (FileNotFoundException e) {
                return;
            }

            if (log.isTraceEnabled()) {
                log.trace("Configuring Log4j using configurator: " +
                        delegate + ", repository: " + repo);
            }

            // Now actually configure Log4j
            delegate.doConfigure(url, repo);
        } catch (Throwable e) {
            log.info("conld not configure log4j", e);
        }
    }
}
