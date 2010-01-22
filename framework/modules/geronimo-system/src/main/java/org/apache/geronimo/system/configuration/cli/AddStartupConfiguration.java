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
package org.apache.geronimo.system.configuration.cli;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A tool for use by clients who really know what they're doing (such as the
 * installer).  You point this to a FileConfigurationList file, and give it
 * a configuration name, and it adds the name to the file.  This should only
 * be run while the server is down, and will only work if you have previous
 * knowledge of which PersistentConfigurationList implementation is going to
 * be used and what file it persists to.  This is NOT a general-purpose
 * feature for users.
 *
 * @version $Rev$ $Date$
 */
public class AddStartupConfiguration {
    public static void main(String[] args) {
        try {
            String file = args[0];
            String configuration = args[1].trim();
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line;
            while((line = in.readLine()) != null) {
                if(line.trim().equals(configuration)) {
                    in.close();
                    System.exit(0);
                }
            }
            in.close();

            PrintWriter out = new PrintWriter(new FileWriter(file, true));
            out.println(configuration);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
