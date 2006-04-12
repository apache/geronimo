/**
 *
 * Copyright 2006 The Apache Software Foundation
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
 * @version $Rev: 391886 $ $Date: 2006-04-06 13:15:39 +1000 (Thu, 06 Apr 2006) $
 */
public final class InPlaceConfigurationUtil {
	private static final String IN_PLACE_LOCATION_FILE = "inPlaceLocation.txt";
	
    private InPlaceConfigurationUtil() {
    }

    public static void writeInPlaceLocation(ConfigurationData configurationData, File source) throws IOException {
    	if (null == configurationData.getInPlaceConfigurationDir()) {
    		return;
    	}
    	
        File metaInf = new File(source, "META-INF");
        metaInf.mkdirs();
        
        File inPlaceLocation = new File(metaInf, IN_PLACE_LOCATION_FILE);
        Writer writer = null;
        try {
        	OutputStream os = new FileOutputStream(inPlaceLocation);
            writer = new PrintWriter(os);
            File inPlaceConfigurationDir = configurationData.getInPlaceConfigurationDir();
            String absolutePath = inPlaceConfigurationDir.getAbsolutePath();
            writer.write(absolutePath);
        } finally {
        	if (null != writer) {
        		try {
        			writer.flush();
				} catch (IOException e) {
				}
        		try {
        			writer.close();
				} catch (IOException e) {
				}
        	}
        }
    }
    
    public static File readInPlaceLocation(File source) throws IOException {
        File inPlaceLocation = new File(source, "META-INF");
        inPlaceLocation = new File(inPlaceLocation, IN_PLACE_LOCATION_FILE);
        
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
}
