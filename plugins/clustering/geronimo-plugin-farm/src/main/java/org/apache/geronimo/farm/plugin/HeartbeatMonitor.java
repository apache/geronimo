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
package org.apache.geronimo.farm.plugin;


import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.farm.discovery.MulticastLocation;
import org.apache.geronimo.farm.discovery.MulticastSearch;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.URI;

@GBean
public class HeartbeatMonitor implements org.apache.geronimo.system.plugin.HeartbeatMonitor {

    private final MulticastLocation location;

    public HeartbeatMonitor(@ParamReference(name = "MulticastLocation") MulticastLocation location) {
        this.location = location;
    }

    public void monitor(final InputStream in, final OutputStream output, String pattern) {
        if (pattern == null || pattern.equals("")) pattern = ".*";

        final PrintStream out = new PrintStream(output);

        final Pattern regex;
        try {
            regex = Pattern.compile(pattern);
        } catch (Exception e) {
            out.println("Invalid java.util.regex.Pattern \"" + pattern + "\"");
            return;
        }

        out.println("Hit any key to stop.");

        final AtomicBoolean stop = new AtomicBoolean();

        Runnable runnable = new Runnable(){
            public void run() {
                try {
                    in.read();
                } catch (IOException e) {
                } finally {
                    stop.set(true);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();

        try {
            MulticastSearch multicast = new MulticastSearch(location.getHost(), location.getPort());


            multicast.search(new MulticastSearch.Filter(){
                public boolean accept(URI service) {
                    String s = service.toString();
                    Matcher matcher = regex.matcher(s);
                    if (matcher.matches()){
                        out.println(s);
                    }
                    return stop.get();
                }
            });
        } catch (IOException e) {
            e.printStackTrace(out);
        }
    }
}
