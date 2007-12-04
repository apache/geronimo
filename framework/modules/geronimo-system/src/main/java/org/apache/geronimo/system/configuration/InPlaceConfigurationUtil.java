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
package org.apache.geronimo.system.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.geronimo.kernel.config.ConfigurationData;

/**
 * //todo make this package access again!
 * @version $Rev$ $Date$
 */
public class InPlaceConfigurationUtil {
    private static final String IN_PLACE_LOCATION_FILE = "inPlaceLocation.config";

    public InPlaceConfigurationUtil() {
    }

    public boolean isInPlaceConfiguration(File source) {
        File inPlaceLocation = getInPlaceLocation(source);
        return inPlaceLocation.exists();
    }

    public void writeInPlaceLocation(ConfigurationData configurationData, File source) throws IOException {
        if (null == configurationData.getInPlaceConfigurationDir()) {
            return;
        }

        File inPlaceLocation = getInPlaceLocation(source);
        Writer writer = null;
        try {
            OutputStream os = new FileOutputStream(inPlaceLocation);
            writer = new PrintWriter(os);
            File inPlaceConfigurationDir = configurationData.getInPlaceConfigurationDir();
            String absolutePath = inPlaceConfigurationDir.getAbsolutePath();
            writer.write(absolutePath);
            writer.close(); // also flushes the stream and shouldn't normally fail
            writer = null;
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }

    public File readInPlaceLocation(File source) throws IOException {
        File inPlaceLocation = getInPlaceLocation(source);

        if (!inPlaceLocation.exists()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(inPlaceLocation);
            reader = new BufferedReader(new InputStreamReader(is));
            String path = reader.readLine();
            return new File(path);
        } finally {
            if (null != reader) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private File getInPlaceLocation(File source) {
        File inPlaceLocation = new File(source, "META-INF");
        inPlaceLocation.mkdirs();
        return new File(inPlaceLocation, IN_PLACE_LOCATION_FILE);
    }
}
